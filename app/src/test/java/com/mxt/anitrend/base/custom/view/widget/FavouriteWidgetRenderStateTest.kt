package com.mxt.anitrend.base.custom.view.widget

import com.mxt.anitrend.data.store.favourite.FavouriteFlag
import com.mxt.anitrend.domain.favourite.model.FavouriteKey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavouriteWidgetRenderStateTest {

    @Test
    fun `render state preserves immutable properties`() {
        val state = FavouriteWidgetRenderState(
            isFavourite = true,
            isEnabled = false,
            isLoading = true,
        )

        assertTrue(state.isFavourite)
        assertFalse(state.isEnabled)
        assertTrue(state.isLoading)
    }

    @Test
    fun `render state defaults to enabled and not loading`() {
        val state = FavouriteWidgetRenderState(isFavourite = true)

        assertTrue(state.isEnabled)
        assertFalse(state.isLoading)
    }

    @Test
    fun `fromFlag maps the committed flag to isFavourite`() {
        val flag = FavouriteFlag(
            key = FavouriteKey.Studio(7L),
            isFavourite = true,
            revision = 2L,
        )

        val state = FavouriteWidgetRenderState.fromFlag(
            flag = flag,
            fallbackIsFavourite = false,
            isLoading = true,
        )

        assertTrue(state.isFavourite)
        assertTrue(state.isLoading)
        assertTrue(state.isEnabled)
    }

    @Test
    fun `fromFlag falls back to the initial flag when no committed value exists`() {
        val state = FavouriteWidgetRenderState.fromFlag(
            flag = null,
            fallbackIsFavourite = true,
            isLoading = false,
        )

        assertTrue(state.isFavourite)
        assertFalse(state.isLoading)
        assertTrue(state.isEnabled)
    }

    @Test
    fun `fromFlag propagates the loading state`() {
        val state = FavouriteWidgetRenderState.fromFlag(
            flag = null,
            fallbackIsFavourite = false,
            isLoading = true,
        )

        assertTrue(state.isLoading)
        assertFalse(state.isFavourite)
    }
}
