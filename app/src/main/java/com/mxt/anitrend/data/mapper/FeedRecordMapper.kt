package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import com.mxt.anitrend.domain.model.AiringScheduleRecord
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo

fun FeedList.toFeedRecord(revision: Long = 0L): FeedRecord = FeedRecord(
    id = id,
    type = type,
    status = status,
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

fun List<UserBase>.toUserSummaryRecords(): List<UserSummaryRecord> =
    map(UserBase::toUserSummaryRecord)

fun UserBase.toUserSummaryRecord(): UserSummaryRecord = UserSummaryRecord(
    id = id,
    name = name,
    avatar = avatar?.large ?: avatar?.medium ?: avatar?.extraLarge,
    siteUrl = null,
)

fun MediaBase.toMediaSummaryRecord(): MediaSummaryRecord = MediaSummaryRecord(
    id = id,
    titleUserPreferred = title?.userPreferred,
    titleRomaji = title?.romaji,
    titleEnglish = title?.english,
    titleOriginal = title?.original,
    coverImage = coverImage?.extraLarge ?: coverImage?.large ?: coverImage?.medium,
    type = type,
    format = format,
    episodes = episodes,
    chapters = chapters,
    volumes = volumes,
    status = status,
    siteUrl = siteUrl,
    isFavourite = isFavourite,
    startDate = startDate?.toFuzzyDateRecord(),
    nextAiringEpisode = nextAiringEpisode?.toAiringScheduleRecord(),
)

fun FuzzyDate.toFuzzyDateRecord(): FuzzyDateRecord = FuzzyDateRecord(
    year = year.takeIf { it != 0 },
    month = month.takeIf { it != 0 },
    day = day.takeIf { it != 0 },
)

fun FeedRecord.toFeedList(replies: List<FeedReply> = emptyList()): FeedList = FeedList(
    id = id,
    replyCount = replyCount,
    type = type,
    status = status,
    text = text,
    createdAt = createdAt,
    user = user?.toUserBase(),
    media = media?.toMediaBase(),
    messenger = messenger?.toUserBase(),
    recipient = recipient?.toUserBase(),
    likes = likes.map(UserSummaryRecord::toUserBase),
    siteUrl = siteUrl,
).apply {
    this.replies = replies
}

fun FeedReplyRecord.toFeedReply(): FeedReply = FeedReply(
    id = id,
    text = reply,
    createdAt = createdAt,
    user = user?.toUserBase(),
    likes = likes.map(UserSummaryRecord::toUserBase),
)

fun PageInfoRecord.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
    hasNextPageValue = hasNextPage,
)

fun UserSummaryRecord.toUserBase(): UserBase = UserBase(name = name).apply {
    id = this@toUserBase.id
    avatar = ImageBase(
        extraLarge = this@toUserBase.avatar,
        large = this@toUserBase.avatar,
        medium = this@toUserBase.avatar,
    )
}

fun MediaSummaryRecord.toMediaBase(): MediaBase = MediaBase().apply {
    id = this@toMediaBase.id
    title = MediaTitle(
        romajiRaw = titleRomaji,
        englishRaw = titleEnglish,
        originalRaw = titleOriginal,
        userPreferredRaw = titleUserPreferred ?: titleRomaji ?: titleEnglish ?: titleOriginal,
    )
    coverImage = ImageBase(
        extraLarge = this@toMediaBase.coverImage,
        large = this@toMediaBase.coverImage,
        medium = this@toMediaBase.coverImage,
    )
    type = this@toMediaBase.type
    format = this@toMediaBase.format
    episodes = this@toMediaBase.episodes
    chapters = this@toMediaBase.chapters
    volumes = this@toMediaBase.volumes
    status = this@toMediaBase.status
    siteUrl = this@toMediaBase.siteUrl
    isFavourite = this@toMediaBase.isFavourite
    startDate = this@toMediaBase.startDate?.toFuzzyDate()
    nextAiringEpisode = this@toMediaBase.nextAiringEpisode?.toAiringSchedule()
}

fun AiringSchedule.toAiringScheduleRecord(): AiringScheduleRecord = AiringScheduleRecord(
    airingAt = airingAt,
    timeUntilAiring = timeUntilAiring,
    episode = episode,
)

fun AiringScheduleRecord.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt,
    timeUntilAiring = timeUntilAiring,
    episode = episode,
)

fun FuzzyDateRecord.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)
