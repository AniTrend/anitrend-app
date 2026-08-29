package com.mxt.anitrend.view.fragment

import com.mxt.anitrend.navigation.model.SettingsCategoryScreenParam
import com.mxt.anitrend.navigation.model.TrailerScreenParam
import com.mxt.anitrend.navigation.model.UserListScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrailer
import com.mxt.anitrend.view.fragment.detail.BrowseReviewFragment
import com.mxt.anitrend.view.fragment.favourite.FavouriteFragment
import com.mxt.anitrend.view.fragment.list.FeedFragment
import com.mxt.anitrend.view.fragment.list.AnimeFragment
import com.mxt.anitrend.view.fragment.list.MangaFragment
import com.mxt.anitrend.view.fragment.list.TrendingFragment
import com.mxt.anitrend.view.fragment.detail.ReviewBrowseFragment
import com.mxt.anitrend.view.fragment.search.SearchFragment
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
 * browse-review, favourites) and the typed sheet/fragment parsers that have
 * real production writers (user list, settings category, YouTube embed).
 */
class FragmentSearchFavouritesSheetsArgsTest {

    // ── documented legacy channel: search query (not identity) ──

    @Test
    fun `unified search destination preserves the legacy query channel`() {
        assertEquals("Raki", SearchFragment.resolveLegacyQuery("Raki"))
        assertNull(SearchFragment.resolveLegacyQuery(null))
    }

    @Test
    fun `search submission trims replacement query and rejects blank input`() {
        assertEquals("Raki", SearchFragment.normalizeSubmittedQuery("  Raki  "))
        assertNull(SearchFragment.normalizeSubmittedQuery("   "))
        assertNull(SearchFragment.normalizeSubmittedQuery(null))
    }

    @Test
    fun `unified feed destination defaults to the first local section`() {
        assertEquals("PROGRESS", FeedFragment.resolveSection(null))
        assertEquals("STATUS", FeedFragment.resolveSection("STATUS"))
    }

    @Test
    fun `unified review destination defaults to anime`() {
        assertEquals("ANIME", ReviewBrowseFragment.resolveSection(null))
        assertEquals("MANGA", ReviewBrowseFragment.resolveSection("MANGA"))
    }

    @Test
    fun `unified trending destination defaults to anime`() {
        assertEquals("ANIME", TrendingFragment.resolveSection(null))
        assertEquals("RECENTLY_ADDED", TrendingFragment.resolveSection("RECENTLY_ADDED"))
    }

    @Test
    fun `unified anime destination defaults to spring`() {
        assertEquals("SPRING", AnimeFragment.resolveSection(null))
        assertEquals("WINTER", AnimeFragment.resolveSection("WINTER"))
    }

    @Test
    fun `unified manga destination defaults to manga list`() {
        assertEquals("MANGA_LIST", MangaFragment.resolveSection(null))
        assertEquals("RECENTLY_ADDED", MangaFragment.resolveSection("RECENTLY_ADDED"))
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

    // ── documented legacy channel: favourites host identity ──

    @Test
    fun `favourites legacy parser keeps user identity and rejects an empty identity`() {
        assertEquals(
            UserScreenParam(userId = 4L, initialName = "Raki"),
            FavouriteFragment.resolveLegacyUser(legacyId = 4L, legacyName = "Raki"),
        )
        assertNull(FavouriteFragment.resolveLegacyUser(legacyId = 0L, legacyName = null))
        assertEquals(
            UserScreenParam(userId = 0L, initialName = "Raki"),
            FavouriteFragment.resolveLegacyUser(legacyId = 0L, legacyName = "Raki"),
        )
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
