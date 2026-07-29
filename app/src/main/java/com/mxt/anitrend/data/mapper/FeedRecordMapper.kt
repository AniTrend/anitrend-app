package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase

fun FeedList.toFeedRecord(revision: Long = 0L): FeedRecord = FeedRecord(
    id = id,
    type = type,
    text = text,
    createdAt = createdAt,
    user = user?.toUserSummaryRecord(),
    messenger = messenger?.toUserSummaryRecord(),
    recipient = recipient?.toUserSummaryRecord(),
    media = media?.toMediaSummaryRecord(),
    likes = likes.orEmpty().map { it.toUserSummaryRecord() },
    replyCount = replyCount,
    siteUrl = siteUrl,
    revision = revision,
)

fun FeedReply.toFeedReplyRecord(
    activityId: Long = 0L,
    revision: Long = 0L,
): FeedReplyRecord = FeedReplyRecord(
    id = id,
    activityId = activityId,
    reply = reply,
    createdAt = createdAt,
    user = user?.toUserSummaryRecord(),
    likes = likes.orEmpty().map { it.toUserSummaryRecord() },
    revision = revision,
)

fun UserBase.toUserSummaryRecord(): UserSummaryRecord = UserSummaryRecord(
    id = id,
    name = name,
    avatar = avatar?.large ?: avatar?.medium ?: avatar?.extraLarge,
    siteUrl = null,
)

fun MediaBase.toMediaSummaryRecord(): MediaSummaryRecord = MediaSummaryRecord(
    id = id,
    titleRomaji = title?.romaji,
    titleEnglish = title?.english,
    titleOriginal = title?.original,
    coverImage = coverImage?.extraLarge ?: coverImage?.large ?: coverImage?.medium,
    type = type,
    episodes = episodes,
    chapters = chapters,
    volumes = volumes,
    status = status,
    siteUrl = siteUrl,
)

fun FuzzyDate.toFuzzyDateRecord(): FuzzyDateRecord = FuzzyDateRecord(
    year = year.takeIf { it != 0 },
    month = month.takeIf { it != 0 },
    day = day.takeIf { it != 0 },
)
