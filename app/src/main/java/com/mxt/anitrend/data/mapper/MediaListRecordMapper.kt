package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.model.entity.anilist.MediaList

fun MediaList.toMediaListRecord(revision: Long = 0L): MediaListRecord = MediaListRecord(
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
)
