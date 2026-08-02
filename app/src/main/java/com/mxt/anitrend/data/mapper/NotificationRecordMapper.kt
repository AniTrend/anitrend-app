package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.NotificationPageResult
import com.mxt.anitrend.domain.model.NotificationRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.UserNotificationsData

/**
 * Maps the generated `UserNotificationsData` GraphQL types to the immutable
 * [NotificationRecord], [NotificationPageResult], and [PageInfoRecord] consumed by
 * the notifications pipeline.
 *
 * Converts generated Int ids to domain Longs and exposes generated enums as their
 * serialized `name`, matching the legacy String-backed entity lane. Every field the
 * notification list, navigation, and worker consumers require is preserved: id,
 * type, createdAt, context, activityId, commentId, user summary, episode, contexts,
 * media summary, thread id, reason, and deleted media titles.
 */
fun UserNotificationsData.PageNotifications.toNotificationRecord(): NotificationRecord = when (this) {
    is UserNotificationsData.PageNotifications.ActivityLikeNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        activityId = activityId.toLong(),
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )

    is UserNotificationsData.PageNotifications.ActivityMentionNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        activityId = activityId.toLong(),
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )

    is UserNotificationsData.PageNotifications.ActivityMessageNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        activityId = activityId.toLong(),
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )

    is UserNotificationsData.PageNotifications.ActivityReplyLikeNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        activityId = activityId.toLong(),
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )

    is UserNotificationsData.PageNotifications.ActivityReplyNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        activityId = activityId.toLong(),
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )

    is UserNotificationsData.PageNotifications.ActivityReplySubscribedNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        activityId = activityId.toLong(),
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )

    is UserNotificationsData.PageNotifications.AiringNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        episode = episode,
        contexts = contexts?.filterNotNull().orEmpty(),
        media = media?.let {
            toMediaSummaryRecord(
                id = it.id,
                titleUserPreferred = it.title?.userPreferred,
                titleRomaji = it.title?.romaji,
                titleEnglish = it.title?.english,
                titleOriginal = it.title?.native,
                coverExtraLarge = it.coverImage?.extraLarge,
                coverLarge = it.coverImage?.large,
                coverMedium = it.coverImage?.medium,
                type = it.type,
                format = it.format,
                episodes = it.episodes,
                chapters = it.chapters,
                volumes = it.volumes,
                status = it.status,
                siteUrl = it.siteUrl,
                isFavourite = it.isFavourite,
                averageScore = it.averageScore,
            )
        },
    )

    is UserNotificationsData.PageNotifications.CharacterSubmissionUpdateNotification -> NotificationRecord(id = 0L)

    is UserNotificationsData.PageNotifications.FollowingNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )

    is UserNotificationsData.PageNotifications.MediaDataChangeNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        reason = reason,
        media = media?.let {
            toMediaSummaryRecord(
                id = it.id,
                titleUserPreferred = it.title?.userPreferred,
                titleRomaji = it.title?.romaji,
                titleEnglish = it.title?.english,
                titleOriginal = it.title?.native,
                coverExtraLarge = it.coverImage?.extraLarge,
                coverLarge = it.coverImage?.large,
                coverMedium = it.coverImage?.medium,
                type = it.type,
                format = it.format,
                episodes = it.episodes,
                chapters = it.chapters,
                volumes = it.volumes,
                status = it.status,
                siteUrl = it.siteUrl,
                isFavourite = it.isFavourite,
                averageScore = it.averageScore,
            )
        },
    )

    is UserNotificationsData.PageNotifications.MediaDeletionNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        reason = reason,
        deletedMediaTitle = deletedMediaTitle,
    )

    is UserNotificationsData.PageNotifications.MediaMergeNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        reason = reason,
        deletedMediaTitles = deletedMediaTitles?.filterNotNull().orEmpty(),
        media = media?.let {
            toMediaSummaryRecord(
                id = it.id,
                titleUserPreferred = it.title?.userPreferred,
                titleRomaji = it.title?.romaji,
                titleEnglish = it.title?.english,
                titleOriginal = it.title?.native,
                coverExtraLarge = it.coverImage?.extraLarge,
                coverLarge = it.coverImage?.large,
                coverMedium = it.coverImage?.medium,
                type = it.type,
                format = it.format,
                episodes = it.episodes,
                chapters = it.chapters,
                volumes = it.volumes,
                status = it.status,
                siteUrl = it.siteUrl,
                isFavourite = it.isFavourite,
                averageScore = it.averageScore,
            )
        },
    )

    is UserNotificationsData.PageNotifications.MediaSubmissionUpdateNotification -> NotificationRecord(id = 0L)

    is UserNotificationsData.PageNotifications.RelatedMediaAdditionNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        media = media?.let {
            toMediaSummaryRecord(
                id = it.id,
                titleUserPreferred = it.title?.userPreferred,
                titleRomaji = it.title?.romaji,
                titleEnglish = it.title?.english,
                titleOriginal = it.title?.native,
                coverExtraLarge = it.coverImage?.extraLarge,
                coverLarge = it.coverImage?.large,
                coverMedium = it.coverImage?.medium,
                type = it.type,
                format = it.format,
                episodes = it.episodes,
                chapters = it.chapters,
                volumes = it.volumes,
                status = it.status,
                siteUrl = it.siteUrl,
                isFavourite = it.isFavourite,
                averageScore = it.averageScore,
            )
        },
    )

    is UserNotificationsData.PageNotifications.StaffSubmissionUpdateNotification -> NotificationRecord(id = 0L)

    is UserNotificationsData.PageNotifications.ThreadCommentLikeNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        commentId = commentId.toLong(),
        threadId = thread?.id?.toLong(),
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )

    is UserNotificationsData.PageNotifications.ThreadCommentMentionNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        commentId = commentId.toLong(),
        threadId = thread?.id?.toLong(),
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )

    is UserNotificationsData.PageNotifications.ThreadCommentReplyNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        commentId = commentId.toLong(),
        threadId = thread?.id?.toLong(),
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )

    is UserNotificationsData.PageNotifications.ThreadCommentSubscribedNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        commentId = commentId.toLong(),
        threadId = thread?.id?.toLong(),
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )

    is UserNotificationsData.PageNotifications.ThreadLikeNotification -> NotificationRecord(
        id = id.toLong(),
        type = type?.name,
        createdAt = createdAt?.toLong() ?: 0L,
        context = context,
        threadId = thread?.id?.toLong(),
        user = user?.let { toUserSummaryRecord(id = it.id, name = it.name, avatarLarge = it.avatar?.large, avatarMedium = it.avatar?.medium) },
    )
}

