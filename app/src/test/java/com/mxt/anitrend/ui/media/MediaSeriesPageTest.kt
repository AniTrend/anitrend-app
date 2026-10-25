package com.mxt.anitrend.ui.media

import com.mxt.anitrend.domain.model.MediaSearchItemRenderModel
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCardUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaSeriesPageTest {
    @Test
    fun `duplicate ids within the first page produce one keyed row`() {
        val first = row(20605L, "First occurrence")
        val result = appendMediaSeriesPage(emptyList(), listOf(first, row(20605L, "Repeated occurrence"), row(2L)))

        assertEquals(listOf(20605L, 2L), result.map { it.id })
        assertEquals("First occurrence", result.first().title)
    }

    @Test
    fun `overlapping and replayed pages do not count duplicates as new content`() {
        val firstPage = appendMediaSeriesPage(emptyList(), listOf(row(1L), row(2L)))
        val appended = appendMediaSeriesPage(firstPage, listOf(row(2L), row(3L), row(3L)))
        val replayed = appendMediaSeriesPage(appended, listOf(row(2L), row(3L)))

        assertEquals(listOf(1L, 2L, 3L), appended.map { it.id })
        assertEquals(1, appended.size - firstPage.size)
        assertEquals(0, replayed.size - appended.size)
        assertEquals(listOf(1L, 2L), firstPage.map { it.id })
    }

    private fun row(id: Long, title: String = "Media $id") = MediaSeriesCardUiState(
        id = id,
        title = title,
        coverImage = null,
        statusColorResource = 0,
        yearTypeText = "",
        renderModel = MediaSearchItemRenderModel(
            mediaStartDate = null,
            mediaType = "ANIME",
            mediaFormat = "TV",
            mediaEpisodes = 12,
            mediaChapters = 0,
            averageScore = null,
            isFavourite = false,
        ),
        hasMediaListEntry = false,
        mediaListStatus = null,
    )
}
