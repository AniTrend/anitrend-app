package com.mxt.anitrend.view.fragment.detail

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileFragmentTest {

    @Test
    fun `profile section is reused while the selected section stays active`() {
        assertFalse(ProfileFragment().shouldRebuildSection("OVERVIEW", "OVERVIEW"))
    }

    @Test
    fun `profile section is rebuilt when selection changes or has not been rendered`() {
        assertTrue(ProfileFragment().shouldRebuildSection("OVERVIEW", "MEDIA_LIST"))
        assertTrue(ProfileFragment().shouldRebuildSection(null, "OVERVIEW"))
    }
}
