package com.mxt.anitrend.domain.feed.interactor

import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.mutation.RevisionProvider
import com.mxt.anitrend.domain.interactor.executeMutation
import com.mxt.anitrend.domain.model.DeleteFeedCommand
import com.mxt.anitrend.repository.FeedRepository

class DeleteFeedInteractor(
    private val feedRepository: FeedRepository,
    private val mutationExecutor: MutationExecutor,
    private val feedStore: FeedStore,
    private val revisionProvider: RevisionProvider,
) {
    suspend operator fun invoke(command: DeleteFeedCommand): MutationResult = executeMutation(
        mutationExecutor = mutationExecutor,
        revisionProvider = revisionProvider,
        resourceKey = ResourceKey.Feed(command.feedId),
        operationKey = OperationKey.feedDelete(command.feedId),
        failureMessage = "Unable to delete feed",
    ) { revision ->
        feedRepository.deleteActivity(
            id = command.feedId,
            commitToStore = false,
            revision = revision,
        ).fold(
            onSuccess = { deleteState ->
                if (!deleteState.isDeleted) {
                    MutationResult.Failure(message = "Feed ${command.feedId} was not deleted")
                } else {
                    feedStore.apply(
                        FeedStoreChange.FeedDeleted(
                            feedId = command.feedId,
                            revision = revision,
                        ),
                    )
                    MutationResult.Success
                }
            },
            onFailure = { throwable ->
                MutationResult.Failure(
                    message = throwable.message ?: "Unable to delete feed",
                    cause = throwable,
                )
            },
        )
    }
}
