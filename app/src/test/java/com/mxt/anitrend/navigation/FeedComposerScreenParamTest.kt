package com.mxt.anitrend.navigation

import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.navigation.extension.ARG_FEED_COMPOSER_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.FeedComposerScreenParam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Focused tests for the feed composer screen parameter (ADR Phase 2 lane A).
 *
 * The composer argument is a dedicated identity/draft contract: it carries only stable
 * ids and display/draft strings and never a canonical [FeedList] or [UserBase]. The
 * real parcel round trip lives in [com.mxt.anitrend.navigation.ScreenParamRoundTripTest].
 */
class FeedComposerScreenParamTest {

    @Test
    fun `composer param resolves to stable wire key`() {
        assertEquals(ARG_FEED_COMPOSER_SCREEN, screenParamKey<FeedComposerScreenParam>())
    }

    @Test
    fun `composer param holds only identity and draft values`() {
        val param = FeedComposerScreenParam(
            feedId = 3L,
            draftText = "draft",
            recipientId = 9L,
            recipientName = "Raki",
        )

        assertEquals(3L, param.feedId)
        assertEquals("draft", param.draftText)
        assertEquals(9L, param.recipientId)
        assertEquals("Raki", param.recipientName)
    }

    @Test
    fun `composer param defaults represent new composition`() {
        val param = FeedComposerScreenParam()

        assertNull(param.feedId)
        assertNull(param.draftText)
        assertNull(param.recipientId)
        assertNull(param.recipientName)
    }

    @Test
    fun `composer param does not carry FeedList or UserBase fields`() {
        val fieldTypes = FeedComposerScreenParam::class.java.declaredFields.map { it.type }.toSet()

        assertFalse(fieldTypes.contains(FeedList::class.java))
        assertFalse(fieldTypes.contains(UserBase::class.java))
    }
}
