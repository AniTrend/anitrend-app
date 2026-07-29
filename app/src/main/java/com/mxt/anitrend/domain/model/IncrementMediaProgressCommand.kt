package com.mxt.anitrend.domain.model

import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.util.media.MediaUtil

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
    val startedAt: FuzzyDateInput?,
    val completedAt: FuzzyDateInput?,
)

fun buildIncrementMediaProgressCommand(
    model: MediaList,
    currentDate: FuzzyDate = DateUtil.currentDate,
): IncrementMediaProgressCommand {
    val requestedProgress = model.progress + 1
    var status = model.status
    var startedAt = model.startedAt
    var completedAt = model.completedAt

    if (model.progress < 1 && (CompatUtil.equals(model.status, KeyUtil.PLANNING) || CompatUtil.equals(model.status, KeyUtil.CURRENT))) {
        status = KeyUtil.CURRENT
        startedAt = currentDate.copyOf()
    }

    if (isIncrementLimitReached(model, requestedProgress)) {
        status = KeyUtil.COMPLETED
        completedAt = currentDate.copyOf()
    }

    return IncrementMediaProgressCommand(
        id = model.id.takeIf { it > 0 }?.toInt(),
        mediaId = model.mediaId,
        currentProgress = model.progress,
        requestedProgress = requestedProgress,
        status = status?.toMediaListStatus(),
        score = model.score.toDouble(),
        scoreRaw = model.scoreRaw,
        progressVolumes = model.progressVolumes,
        repeat = model.repeat,
        priority = model.priority,
        isPrivate = model.isHidden,
        hiddenFromStatusLists = model.isHiddenFromStatusLists,
        customLists = model.customLists?.filter { it.isEnabled }?.mapNotNull { it.name?.takeIf(String::isNotEmpty) },
        advancedScores = model.advancedScores?.values?.map { it.toDouble() },
        notes = model.notes,
        startedAt = startedAt?.toInput(),
        completedAt = completedAt?.toInput(),
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
    startedAt = startedAt,
    completedAt = completedAt,
)

fun resolveIncrementResultModel(
    committedModel: MediaList,
    result: Result<MediaList>,
): MediaList = result.getOrNull()?.also { savedResult ->
    savedResult.media = committedModel.media
} ?: committedModel

private fun isIncrementLimitReached(
    model: MediaList,
    requestedProgress: Int,
): Boolean {
    val mediaBase = model.media
    return if (MediaUtil.isAnimeType(mediaBase)) {
        mediaBase.episodes == requestedProgress && mediaBase.episodes != 0
    } else {
        mediaBase.chapters == requestedProgress && mediaBase.chapters != 0
    }
}

private fun FuzzyDate.toInput(): FuzzyDateInput = FuzzyDateInput(
    day = day,
    month = month,
    year = year,
)

private fun String.toMediaListStatus(): MediaListStatus? = runCatching {
    MediaListStatus.valueOf(this)
}.getOrNull()

private fun FuzzyDate.copyOf(): FuzzyDate = FuzzyDate(
    day = day,
    month = month,
    year = year,
)
