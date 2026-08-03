package com.mxt.anitrend.adapter.recycler.group

import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.RecommendationItemUiModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationAdapterTest {

    private val diffCallback = RecommendationAdapter.DIFF_CALLBACK

    @Test
    fun `DiffCallback identifies items by recommendation id`() {
        val first = recommendationItem(id = 1L)
        val second = recommendationItem(id = 2L)

        assertFalse(diffCallback.areItemsTheSame(first, second))
    }

    @Test
    fun `DiffCallback treats same recommendation id as same item`() {
        val first = recommendationItem(id = 1L)
        val second = recommendationItem(id = 1L)

        assertTrue(diffCallback.areItemsTheSame(first, second))
        assertTrue(diffCallback.areContentsTheSame(first, second))
    }

    @Test
    fun `DiffCallback detects content changes for the same id`() {
        val first = recommendationItem(id = 1L, title = "Alpha", averageScore = 80)
        val second = recommendationItem(id = 1L, title = "Beta", averageScore = 85)

        assertTrue(diffCallback.areItemsTheSame(first, second))
        assertFalse(diffCallback.areContentsTheSame(first, second))
    }

    @Test
    fun `DiffCallback ignores content when id differs`() {
        val first = recommendationItem(id = 1L, title = "Alpha")
        val second = recommendationItem(id = 2L, title = "Alpha")

        assertFalse(diffCallback.areItemsTheSame(first, second))
    }

    private fun recommendationItem(
        id: Long,
        title: String = "Title $id",
        averageScore: Int? = 0,
    ): RecommendationItemUiModel = RecommendationItemUiModel(
        id = id,
        mediaId = 100L + id,
        title = title,
        titleEnglish = null,
        titleOriginal = null,
        coverImage = null,
        mediaType = "ANIME",
        mediaFormat = "TV",
        mediaStatus = "RELEASING",
        mediaEpisodes = 12,
        mediaChapters = 0,
        mediaVolumes = 0,
        mediaStartDate = FuzzyDateRecord(year = 2024, month = 1, day = 1),
        averageScore = averageScore,
        isFavourite = false,
    )
}
