package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.MediaSearchItemUiModel
import com.mxt.anitrend.model.entity.base.MediaBase

/**
 * Maps the legacy [MediaBase] search result entity into the immutable
 * [MediaSearchItemUiModel] consumed by the media search paging pipeline.
 *
 * The media search repository still serves the legacy
 * [com.mxt.anitrend.model.entity.container.body.PageContainer] lane, so this
 * mapper is the legacy-to-domain bridge for the search feature. The card
 * title resolves with the same precedence as the recommendations projection
 * (user preferred, then romaji, then english, then original), the cover image
 * prefers extraLarge over large over medium, and the start date is converted
 * through the existing [toFuzzyDateRecord] mapping.
 */
fun MediaBase.toMediaSearchItemUiModel(): MediaSearchItemUiModel = MediaSearchItemUiModel(
    id = id,
    title = title?.userPreferred
        ?: title?.romaji
        ?: title?.english
        ?: title?.original.orEmpty(),
    titleEnglish = title?.english,
    titleOriginal = title?.original,
    coverImage = coverImage?.extraLarge ?: coverImage?.large ?: coverImage?.medium,
    mediaType = type,
    mediaFormat = format,
    mediaStatus = status,
    mediaEpisodes = episodes,
    mediaChapters = chapters,
    mediaVolumes = volumes,
    mediaStartDate = startDate?.toFuzzyDateRecord(),
    averageScore = averageScore,
    isFavourite = isFavourite,
)
