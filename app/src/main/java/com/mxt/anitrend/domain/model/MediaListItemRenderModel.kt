package com.mxt.anitrend.domain.model

/**
 * Immutable projection of [MediaListItemUiModel] carrying only the fields the
 * series list view helpers need to render a row. Using this render model keeps
 * the adapter from rebuilding a mutable legacy `MediaList` entity merely to call
 * view helper APIs.
 */
data class MediaListItemRenderModel(
    val score: Double,
    val progress: Int,
    val mediaStatus: String?,
    val nextAiringEpisode: AiringScheduleRecord?,
    val mediaStartDate: FuzzyDateRecord?,
    val mediaType: String?,
    val mediaFormat: String?,
    val mediaEpisodes: Int,
    val mediaChapters: Int,
    val mediaIsFavourite: Boolean,
)

fun MediaListItemUiModel.toRenderModel(): MediaListItemRenderModel = MediaListItemRenderModel(
    score = score,
    progress = progress,
    mediaStatus = mediaStatus,
    nextAiringEpisode = nextAiringEpisode,
    mediaStartDate = mediaStartDate,
    mediaType = mediaType,
    mediaFormat = mediaFormat,
    mediaEpisodes = mediaEpisodes,
    mediaChapters = mediaChapters,
    mediaIsFavourite = mediaIsFavourite,
)
