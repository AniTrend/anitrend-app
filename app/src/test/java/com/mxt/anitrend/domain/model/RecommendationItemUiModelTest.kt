package com.mxt.anitrend.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationItemUiModelTest {

    @Test
    fun `title fallback prefers userPreferred then romaji then english then original`() {
        val record = recommendationRecord(
            media = mediaSummary(
                titleUserPreferred = "Preferred",
                titleRomaji = "Romaji",
                titleEnglish = "English",
                titleOriginal = "Original",
            ),
        )

        val item = record.toRecommendationItemUiModel()

        assertEquals("Preferred", item?.title)
    }

    @Test
    fun `title fallback uses romaji when userPreferred is missing`() {
        val record = recommendationRecord(
            media = mediaSummary(
                titleUserPreferred = null,
                titleRomaji = "Romaji",
                titleEnglish = "English",
                titleOriginal = "Original",
            ),
        )

        val item = record.toRecommendationItemUiModel()

        assertEquals("Romaji", item?.title)
    }

    @Test
    fun `title fallback uses english when romaji and userPreferred are missing`() {
        val record = recommendationRecord(
            media = mediaSummary(
                titleUserPreferred = null,
                titleRomaji = null,
                titleEnglish = "English",
                titleOriginal = "Original",
            ),
        )

        val item = record.toRecommendationItemUiModel()

        assertEquals("English", item?.title)
    }

    @Test
    fun `title fallback uses original when all other titles are missing`() {
        val record = recommendationRecord(
            media = mediaSummary(
                titleUserPreferred = null,
                titleRomaji = null,
                titleEnglish = null,
                titleOriginal = "Original",
            ),
        )

        val item = record.toRecommendationItemUiModel()

        assertEquals("Original", item?.title)
    }

    @Test
    fun `title falls back to empty string when no title is available`() {
        val record = recommendationRecord(
            media = mediaSummary(
                titleUserPreferred = null,
                titleRomaji = null,
                titleEnglish = null,
                titleOriginal = null,
            ),
        )

        val item = record.toRecommendationItemUiModel()

        assertEquals("", item?.title)
    }

    @Test
    fun `returns null when the recommendation has no media recommendation`() {
        val record = recommendationRecord(media = null)

        val item = record.toRecommendationItemUiModel()

        assertNull(item)
    }

    @Test
    fun `carries render fields from the media summary`() {
        val record = recommendationRecord(
            media = mediaSummary(
                coverImage = "cover.jpg",
                type = "ANIME",
                format = "TV",
                status = "RELEASING",
                episodes = 12,
                chapters = 0,
                volumes = 0,
                startDate = FuzzyDateRecord(year = 2024, month = 1, day = 15),
                averageScore = 82,
                isFavourite = true,
            ),
        )

        val item = record.toRecommendationItemUiModel()
        val renderModel = item?.toRenderModel()

        assertEquals("cover.jpg", item?.coverImage)
        assertEquals("ANIME", item?.mediaType)
        assertEquals("TV", item?.mediaFormat)
        assertEquals("RELEASING", item?.mediaStatus)
        assertEquals(12, item?.mediaEpisodes)
        assertEquals(82, item?.averageScore)
        assertTrue(item?.isFavourite == true)
        assertEquals(2024, renderModel?.mediaStartDate?.year)
        assertEquals(82, renderModel?.averageScore)
        assertTrue(renderModel?.isFavourite == true)
    }

    private fun recommendationRecord(
        media: MediaSummaryRecord?,
    ): RecommendationRecord = RecommendationRecord(
        id = 1L,
        mediaRecommendation = media,
        rating = 50,
        user = null,
        userRating = null,
    )

    private fun mediaSummary(
        titleUserPreferred: String? = "Default",
        titleRomaji: String? = null,
        titleEnglish: String? = null,
        titleOriginal: String? = null,
        coverImage: String? = null,
        type: String? = null,
        format: String? = null,
        status: String? = null,
        episodes: Int = 0,
        chapters: Int = 0,
        volumes: Int = 0,
        startDate: FuzzyDateRecord? = null,
        averageScore: Int? = null,
        isFavourite: Boolean = false,
    ): MediaSummaryRecord = MediaSummaryRecord(
        id = 10L,
        titleUserPreferred = titleUserPreferred,
        titleRomaji = titleRomaji,
        titleEnglish = titleEnglish,
        titleOriginal = titleOriginal,
        coverImage = coverImage,
        type = type,
        format = format,
        episodes = episodes,
        chapters = chapters,
        volumes = volumes,
        status = status,
        siteUrl = null,
        isFavourite = isFavourite,
        startDate = startDate,
        nextAiringEpisode = null,
        averageScore = averageScore,
    )
}
