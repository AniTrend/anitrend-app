package com.mxt.anitrend.domain.model

/**
 * Narrow render projection of [RecommendationItemUiModel] for the shared
 * recommendation card helpers.
 *
 * Carries only the fields that the existing [SeriesYearTypeTextView] and
 * [RatingTextView] helpers need, so the adapter does not have to rebuild a
 * mutable legacy [com.mxt.anitrend.model.entity.base.MediaBase] just to bind
 * the same layout.
 */
data class RecommendationItemRenderModel(
    val mediaStartDate: FuzzyDateRecord?,
    val mediaType: String?,
    val mediaFormat: String?,
    val mediaEpisodes: Int,
    val mediaChapters: Int,
    val averageScore: Int?,
    val isFavourite: Boolean,
)

fun RecommendationItemUiModel.toRenderModel(): RecommendationItemRenderModel = RecommendationItemRenderModel(
    mediaStartDate = mediaStartDate,
    mediaType = mediaType,
    mediaFormat = mediaFormat,
    mediaEpisodes = mediaEpisodes,
    mediaChapters = mediaChapters,
    averageScore = averageScore,
    isFavourite = isFavourite,
)