/**
 * Maps the generated page wrapper to the page result consumed by the notifications
 * pipeline, preserving the server-returned node ordering together with paging
 * metadata.
 */
fun UserNotificationsData.Page.toNotificationPageResult(): NotificationPageResult = NotificationPageResult(
    notifications = notifications?.mapNotNull { it?.toNotificationRecord() }.orEmpty(),
    pageInfo = pageInfo?.toPageInfoRecord(),
)

fun UserNotificationsData.PagePageInfo.toPageInfoRecord(): PageInfoRecord = PageInfoRecord(
    currentPage = currentPage,
    lastPage = lastPage,
    perPage = perPage,
    total = total,
    hasNextPage = hasNextPage ?: false,
    hasPreviousPage = (currentPage ?: 0) > 1,
)

private fun toUserSummaryRecord(
    id: Int,
    name: String,
    avatarLarge: String?,
    avatarMedium: String?,
): UserSummaryRecord = UserSummaryRecord(
    id = id.toLong(),
    name = name,
    avatar = avatarLarge ?: avatarMedium,
    siteUrl = null,
)

private fun toMediaSummaryRecord(
    id: Int,
    titleUserPreferred: String?,
    titleRomaji: String?,
    titleEnglish: String?,
    titleOriginal: String?,
    coverExtraLarge: String?,
    coverLarge: String?,
    coverMedium: String?,
    type: MediaType?,
    format: MediaFormat?,
    episodes: Int?,
    chapters: Int?,
    volumes: Int?,
    status: MediaStatus?,
    siteUrl: String?,
    isFavourite: Boolean,
    averageScore: Int?,
): MediaSummaryRecord = MediaSummaryRecord(
    id = id.toLong(),
    titleUserPreferred = titleUserPreferred,
    titleRomaji = titleRomaji,
    titleEnglish = titleEnglish,
    titleOriginal = titleOriginal,
    coverImage = coverExtraLarge ?: coverLarge ?: coverMedium,
    type = type?.name,
    format = format?.name,
    episodes = episodes ?: 0,
    chapters = chapters ?: 0,
    volumes = volumes ?: 0,
    status = status?.name,
    siteUrl = siteUrl,
    isFavourite = isFavourite,
    averageScore = averageScore,
)
