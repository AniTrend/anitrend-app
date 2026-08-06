package com.mxt.anitrend.view.fragment

import com.mxt.anitrend.navigation.model.SettingsCategoryScreenParam
import com.mxt.anitrend.navigation.model.TrailerScreenParam
import com.mxt.anitrend.navigation.model.UserListScreenParam
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrailer
import com.mxt.anitrend.view.fragment.detail.BrowseReviewFragment
import com.mxt.anitrend.view.fragment.favourite.MediaFavouriteFragment
import com.mxt.anitrend.view.fragment.search.MediaSearchFragment
import com.mxt.anitrend.view.fragment.search.UserSearchFragment
import com.mxt.anitrend.view.fragment.settings.SettingsCategoryLegacyFragment
import com.mxt.anitrend.view.fragment.youtube.YouTubeEmbedFragment
import com.mxt.anitrend.view.sheet.BottomSheetListUsers
import com.mxt.anitrend.view.sheet.BottomSheetSpoiler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Compatibility tests for the documented legacy-channel parsers (search, spoiler,
 * browse-review, media-favourites) and the typed sheet/fragment parsers that have
 * real production writers (user list, settings category, YouTube embed).
 */
class FragmentSearchFavouritesSheetsArgsTest {

    // ── documented legacy channel: search query (not identity) ──

    @Test
    fun `user search legacy parser passes raw query through exactly`() {
        assertEquals("Raki", UserSearchFragment.resolveLegacyQuery("Raki"))
        assertNull(UserSearchFragment.resolveLegacyQuery(null))
    }

    @Test
    fun `media search legacy parser passes raw query and type through exactly`() {
        assertEquals("Naruto", MediaSearchFragment.resolve(legacyQuery = "Naruto", legacyType = "ANIME").searchQuery)
        assertEquals("ANIME", MediaSearchFragment.resolve(legacyQuery = "Naruto", legacyType = "ANIME").mediaType)
        assertEquals(null, MediaSearchFragment.resolve(legacyQuery = null, legacyType = null).searchQuery)
        assertEquals(null, MediaSearchFragment.resolve(legacyQuery = null, legacyType = null).mediaType)
    }

    // ── documented legacy channel: spoiler rendered text (not identity) ──

    @Test
    fun `spoiler legacy parser passes raw text through exactly`() {
        assertEquals("spoiler", BottomSheetSpoiler.resolveLegacyText("spoiler"))
        assertNull(BottomSheetSpoiler.resolveLegacyText(null))
    }

    // ── documented legacy channel: browse-review media type (not identity) ──

    @Test
    fun `browse review legacy parser passes raw type through exactly`() {
        assertEquals("MANGA", BrowseReviewFragment.resolveLegacyType("MANGA"))
        assertNull(BrowseReviewFragment.resolveLegacyType(null))
    }

    // ── documented legacy channel: media favourites (identity host writes legacy) ──

    @Test
    fun `media favourites legacy parser passes raw id and type through exactly`() {
        val args = MediaFavouriteFragment.resolve(legacyId = 4L, legacyType = "ANIME")
        assertEquals(4L, args.userId)
        assertEquals("ANIME", args.mediaType)
        // Zero id passes through exactly (pre-refactor getter contract).
        assertEquals(0L, MediaFavouriteFragment.resolve(legacyId = 0L, legacyType = null).userId)
        assertEquals(-1L, MediaFavouriteFragment.resolve(legacyId = -1L, legacyType = null).userId)
    }

    // ── user list sheet (typed writer in Builder.build) ──

    @Test
    fun `user list sheet resolve passes absent and negative legacy user ids through exactly`() {
        // Pre-refactor getter contract: absent resolves to 0, negative ids are not normalized.
        assertEquals(
            UserListScreenParam(userId = 0L, requestType = 0),
            BottomSheetListUsers.resolve(typed = null, legacyUserId = 0L, legacyRequestType = 0),
        )
        assertEquals(
            UserListScreenParam(userId = -5L, requestType = 1),
            BottomSheetListUsers.resolve(typed = null, legacyUserId = -5L, legacyRequestType = 1),
        )
    }

    @Test
    fun `user list sheet resolve bridges legacy user id and request type`() {
        assertEquals(
            UserListScreenParam(userId = 15L, requestType = 2),
            BottomSheetListUsers.resolve(typed = null, legacyUserId = 15L, legacyRequestType = 2),
        )
    }

    @Test
    fun `user list sheet resolve prefers typed param when valid`() {
        val typed = UserListScreenParam(userId = 9L, requestType = 1)
        assertEquals(typed, BottomSheetListUsers.resolve(typed = typed, legacyUserId = 15L, legacyRequestType = 2))
    }

    @Test
    fun `user list sheet resolve keeps raw legacy when typed param is invalid`() {
        // Typed param present but invalid (non-positive id) must not win; the exact
        // raw legacy values remain unchanged, including negative user ids.
        assertEquals(
            UserListScreenParam(userId = -5L, requestType = 1),
            BottomSheetListUsers.resolve(typed = UserListScreenParam(userId = 0L), legacyUserId = -5L, legacyRequestType = 1),
        )
    }

    // ── settings category (typed writer in SettingsHubFragment) ──

    @Test
    fun `settings category resolve bridges legacy category id`() {
        assertEquals(
            SettingsCategoryScreenParam(categoryId = "general"),
            SettingsCategoryLegacyFragment.resolve(typed = null, legacyCategoryId = "general"),
        )
        assertNull(SettingsCategoryLegacyFragment.resolve(typed = null, legacyCategoryId = null))
    }

    // ── YouTube embed: identity-only, entity bridge ──

    @Test
    fun `youtube embed resolve returns null when no identity is supplied`() {
        assertNull(YouTubeEmbedFragment.resolve(typed = null, legacyTrailer = null))
        assertNull(YouTubeEmbedFragment.resolve(typed = null, legacyTrailer = MediaTrailer(id = null, site = "youtube")))
    }

    @Test
    fun `youtube embed resolve extracts identity from legacy trailer entity`() {
        val result = YouTubeEmbedFragment.resolve(typed = null, legacyTrailer = MediaTrailer(id = "abc123", site = "youtube"))
        assertNotNull(result)
        assertEquals(TrailerScreenParam(trailerId = "abc123", site = "youtube"), result)
    }

    @Test
    fun `youtube embed resolve prefers typed param`() {
        val typed = TrailerScreenParam(trailerId = "typed", site = "youtube")
        assertEquals(typed, YouTubeEmbedFragment.resolve(typed = typed, legacyTrailer = MediaTrailer(id = "legacy", site = "youtube")))
    }
}
