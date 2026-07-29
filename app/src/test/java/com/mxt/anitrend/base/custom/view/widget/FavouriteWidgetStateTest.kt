package com.mxt.anitrend.base.custom.view.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavouriteWidgetStateTest {
    @Test
    fun `state preserves immutable render properties`() {
        val state = FavouriteWidgetState(
            count = 12,
            isLiked = true,
            isEnabled = false,
            isLoading = true,
        )

        assertEquals(12, state.count)
        assertTrue(state.isLiked)
        assertFalse(state.isEnabled)
        assertTrue(state.isLoading)
    }

    @Test
    fun `convertToText formats count consistently`() {
        assertEquals(" 5 ", FavouriteWidget.convertToText(5))
    }
}
