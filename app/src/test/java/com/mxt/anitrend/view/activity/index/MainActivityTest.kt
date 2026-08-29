package com.mxt.anitrend.view.activity.index

import android.content.Intent
import com.mxt.anitrend.R
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    // ── NFR-001: /user deep-link route decision ──

    @Test
    fun `resolveUserRoute rejects missing user identity`() {
        assertNull(MainActivity.resolveUserRoute(userId = 0L, userName = null, mediaType = null))
        assertNull(MainActivity.resolveUserRoute(userId = 0L, userName = " ", mediaType = KeyUtil.ANIME))
    }

    @Test
    fun `resolveUserRoute lands on profile only for plain user links`() {
        val decision = MainActivity.resolveUserRoute(userId = 0L, userName = "raki", mediaType = null)
        assertTrue(decision != null)
        assertEquals(UserScreenParam(userId = 0L, initialName = "raki"), decision!!.profile)
        assertNull(decision.mediaListType)
    }

    @Test
    fun `resolveUserRoute carries typed media list for animelist and mangalist links`() {
        val anime = MainActivity.resolveUserRoute(userId = 42L, userName = "raki", mediaType = KeyUtil.ANIME)
        assertEquals(UserScreenParam(userId = 42L, initialName = "raki"), anime?.profile)
        assertEquals(KeyUtil.ANIME, anime?.mediaListType)

        val manga = MainActivity.resolveUserRoute(userId = 42L, userName = "raki", mediaType = KeyUtil.MANGA)
        assertEquals(UserScreenParam(userId = 42L, initialName = "raki"), manga?.profile)
        assertEquals(KeyUtil.MANGA, manga?.mediaListType)
    }

    @Test
    fun `resolveUserRoute ignores unknown media types and keeps the profile landing`() {
        val decision = MainActivity.resolveUserRoute(userId = 42L, userName = "raki", mediaType = "HENTAI")
        assertEquals(UserScreenParam(userId = 42L, initialName = "raki"), decision?.profile)
        assertNull(decision?.mediaListType)
    }

    // ── NFR-003: external entry detection and clearing ──

    @Test
    fun `resolveExternalEntry requires a rooted task`() {
        assertFalse(MainActivity.resolveExternalEntry(isTaskRoot = false, action = Intent.ACTION_VIEW, hasData = true))
        assertFalse(MainActivity.resolveExternalEntry(isTaskRoot = false, action = Intent.ACTION_SEND, hasData = false))
    }

    @Test
    fun `resolveExternalEntry detects view deep links and share intents`() {
        assertTrue(MainActivity.resolveExternalEntry(isTaskRoot = true, action = Intent.ACTION_VIEW, hasData = true))
        assertTrue(MainActivity.resolveExternalEntry(isTaskRoot = true, action = Intent.ACTION_SEND, hasData = false))
    }

    @Test
    fun `resolveExternalEntry ignores data-less views and unrelated actions`() {
        assertFalse(MainActivity.resolveExternalEntry(isTaskRoot = true, action = Intent.ACTION_VIEW, hasData = false))
        assertFalse(MainActivity.resolveExternalEntry(isTaskRoot = true, action = Intent.ACTION_MAIN, hasData = false))
    }

    @Test
    fun `initial ingress route preserves external finish semantics and follow-up navigation clears it`() {
        // No dispatch yet: the flag survives.
        assertTrue(
            MainActivity.externalEntryAfterDispatch(
                externalEntry = true,
                dispatchCount = 0,
            ),
        )
        // The initial ingress route (a direct /anime/1 or /activity/1
        // landing) is not a follow-up: the flag survives so back finishes the
        // external task.
        assertTrue(
            MainActivity.externalEntryAfterDispatch(
                externalEntry = true,
                dispatchCount = 1,
            ),
        )
        // A subsequent internal navigation (the Profile -> MediaList push of
        // a /user/<name>/animelist chain) clears the flag.
        assertFalse(
            MainActivity.externalEntryAfterDispatch(
                externalEntry = true,
                dispatchCount = 2,
            ),
        )
        // Returning to the ingress destination is not a dispatch: the cleared
        // flag stays cleared instead of being re-armed.
        assertFalse(
            MainActivity.externalEntryAfterDispatch(
                externalEntry = false,
                dispatchCount = 0,
            ),
        )
        assertFalse(
            MainActivity.externalEntryAfterDispatch(
                externalEntry = false,
                dispatchCount = 1,
            ),
        )
    }

    // ── wire key ──

    @Test
    fun `legacy wire key matches KeyUtil arg_redirect constant`() {
        assertEquals("arg_redirect", KeyUtil.arg_redirect)
    }
}
