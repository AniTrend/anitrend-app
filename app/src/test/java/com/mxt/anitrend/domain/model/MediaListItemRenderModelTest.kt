package com.mxt.anitrend.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaListItemRenderModelTest {

    @Test
    fun `toRenderModel maps all view-relevant fields`() {
        val uiModel = createUiModel()

        val renderModel = uiModel.toRenderModel()

        assertEquals(uiModel.score, renderModel.score, 0.0)
        assertEquals(uiModel.progress, renderModel.progress)
        assertEquals(uiModel.mediaStatus, renderModel.mediaStatus)
        assertEquals(uiModel.nextAiringEpisode, renderModel.nextAiringEpisode)
        assertEquals(uiModel.mediaStartDate, renderModel.mediaStartDate)
        assertEquals(uiModel.mediaType, renderModel.mediaType)
        assertEquals(uiModel.mediaFormat, renderModel.mediaFormat)
        assertEquals(uiModel.mediaEpisodes, renderModel.mediaEpisodes)
        assertEquals(uiModel.mediaChapters, renderModel.mediaChapters)
        assertEquals(uiModel.mediaIsFavourite, renderModel.mediaIsFavourite)
    }

    @Test
    fun `toRenderModel keeps optional media fields when absent`() {
        val uiModel = createUiModel().copy(
            mediaStatus = null,
            nextAiringEpisode = null,
            mediaStartDate = null,
            mediaIsFavourite = false,
        )

        val renderModel = uiModel.toRenderModel()

        assertNull(renderModel.mediaStatus)
        assertNull(renderModel.nextAiringEpisode)
        assertNull(renderModel.mediaStartDate)
        assertFalse(renderModel.mediaIsFavourite)
    }

    @Test
    fun `render model is immutable and not a legacy MediaList`() {
        val renderModel = createUiModel().toRenderModel()
        // A data class projection is the intended render model; score is a Double, not a Float entity field.
        assertTrue(renderModel is MediaListItemRenderModel)
    }

    @Test
    fun `fuzzy date record validity treats null fields as zero`() {
        assertFalse(FuzzyDateRecord(year = null, month = null, day = null).isValidDate)
        assertFalse(FuzzyDateRecord(year = 0, month = 0, day = 0).isValidDate)
        assertTrue(FuzzyDateRecord(year = 2020, month = null, day = null).isValidDate)
        assertTrue(FuzzyDateRecord(year = null, month = null, day = 5).isValidDate)
    }

    private fun createUiModel() = MediaListItemUiModel(
        id = 1L,
        mediaId = 100L,
        status = "CURRENT",
        progress = 5,
        progressVolumes = 0,
        score = 8.5,
        repeat = 0,
        mediaTitle = "Title",
        mediaTitleEnglish = "Title",
        mediaTitleOriginal = null,
        mediaCoverImage = "https://example.com/cover.jpg",
        mediaType = "ANIME",
        mediaFormat = "TV",
        mediaStatus = "RELEASING",
        mediaEpisodes = 12,
        mediaChapters = 0,
        mediaVolumes = 0,
        mediaStartDate = FuzzyDateRecord(year = 2020, month = 4, day = 1),
        nextAiringEpisode = AiringScheduleRecord(airingAt = 1L, timeUntilAiring = 3600L, episode = 6),
        mediaIsFavourite = true,
        isIncrementPending = false,
        isDeletePending = false,
        canIncrement = true,
    )
}
