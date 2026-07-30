package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.mapper.toFeedList
import com.mxt.anitrend.data.mapper.toFeedRecord
import com.mxt.anitrend.data.mapper.toFeedReplyRecord
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaFeedViewModel(
    private val mediaRepository: MediaRepository,
    private val feedStore: FeedStore,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val content: PageContainer<FeedList>,
            val replaceExisting: Boolean = false,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private data class ScreenState(
        val queryKey: FeedQueryKey? = null,
        val requestGeneration: Int = 0,
        val lastRequestedPage: Int = 1,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val state: StateFlow<UiState> =
        screenState
            .flatMapLatest { screen ->
                val queryKey = screen.queryKey ?: return@flatMapLatest flowOf(
                    if (screen.errorMessage != null) {
                        UiState.Error(screen.errorMessage)
                    } else {
                        UiState.Loading
                    },
                )

                feedStore.observeQuery(queryKey).map { query ->
                    val renderedFeeds = query.feeds.map { it.toFeedList() }
                    when {
                        screen.errorMessage != null -> UiState.Error(screen.errorMessage)
                        screen.isLoading && renderedFeeds.isEmpty() -> UiState.Loading
                        else -> UiState.Success(
                            content = PageContainer<FeedList>().apply {
                                query.pageInfo?.toPageInfo()?.let { pageInfo = it }
                                pageData = renderedFeeds
                            },
                            replaceExisting = screen.lastRequestedPage <= 1,
                        )
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading,
            )

    fun applyReturnedFeed(feed: FeedList) {
        viewModelScope.launch {
            val feedRevision = feedStore.state.value.feedsById[feed.id]?.revision ?: 0L
            feedStore.apply(
                FeedStoreChange.FeedDetailLoaded(
                    feed = feed.toFeedRecord(revision = feedRevision),
                    replies = feed.replies.orEmpty().map { reply ->
                        reply.toFeedReplyRecord(
                            activityId = feed.id,
                            revision = feedStore.state.value.repliesById[reply.id]?.revision ?: feedRevision,
                        )
                    },
                ),
            )
        }
    }

    /**
     * Loads media feed (social activity). Repeatable for pagination; no loadedOnce guard.
     */
    fun load(mediaId: Long, isFollowing: Boolean, page: Int, pageLimit: Int) {
        val queryKey = FeedQueryKey(
            scope = FeedScope.MEDIA,
            userId = null,
            mediaId = mediaId,
            activityType = null,
            isFollowing = isFollowing,
            isMixed = null,
        )
        val generation = screenState.value.requestGeneration.takeIf { page > 1 } ?: (screenState.value.requestGeneration + 1)
        screenState.update {
            it.copy(
                queryKey = queryKey,
                requestGeneration = generation,
                lastRequestedPage = page,
                isLoading = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            runCatching {
                mediaRepository.getMediaSocial(
                    mediaId = mediaId,
                    isFollowing = isFollowing,
                    page = page,
                    perPage = pageLimit,
                    queryKey = queryKey,
                    queryGeneration = generation,
                ).getOrThrow()
            }.onSuccess {
                if (screenState.value.requestGeneration != generation) {
                    return@onSuccess
                }
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                if (screenState.value.requestGeneration != generation) {
                    return@onFailure
                }
                Timber.e(throwable, "MediaFeedViewModel load failed")
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load media feed",
                    )
                }
            }
        }
    }
}
