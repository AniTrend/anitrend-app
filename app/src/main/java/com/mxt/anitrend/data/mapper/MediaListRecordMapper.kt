package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.model.entity.anilist.meta.CustomList
import com.mxt.anitrend.model.entity.anilist.MediaList

fun MediaList.toMediaListRecord(
    revision: Long = 0L,
    ownerUserId: Long? = null,
    ownerUserName: String? = null,
): MediaListRecord = MediaListRecord(
    id = id,
    mediaId = mediaId,
    status = status,
    score = score.toDouble(),
    scoreRaw = scoreRaw,
    progress = progress,
    progressVolumes = progressVolumes,
    repeat = repeat,
    priority = priority,
    `private` = isHidden,
    hiddenFromStatusLists = isHiddenFromStatusLists,
    customLists = customLists.orEmpty().filter { it.isEnabled }.mapNotNull { it.name }.toList(),
    advancedScores = advancedScores.orEmpty().mapValues { it.value.toDouble() }.toMap(),
    notes = notes,
    startedAt = startedAt?.toFuzzyDateRecord(),
    completedAt = completedAt?.toFuzzyDateRecord(),
    media = media.toMediaSummaryRecord(),
    revision = revision,
    ownerUserId = ownerUserId,
    ownerUserName = ownerUserName,
)

fun MediaListRecord.toMediaList(): MediaList = MediaList().apply {
    id = this@toMediaList.id
    mediaId = this@toMediaList.mediaId
    status = this@toMediaList.status
    score = this@toMediaList.score.toFloat()
    scoreRaw = this@toMediaList.scoreRaw
    progress = this@toMediaList.progress
    progressVolumes = this@toMediaList.progressVolumes
    repeat = this@toMediaList.repeat
    priority = this@toMediaList.priority
    notes = this@toMediaList.notes
    isHidden = this@toMediaList.private
    isHiddenFromStatusLists = this@toMediaList.hiddenFromStatusLists
    advancedScores = this@toMediaList.advancedScores.mapValues { it.value.toFloat() }
    customLists = this@toMediaList.customLists.map { name ->
        CustomList(name = name, isEnabled = true)
    }
    startedAt = this@toMediaList.startedAt?.toFuzzyDate()
    completedAt = this@toMediaList.completedAt?.toFuzzyDate()
    media = this@toMediaList.media?.toMediaBase() ?: media
}
