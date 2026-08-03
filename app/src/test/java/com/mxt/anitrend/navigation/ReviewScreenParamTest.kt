package com.mxt.anitrend.navigation

import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.navigation.extension.ARG_REVIEW_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.ReviewScreenParam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Focused tests for the review reader screen parameter (Reviews Phase 3).
 *
 * The reader argument is an identity-only contract: it carries the stable review id plus
 * the nested user/media ids needed for related navigation and never a complete canonical
 * [ReviewRecord] or the legacy mutable [Review]. The real parcel round trip lives in
 * [com.mxt.anitrend.navigation.ScreenParamRoundTripTest].
 */
class ReviewScreenParamTest {

    @Test
    fun `review param resolves to stable wire key`() {
        assertEquals(ARG_REVIEW_SCREEN, screenParamKey<ReviewScreenParam>())
    }

    @Test
    fun `review param holds only identity values`() {
        val param =
            ReviewScreenParam(
                reviewId = 7L,
                mediaId = 100L,
                mediaType = "ANIME",
                userId = 42L,
            )

        assertEquals(7L, param.reviewId)
        assertEquals(100L, param.mediaId)
        assertEquals("ANIME", param.mediaType)
        assertEquals(42L, param.userId)
    }

    @Test
    fun `review param defaults to null nested identities`() {
        val param = ReviewScreenParam(reviewId = 5L)

        assertEquals(5L, param.reviewId)
        assertNull(param.mediaId)
        assertNull(param.mediaType)
        assertNull(param.userId)
    }

    @Test
    fun `review param does not carry ReviewRecord or Review fields`() {
        val fieldTypes = ReviewScreenParam::class.java.declaredFields.map { it.type }.toSet()

        assertFalse(fieldTypes.contains(ReviewRecord::class.java))
        assertFalse(fieldTypes.contains(Review::class.java))
        assertFalse(fieldTypes.contains(UserSummaryRecord::class.java))
        assertFalse(fieldTypes.contains(MediaSummaryRecord::class.java))
    }
}
