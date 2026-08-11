package com.mxt.anitrend.domain.model

import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil

data class IncrementMediaProgressCommand(
    val id: Int?,
    val mediaId: Long?,
    val currentProgress: Int,
    val requestedProgress: Int,
    val status: MediaListStatus?,
    val score: Double?,
    val scoreRaw: Int? = null,
    val progressVolumes: Int?,
    val repeat: Int?,
    val priority: Int?,
    val isPrivate: Boolean,
    val hiddenFromStatusLists: Boolean,
    val customLists: List<String?>?,
    val advancedScores: List<Double?>?,
    val notes: String?,
    val startedAt: FuzzyDateRecord?,
    val completedAt: FuzzyDateRecord?,
)

fun buildIncrementMediaProgressCommand(
    record: MediaListRecord,
    currentDate: FuzzyDateRecord = FuzzyDateRecord(
        year = DateUtil.year,
        month = DateUtil.month + 1,
        day = DateUtil.date,
    ),
): IncrementMediaProgressCommand {
    val requestedProgress = record.progress + 1
    var status = record.status
    var startedAt = record.startedAt
    var completedAt = record.completedAt

    if (record.progress < 1 &&
        (CompatUtil.equals(record.status, KeyUtil.PLANNING) || CompatUtil.equals(record.status, KeyUtil.CURRENT))
    ) {
        status = KeyUtil.CURRENT
        startedAt = currentDate.copy()
    }

    if (isIncrementLimitReached(record, requestedProgress)) {
        status = KeyUtil.COMPLETED
        completedAt = currentDate.copy()
    }

    return IncrementMediaProgressCommand(
        id = record.id.takeIf { it > 0 }?.toInt(),
        mediaId = record.mediaId,
        currentProgress = record.progress,
        requestedProgress = requestedProgress,
        status = status?.toMediaListStatus(),
        score = record.score,
        scoreRaw = record.scoreRaw,
        progressVolumes = record.progressVolumes,
        repeat = record.repeat,
        priority = record.priority,
        isPrivate = record.`private`,
        hiddenFromStatusLists = record.hiddenFromStatusLists,
        customLists = record.customLists.takeIf { it.isNotEmpty() },
        advancedScores = record.advancedScores.values.toList().takeIf { it.isNotEmpty() },
        notes = record.notes,
        startedAt = startedAt,
        completedAt = completedAt,
    )
}

fun IncrementMediaProgressCommand.toSaveMediaListEntryCommand(): SaveMediaListEntryCommand = SaveMediaListEntryCommand(
    id = id,
    mediaId = mediaId,
    status = status,
    score = score,
    scoreRaw = scoreRaw,
    progress = requestedProgress,
    progressVolumes = progressVolumes,
    repeat = repeat,
    priority = priority,
    isPrivate = isPrivate,
    hiddenFromStatusLists = hiddenFromStatusLists,
    customLists = customLists,
    advancedScores = advancedScores,
    notes = notes,
    startedAt = startedAt?.toInput(),
    completedAt = completedAt?.toInput(),
)

private fun isIncrementLimitReached(
    record: MediaListRecord,
    requestedProgress: Int,
): Boolean {
    val mediaSummary = record.media ?: return false
    return if (CompatUtil.equals(mediaSummary.type, KeyUtil.ANIME)) {
        mediaSummary.episodes == requestedProgress && mediaSummary.episodes != 0
    } else {
        mediaSummary.chapters == requestedProgress && mediaSummary.chapters != 0
    }
}

private fun FuzzyDateRecord.toInput(): FuzzyDateInput = FuzzyDateInput(
    day = day,
    month = month,
    year = year,
)

private fun String.toMediaListStatus(): MediaListStatus? = runCatching {
    MediaListStatus.valueOf(this)
}.getOrNull()
