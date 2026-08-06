package com.mxt.anitrend.view.fragment

import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.view.fragment.detail.CharacterOverviewFragment
import com.mxt.anitrend.view.fragment.detail.MediaFeedFragment
import com.mxt.anitrend.view.fragment.detail.MediaOverviewFragment
import com.mxt.anitrend.view.fragment.detail.MediaStaffFragment
import com.mxt.anitrend.view.fragment.detail.MediaStatsFragment
import com.mxt.anitrend.view.fragment.detail.ReviewFragment
import com.mxt.anitrend.view.fragment.detail.StudioMediaFragment
import com.mxt.anitrend.view.fragment.detail.UserFeedFragment
import com.mxt.anitrend.view.fragment.detail.UserOverviewFragment
import com.mxt.anitrend.view.fragment.group.CharacterActorsFragment
import com.mxt.anitrend.view.fragment.group.MediaCharacterFragment
import com.mxt.anitrend.view.fragment.group.MediaFormatFragment
import com.mxt.anitrend.view.fragment.group.MediaRecommendationsFragment
import com.mxt.anitrend.view.fragment.group.MediaRelationFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Compatibility tests for the media/user/character/studio family fragment parsers.
 * All cases invoke the production `resolve` functions; absent, zero/negative,
 * typed-first precedence, and legacy-fallback behavior are pinned exactly.
 */
class FragmentMediaFamilyArgsTest {

    // ── media family (MediaScreenParam): absent / zero / negative legacy values ──

    @Test
    fun `media family resolve passes absent legacy id through as zero`() {
        // Pre-refactor: getLong(arg_id) resolves to 0 when absent.
        assertEquals(MediaScreenParam(mediaId = 0L, mediaType = null), MediaOverviewFragment.resolve(typed = null, legacyId = 0L, legacyType = null))
        assertEquals(MediaScreenParam(mediaId = 0L, mediaType = null), MediaStatsFragment.resolve(typed = null, legacyId = 0L, legacyType = null))
        assertEquals(MediaScreenParam(mediaId = 0L, mediaType = null), MediaRelationFragment.resolve(typed = null, legacyId = 0L, legacyType = null))
        assertEquals(MediaScreenParam(mediaId = 0L, mediaType = null), MediaCharacterFragment.resolve(typed = null, legacyId = 0L, legacyType = null))
        assertEquals(MediaScreenParam(mediaId = 0L, mediaType = null), MediaRecommendationsFragment.resolve(typed = null, legacyId = 0L, legacyType = null))
        assertEquals(MediaScreenParam(mediaId = 0L, mediaType = null), MediaStaffFragment.resolve(typed = null, legacyId = 0L, legacyType = null))
        assertEquals(MediaScreenParam(mediaId = 0L, mediaType = null), ReviewFragment.resolve(typed = null, legacyId = 0L, legacyType = null))
    }

    @Test
    fun `media family resolve passes negative legacy id through exactly`() {
        // Pre-refactor getter contract: negative ids are not normalized away.
        assertEquals(MediaScreenParam(mediaId = -5L, mediaType = "ANIME"), MediaRelationFragment.resolve(typed = null, legacyId = -5L, legacyType = "ANIME"))
    }

    @Test
    fun `media family resolve bridges positive legacy id and type`() {
        val result = MediaRelationFragment.resolve(typed = null, legacyId = 123L, legacyType = "ANIME")
        assertNotNull(result)
        assertEquals(MediaScreenParam(mediaId = 123L, mediaType = "ANIME"), result)
    }

    @Test
    fun `media family resolve bridges legacy id without type`() {
        assertEquals(MediaScreenParam(mediaId = 123L, mediaType = null), MediaCharacterFragment.resolve(typed = null, legacyId = 123L, legacyType = null))
    }

    // ── media family: typed-first precedence and legacy fallback ──

    @Test
    fun `media family resolve prefers typed param when present and valid`() {
        val typed = MediaScreenParam(mediaId = 77L, mediaType = "MANGA")
        assertEquals(typed, MediaOverviewFragment.resolve(typed = typed, legacyId = 5L, legacyType = "ANIME"))
    }

    @Test
    fun `media family resolve falls back to raw legacy when typed param is invalid`() {
        // Typed param present but invalid (id 0) must not win; the exact raw legacy
        // values are used instead.
        assertEquals(
            MediaScreenParam(mediaId = 5L, mediaType = "ANIME"),
            MediaStatsFragment.resolve(typed = MediaScreenParam(mediaId = 0L), legacyId = 5L, legacyType = "ANIME"),
        )
        assertEquals(
            MediaScreenParam(mediaId = -2L, mediaType = null),
            MediaStatsFragment.resolve(typed = MediaScreenParam(mediaId = 0L), legacyId = -2L, legacyType = null),
        )
    }

    // ── MediaFeedFragment (legacy key arg_mediaId) ──

    @Test
    fun `media feed resolve passes absent legacy id through as zero`() {
        assertEquals(MediaScreenParam(mediaId = 0L), MediaFeedFragment.resolve(typed = null, legacyMediaId = 0L))
    }

    @Test
    fun `media feed resolve bridges legacy media id and prefers typed param`() {
        assertEquals(MediaScreenParam(mediaId = 42L), MediaFeedFragment.resolve(typed = null, legacyMediaId = 42L))
        assertEquals(
            MediaScreenParam(mediaId = 9L),
            MediaFeedFragment.resolve(typed = MediaScreenParam(mediaId = 9L), legacyMediaId = 42L),
        )
    }

