package com.mxt.anitrend.domain.mediadetail.model

/**
 * Immutable canonical representation of the media stats query
 * (`MediaStats.graphql`) in the media detail pipeline.
 *
 * Covers the exact field set requested by `MediaStats.graphql`: media type,
 * external links, score/status distributions, and rankings. Pure Kotlin value
 * type, intentionally not Parcelable and not ObjectBox-backed. Mapped from the
 * generated GraphQL `MediaStatsData.Media` type by
 * `com.mxt.anitrend.data.mapper.toMediaStatsRecord`. Generated Int ids are
 * converted to domain Longs and the generated enums (type, status-distribution
 * status, ranking type/format/season) are exposed as their serialized `name`,
 * matching the legacy String-backed entity lane.
 *
 * The nullable `stats` block from the generated transport is collapsed into the
 * nullable [scoreDistribution] and [statusDistribution] lists: a null block
 * yields null lists, preserving the nullable semantics of the generated shape.
 * Null list elements are dropped by the mapper, following the established
 * node-list mapping convention. The legacy mutable
 * [com.mxt.anitrend.model.entity.anilist.Media] remains unchanged for its
 * remaining consumers.
 */
data class MediaStatsRecord(
    val type: String?,
    val externalLinks: List<MediaStatsExternalLinkRecord>?,
    val scoreDistribution: List<MediaStatsScoreDistributionRecord>?,
    val statusDistribution: List<MediaStatsStatusDistributionRecord>?,
    val rankings: List<MediaStatsRankingRecord>?,
)

/**
 * External-link projection as requested by `MediaStats.graphql` (`id`, `url`,
 * `site`).
 */
data class MediaStatsExternalLinkRecord(
    val id: Long,
    val url: String?,
    val site: String,
)

/**
 * Score-distribution projection as requested by `MediaStats.graphql` (`score`,
 * `amount`).
 */
data class MediaStatsScoreDistributionRecord(
    val score: Int?,
    val amount: Int?,
)

/**
 * Status-distribution projection as requested by `MediaStats.graphql`
 * (`status`, `amount`). The generated status enum is exposed as its serialized
 * `name`.
 */
data class MediaStatsStatusDistributionRecord(
    val status: String?,
    val amount: Int?,
)

/**
 * Ranking projection as requested by `MediaStats.graphql` (`id`, `rank`, `type`,
 * `format`, `year`, `season`, `allTime`, `context`). The generated `type`,
 * `format`, and `season` enums are exposed as their serialized `name`.
 */
data class MediaStatsRankingRecord(
    val id: Long,
    val rank: Int,
    val type: String,
    val format: String,
    val year: Int?,
    val season: String?,
    val allTime: Boolean?,
    val context: String,
)
