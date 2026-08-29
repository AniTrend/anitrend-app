package com.mxt.anitrend.view.fragment.detail

import com.mxt.anitrend.navigation.extension.ARG_CHARACTER_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CharacterFragmentTest {

    @Test
    fun `resolve returns null when no identity is supplied`() {
        assertNull(CharacterFragment.resolve(typed = null, legacyId = 0L))
        assertNull(CharacterFragment.resolve(typed = null, legacyId = -1L))
    }

    @Test
    fun `resolve bridges positive legacy id`() {
        val result = CharacterFragment.resolve(typed = null, legacyId = 123L)
        assertNotNull(result)
        assertEquals(123L, result?.characterId)
    }

    @Test
    fun `resolve uses the valid typed parameter`() {
        val result = CharacterFragment.resolve(
            typed = CharacterScreenParam(characterId = 77L),
            legacyId = 5L,
        )
        assertEquals(CharacterScreenParam(characterId = 77L), result)
    }

    @Test
    fun `resolve rejects invalid typed parameter`() {
        assertNull(
            CharacterFragment.resolve(
                typed = CharacterScreenParam(characterId = 0L),
                legacyId = 5L,
            ),
        )
    }

    @Test
    fun `character identity keeps its stable wire key`() {
        assertEquals("id", KeyUtil.arg_id)
        assertEquals("arg.character.screen", ARG_CHARACTER_SCREEN)
        assertEquals(ARG_CHARACTER_SCREEN, screenParamKey<CharacterScreenParam>())
    }
}
