package com.mxt.anitrend.domain.medialist.interactor

import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.domain.model.IncrementMediaProgressCommand
import com.mxt.anitrend.domain.model.toSaveMediaListEntryCommand

class IncrementMediaProgressInteractor(
    private val saveMediaListEntryInteractor: SaveMediaListEntryInteractor,
) {
    suspend operator fun invoke(command: IncrementMediaProgressCommand): MutationResult {
        val resourceKey = command.mediaId?.let(ResourceKey::MediaListByMedia)
            ?: command.id?.toLong()?.let(ResourceKey::MediaListById)
            ?: return MutationResult.Failure(message = "Increment requires mediaId or id")
        val operationKey = command.mediaId?.let(OperationKey::mediaListIncrementProgress)
            ?: OperationKey.of(resourceKey, OperationKey.Type.MEDIA_LIST_INCREMENT_PROGRESS)

        return saveMediaListEntryInteractor.save(
            command = command.toSaveMediaListEntryCommand(),
            resourceKey = resourceKey,
            operationKey = operationKey,
            failureMessage = "Unable to increment media progress",
        )
    }
}
