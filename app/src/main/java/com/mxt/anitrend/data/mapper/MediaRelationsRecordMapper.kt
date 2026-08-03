package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.mediadetail.model.MediaListEntryRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaRelationsCoverImageRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaRelationsEdgeRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaRelationsNodeRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaRelationsRecord
import com.mxt.anitrend.domain.model.AiringScheduleRecord
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.graphql.generated.MediaRelationsData

/**
 * Maps the generated `MediaRelationsData.Media` GraphQL types to the immutable
 * [MediaRelationsRecord] and its feature-local projections consumed by the media
 * relations pipeline.
 *
 * Covers the exact field set requested by `MediaRelations.graphql`,
 * `PageInfoFragment`, and `MediaCoreFragment`. Converts generated Int ids and
 * timestamps to domain Longs and exposes the generated enums (relationType,
 * type, format, season, status, mediaListEntry status) as their serialized
 * `name`, matching the legacy String-backed entity lane. Nullable semantics of
 * the optional relations/pageInfo blocks, the nullable edges list, and the
 * nullable node blocks are preserved; null list elements within the edges list
 * are dropped via `mapNotNull`, following the established node-list mapping
 * convention. The legacy mutable
 * [com.mxt.anitrend.model.entity.anilist.edge.MediaEdge] lane is unchanged for
 * its remaining consumers.
 */
fun MediaRelationsData.Media.toMediaRelationsRecord(): MediaRelationsRecord = MediaRelationsRecord(
    edges = relations?.edges?.mapNotNull { it?.toMediaRelationsEdgeRecord() },
    pageInfo = relations?.pageInfo?.toPageInfoRecord(),
)

private fun MediaRelationsData.MediaRelationsEdges.toMediaRelationsEdgeRecord(): MediaRelationsEdgeRecord = MediaRelationsEdgeRecord(
    relationType = relationType?.name,
    node = node?.toMediaRelationsNodeRecord(),
)

private fun MediaRelationsData.MediaRelationsEdgesNode.toMediaRelationsNodeRecord(): MediaRelationsNodeRecord = MediaRelationsNodeRecord(
    id = id.toLong(),
    titleUserPreferred = title?.userPreferred,
    titleRomaji = title?.romaji,
    titleEnglish = title?.english,
    titleOriginal = title?.native,
    bannerImage = bannerImage,
    coverImage = coverImage?.toMediaRelationsCoverImageRecord(),
    type = type?.name,
    format = format?.name,
    season = season?.name,
    status = status?.name,
    meanScore = meanScore,
    averageScore = averageScore,
    startDate = startDate?.toFuzzyDateRecord(),
    endDate = endDate?.toFuzzyDateRecord(),
    episodes = episodes,
    chapters = chapters,
    volumes = volumes,
    isAdult = isAdult,
    isFavourite = isFavourite,
    nextAiringEpisode = nextAiringEpisode?.toAiringScheduleRecord(),
    mediaListEntry = mediaListEntry?.toMediaListEntryRecord(),
    siteUrl = siteUrl,
    updatedAt = updatedAt?.toLong(),
)

private fun MediaRelationsData.MediaRelationsEdgesNodeCoverImage.toMediaRelationsCoverImageRecord(): MediaRelationsCoverImageRecord = MediaRelationsCoverImageRecord(
    color = color,
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun MediaRelationsData.MediaRelationsEdgesNodeStartDate.toFuzzyDateRecord(): FuzzyDateRecord = FuzzyDateRecord(
    year = year,
    month = month,
    day = day,
)

private fun MediaRelationsData.MediaRelationsEdgesNodeEndDate.toFuzzyDateRecord(): FuzzyDateRecord = FuzzyDateRecord(
    year = year,
    month = month,
    day = day,
)

private fun MediaRelationsData.MediaRelationsEdgesNodeNextAiringEpisode.toAiringScheduleRecord(): AiringScheduleRecord = AiringScheduleRecord(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun MediaRelationsData.MediaRelationsEdgesNodeMediaListEntry.toMediaListEntryRecord(): MediaListEntryRecord = MediaListEntryRecord(
    id = id.toLong(),
    status = status?.name,
)

private fun MediaRelationsData.MediaRelationsPageInfo.toPageInfoRecord(): PageInfoRecord = PageInfoRecord(
    currentPage = currentPage,
    lastPage = lastPage,
    perPage = perPage,
    total = total,
    hasNextPage = hasNextPage ?: false,
    hasPreviousPage = (currentPage ?: 0) > 1,
)
