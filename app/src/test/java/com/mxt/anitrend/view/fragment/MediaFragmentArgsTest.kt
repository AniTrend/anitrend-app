package com.mxt.anitrend.view.fragment

import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.domain.mediadetail.model.MediaDetailRecord
import com.mxt.anitrend.view.fragment.detail.MediaFragment
import com.mxt.anitrend.view.fragment.detail.MediaSection
import com.mxt.anitrend.viewmodel.MediaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaFragmentArgsTest {
    @Test
    fun `typed media parameter takes precedence`() {
        val typed = MediaScreenParam(77L, "MANGA")
        assertEquals(typed, MediaFragment.resolve(typed, 5L, "ANIME"))
    }

    @Test
    fun `positive legacy identity is bridged`() {
        assertEquals(MediaScreenParam(123L, "ANIME"), MediaFragment.resolve(null, 123L, "ANIME"))
    }

    @Test
    fun `invalid identities are rejected`() {
        assertNull(MediaFragment.resolve(null, 0L, null))
        assertNull(MediaFragment.resolve(MediaScreenParam(0L), 0L, null))
    }

    @Test
    fun `unauthenticated sections exclude social content`() {
        assertEquals(
            listOf(
                MediaSection.OVERVIEW,
                MediaSection.RELATIONS,
                MediaSection.RECOMMENDATIONS,
                MediaSection.STATS,
                MediaSection.CHARACTERS,
                MediaSection.STAFF,
            ),
            MediaSection.visibleSections(false),
        )
    }

    @Test
    fun `section restoration falls back to overview when unavailable`() {
        assertEquals(MediaSection.OVERVIEW, MediaSection.fromOrdinal(6, false))
        assertEquals(MediaSection.REVIEWS, MediaSection.fromOrdinal(7, true))
    }

    @Test
    fun `primary media load states expose inline recoverable error presentation`() {
        assertEquals(
            "LOADING",
            MediaFragment.primaryLoadPresentation(MediaViewModel.UiState.Loading).name,
        )
        assertEquals(
            "ERROR",
            MediaFragment.primaryLoadPresentation(MediaViewModel.UiState.Error("network")).name,
        )
        assertEquals(
            "CONTENT",
            MediaFragment.primaryLoadPresentation(
                MediaViewModel.UiState.Success(
                    MediaDetailRecord(
                        id = 1L,
                        idMal = null,
                        titleUserPreferred = "Title",
                        type = "ANIME",
                        bannerImage = null,
                        isFavourite = false,
                        siteUrl = null,
                        mediaListEntry = null,
                    ),
                ),
            ).name,
        )
    }
}
