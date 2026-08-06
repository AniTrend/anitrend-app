package com.mxt.anitrend.view.activity.detail

import android.content.Intent
import com.mxt.anitrend.navigation.extension.ARG_USER_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class ProfileActivityTest {

    // ── production resolve(): missing/invalid identity ──

    @Test
    fun `resolve returns null when no identity is supplied`() {
        // Mirrors an intent without extras: getLongExtra(KeyUtil.arg_id, -1) → -1
        // and getStringExtra(KeyUtil.arg_userName) → null.
        assertNull(ProfileActivity.resolve(typed = null, legacyId = -1, legacyName = null))
    }

    @Test
    fun `resolve returns null when legacy id is negative and name is absent`() {
        assertNull(ProfileActivity.resolve(typed = null, legacyId = -7, legacyName = null))
    }

    @Test
    fun `resolve returns null when legacy id is zero and name is blank`() {
        assertNull(ProfileActivity.resolve(typed = null, legacyId = 0, legacyName = "   "))
    }

    // ── production resolve(): legacy bridge ──

    @Test
    fun `resolve bridges positive legacy id`() {
        val result = ProfileActivity.resolve(typed = null, legacyId = 123L, legacyName = null)
        assertNotNull(result)
        assertEquals(123L, result!!.userId)
        assertNull(result.initialName)
    }

    @Test
    fun `resolve bridges legacy user name`() {
        val result = ProfileActivity.resolve(typed = null, legacyId = -1, legacyName = "Raki")
        assertNotNull(result)
        assertEquals(0L, result!!.userId)
        assertEquals("Raki", result.initialName)
    }

    @Test
    fun `resolve bridges legacy id and name together`() {
        val result = ProfileActivity.resolve(typed = null, legacyId = 123L, legacyName = "Raki")
        assertNotNull(result)
        assertEquals(123L, result!!.userId)
        assertEquals("Raki", result.initialName)
    }

    // ── production resolve(): typed-first precedence ──

    @Test
    fun `resolve returns typed param when present and valid`() {
        val result = ProfileActivity.resolve(typed = UserScreenParam(userId = 77L, initialName = "Raki"), legacyId = -1, legacyName = null)
        assertNotNull(result)
        assertEquals(77L, result!!.userId)
        assertEquals("Raki", result.initialName)
    }

    @Test
    fun `resolve returns typed name-only param when present and valid`() {
        val result = ProfileActivity.resolve(typed = UserScreenParam(userId = 0L, initialName = "Raki"), legacyId = 5L, legacyName = null)
        assertNotNull(result)
        assertEquals(0L, result!!.userId)
        assertEquals("Raki", result.initialName)
    }

    @Test
    fun `resolve returns null when typed param is present but invalid, even with valid legacy identity`() {
        // Typed parameter wins: an invalid typed param must not fall back to legacy.
        assertNull(ProfileActivity.resolve(typed = UserScreenParam(userId = 0L, initialName = null), legacyId = 1, legacyName = "Raki"))
    }

    // ── production hasMediaListRedirect(): deep-link routing presence semantics ──
    //
    // Only hasExtra is stubbed on the mocked intent; getStringExtra stays unstubbed
    // (returns null by default), so these tests fail if the production rule ever
    // regresses from presence-based hasExtra semantics back to value-based parsing.

    @Test
    fun `redirect does not fire when media type extra is absent`() {
        val intent = mock(Intent::class.java)
        `when`(intent.hasExtra(KeyUtil.arg_mediaType)).thenReturn(false)
        assertFalse(ProfileActivity.hasMediaListRedirect(intent))
    }

    @Test
    fun `redirect fires for anime deep link`() {
        val intent = mock(Intent::class.java)
        `when`(intent.hasExtra(KeyUtil.arg_mediaType)).thenReturn(true)
        assertTrue(ProfileActivity.hasMediaListRedirect(intent))
    }

    @Test
    fun `redirect fires for manga deep link`() {
        val intent = mock(Intent::class.java)
        `when`(intent.hasExtra(KeyUtil.arg_mediaType)).thenReturn(true)
        assertTrue(ProfileActivity.hasMediaListRedirect(intent))
    }

    @Test
    fun `redirect fires for explicitly present null value, mirroring legacy hasExtra`() {
        // hasExtra is value-agnostic: a key present with an explicitly null value
        // still reports presence (getStringExtra would return null for it, which is
        // exactly the semantic gap being closed).
        val intent = mock(Intent::class.java)
        `when`(intent.hasExtra(KeyUtil.arg_mediaType)).thenReturn(true)
        assertTrue(ProfileActivity.hasMediaListRedirect(intent))
    }

    @Test
    fun `redirect fires for non-String media type value, mirroring legacy hasExtra`() {
        // e.g. an external intent writing a numeric extra under the media-type key:
        // hasExtra still reports presence and the redirect fires, whereas
        // getStringExtra would have returned null.
        val intent = mock(Intent::class.java)
        `when`(intent.hasExtra(KeyUtil.arg_mediaType)).thenReturn(true)
        assertTrue(ProfileActivity.hasMediaListRedirect(intent))
    }

    // ── wire keys ──

    @Test
    fun `legacy wire keys match KeyUtil constants`() {
        assertEquals("id", KeyUtil.arg_id)
        assertEquals("userName", KeyUtil.arg_userName)
        assertEquals("type", KeyUtil.arg_mediaType)
    }

    @Test
    fun `profile reuses the user screen key namespace`() {
        assertEquals("arg.user.screen", ARG_USER_SCREEN)
        assertEquals(ARG_USER_SCREEN, screenParamKey<UserScreenParam>())
    }

    // ── parameter construction ──

    @Test
    fun `UserScreenParam holds userId and optional name`() {
        val param = UserScreenParam(userId = 456L, initialName = "Raki")
        assertEquals(456L, param.userId)
        assertEquals("Raki", param.initialName)
    }
}
