package com.mxt.anitrend.base.custom.view.widget

import com.mxt.anitrend.base.custom.view.widget.AboutPanelWidget.StatClickAction
import com.mxt.anitrend.base.custom.view.widget.AboutPanelWidget.StatState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Consumer-level click contract for the profile about panel
 * ([AboutPanelWidget]). A stat container must distinguish not-yet-loaded, failed,
 * loaded nonzero, and loaded-zero: loaded counts (including zero) open the normal
 * destination or list sheet, while not-yet-loaded and failed counts keep the
 * loading toast. A loaded zero must never fall back to the loading toast.
 */
class AboutPanelWidgetStatStateTest {

    @Test
    fun `loaded zero resolves to open and never emits the loading toast`() {
        assertEquals(StatClickAction.Open, StatState.Loaded(0).resolveStatClick())
    }

    @Test
    fun `loaded nonzero resolves to open`() {
        assertEquals(StatClickAction.Open, StatState.Loaded(5).resolveStatClick())
    }

    @Test
    fun `not yet loaded resolves to the loading toast`() {
        assertEquals(StatClickAction.ShowLoading, StatState.NotLoaded.resolveStatClick())
    }

    @Test
    fun `failed resolves to the loading toast`() {
        assertEquals(StatClickAction.ShowLoading, StatState.Failed.resolveStatClick())
    }
}
