package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.mediadetail.model.MediaStatsExternalLinkRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaStatsRankingRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaStatsRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaStatsScoreDistributionRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaStatsStatusDistributionRecord
import com.mxt.anitrend.graphql.generated.MediaStatsData

/**
 * Maps the generated `MediaStatsData.Media` GraphQL types to the immutable
 * [MediaStatsRecord] and its feature-local projections consumed by the media
 * stats pipeline.
 *
 * Covers the exact field set requested by `MediaStats.graphql`. Converts
 * generated Int ids to domain Longs and exposes the generated enums (type,
 * ranking type/format/season, status-distribution status) as their serialized
 * `name`, matching the legacy String-backed entity lane. Nullable semantics of
 * the optional external-link/ranking blocks and the `stats` block (collapsed
 * into the two distribution lists) are preserved; null list elements within the
 * distribution, ranking, and external-link nodes are dropped via `mapNotNull`,
 * following the established node-list mapping convention. The legacy mutable
 * [com.mxt.anitrend.model.entity.anilist.Media] lane is unchanged for its
 * remaining consumers.
 */
fun MediaStatsData.Media.toMediaStatsRecord(): MediaStatsRecord = MediaStatsRecord(
    type = type?.name,
    externalLinks = externalLinks?.mapNotNull { it?.toMediaStatsExternalLinkRecord() },
    scoreDistribution = stats?.scoreDistribution?.mapNotNull { it?.toMediaStatsScoreDistributionRecord() },
    statusDistribution = stats?.statusDistribution?.mapNotNull { it?.toMediaStatsStatusDistributionRecord() },
    rankings = rankings?.mapNotNull { it?.toMediaStatsRankingRecord() },
)

private fun MediaStatsData.MediaExternalLinks.toMediaStatsExternalLinkRecord(): MediaStatsExternalLinkRecord = MediaStatsExternalLinkRecord(
    id = id.toLong(),
    url = url,
    site = site,
)

private fun MediaStatsData.MediaStatsScoreDistribution.toMediaStatsScoreDistributionRecord(): MediaStatsScoreDistributionRecord = MediaStatsScoreDistributionRecord(
    score = score,
    amount = amount,
)

private fun MediaStatsData.MediaStatsStatusDistribution.toMediaStatsStatusDistributionRecord(): MediaStatsStatusDistributionRecord = MediaStatsStatusDistributionRecord(
    status = status?.name,
    amount = amount,
)

private fun MediaStatsData.MediaRankings.toMediaStatsRankingRecord(): MediaStatsRankingRecord = MediaStatsRankingRecord(
    id = id.toLong(),
    rank = rank,
    type = type.name,
    format = format.name,
    year = year,
    season = season?.name,
    allTime = allTime,
    context = context,
)
