package com.mxt.anitrend.view.activity.index

import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class MainActivityTest {

    // ── production resolveRedirect(): shortcut redirect parsing ──

    @Test
    fun `resolveRedirect returns NO_REDIRECT when extra is absent`() {
        // Mirrors an intent without extras: getIntExtra(KeyUtil.arg_redirect, 0) → 0.
        assertEquals(MainActivity.NO_REDIRECT, MainActivity.resolveRedirect(0))
    }

    @Test
    fun `resolveRedirect returns NO_REDIRECT for negative garbage`() {
        // Legacy shortcuts only write positive nav-item ids; non-positive values
        // are normalized to no redirect instead of flowing into setCheckedItem.
        assertEquals(MainActivity.NO_REDIRECT, MainActivity.resolveRedirect(-1))
    }

    @Test
    fun `resolveRedirect passes through a positive nav-item id`() {
        assertEquals(R.id.nav_myanime, MainActivity.resolveRedirect(R.id.nav_myanime))
    }

    // ── wire key ──

    @Test
    fun `legacy wire key matches KeyUtil arg_redirect constant`() {
        assertEquals("arg_redirect", KeyUtil.arg_redirect)
    }
}
