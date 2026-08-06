package com.mxt.anitrend.view.sheet

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Documents the deliberate entity navigation boundary of the manage-list sheet:
 * the stable wire key is a compatibility contract and must not change.
 */
class BottomSheetSeriesManageTest {

    @Test
    fun `entity navigation wire key is stable`() {
        assertEquals("arg_media_base", BottomSheetSeriesManage.ARG_MEDIA_BASE)
    }
}
