package com.mxt.anitrend.view.fragment

import com.mxt.anitrend.view.fragment.detail.MediaFragment
import com.mxt.anitrend.view.fragment.detail.MediaSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaFragmentSectionOrderTest {

    private val allEightSections = listOf(
        MediaSection.OVERVIEW,
        MediaSection.RELATIONS,
        MediaSection.RECOMMENDATIONS,
        MediaSection.STATS,
        MediaSection.CHARACTERS,
        MediaSection.STAFF,
        MediaSection.FEED,
        MediaSection.REVIEWS,
    )

    @Test
    fun `section view order covers the full mapping of all eight sections once`() {
        assertEquals(allEightSections, MediaFragment.sectionViewOrder)
        assertEquals(allEightSections.size, MediaFragment.sectionViewOrder.toSet().size)
    }

    @Test
    fun `section view order never drifts from the selector order`() {
        // selectSection resolves the container child through sectionViewOrder, so it must
        // stay identical to the enum order used by the selector and visibility rules.
        assertEquals(MediaSection.entries, MediaFragment.sectionViewOrder)
    }

    @Test
    fun `authenticated visibility exposes all eight sections in view order`() {
        assertEquals(allEightSections, MediaSection.visibleSections(true))
    }

    @Test
    fun `unauthenticated visibility hides feed and reviews but preserves view order`() {
        val expected = allEightSections.filterNot { it == MediaSection.FEED || it == MediaSection.REVIEWS }
        assertEquals(expected, MediaSection.visibleSections(false))
        assertTrue(MediaSection.FEED !in MediaSection.visibleSections(false))
        assertTrue(MediaSection.REVIEWS !in MediaSection.visibleSections(false))
    }
}