    // ── character family (CharacterScreenParam via CharacterActivity) ──

    @Test
    fun `character overview resolve passes absent and negative legacy ids through exactly`() {
        assertEquals(CharacterScreenParam(characterId = 0L), CharacterOverviewFragment.resolve(typed = null, legacyId = 0L))
        assertEquals(CharacterScreenParam(characterId = -3L), CharacterOverviewFragment.resolve(typed = null, legacyId = -3L))
    }

    @Test
    fun `character overview resolve bridges legacy id and prefers typed param`() {
        assertEquals(CharacterScreenParam(characterId = 8L), CharacterOverviewFragment.resolve(typed = null, legacyId = 8L))
        val typed = CharacterScreenParam(characterId = 3L)
        assertEquals(typed, CharacterOverviewFragment.resolve(typed = typed, legacyId = 8L))
    }

    @Test
    fun `character actors resolve bridges legacy id and falls back when typed invalid`() {
        assertEquals(CharacterScreenParam(characterId = 8L), CharacterActorsFragment.resolve(typed = null, legacyId = 8L))
        assertEquals(
            CharacterScreenParam(characterId = 8L),
            CharacterActorsFragment.resolve(typed = CharacterScreenParam(characterId = 0L), legacyId = 8L),
        )
    }

    // ── studio family (StudioScreenParam via StudioActivity) ──

    @Test
    fun `studio media resolve passes absent and negative legacy ids through exactly`() {
        assertEquals(0L, StudioMediaFragment.resolve(typed = null, legacyId = 0L))
        assertEquals(-4L, StudioMediaFragment.resolve(typed = null, legacyId = -4L))
    }

    @Test
    fun `studio media resolve bridges legacy id and prefers typed param`() {
        assertEquals(6L, StudioMediaFragment.resolve(typed = null, legacyId = 6L))
        assertEquals(7L, StudioMediaFragment.resolve(typed = com.mxt.anitrend.navigation.model.StudioScreenParam(studioId = 7L), legacyId = 6L))
        // Invalid typed param falls back to the raw legacy value.
        assertEquals(6L, StudioMediaFragment.resolve(typed = com.mxt.anitrend.navigation.model.StudioScreenParam(studioId = 0L), legacyId = 6L))
    }

    // ── media format (shared character/staff pager tab) ──

    @Test
    fun `media format resolve passes absent and negative legacy ids through exactly`() {
        assertEquals(0L, MediaFormatFragment.resolve(character = null, staff = null, legacyId = 0L))
        assertEquals(-2L, MediaFormatFragment.resolve(character = null, staff = null, legacyId = -2L))
    }

    @Test
    fun `media format resolve reads typed identity from character or staff pager`() {
        assertEquals(9L, MediaFormatFragment.resolve(character = CharacterScreenParam(characterId = 9L), staff = null, legacyId = 3L))
        assertEquals(8L, MediaFormatFragment.resolve(character = null, staff = StaffScreenParam(staffId = 8L), legacyId = 3L))
        // Character identity takes precedence when both typed sources are present.
        assertEquals(
            9L,
            MediaFormatFragment.resolve(character = CharacterScreenParam(characterId = 9L), staff = StaffScreenParam(staffId = 8L), legacyId = 3L),
        )
    }

    @Test
    fun `media format resolve falls back to raw legacy when typed invalid`() {
        assertEquals(3L, MediaFormatFragment.resolve(character = CharacterScreenParam(characterId = 0L), staff = null, legacyId = 3L))
        assertEquals(3L, MediaFormatFragment.resolve(character = null, staff = StaffScreenParam(staffId = 0L), legacyId = 3L))
        assertEquals(-3L, MediaFormatFragment.resolve(character = CharacterScreenParam(characterId = 0L), staff = null, legacyId = -3L))
    }

    // ── user family (UserScreenParam via ProfileActivity) ──

    @Test
    fun `user overview resolve keeps exact legacy defaults`() {
        // Pre-refactor: getLong(arg_id, 0L) and getString(arg_userName, "").
        assertEquals(UserScreenParam(userId = 0L, initialName = ""), UserOverviewFragment.resolve(typed = null, legacyId = 0L, legacyName = ""))
        assertEquals(UserScreenParam(userId = 5L, initialName = "Raki"), UserOverviewFragment.resolve(typed = null, legacyId = 5L, legacyName = "Raki"))
    }

    @Test
    fun `user overview resolve prefers typed param`() {
        val typed = UserScreenParam(userId = 9L, initialName = "Raki")
        assertEquals(typed, UserOverviewFragment.resolve(typed = typed, legacyId = 5L, legacyName = "Other"))
    }

    @Test
    fun `user feed resolve preserves containsKey id-versus-name semantics`() {
        // Pre-refactor: containsKey(arg_id) ? getLong(arg_id) : getString(arg_userName).
        assertEquals(UserScreenParam(userId = 5L), UserFeedFragment.resolve(typed = null, hasLegacyId = true, legacyId = 5L, legacyName = "Raki"))
        assertEquals(
            UserScreenParam(userId = 0L, initialName = "Raki"),
            UserFeedFragment.resolve(typed = null, hasLegacyId = false, legacyId = 0L, legacyName = "Raki"),
        )
    }

    @Test
    fun `user feed resolve prefers typed param over legacy`() {
        val typed = UserScreenParam(userId = 9L)
        assertEquals(typed, UserFeedFragment.resolve(typed = typed, hasLegacyId = true, legacyId = 5L, legacyName = null))
    }
}
