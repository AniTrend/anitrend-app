package com.mxt.anitrend.domain.model

/**
 * Narrow render projection of [MediaSearchItemUiModel] for the shared media
 * card helpers.
 *
 * Carries only the fields that the existing [com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView]
 * and [com.mxt.anitrend.base.custom.view.text.RatingTextView] helpers need, so
 * the adapter does not have to rebuild a mutable legacy
 * [com.mxt.anitrend.model.entity.base.MediaBase] just to bind the same layout.
 */
data class MediaSearchItemRenderModel(
    val mediaStartDate: FuzzyDateRecord?,
    val mediaType: String?,
    val mediaFormat: String?,
    val mediaEpisodes: Int,
    val mediaChapters: Int,
    val averageScore: Int?,
    val isFavourite: Boolean,
)

/**
 * Projects this UI model into the narrow [MediaSearchItemRenderModel] consumed
 * by the shared series-card view helpers.
 */
fun MediaSearchItemUiModel.toRenderModel(): MediaSearchItemRenderModel = MediaSearchItemRenderModel(
    mediaStartDate = mediaStartDate,
    mediaType = mediaType,
    mediaFormat = mediaFormat,
    mediaEpisodes = mediaEpisodes,
    mediaChapters = mediaChapters,
    averageScore = averageScore,
    isFavourite = isFavourite,
)
