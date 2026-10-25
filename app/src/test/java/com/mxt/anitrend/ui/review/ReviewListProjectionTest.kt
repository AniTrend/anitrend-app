package com.mxt.anitrend.ui.review

import com.mxt.anitrend.domain.model.ReviewRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewListProjectionTest {

    @Test
    fun `review snapshots contain one row per stable review id`() {
        val first = review(id = 42L, summary = "first")
        val duplicate = review(id = 42L, summary = "duplicate")
        val other = review(id = 43L, summary = "other")

        val rows = distinctReviewRows(listOf(first, duplicate, other))

        assertEquals(listOf(42L, 43L), rows.map { it.id })
    }

    private fun review(id: Long, summary: String) = ReviewRecord(
        id = id,
        summary = summary,
        mediaType = "ANIME",
        body = "body",
        rating = 0,
        ratingAmount = 0,
        userRating = null,
        score = 0,
        isPrivate = false,
        createdAt = 1_600_000_000L,
        user = null,
        media = null,
        revision = 1L,
    )
}
