package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BaseMutation
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaFeedViewModel(
    private val mediaRepository: MediaRepository,
    private val baseRepository: BaseRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val content: PageContainer<FeedList>,
            val replaceExisting: Boolean = false,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            baseRepository.mutationEvents.collect { event ->
                when (event) {
                    is BaseMutation.LikeToggled -> {
                        if (event.targetType == LikeableType.ACTIVITY) {
                            replaceCurrentPage { feeds ->
                                val index = feeds.indexOfFirst { it.id == event.targetId }
                                if (index >= 0) {
                                    feeds[index].likes = event.users
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

    fun applyReturnedFeed(feed: FeedList) {
        replaceCurrentPage { feeds ->
            val index = feeds.indexOfFirst { it.id == feed.id }
            if (index >= 0) {
                feeds[index] = feed
                true
            } else {
                false
            }
        }
    }

    /**
     * Loads media feed (social activity). Repeatable for pagination; no loadedOnce guard.
     */
    fun load(mediaId: Long, isFollowing: Boolean, page: Int, pageLimit: Int) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                mediaRepository.getMediaSocial(
                    mediaId = mediaId,
                    isFollowing = isFollowing,
                    page = page,
                    perPage = pageLimit,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaFeedViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media feed",
                )
            }
        }
    }

    private fun replaceCurrentPage(update: (MutableList<FeedList>) -> Boolean) {
        val current = _state.value as? UiState.Success ?: return
        val feeds = current.content.pageData.toMutableList()
        if (!update(feeds)) {
            return
        }
        _state.value = current.copy(
            content = PageContainer<FeedList>().apply {
                if (current.content.hasPageInfo()) {
                    pageInfo = current.content.pageInfo
                }
                pageData = feeds
            },
            replaceExisting = true,
        )
    }
}
