package com.mxt.anitrend.domain.feed.interactor

import com.mxt.anitrend.data.mapper.toFeedRecord
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.domain.interactor.executeMutation
import com.mxt.anitrend.repository.FeedRepository

sealed interface SaveFeedRequest {
    val id: Long?
    val asHtml: Boolean

    data class Text(
        override val id: Long? = null,
        val text: String?,
        override val asHtml: Boolean = false,
    ) : SaveFeedRequest

    data class Message(
        override val id: Long? = null,
        val message: String?,
        val recipientId: Long,
        override val asHtml: Boolean = false,
    ) : SaveFeedRequest
}

class SaveFeedInteractor(
    private val feedRepository: FeedRepository,
    private val mutationExecutor: MutationExecutor,
    private val feedStore: FeedStore,
    private val requestSequence: RequestSequence,
) {
    suspend operator fun invoke(request: SaveFeedRequest): MutationResult {
        val resourceKey = ResourceKey.Feed(request.id ?: 0L)
        val operationKey = OperationKey.feedSave(request.id ?: 0L)

        return executeMutation(
            mutationExecutor = mutationExecutor,
            requestSequence = requestSequence,
            resourceKey = resourceKey,
            operationKey = operationKey,
            failureMessage = "Unable to save feed",
        ) { revision, context ->
            val result =
                when (request) {
                    is SaveFeedRequest.Text ->
                        feedRepository.saveTextActivity(
                            id = request.id,
                            text = request.text,
                            asHtml = request.asHtml,
                            commitToStore = false,
                            revision = revision,
                        )
                    is SaveFeedRequest.Message ->
                        feedRepository.saveMessageActivity(
                            id = request.id,
                            message = request.message,
                            recipientId = request.recipientId,
                            asHtml = request.asHtml,
                            commitToStore = false,
                            revision = revision,
                        )
                }

            result.fold(
                onSuccess = { feed ->
                    context.ensureSessionActive()
                    feedStore.apply(
                        FeedStoreChange.FeedUpserted(
                            feed = feed.toFeedRecord(revision = revision),
                        ),
                    )
                    MutationResult.Success
                },
                onFailure = { throwable ->
                    MutationResult.Failure(
                        message = throwable.message ?: "Unable to save feed",
                        cause = throwable,
                    )
                },
            )
        }
    }
}
