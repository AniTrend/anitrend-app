package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.mutation.MutationRegistry
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.OperationStatus
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.domain.feed.interactor.DeleteFeedInteractor
import com.mxt.anitrend.domain.like.interactor.ToggleLikeInteractor
import com.mxt.anitrend.domain.model.DeleteFeedCommand
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.ToggleLikeCommand
import com.mxt.anitrend.domain.model.toFeedItemUiModel
import com.mxt.anitrend.graphql.generated.LikeableType
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

class MessageFeedViewModel(
    private val feedRepository: FeedRepository,
    private val feedStore: FeedStore,
    private val mutationRegistry: MutationRegistry,
    private val toggleLikeInteractor: ToggleLikeInteractor,
    private val deleteFeedInteractor: DeleteFeedInteractor,
    private val requestSequence: RequestSequence,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val items: List<FeedItemUiModel>,
            val pageInfo: PageInfoRecord?,
            val loadedPages: Set<Int>,
            val replaceExisting: Boolean,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private data class ScreenState(
        val queryKey: FeedQueryKey? = null,
        val currentUserId: Long? = null,
        val requestToken: Long = 0L,
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
                    when {
                        currentScreen.errorMessage != null -> {
                            UiState.Error(currentScreen.errorMessage)
                        }
                        currentScreen.isLoading && query.feeds.isEmpty() -> {
                            UiState.Loading
                        }
                        else -> {
                            UiState.Success(
                                items = query.feeds.map { feed ->
                                    feed.toFeedItemUiModel(
                                        isLikePending = operations[OperationKey.feedLike(feed.id)].isRunning(),
                                        isDeletePending = operations[OperationKey.feedDelete(feed.id)].isRunning(),
                                        currentUserId = currentScreen.currentUserId,
                                    )
                                },
                                pageInfo = query.pageInfo,
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

    /**
     * Loads message feed. Repeatable for pagination; no loadedOnce guard.
     *
     * @param messageType [KeyUtil.MESSAGE_TYPE_INBOX] (0) for inbox; otherwise outbox.
     */
    fun load(
        userId: Long,
        page: Int,
        pageLimit: Int,
        messageType: Int,
        currentUserId: Long? = null,
    ) {
        val queryKey = FeedQueryKey(
            scope = if (messageType == 0) FeedScope.MESSAGE_INBOX else FeedScope.MESSAGE_OUTBOX,
            userId = userId,
            mediaId = null,
            activityType = null,
            isFollowing = null,
            isMixed = null,
        )
        val token = if (page > 1) screenState.value.requestToken else requestSequence.next()
        screenState.update {
            it.copy(
                queryKey = queryKey,
                currentUserId = currentUserId,
                requestToken = token,
                lastRequestedPage = page,
                isLoading = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            feedRepository.getFeedMessage(
                page = page,
                perPage = pageLimit,
                userId = if (messageType == 0) userId else null,
                messengerId = if (messageType != 0) userId else null,
                queryKey = queryKey,
                readToken = token,
            ).onSuccess {
                if (screenState.value.requestToken != token) {
                    return@onSuccess
                }
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                if (screenState.value.requestToken != token) {
                    return@onFailure
                }
                Timber.e(throwable, "MessageFeedViewModel load failed")
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load message feed",
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
