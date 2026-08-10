package com.mxt.anitrend.domain.model

import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.base.MediaBase

data class MediaListDraft(
    val status: String?,
    val score: Float,
    val scoreRaw: Int?,
    val progress: Int,
    val progressVolumes: Int,
    val repeat: Int,
    val priority: Int,
    val isHidden: Boolean,
    val isHiddenFromStatusLists: Boolean,
    val notes: String?,
    val advancedScores: Map<String, Float>?,
    val customLists: List<String>?,
    val startedAt: FuzzyDate?,
    val completedAt: FuzzyDate?,
)

fun MediaList.toDraft(): MediaListDraft = MediaListDraft(
    status = status,
    score = score,
    scoreRaw = scoreRaw,
    progress = progress,
    progressVolumes = progressVolumes,
    repeat = repeat,
    priority = priority,
    isHidden = isHidden,
    isHiddenFromStatusLists = isHiddenFromStatusLists,
    notes = notes,
    advancedScores = advancedScores?.toMap(),
    customLists = customLists?.filter { it.isEnabled }?.mapNotNull { it.name },
    startedAt = startedAt?.copyOf(),
    completedAt = completedAt?.copyOf(),
)

fun MediaList.copyForEditing(mediaBase: MediaBase): MediaList = MediaList().apply {
    id = this@copyForEditing.id
    mediaId = this@copyForEditing.mediaId.takeIf { it > 0 } ?: mediaBase.id
    status = this@copyForEditing.status
    score = this@copyForEditing.score
    scoreRaw = this@copyForEditing.scoreRaw
    progress = this@copyForEditing.progress
    progressVolumes = this@copyForEditing.progressVolumes
    repeat = this@copyForEditing.repeat
    priority = this@copyForEditing.priority
    notes = this@copyForEditing.notes
    isHidden = this@copyForEditing.isHidden
    isHiddenFromStatusLists = this@copyForEditing.isHiddenFromStatusLists
    advancedScores = this@copyForEditing.advancedScores?.toMap()
    customLists = this@copyForEditing.customLists?.toList()
    startedAt = this@copyForEditing.startedAt?.copyOf()
    completedAt = this@copyForEditing.completedAt?.copyOf()
    updatedAt = this@copyForEditing.updatedAt
    createdAt = this@copyForEditing.createdAt
    media = this@copyForEditing.media.takeIf { it.id > 0 } ?: mediaBase
}

fun createEditableMediaList(
    source: MediaList?,
    mediaBase: MediaBase,
): MediaList = source?.copyForEditing(mediaBase) ?: MediaList().apply {
    mediaId = mediaBase.id
    media = mediaBase
}

fun MediaListDraft.toSaveMediaListEntryCommand(
    committedModel: MediaList,
): SaveMediaListEntryCommand = SaveMediaListEntryCommand(
    id = committedModel.id.takeIf { it > 0 }?.toInt(),
    mediaId = committedModel.mediaId.takeIf { it > 0 } ?: committedModel.media.id.takeIf { it > 0 },
    status = status?.toMediaListStatus(),
    score = score.toDouble(),
    scoreRaw = scoreRaw,
    progress = progress,
    progressVolumes = progressVolumes,
    repeat = repeat,
    priority = priority,
    isPrivate = isHidden,
    hiddenFromStatusLists = isHiddenFromStatusLists,
    customLists = customLists.orEmpty(),
    advancedScores = advancedScores?.values?.map { it.toDouble() },
    notes = notes,
    startedAt = startedAt?.toInput(),
    completedAt = completedAt?.toInput(),
)

private fun String.toMediaListStatus(): MediaListStatus? = runCatching {
    MediaListStatus.valueOf(this)
}.getOrNull()

private fun FuzzyDate.toInput(): FuzzyDateInput = FuzzyDateInput(
    day = day,
    month = month,
    year = year,
)

private fun FuzzyDate.copyOf(): FuzzyDate = FuzzyDate(
    day = day,
    month = month,
    year = year,
)
