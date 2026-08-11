package com.mxt.anitrend.domain.model

/**
 * Immutable canonical representation of a user notification in the notifications
 * pipeline.
 *
 * Pure Kotlin value type, intentionally not Parcelable and not ObjectBox-backed.
 * Mapped from the generated `UserNotificationsData.PageNotifications` sealed types
 * by `com.mxt.anitrend.data.mapper.toNotificationRecord`. Reuses the existing
 * [UserSummaryRecord], [MediaSummaryRecord], and [PageInfoRecord] domain values and
 * preserves every field the notification list, navigation, and worker consumers
 * require: id, type, createdAt, context, activityId, commentId, user summary,
 * episode, contexts, media summary, thread id, reason, and deleted media titles.
 * Generated Int ids are converted to Longs. The notification list lane is fully
 * migrated to this record and its [NotificationItemUiModel] projection; no legacy
 * mutable `Notification` entity is involved anymore.
 */
data class NotificationRecord(
    val id: Long,
    val type: String? = null,
    val createdAt: Long = 0L,
    val context: String? = null,
    val activityId: Long? = null,
    val commentId: Long? = null,
    val user: UserSummaryRecord? = null,
    val episode: Int? = null,
    val contexts: List<String> = emptyList(),
    val media: MediaSummaryRecord? = null,
    val threadId: Long? = null,
    val reason: String? = null,
    val deletedMediaTitle: String? = null,
    val deletedMediaTitles: List<String> = emptyList(),
)

/**
 * Page-level result of a user notifications request.
 *
 * Preserves the server-returned node ordering ([notifications]) together with the
 * paging metadata ([pageInfo]) needed to render and page the notifications screen.
 */
data class NotificationPageResult(
    val notifications: List<NotificationRecord>,
    val pageInfo: PageInfoRecord?,
)

/**
 * Resolves the activity id a comment-style notification (reply, mention,
 * message, like) can open, or null when the record carries no usable activity
 * reference.
 *
 * AniList reports `activityId` as 0 when the referenced activity was deleted
 * after the notification was created. The comment detail screen rejects
 * non-positive ids with an unrecoverable error state, so navigation callers
 * must never launch it with 0; they surface the unavailable row with the
 * established user-facing message instead.
 */
fun NotificationRecord.commentActivityId(): Long? = activityId?.takeIf { it > 0L }
