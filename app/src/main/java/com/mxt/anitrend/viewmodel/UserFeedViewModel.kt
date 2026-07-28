package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BaseMutation
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.FeedMutation
import com.mxt.anitrend.repository.FeedRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import com.mxt.anitrend.model.entity.anilist.FeedList as FeedListEntity

class UserFeedViewModel(
    private val feedRepository: FeedRepository,
    private val baseRepository: BaseRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val content: PageContainer<com.mxt.anitrend.model.entity.anilist.FeedList>,
            val replaceExisting: Boolean = false,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state =
        MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            feedRepository.mutationEvents.collect { event ->
                when (event) {
                    is FeedMutation.FeedSaved -> {
                        upsertFeed(event.feed)
                    }
                    is FeedMutation.FeedDeleted -> {
                        replaceCurrentPage { items ->
                            items.removeAll { it.id == event.id }
                        }
                    }
                    else -> { /* ignore reply events - not relevant to user feed */ }
                }
            }
        }

        viewModelScope.launch {
            baseRepository.mutationEvents.collect { event ->
                when (event) {
                    is BaseMutation.LikeToggled -> {
                        if (event.targetType == LikeableType.ACTIVITY) {
                            replaceCurrentPage { items ->
                                val index = items.indexOfFirst { it.id == event.targetId }
                                if (index >= 0) {
                                    items[index].likes = event.users
                                    true
                                } else {
                                    false
                                }
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    fun applyReturnedFeed(feed: FeedListEntity) {
        upsertFeed(feed, addIfMissing = false)
    }

    /**
     * Loads user feed. Repeatable for pagination; no loadedOnce guard.
     * Only dispatches a request when [userId] > 0.
     */
    fun load(
        userId: Int?,
        page: Int,
        pageLimit: Int,
        isFollowing: Boolean?,
        type: ActivityType?,
        isMixed: Boolean?,
    ) {
        if (userId == null || userId <= 0) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            feedRepository.getFeedList(
                userId = userId.toLong(),
                page = page,
                perPage = pageLimit,
                isFollowing = isFollowing,
                type = type,
                isMixed = isMixed,
            ).onSuccess { content ->
                _state.value = UiState.Success(content = content)
            }.onFailure { throwable ->
                Timber.e(throwable, "UserFeedViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load user feed",
                )
            }
        }
    }

    private fun upsertFeed(
        feed: FeedListEntity,
        addIfMissing: Boolean = true,
    ) {
        replaceCurrentPage { items ->
            val index = items.indexOfFirst { it.id == feed.id }
            when {
                index >= 0 -> {
                    items[index] = feed
                    true
                }
                addIfMissing -> {
                    items.add(0, feed)
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceCurrentPage(update: (MutableList<FeedListEntity>) -> Boolean) {
        val current = _state.value as? UiState.Success ?: return
        val items = current.content.pageData.toMutableList()
        if (!update(items)) {
            return
        }
        _state.value = current.copy(
            content = PageContainer<FeedListEntity>().apply {
                if (current.content.hasPageInfo()) {
                    pageInfo = current.content.pageInfo
                }
                pageData = items
            },
            replaceExisting = true,
        )
    }
}
