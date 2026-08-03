package com.mxt.anitrend.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationItemUiModelProjectionTest {

    @Test
    fun `record projection maps stable id record and read flag`() {
        val record = createRecord()

        val model = record.toNotificationItemUiModel(isRead = true)

        assertNotNull(model)
        model?.let {
            assertEquals(42L, it.id)
            assertEquals(record, it.record)
            assertTrue(it.isRead)
        }
    }

    @Test
    fun `record projection keeps unread flag when read is false`() {
        val model = createRecord().toNotificationItemUiModel(isRead = false)

        assertNotNull(model)
        assertFalse(model!!.isRead)
    }

    @Test
    fun `record projection drops blank type placeholder records`() {
        assertNull(NotificationRecord(id = 0L, type = null).toNotificationItemUiModel(isRead = false))
        assertNull(NotificationRecord(id = 0L, type = "").toNotificationItemUiModel(isRead = true))
        assertNull(NotificationRecord(id = 0L, type = "   ").toNotificationItemUiModel(isRead = false))
    }

    @Test
    fun `page projection computes read state from read ids and preserves ordering`() {
        val page =
            NotificationPageResult(
                notifications = listOf(
                    NotificationRecord(id = 1L, type = "FOLLOWING"),
                    NotificationRecord(id = 0L, type = null),
                    NotificationRecord(id = 2L, type = "AIRING"),
                    NotificationRecord(id = 3L, type = "THREAD_LIKE"),
                ),
                pageInfo = null,
            )

        val models = page.toNotificationItemUiModels(readIds = setOf(1L, 3L))

        assertEquals(listOf(1L, 2L, 3L), models.map { it.id })
        assertTrue(models[0].isRead)
        assertFalse(models[1].isRead)
        assertTrue(models[2].isRead)
    }

    @Test
    fun `page projection returns empty list for empty page`() {
        val page = NotificationPageResult(notifications = emptyList(), pageInfo = null)

        assertTrue(page.toNotificationItemUiModels(readIds = emptySet()).isEmpty())
    }

    private fun createRecord(): NotificationRecord = NotificationRecord(
        id = 42L,
        type = "FOLLOWING",
        createdAt = 1_700_000_000L,
        context = "followed you",
        user = UserSummaryRecord(id = 7L, name = "follower", avatar = "https://avatar", siteUrl = null),
    )
}
