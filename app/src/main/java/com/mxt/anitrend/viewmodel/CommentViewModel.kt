package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.mapper.toFeedList
import com.mxt.anitrend.data.mapper.toFeedReply
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.mutation.MutationRegistry
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.OperationStatus
import com.mxt.anitrend.domain.feed.interactor.DeleteFeedInteractor
import com.mxt.anitrend.domain.feed.interactor.DeleteReplyInteractor
import com.mxt.anitrend.domain.feed.interactor.SaveFeedInteractor
import com.mxt.anitrend.domain.feed.interactor.SaveFeedRequest
import com.mxt.anitrend.domain.feed.interactor.SaveReplyInteractor
import com.mxt.anitrend.domain.feed.interactor.SaveReplyRequest
import com.mxt.anitrend.domain.like.interactor.ToggleLikeInteractor
import com.mxt.anitrend.domain.model.CommentReplyUiModel
import com.mxt.anitrend.domain.model.DeleteFeedCommand
import com.mxt.anitrend.domain.model.DeleteReplyCommand
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.ToggleLikeCommand
import com.mxt.anitrend.domain.model.toCommentReplyUiModel
import com.mxt.anitrend.domain.model.toFeedItemUiModel
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class CommentViewModel(
    private val feedStore: FeedStore,
    private val feedRepository: FeedRepository,
    private val mutationRegistry: MutationRegistry,
    private val toggleLikeInteractor: ToggleLikeInteractor,
    private val saveReplyInteractor: SaveReplyInteractor,
    private val deleteReplyInteractor: DeleteReplyInteractor,
    private val deleteFeedInteractor: DeleteFeedInteractor,
    private val saveFeedInteractor: SaveFeedInteractor,
) : ViewModel() {

    data class CommentUiState(
        val isLoading: Boolean = true,
        val feed: FeedList? = null,
        val feedItem: FeedItemUiModel? = null,
        val replies: List<CommentReplyUiModel> = emptyList(),
        val isDeleted: Boolean = false,
        val errorMessage: String? = null,
    )

    private data class ScreenState(
        val feedId: Long? = null,
        val isLoading: Boolean = false,
        val hasAttemptedLoad: Boolean = false,
        val errorMessage: String? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val state: StateFlow<CommentUiState> =
        screenState
            .map { it.feedId }
            .filterNotNull()
            .flatMapLatest { feedId ->
                combine(
                    feedStore.observeFeed(feedId),
                    feedStore.observeReplies(feedId),
                    mutationRegistry.state,
                    screenState,
                ) { feed, replies, operations, screen ->
                    val renderedFeed = feed?.toFeedList(replies = replies.map { it.toFeedReply() })
                    CommentUiState(
                        isLoading = screen.isLoading && feed == null,
                        feed = renderedFeed,
                        feedItem = feed?.toFeedItemUiModel(
                            isLikePending = operations[OperationKey.feedLike(feed.id)].isRunning(),
                            isDeletePending = operations[OperationKey.feedDelete(feed.id)].isRunning(),
                        ),
                        replies = replies.map { reply ->
                            reply.toCommentReplyUiModel(
                                isLikePending = operations[OperationKey.replyLike(reply.id)].isRunning(),
                                isDeletePending = operations[OperationKey.replyDelete(reply.id)].isRunning(),
                            )
                        },
                        isDeleted = screen.hasAttemptedLoad && !screen.isLoading && feed == null,
                        errorMessage = screen.errorMessage,
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CommentUiState(),
            )

    fun load(feedId: Long) {
        if (feedId <= 0L) {
            screenState.update {
                it.copy(
                    feedId = feedId,
                    isLoading = false,
                    hasAttemptedLoad = true,
                    errorMessage = "Invalid feed id",
                )
            }
            return
        }

        screenState.update {
            it.copy(
                feedId = feedId,
                isLoading = true,
                hasAttemptedLoad = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            feedRepository.getFeedListReply(
                id = feedId,
                asHtml = false,
                commitToStore = true,
            ).onSuccess {
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                Timber.e(throwable, "CommentViewModel load failed")
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load activity",
                    )
                }
            }
        }
    }

    suspend fun toggleFeedLike(feedId: Long): MutationResult =
        if (mutationRegistry.state.value[OperationKey.feedLike(feedId)].isRunning()) {
            MutationResult.Success
        } else {
            toggleLikeInteractor(
                ToggleLikeCommand(
                    id = feedId,
                    likeableType = LikeableType.ACTIVITY,
                ),
            )
        }

    suspend fun toggleReplyLike(replyId: Long): MutationResult =
        if (mutationRegistry.state.value[OperationKey.replyLike(replyId)].isRunning()) {
            MutationResult.Success
        } else {
            toggleLikeInteractor(
                ToggleLikeCommand(
                    id = replyId,
                    likeableType = LikeableType.ACTIVITY_REPLY,
                ),
            )
        }

    suspend fun submitReply(
        feedId: Long,
        text: String,
        replyId: Long? = null,
    ): MutationResult = saveReplyInteractor(
        SaveReplyRequest(
            replyId = replyId,
            feedId = feedId,
            text = text,
        ),
    )

    suspend fun deleteReply(replyId: Long): MutationResult =
        if (mutationRegistry.state.value[OperationKey.replyDelete(replyId)].isRunning()) {
            MutationResult.Success
        } else {
            deleteReplyInteractor(DeleteReplyCommand(replyId = replyId))
        }

    suspend fun deleteFeed(feedId: Long): MutationResult =
        if (mutationRegistry.state.value[OperationKey.feedDelete(feedId)].isRunning()) {
            MutationResult.Success
        } else {
            deleteFeedInteractor(DeleteFeedCommand(feedId = feedId))
        }

    suspend fun editFeed(feedId: Long, text: String): MutationResult =
        saveFeedInteractor(
            SaveFeedRequest.Text(
                id = feedId,
                text = text,
            ),
        )

    private fun OperationStatus?.isRunning(): Boolean = this is OperationStatus.Running
}
