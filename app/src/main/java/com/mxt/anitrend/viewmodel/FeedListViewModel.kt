package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.toFeedItemUiModel
import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
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

class FeedListViewModel(
    private val feedRepository: FeedRepository,
    private val baseRepository: BaseRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val content: PageContainer<com.mxt.anitrend.model.entity.anilist.FeedList>,
            val items: List<FeedItemUiModel>,
            val loadedPages: Set<Int>,
            val replaceExisting: Boolean = false,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()
    private val loadedFeeds = mutableListOf<FeedListEntity>()
    private val loadedPages = linkedSetOf<Int>()
    private var currentPageInfo: PageInfo? = null
    private var requestGeneration: Int = 0

    init {
        viewModelScope.launch {
            feedRepository.mutationEvents.collect { event ->
                when (event) {
                    is FeedMutation.FeedSaved -> {
                        upsertFeed(event.feed)
                    }
                    is FeedMutation.FeedDeleted -> {
                        replaceLoadedFeeds { items ->
                            items.removeAll { it.id == event.id }
                        }
                    }
                    else -> { /* ignore reply events - not relevant to feed list */ }
                }
            }
        }

        viewModelScope.launch {
            baseRepository.mutationEvents.collect { event ->
                when (event) {
                    is BaseMutation.LikeToggled -> {
                        if (event.targetType == LikeableType.ACTIVITY) {
                            replaceLoadedFeeds { items ->
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

    private fun upsertFeed(
        feed: FeedListEntity,
        addIfMissing: Boolean = true,
    ) {
        replaceLoadedFeeds { items ->
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

    private fun emitSuccess(replaceExisting: Boolean) {
        _state.value = UiState.Success(
            content = PageContainer<FeedListEntity>().apply {
                currentPageInfo?.let { pageInfo = it }
                pageData = loadedFeeds.toList()
            },
            items = loadedFeeds.map { it.toFeedItemUiModel() },
            loadedPages = loadedPages.toSet(),
            replaceExisting = replaceExisting,
        )
    }

    private fun mergePage(
        page: Int,
        content: PageContainer<FeedListEntity>,
    ) {
        if (page <= 1) {
            loadedFeeds.clear()
            loadedPages.clear()
        }

        content.pageData.forEach { feed ->
            val index = loadedFeeds.indexOfFirst { it.id == feed.id }
            if (index >= 0) {
                loadedFeeds[index] = feed
            } else {
                loadedFeeds.add(feed)
            }
        }

        loadedPages.add(page)
        currentPageInfo = if (content.hasPageInfo()) content.pageInfo else null
        emitSuccess(replaceExisting = page <= 1)
    }

    internal fun beginRequestGeneration(page: Int): Int =
        if (page <= 1) ++requestGeneration else requestGeneration

    internal fun applyLoadResult(
        page: Int,
        generation: Int,
        content: PageContainer<FeedListEntity>,
    ) {
        if (generation != requestGeneration) {
            return
        }
        mergePage(page = page, content = content)
    }

    private fun replaceLoadedFeeds(update: (MutableList<FeedListEntity>) -> Boolean) {
        if (loadedFeeds.isEmpty()) {
            return
        }
        val items = loadedFeeds.toMutableList()
        if (!update(items)) {
            return
        }
        loadedFeeds.clear()
        loadedFeeds.addAll(items)
        emitSuccess(replaceExisting = true)
    }

    /**
     * Loads the global/home feed. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(
        page: Int,
        pageLimit: Int,
        isFollowing: Boolean?,
        type: ActivityType?,
        isMixed: Boolean?,
    ) {
        val generation = beginRequestGeneration(page)
        if (page <= 1 && loadedFeeds.isEmpty()) {
            _state.value = UiState.Loading
        }
        viewModelScope.launch {
            feedRepository.getFeedList(
                page = page,
                perPage = pageLimit,
                isFollowing = isFollowing,
                type = type,
                isMixed = isMixed,
            ).onSuccess { content ->
                if (generation != requestGeneration) {
                    return@onSuccess
                }
                applyLoadResult(page = page, generation = generation, content = content)
            }.onFailure { throwable ->
                if (generation != requestGeneration) {
                    return@onFailure
                }
                Timber.e(throwable, "FeedListViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load feed",
                )
            }
        }
    }
}
