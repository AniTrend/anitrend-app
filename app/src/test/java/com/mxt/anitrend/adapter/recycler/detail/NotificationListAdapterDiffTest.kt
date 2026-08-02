package com.mxt.anitrend.adapter.recycler.detail

import com.mxt.anitrend.domain.model.NotificationItemUiModel
import com.mxt.anitrend.domain.model.NotificationRecord
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationListAdapterDiffTest {

    @Test
    fun `diff callback matches stable notification id`() {
        val original = model(id = 1L)
        val sameIdDifferentRead = original.copy(isRead = true)
        val differentId = original.copy(id = 2L)

        assertTrue(NotificationListAdapter.DIFF_CALLBACK.areItemsTheSame(original, sameIdDifferentRead))
        assertFalse(NotificationListAdapter.DIFF_CALLBACK.areItemsTheSame(original, differentId))
    }

    @Test
    fun `diff callback flags read state as a content change`() {
        val original = model(id = 1L, isRead = false)
        val nowRead = original.copy(isRead = true)

        assertTrue(NotificationListAdapter.DIFF_CALLBACK.areItemsTheSame(original, nowRead))
        assertFalse(NotificationListAdapter.DIFF_CALLBACK.areContentsTheSame(original, nowRead))
    }

    @Test
    fun `diff callback treats record content changes as content changes`() {
        val original = model(id = 1L)
        val changedContext =
            original.copy(record = original.record.copy(context = "updated context"))

        assertTrue(NotificationListAdapter.DIFF_CALLBACK.areItemsTheSame(original, changedContext))
        assertFalse(NotificationListAdapter.DIFF_CALLBACK.areContentsTheSame(original, changedContext))
    }

    @Test
    fun `diff callback considers identical rows unchanged`() {
        val original = model(id = 1L)

        assertTrue(NotificationListAdapter.DIFF_CALLBACK.areItemsTheSame(original, original.copy()))
        assertTrue(NotificationListAdapter.DIFF_CALLBACK.areContentsTheSame(original, original.copy()))
    }

    private fun model(
        id: Long,
        isRead: Boolean = false,
    ): NotificationItemUiModel = NotificationItemUiModel(
        id = id,
        record =
            NotificationRecord(
                id = id,
                type = "FOLLOWING",
                createdAt = 1_700_000_000L,
                context = "followed you",
            ),
        isRead = isRead,
    )
}
