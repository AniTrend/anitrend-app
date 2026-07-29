package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.mapper.toFeedList
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.mutation.MutationRegistry
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.OperationStatus
import com.mxt.anitrend.domain.feed.interactor.DeleteFeedInteractor
import com.mxt.anitrend.domain.like.interactor.ToggleLikeInteractor
import com.mxt.anitrend.domain.model.DeleteFeedCommand
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.ToggleLikeCommand
import com.mxt.anitrend.domain.model.toFeedItemUiModel
import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class FeedListViewModel(
    private val feedRepository: FeedRepository,
    private val feedStore: FeedStore,
    private val mutationRegistry: MutationRegistry,
    private val toggleLikeInteractor: ToggleLikeInteractor,
    private val deleteFeedInteractor: DeleteFeedInteractor,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState

        data class Success(
            val content: PageContainer<FeedList>,
            val items: List<FeedItemUiModel>,
            val loadedPages: Set<Int>,
            val replaceExisting: Boolean,
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

                combine(
                    feedStore.observeQuery(queryKey),
                    mutationRegistry.state,
                    flowOf(screen),
                ) { query, operations, currentScreen ->
                    val renderedFeeds = query.feeds.map { it.toFeedList() }
                    when {
                        currentScreen.errorMessage != null -> {
                            UiState.Error(currentScreen.errorMessage)
                        }
                        currentScreen.isLoading && renderedFeeds.isEmpty() -> {
                            UiState.Loading
                        }
                        else -> {
                            UiState.Success(
                                content = PageContainer<FeedList>().apply {
                                    query.pageInfo?.toPageInfo()?.let { pageInfo = it }
                                    pageData = renderedFeeds
                                },
                                items = query.feeds.map { feed ->
                                    feed.toFeedItemUiModel(
                                        isLikePending = operations[OperationKey.feedLike(feed.id)].isRunning(),
                                        isDeletePending = operations[OperationKey.feedDelete(feed.id)].isRunning(),
                                    )
                                },
                                loadedPages = query.loadedPages,
                                replaceExisting = currentScreen.lastRequestedPage <= 1,
                            )
                        }
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading,
            )

    fun load(
        page: Int,
        pageLimit: Int,
        isFollowing: Boolean?,
        type: ActivityType?,
        isMixed: Boolean?,
    ) {
        val queryKey = FeedQueryKey(
            scope = FeedScope.GLOBAL,
            userId = null,
            mediaId = null,
            activityType = type,
            isFollowing = isFollowing,
            isMixed = isMixed,
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
            feedRepository.getFeedList(
                page = page,
                perPage = pageLimit,
                isFollowing = isFollowing,
                type = type,
                isMixed = isMixed,
                queryKey = queryKey,
                queryGeneration = generation,
            ).onSuccess {
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
                Timber.e(throwable, "FeedListViewModel load failed")
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load feed",
                    )
                }
            }
        }
    }

    fun toggleLike(feedId: Long) {
        if (mutationRegistry.state.value[OperationKey.feedLike(feedId)].isRunning()) {
            return
        }
        viewModelScope.launch {
            toggleLikeInteractor(
                ToggleLikeCommand(
                    id = feedId,
                    likeableType = LikeableType.ACTIVITY,
                ),
            )
        }
    }

    fun deleteFeed(feedId: Long) {
        if (mutationRegistry.state.value[OperationKey.feedDelete(feedId)].isRunning()) {
            return
        }
        viewModelScope.launch {
            deleteFeedInteractor(DeleteFeedCommand(feedId = feedId))
        }
    }

    private fun OperationStatus?.isRunning(): Boolean = this is OperationStatus.Running
}
