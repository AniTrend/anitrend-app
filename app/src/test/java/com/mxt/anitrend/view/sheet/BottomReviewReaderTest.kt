package com.mxt.anitrend.view.sheet

import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.navigation.extension.ARG_REVIEW_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.ReviewScreenParam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Writer/reader compatibility tests for the review reader sheet. The writer
 * mapping extracts only stable identities from the review record, and the reader
 * resolves typed-first with a legacy arg_model bridge.
 */
class BottomReviewReaderTest {

    private fun reviewRecord() = ReviewRecord(
        id = 7L,
        summary = "summary",
        mediaType = "ANIME",
        body = "body",
        rating = 80,
        ratingAmount = 10,
        userRating = null,
        score = 80,
        isPrivate = false,
        createdAt = 1L,
        user = UserSummaryRecord(id = 42L, name = "Raki", avatar = null, siteUrl = null),
        media = MediaSummaryRecord(
            id = 100L,
            titleRomaji = null,
            titleEnglish = null,
            titleOriginal = null,
            coverImage = null,
            type = "ANIME",
            episodes = 0,
            chapters = 0,
            volumes = 0,
            status = null,
            siteUrl = null,
        ),
        revision = 0L,
    )

    // ── writer mapping: entity → identity-only param ──

    @Test
    fun `writer mapping extracts only stable identities`() {
        val param = reviewRecord().toReviewScreenParam()
        assertEquals(
            ReviewScreenParam(reviewId = 7L, mediaId = 100L, mediaType = "ANIME", userId = 42L),
            param,
        )
    }

    @Test
    fun `writer mapping tolerates absent nested identities`() {
        val bare = ReviewRecord(
            id = 1L,
            summary = null,
            mediaType = null,
            body = null,
            rating = 0,
            ratingAmount = 0,
            userRating = null,
            score = 0,
            isPrivate = false,
            createdAt = 0L,
            user = null,
            media = null,
            revision = 0L,
        )
        assertEquals(ReviewScreenParam(reviewId = 1L), bare.toReviewScreenParam())
    }

    @Test
    fun `writer uses the stable review screen key`() {
        assertEquals(ARG_REVIEW_SCREEN, screenParamKey<ReviewScreenParam>())
    }

    // ── reader: typed-first precedence and legacy bridge ──

    @Test
    fun `reader prefers typed param when present`() {
        val typed = ReviewScreenParam(reviewId = 3L)
        val legacy = ReviewScreenParam(reviewId = 9L)
        assertEquals(typed, BottomReviewReader.resolve(typed = typed, legacy = legacy))
    }

    @Test
    fun `reader falls back to legacy arg_model param when typed absent`() {
        val legacy = ReviewScreenParam(reviewId = 9L)
        assertEquals(legacy, BottomReviewReader.resolve(typed = null, legacy = legacy))
    }

    @Test
    fun `reader returns null when nothing is supplied`() {
        assertNull(BottomReviewReader.resolve(typed = null, legacy = null))
    }
}
