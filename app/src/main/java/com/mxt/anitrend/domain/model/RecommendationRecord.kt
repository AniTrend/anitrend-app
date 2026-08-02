package com.mxt.anitrend.domain.model

/**
 * Immutable canonical representation of a media recommendation in the media detail
 * recommendations pipeline.
 *
 * Pure Kotlin value type, intentionally not Parcelable and not ObjectBox-backed.
 * Mapped from the generated GraphQL
 * `RecommendationMediaData.MediaRecommendationsNodes` type by
 * `com.mxt.anitrend.data.mapper.toRecommendationRecord`. Reuses the existing
 * [MediaSummaryRecord], [UserSummaryRecord], and [PageInfoRecord] domain values. The
 * legacy mutable [com.mxt.anitrend.model.entity.base.RecommendationBase] remains for
 * the group-series adapters and navigation consumers until they are migrated.
 */
data class RecommendationRecord(
    val id: Long,
    val mediaRecommendation: MediaSummaryRecord?,
    val rating: Int?,
    val user: UserSummaryRecord?,
    val userRating: String?,
)

/**
 * Page-level result of a media recommendations request.
 *
 * Preserves the server-returned node ordering ([recommendations]) together with the
 * paging metadata ([pageInfo]) needed to render and page the recommendations screen.
 */
data class RecommendationPageResult(
    val recommendations: List<RecommendationRecord>,
    val pageInfo: PageInfoRecord?,
)
