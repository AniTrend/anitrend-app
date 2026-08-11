package com.mxt.anitrend.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Focused tests for the notification-to-comment navigation guard. The comment
 * detail screen rejects non-positive activity ids with an unrecoverable error
 * state, so comment-style notifications (reply, mention, message, like) must
 * never resolve to id 0; AniList reports 0 when the referenced activity was
 * deleted after the notification was created.
 */
class NotificationRecordNavigationTest {

    @Test
    fun `comment activity id resolves a positive activity id`() {
        val record = NotificationRecord(id = 1L, type = "ACTIVITY_REPLY", activityId = 42L)

        assertEquals(42L, record.commentActivityId())
    }

    @Test
    fun `comment activity id is null when the activity id is zero`() {
        val record = NotificationRecord(id = 1L, type = "ACTIVITY_REPLY", activityId = 0L)

        assertNull(record.commentActivityId())
    }

    @Test
    fun `comment activity id is null when the activity id is missing`() {
        val record = NotificationRecord(id = 1L, type = "ACTIVITY_MENTION", activityId = null)

        assertNull(record.commentActivityId())
    }

    @Test
    fun `comment activity id is null for negative activity ids`() {
        val record = NotificationRecord(id = 1L, type = "ACTIVITY_LIKE", activityId = -5L)

        assertNull(record.commentActivityId())
    }

    @Test
    fun `comment activity id resolves across all comment style types`() {
        listOf(
            "ACTIVITY_MESSAGE",
            "ACTIVITY_MENTION",
            "ACTIVITY_LIKE",
            "ACTIVITY_REPLY",
            "ACTIVITY_REPLY_SUBSCRIBED",
            "ACTIVITY_REPLY_LIKE",
        ).forEach { type ->
            val record = NotificationRecord(id = 1L, type = type, activityId = 7L)

            assertEquals("type $type", 7L, record.commentActivityId())
        }
    }
}
