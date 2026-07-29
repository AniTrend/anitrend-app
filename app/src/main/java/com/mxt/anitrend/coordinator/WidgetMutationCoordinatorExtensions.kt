package com.mxt.anitrend.coordinator

import com.mxt.anitrend.domain.model.SaveMediaListEntryCommand
import com.mxt.anitrend.model.entity.anilist.MediaList

fun WidgetMutationCoordinator.saveMediaListEntry(
    command: SaveMediaListEntryCommand,
    onResult: (Result<MediaList>) -> Unit,
) {
    saveMediaListEntry(
        id = command.id,
        mediaId = command.mediaId,
        status = command.status,
        score = command.score,
        scoreRaw = command.scoreRaw,
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
        onResult = onResult,
    )
}
