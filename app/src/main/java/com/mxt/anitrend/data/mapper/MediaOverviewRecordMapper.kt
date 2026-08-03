package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.mediadetail.model.MediaListEntryRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewCoverImageRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewStudioRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewTagRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewTrailerRecord
import com.mxt.anitrend.domain.model.AiringScheduleRecord
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.graphql.generated.MediaOverviewData

/**
 * Maps the generated `MediaOverviewData.Media` GraphQL types to the immutable
 * [MediaOverviewRecord] and its feature-local projections consumed by the media
 * overview pipeline.
 *
 * Covers the exact field set requested by `MediaOverview.graphql`,
 * `MediaCoreFragment`, `MediaTagFragment`, and `StudioFragment`. Converts generated
 * Int ids and timestamps to domain Longs and exposes the generated enums (type,
 * format, season, status, source, mediaListEntry status) as their serialized
 * `name`, matching the legacy String-backed entity lane. Nullable semantics of the
 * optional cover/trailer/studio/tag/date/airing blocks are preserved; null list
 * elements within the tag and studio nodes are dropped via `mapNotNull`, following
 * the established node-list mapping convention. The legacy mutable
 * [com.mxt.anitrend.model.entity.anilist.Media] lane is unchanged for its
 * remaining consumers.
 */
fun MediaOverviewData.Media.toMediaOverviewRecord(): MediaOverviewRecord = MediaOverviewRecord(
    id = id.toLong(),
    titleUserPreferred = title?.userPreferred,
    // Matches the legacy MediaTitle getters, which fall back to userPreferred
    // when the raw romaji/english/native fields are absent.
    titleRomaji = title?.romaji ?: title?.userPreferred,
    titleEnglish = title?.english ?: title?.userPreferred,
    titleOriginal = title?.native ?: title?.userPreferred,
    bannerImage = bannerImage,
    coverImage = coverImage?.toMediaOverviewCoverImageRecord(),
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
    genres = genres,
    tags = tags?.mapNotNull { it?.toMediaOverviewTagRecord() },
    trailer = trailer?.toMediaOverviewTrailerRecord(),
    duration = duration,
    hashtag = hashtag,
    source = source?.name,
    studios = studios?.nodes?.mapNotNull { it?.toMediaOverviewStudioRecord() },
    description = description,
)

private fun MediaOverviewData.MediaCoverImage.toMediaOverviewCoverImageRecord(): MediaOverviewCoverImageRecord = MediaOverviewCoverImageRecord(
    color = color,
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun MediaOverviewData.MediaStartDate.toFuzzyDateRecord(): FuzzyDateRecord = FuzzyDateRecord(
    year = year,
    month = month,
    day = day,
)

private fun MediaOverviewData.MediaEndDate.toFuzzyDateRecord(): FuzzyDateRecord = FuzzyDateRecord(
    year = year,
    month = month,
    day = day,
)

private fun MediaOverviewData.MediaNextAiringEpisode.toAiringScheduleRecord(): AiringScheduleRecord = AiringScheduleRecord(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun MediaOverviewData.MediaMediaListEntry.toMediaListEntryRecord(): MediaListEntryRecord = MediaListEntryRecord(
    id = id.toLong(),
    status = status?.name,
)

private fun MediaOverviewData.MediaTags.toMediaOverviewTagRecord(): MediaOverviewTagRecord = MediaOverviewTagRecord(
    id = id.toLong(),
    name = name,
    description = description,
    category = category,
    rank = rank ?: 0,
    isGeneralSpoiler = isGeneralSpoiler ?: false,
    isAdult = isAdult ?: false,
)

private fun MediaOverviewData.MediaStudiosNodes.toMediaOverviewStudioRecord(): MediaOverviewStudioRecord = MediaOverviewStudioRecord(
    id = id.toLong(),
    name = name,
    isAnimationStudio = isAnimationStudio,
    siteUrl = siteUrl,
    isFavourite = isFavourite,
)

private fun MediaOverviewData.MediaTrailer.toMediaOverviewTrailerRecord(): MediaOverviewTrailerRecord = MediaOverviewTrailerRecord(
    id = id,
    site = site,
    thumbnail = thumbnail,
)
