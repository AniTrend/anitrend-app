package com.mxt.anitrend.view.sheet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MediaFilterSheetResultTest {

    @Test
    fun `reset followed by reselect applies the new draft`() {
        val draft =
            MediaFilterSheetDraft()
                .select(listOf(1, 3))
                .reset()
                .select(listOf(2))

        assertFalse(draft.resetRequested)
        assertEquals(MediaFilterSheetResult.ACTION_APPLY, draft.action())
        assertEquals(listOf(2), draft.selectedIndices)
    }

    @Test
    fun `draft state can be restored without changing reset intent`() {
        val restored = MediaFilterSheetDraft(selectedIndices = listOf(4, 6), resetRequested = true)

        assertEquals(listOf(4, 6), restored.selectedIndices)
        assertEquals(MediaFilterSheetResult.ACTION_RESET, restored.action())
    }

    @Test
    fun `request id is carried by every result action`() {
        val result =
            MediaFilterSheetResult(
                requestId = "browse-tags-7",
                action = MediaFilterSheetResult.ACTION_APPLY,
                selectedIndices = intArrayOf(2),
                selectedValues = arrayListOf("Action"),
            )

        assertEquals("browse-tags-7", result.requestId)
        assertEquals(MediaFilterSheetResult.ACTION_APPLY, result.action)
    }
}
