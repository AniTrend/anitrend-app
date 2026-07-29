package com.mxt.anitrend.domain.medialist.interactor

import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.mutation.RevisionProvider
import com.mxt.anitrend.domain.interactor.executeMutation
import com.mxt.anitrend.repository.BrowseRepository

class DeleteMediaListEntryInteractor(
    private val browseRepository: BrowseRepository,
    private val mutationExecutor: MutationExecutor,
    private val mediaListStore: MediaListStore,
    private val revisionProvider: RevisionProvider,
) {
    suspend operator fun invoke(
        entryId: Long,
        mediaId: Long?,
    ): MutationResult {
        val resourceKey = mediaId?.let(ResourceKey::MediaListByMedia) ?: ResourceKey.MediaListById(entryId)
        val operationKey = mediaId?.let(OperationKey::mediaListDeleteByMedia) ?: OperationKey.mediaListDelete(entryId)

        return executeMutation(
            mutationExecutor = mutationExecutor,
            revisionProvider = revisionProvider,
            resourceKey = resourceKey,
            operationKey = operationKey,
            failureMessage = "Unable to delete media list entry",
        ) { revision ->
            browseRepository.deleteMediaListEntry(
                id = entryId,
                mediaId = mediaId,
                commitToStore = false,
                revision = revision,
            ).fold(
                onSuccess = { deleteState ->
                    if (!deleteState.isDeleted) {
                        MutationResult.Failure(message = "Media list entry $entryId was not deleted")
                    } else {
                        mediaListStore.apply(
                            MediaListStoreChange.EntryDeleted(
                                entryId = entryId,
                                mediaId = mediaId,
                                revision = revision,
                            ),
                        )
                        MutationResult.Success
                    }
                },
                onFailure = { throwable ->
                    MutationResult.Failure(
                        message = throwable.message ?: "Unable to delete media list entry",
                        cause = throwable,
                    )
                },
            )
        }
    }
}
