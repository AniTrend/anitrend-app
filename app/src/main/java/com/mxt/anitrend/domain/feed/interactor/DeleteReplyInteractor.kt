package com.mxt.anitrend.domain.feed.interactor

import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.mutation.RevisionProvider
import com.mxt.anitrend.domain.interactor.executeMutation
import com.mxt.anitrend.domain.model.DeleteReplyCommand
import com.mxt.anitrend.repository.FeedRepository

class DeleteReplyInteractor(
    private val feedRepository: FeedRepository,
    private val mutationExecutor: MutationExecutor,
    private val feedStore: FeedStore,
    private val revisionProvider: RevisionProvider,
) {
    suspend operator fun invoke(command: DeleteReplyCommand): MutationResult = executeMutation(
        mutationExecutor = mutationExecutor,
        revisionProvider = revisionProvider,
        resourceKey = ResourceKey.Reply(command.replyId),
        operationKey = OperationKey.replyDelete(command.replyId),
        failureMessage = "Unable to delete reply",
    ) { revision ->
        val feedId = feedStore.state.value.repliesById[command.replyId]?.activityId
        if (feedId == null) {
            MutationResult.Failure(
                message = "Reply ${command.replyId} is not available in FeedStore",
            )
        } else {
            feedRepository.deleteActivityReply(
                id = command.replyId,
                feedId = feedId,
                commitToStore = false,
                revision = revision,
            ).fold(
                onSuccess = { deleteState ->
                    if (!deleteState.isDeleted) {
                        MutationResult.Failure(message = "Reply ${command.replyId} was not deleted")
                    } else {
                        feedStore.apply(
                            FeedStoreChange.ReplyDeleted(
                                feedId = feedId,
                                replyId = command.replyId,
                                revision = revision,
                            ),
                        )
                        MutationResult.Success
                    }
                },
                onFailure = { throwable ->
                    MutationResult.Failure(
                        message = throwable.message ?: "Unable to delete reply",
                        cause = throwable,
                    )
                },
            )
        }
    }
}
