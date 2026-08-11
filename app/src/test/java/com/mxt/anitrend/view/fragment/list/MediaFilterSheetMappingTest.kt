package com.mxt.anitrend.view.fragment.list

import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.GenreTagUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.view.sheet.MediaFilterSheetResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure index-to-settings mapping for the Phase 2 filter sheet integration.
 *
 * Covers the single-choice resolution shared by the browse, suggestion and
 * media-list fragments ([resolveSingleFilterValue], [resolveSingleFilterYear])
 * and the genre/tag reset clearing that persists an actually cleared selection
 * through the existing [GenreTagUtil] mapping helpers.
 */
class MediaFilterSheetMappingTest {

    // ---- resolveSingleFilterValue ----

    @Test
    fun `apply maps the selected index to the option value`() {
        val (changed, value) = resolveSingleFilterValue(
            MediaFilterSheetResult.ACTION_APPLY,
            3,
            KeyUtil.MediaSortType,
            KeyUtil.POPULARITY,
        )

        assertTrue(changed)
        assertEquals(KeyUtil.MediaSortType[3], value)
    }

    @Test
    fun `apply without a selection reports no change`() {
        val (changed, value) = resolveSingleFilterValue(
            MediaFilterSheetResult.ACTION_APPLY,
            -1,
            KeyUtil.MediaSortType,
            KeyUtil.POPULARITY,
        )

        assertFalse(changed)
        assertNull(value)
    }

    @Test
    fun `reset restores the media sort default`() {
        val (changed, value) = resolveSingleFilterValue(
            MediaFilterSheetResult.ACTION_RESET,
            0,
            KeyUtil.MediaSortType,
            KeyUtil.POPULARITY,
        )

        assertTrue(changed)
        assertEquals(KeyUtil.POPULARITY, value)
    }

    @Test
    fun `reset restores the media list sort default`() {
        val (changed, value) = resolveSingleFilterValue(
            MediaFilterSheetResult.ACTION_RESET,
            0,
            KeyUtil.MediaListSortType,
            KeyUtil.PROGRESS,
        )

        assertTrue(changed)
        assertEquals(KeyUtil.PROGRESS, value)
    }

    @Test
    fun `reset restores the sort order default`() {
        val (changed, value) = resolveSingleFilterValue(
            MediaFilterSheetResult.ACTION_RESET,
            0,
            mediaFilterSortOrders,
            KeyUtil.DESC,
        )

        assertTrue(changed)
        assertEquals(KeyUtil.DESC, value)
    }

    @Test
    fun `null sentinel index is a real selection for nullable format arrays`() {
        val formats = arrayOf<String?>(null, KeyUtil.TV, KeyUtil.MOVIE)

        val (changed, value) = resolveSingleFilterValue(
            MediaFilterSheetResult.ACTION_APPLY,
            0,
            formats,
            null,
        )

        assertTrue(changed)
        assertNull(value)
    }

    @Test
    fun `reset of nullable sentinel filters restores null`() {
        val formats = arrayOf<String?>(null, KeyUtil.TV, KeyUtil.MOVIE)

        val (changed, value) = resolveSingleFilterValue(
            MediaFilterSheetResult.ACTION_RESET,
            1,
            formats,
            null,
        )

        assertTrue(changed)
        assertNull(value)
    }

    // ---- resolveSingleFilterYear ----

    @Test
    fun `year apply maps the selected year`() {
        val years = listOf(1990, 1991, 1992)

        val (changed, year) = resolveSingleFilterYear(MediaFilterSheetResult.ACTION_APPLY, 1, years)

        assertTrue(changed)
        assertEquals(1991, year)
    }

    @Test
    fun `year apply without a selection reports no change`() {
        val (changed, year) = resolveSingleFilterYear(MediaFilterSheetResult.ACTION_APPLY, -1, listOf(1990, 1991))

        assertFalse(changed)
        assertEquals(0, year)
    }

    @Test
    fun `year reset restores the settings getter default`() {
        val (changed, year) = resolveSingleFilterYear(MediaFilterSheetResult.ACTION_RESET, 0, listOf(1990, 1991))

        assertTrue(changed)
        assertEquals(DateUtil.getCurrentYear(1), year)
    }

    // ---- Genre/tag mapping through the existing helpers ----

    @Test
    fun `genre indices map to the matching genre names`() {
        val genres = listOf(Genre("Action"), Genre("Comedy"))

        val mapped = GenreTagUtil.createGenreSelectionMap(genres, intArrayOf(0, 1).toTypedArray())

        assertEquals(mapOf(0 to "Action", 1 to "Comedy"), mapped)
    }

    @Test
    fun `reset genres clears the stored selection map`() {
        val genres = listOf(Genre("Action"), Genre("Comedy"))

        val cleared = GenreTagUtil.createGenreSelectionMap(genres, intArrayOf().toTypedArray())

        assertTrue(cleared!!.isEmpty())
    }

    @Test
    fun `tag indices map to the matching tag names`() {
        val tags = listOf(MediaTag(name = "Action"), MediaTag(name = "Comedy"))

        val mapped = GenreTagUtil.createTagSelectionMap(tags, intArrayOf(1).toTypedArray())

        assertEquals(mapOf(1 to "Comedy"), mapped)
    }

    @Test
    fun `reset tags clears the stored selection map`() {
        val tags = listOf(MediaTag(name = "Action"), MediaTag(name = "Comedy"))

        val cleared = GenreTagUtil.createTagSelectionMap(tags, intArrayOf().toTypedArray())

        assertTrue(cleared!!.isEmpty())
    }

    // ---- Request correlation ----

    @Test
    fun `result with the exact pending request id is accepted`() {
        val result = MediaFilterSheetResult("req-42", MediaFilterSheetResult.ACTION_APPLY)

        assertTrue(shouldAcceptFilterResult("SORT", "req-42", result))
    }

    @Test
    fun `result with a mismatched request id is rejected`() {
        val result = MediaFilterSheetResult("req-old", MediaFilterSheetResult.ACTION_APPLY)

        assertFalse(shouldAcceptFilterResult("SORT", "req-current", result))
    }

    @Test
    fun `result is rejected when no pending filter identity is active`() {
        val result = MediaFilterSheetResult("req-42", MediaFilterSheetResult.ACTION_APPLY)

        assertFalse(shouldAcceptFilterResult(null, "req-42", result))
    }

    @Test
    fun `result is rejected when the pending request id is missing`() {
        val result = MediaFilterSheetResult("req-42", MediaFilterSheetResult.ACTION_CANCEL)

        assertFalse(shouldAcceptFilterResult("SORT", null, result))
    }

    @Test
    fun `cancel with the exact pending request id is accepted`() {
        val result = MediaFilterSheetResult("req-42", MediaFilterSheetResult.ACTION_CANCEL)

        assertTrue(shouldAcceptFilterResult("GENRES", "req-42", result))
    }
}
