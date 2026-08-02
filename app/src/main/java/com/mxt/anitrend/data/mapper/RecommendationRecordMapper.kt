package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.AiringScheduleRecord
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.RecommendationRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.graphql.generated.RecommendationMediaData

/**
 * Maps the generated `RecommendationMediaData` GraphQL types to the immutable
 * [RecommendationRecord] and [PageInfoRecord] consumed by the media recommendations
 * pipeline.
 *
 * Converts generated Int ids to domain Longs and preserves the recommendation id, media
 * recommendation summary, rating, user summary, and user rating. Generated enums
 * (type, format, status, userRating) are exposed as their serialized `name`, matching
 * the legacy String-backed entity lane. The media title's original-language value is
 * mapped from the generated `native` field, matching the existing
 * [com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore] sort semantics. The
 * legacy mutable [com.mxt.anitrend.model.entity.base.RecommendationBase] lane is
 * unchanged for its remaining consumers.
 */
fun RecommendationMediaData.MediaRecommendationsNodes.toRecommendationRecord(): RecommendationRecord = RecommendationRecord(
    id = id.toLong(),
    mediaRecommendation = mediaRecommendation?.toMediaSummaryRecord(),
    rating = rating,
    user = user?.toUserSummaryRecord(),
    userRating = userRating?.name,
)

fun RecommendationMediaData.MediaRecommendationsNodesMediaRecommendation.toMediaSummaryRecord(): MediaSummaryRecord = MediaSummaryRecord(
    id = id.toLong(),
    titleUserPreferred = title?.userPreferred,
    titleRomaji = title?.romaji,
    titleEnglish = title?.english,
    titleOriginal = title?.native,
    coverImage = coverImage?.extraLarge ?: coverImage?.large ?: coverImage?.medium,
    type = type?.name,
    format = format?.name,
    episodes = episodes ?: 0,
    chapters = chapters ?: 0,
    volumes = volumes ?: 0,
    status = status?.name,
    siteUrl = siteUrl,
    isFavourite = isFavourite,
    startDate = startDate?.toFuzzyDateRecord(),
    nextAiringEpisode = nextAiringEpisode?.toAiringScheduleRecord(),
    averageScore = averageScore,
)

fun RecommendationMediaData.MediaRecommendationsNodesUser.toUserSummaryRecord(): UserSummaryRecord = UserSummaryRecord(
    id = id.toLong(),
    name = name,
    avatar = avatar?.large ?: avatar?.medium,
    siteUrl = null,
)

fun RecommendationMediaData.MediaRecommendationsPageInfo.toPageInfoRecord(): PageInfoRecord = PageInfoRecord(
    currentPage = currentPage,
    lastPage = lastPage,
    perPage = perPage,
    total = total,
    hasNextPage = hasNextPage ?: false,
    hasPreviousPage = (currentPage ?: 0) > 1,
)

private fun RecommendationMediaData.MediaRecommendationsNodesMediaRecommendationStartDate.toFuzzyDateRecord(): FuzzyDateRecord = FuzzyDateRecord(
    year = year,
    month = month,
    day = day,
)

private fun RecommendationMediaData.MediaRecommendationsNodesMediaRecommendationNextAiringEpisode.toAiringScheduleRecord(): AiringScheduleRecord = AiringScheduleRecord(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)
