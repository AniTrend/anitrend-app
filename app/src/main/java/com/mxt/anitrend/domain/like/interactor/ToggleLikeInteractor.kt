package com.mxt.anitrend.domain.like.interactor

import com.mxt.anitrend.data.mapper.toUserSummaryRecords
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.mutation.RevisionProvider
import com.mxt.anitrend.domain.interactor.executeMutation
import com.mxt.anitrend.domain.model.ToggleLikeCommand
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.repository.BaseRepository

class ToggleLikeInteractor(
    private val baseRepository: BaseRepository,
    private val mutationExecutor: MutationExecutor,
    private val feedStore: FeedStore,
    private val revisionProvider: RevisionProvider,
) {
    suspend operator fun invoke(command: ToggleLikeCommand): MutationResult {
        val resourceKey =
            when (command.likeableType) {
                LikeableType.ACTIVITY -> ResourceKey.Feed(command.id)
                LikeableType.ACTIVITY_REPLY -> ResourceKey.Reply(command.id)
                else -> {
                    return MutationResult.Failure(
                        message = "Unsupported likeable type ${command.likeableType}",
                    )
                }
            }
        val operationKey =
            when (command.likeableType) {
                LikeableType.ACTIVITY -> OperationKey.feedLike(command.id)
                LikeableType.ACTIVITY_REPLY -> OperationKey.replyLike(command.id)
            }

        return executeMutation(
            mutationExecutor = mutationExecutor,
            revisionProvider = revisionProvider,
            resourceKey = resourceKey,
            operationKey = operationKey,
            failureMessage = "Unable to toggle like",
        ) { revision ->
            val replyFeedId =
                if (command.likeableType == LikeableType.ACTIVITY_REPLY) {
                    feedStore.state.value.repliesById[command.id]?.activityId
                } else {
                    null
                }

            baseRepository.toggleLike(
                id = command.id,
                type = command.likeableType,
                commitToStore = false,
                replyFeedId = replyFeedId,
                revision = revision,
            ).fold(
                onSuccess = { users ->
                    val likes = users.toUserSummaryRecords()
                    when (command.likeableType) {
                        LikeableType.ACTIVITY -> {
                            feedStore.apply(
                                FeedStoreChange.FeedLikesReplaced(
                                    feedId = command.id,
                                    likes = likes,
                                    revision = revision,
                                ),
                            )
                            MutationResult.Success
                        }
                        LikeableType.ACTIVITY_REPLY -> {
                            val feedId =
                                replyFeedId ?: return@fold MutationResult.Failure(
                                    message = "Reply ${command.id} is not available in FeedStore",
                                )
                            feedStore.apply(
                                FeedStoreChange.ReplyLikesReplaced(
                                    feedId = feedId,
                                    replyId = command.id,
                                    likes = likes,
                                    revision = revision,
                                ),
                            )
                            MutationResult.Success
                        }
                    }
                },
                onFailure = { throwable ->
                    MutationResult.Failure(
                        message = throwable.message ?: "Unable to toggle like",
                        cause = throwable,
                    )
                },
            )
        }
    }
}
