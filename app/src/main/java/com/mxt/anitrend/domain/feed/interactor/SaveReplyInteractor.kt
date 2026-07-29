package com.mxt.anitrend.domain.feed.interactor

import com.mxt.anitrend.data.mapper.toFeedReplyRecord
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.mutation.RevisionProvider
import com.mxt.anitrend.domain.interactor.executeMutation
import com.mxt.anitrend.repository.FeedRepository

data class SaveReplyRequest(
    val replyId: Long? = null,
    val feedId: Long,
    val text: String?,
    val asHtml: Boolean = false,
)

class SaveReplyInteractor(
    private val feedRepository: FeedRepository,
    private val mutationExecutor: MutationExecutor,
    private val feedStore: FeedStore,
    private val revisionProvider: RevisionProvider,
) {
    suspend operator fun invoke(request: SaveReplyRequest): MutationResult {
        val resourceKey = request.replyId?.let(ResourceKey::Reply) ?: ResourceKey.Feed(request.feedId)
        val operationKey = request.replyId?.let(OperationKey::replySave)
            ?: OperationKey.of(resourceKey, OperationKey.Type.REPLY_SAVE)

        return executeMutation(
            mutationExecutor = mutationExecutor,
            revisionProvider = revisionProvider,
            resourceKey = resourceKey,
            operationKey = operationKey,
            failureMessage = "Unable to save reply",
        ) { revision ->
            feedRepository.saveActivityReply(
                id = request.replyId,
                activityId = request.feedId,
                text = request.text,
                asHtml = request.asHtml,
                commitToStore = false,
                revision = revision,
            ).fold(
                onSuccess = { reply ->
                    feedStore.apply(
                        FeedStoreChange.ReplyUpserted(
                            feedId = request.feedId,
                            reply = reply.toFeedReplyRecord(
                                activityId = request.feedId,
                                revision = revision,
                            ),
                        ),
                    )
                    MutationResult.Success
                },
                onFailure = { throwable ->
                    MutationResult.Failure(
                        message = throwable.message ?: "Unable to save reply",
                        cause = throwable,
                    )
                },
            )
        }
    }
}
