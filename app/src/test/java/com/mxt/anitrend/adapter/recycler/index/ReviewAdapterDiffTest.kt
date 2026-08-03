package com.mxt.anitrend.adapter.recycler.index

import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Focused tests for the immutable [ReviewAdapter] item identity (Reviews Phase 3).
 *
 * The diff callback must treat stable review ids as item identity and full record
 * equality as content identity, so a rated review in the store re-renders without
 * rebuilding the whole list.
 */
class ReviewAdapterDiffTest {

    @Test
    fun `diff callback matches stable review ids`() {
        val original = record(id = 1L)
        val sameIdDifferentContent = original.copy(score = 55)
        val differentId = original.copy(id = 2L)

        assertTrue(ReviewAdapter.DIFF_CALLBACK.areItemsTheSame(original, sameIdDifferentContent))
        assertFalse(ReviewAdapter.DIFF_CALLBACK.areItemsTheSame(original, differentId))
    }

    @Test
    fun `diff callback treats equal records as same content`() {
        val original = record(id = 1L)

        assertTrue(ReviewAdapter.DIFF_CALLBACK.areContentsTheSame(original, original.copy()))
        assertFalse(ReviewAdapter.DIFF_CALLBACK.areContentsTheSame(original, original.copy(rating = 90)))
        assertFalse(ReviewAdapter.DIFF_CALLBACK.areContentsTheSame(original, original.copy(userRating = "DOWN_VOTE")))
    }

    private fun record(id: Long) = ReviewRecord(
        id = id,
        summary = "summary",
        mediaType = "ANIME",
        body = "body",
        rating = 80,
        ratingAmount = 100,
        userRating = "UP_VOTE",
        score = 90,
        isPrivate = false,
        createdAt = 1_600_000_000L,
        user =
        UserSummaryRecord(
            id = 7L,
            name = "alice",
            avatar = "https://avatar",
            siteUrl = null,
        ),
        media =
        MediaSummaryRecord(
            id = 44L,
            titleUserPreferred = "Preferred",
            titleRomaji = null,
            titleEnglish = null,
            titleOriginal = null,
            coverImage = "https://cover",
            bannerImage = "https://banner",
            type = "ANIME",
            format = null,
            episodes = 12,
            chapters = 0,
            volumes = 0,
            status = null,
            siteUrl = null,
        ),
        revision = 1L,
    )
}
