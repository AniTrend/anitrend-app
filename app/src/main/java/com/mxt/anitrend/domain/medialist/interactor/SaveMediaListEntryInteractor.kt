package com.mxt.anitrend.domain.medialist.interactor

import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.mutation.RevisionProvider
import com.mxt.anitrend.domain.interactor.executeMutation
import com.mxt.anitrend.domain.model.SaveMediaListEntryCommand
import com.mxt.anitrend.repository.BrowseRepository

class SaveMediaListEntryInteractor(
    private val browseRepository: BrowseRepository,
    private val mutationExecutor: MutationExecutor,
    private val mediaListStore: MediaListStore,
    private val revisionProvider: RevisionProvider,
) {
    suspend operator fun invoke(command: SaveMediaListEntryCommand): MutationResult {
        val resourceKey = resolveResourceKey(command)
            ?: return MutationResult.Failure(message = "Media list save requires mediaId or id")
        val operationKey = resolveSaveOperationKey(command, resourceKey)

        return save(
            command = command,
            resourceKey = resourceKey,
            operationKey = operationKey,
            failureMessage = "Unable to save media list entry",
        )
    }

    internal suspend fun save(
        command: SaveMediaListEntryCommand,
        resourceKey: ResourceKey,
        operationKey: OperationKey,
        failureMessage: String = "Unable to save media list entry",
    ): MutationResult = executeMutation(
        mutationExecutor = mutationExecutor,
        revisionProvider = revisionProvider,
        resourceKey = resourceKey,
        operationKey = operationKey,
        failureMessage = failureMessage,
    ) { revision ->
        browseRepository.saveMediaListEntry(
            id = command.id,
            mediaId = command.mediaId,
            status = command.status,
            scoreRaw = command.scoreRaw,
            score = command.score,
            progress = command.progress,
            progressVolumes = command.progressVolumes,
            repeat = command.repeat,
            priority = command.priority,
            private = command.isPrivate,
            hiddenFromStatusLists = command.hiddenFromStatusLists,
            customLists = command.customLists,
            advancedScores = command.advancedScores,
            notes = command.notes,
            startedAt = command.startedAt,
            completedAt = command.completedAt,
            commitToStore = false,
            revision = revision,
        ).fold(
            onSuccess = { entry ->
                mediaListStore.apply(
                    MediaListStoreChange.EntryUpserted(
                        entry = entry.toMediaListRecord(revision = revision),
                    ),
                )
                MutationResult.Success
            },
            onFailure = { throwable ->
                MutationResult.Failure(
                    message = throwable.message ?: failureMessage,
                    cause = throwable,
                )
            },
        )
    }

    private fun resolveResourceKey(command: SaveMediaListEntryCommand): ResourceKey? =
        command.mediaId?.let(ResourceKey::MediaListByMedia)
            ?: command.id?.toLong()?.let(ResourceKey::MediaListById)

    private fun resolveSaveOperationKey(
        command: SaveMediaListEntryCommand,
        resourceKey: ResourceKey,
    ): OperationKey =
        command.mediaId?.let(OperationKey::mediaListSave)
            ?: command.id?.toLong()?.let(OperationKey::mediaListSaveById)
            ?: OperationKey.of(resourceKey, OperationKey.Type.MEDIA_LIST_SAVE)
}
