package com.mxt.anitrend.view.activity.detail

import com.mxt.anitrend.navigation.extension.ARG_CHARACTER_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CharacterActivityTest {

    // ── production resolve(): missing/invalid ids ──

    @Test
    fun `resolve returns null when no identity is supplied`() {
        // Mirrors an intent without extras: getLongExtra(KeyUtil.arg_id, -1) → -1.
        assertNull(CharacterActivity.resolve(typed = null, legacyId = -1))
    }

    @Test
    fun `resolve returns null when legacy id is negative`() {
        assertNull(CharacterActivity.resolve(typed = null, legacyId = -5))
    }

    @Test
    fun `resolve returns null when legacy id is zero`() {
        assertNull(CharacterActivity.resolve(typed = null, legacyId = 0))
    }

    // ── production resolve(): legacy bridge ──

    @Test
    fun `resolve bridges positive legacy id`() {
        val result = CharacterActivity.resolve(typed = null, legacyId = 123L)
        assertNotNull(result)
        assertEquals(123L, result!!.characterId)
    }

    // ── production resolve(): typed-first precedence ──

    @Test
    fun `resolve returns typed param when present and positive`() {
        val result = CharacterActivity.resolve(typed = CharacterScreenParam(characterId = 77L), legacyId = 5L)
        assertNotNull(result)
        assertEquals(77L, result!!.characterId)
    }

    @Test
    fun `resolve returns null when typed param is present but invalid, even with valid legacy id`() {
        // Typed parameter wins: an invalid typed param must not fall back to legacy.
        assertNull(CharacterActivity.resolve(typed = CharacterScreenParam(characterId = 0), legacyId = 5L))
    }

    // ── wire keys ──

    @Test
    fun `legacy wire key matches KeyUtil arg_id constant`() {
        assertEquals("id", KeyUtil.arg_id)
    }

    @Test
    fun `stable character screen key uses destination-owned namespace`() {
        assertEquals("arg.character.screen", ARG_CHARACTER_SCREEN)
    }

    @Test
    fun `screenParamKey resolves character param to its stable key`() {
        assertEquals(ARG_CHARACTER_SCREEN, screenParamKey<CharacterScreenParam>())
    }

    // ── parameter construction ──

    @Test
    fun `CharacterScreenParam holds characterId correctly`() {
        val param = CharacterScreenParam(characterId = 456L)
        assertEquals(456L, param.characterId)
    }
}
