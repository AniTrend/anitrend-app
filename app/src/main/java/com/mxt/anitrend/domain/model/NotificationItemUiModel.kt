package com.mxt.anitrend.domain.model

/**
 * Immutable render projection of a single notification row.
 *
 * [NotificationListAdapter] renders exclusively from this model and never
 * resolves repository or persistence state locally. The stable [id] is the
 * notification id from the server; [record] carries every field the row renderer
 * and the navigation handlers need (type, context, activity id, comment id,
 * thread id, user summary, media summary, episode, and deleted media titles),
 * and [isRead] is precomputed by the fragment from the ObjectBox
 * `NotificationHistory` box so the adapter never touches local persistence.
 *
 * Pure Kotlin value type with no Android, ObjectBox, repository, or Koin
 * dependencies, matching the domain layer contract.
 */
data class NotificationItemUiModel(
    val id: Long,
    val record: NotificationRecord,
    val isRead: Boolean,
)

/**
 * Projects a [NotificationRecord] into a [NotificationItemUiModel], or returns
 * null for placeholder records with no renderable type (for example the
 * submission update notifications that the backend returns without a type). This
 * preserves the legacy list behavior of dropping blank-type rows before they
 * reach the adapter.
 */
fun NotificationRecord.toNotificationItemUiModel(isRead: Boolean): NotificationItemUiModel? {
    if (type.isNullOrBlank()) return null
    return NotificationItemUiModel(
        id = id,
        record = this,
        isRead = isRead,
    )
}

/**
 * Projects an entire page of notifications, computing each row's read flag from
 * the ids present in the caller-supplied read set and preserving the
 * server-returned node ordering.
 */
fun NotificationPageResult.toNotificationItemUiModels(
    readIds: Set<Long>,
): List<NotificationItemUiModel> = notifications.mapNotNull { record ->
    record.toNotificationItemUiModel(isRead = record.id in readIds)
}
