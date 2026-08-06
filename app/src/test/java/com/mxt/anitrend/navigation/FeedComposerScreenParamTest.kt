package com.mxt.anitrend.navigation

import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.navigation.model.FeedComposerScreenParam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Focused tests for the feed composer draft argument.
 *
 * The composer argument is a legacy parcelable local draft contract, NOT identity
 * navigation: it lives on the legacy `arg_model` channel (BottomSheetComposer) and
 * carries only stable ids and display/draft strings, never a canonical [FeedList]
 * or [UserBase]. The real parcel round trip lives in instrumentation
 * ([com.mxt.anitrend.navigation.FragmentBundleRoundTripTest]).
 */
class FeedComposerScreenParamTest {

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

    @Test
    fun `composer param is not part of the ScreenParam family`() {
        // The composer draft is a legacy parcelable local argument; it must not
        // resolve through the ScreenParam wire-key contract.
        assertFalse(FeedComposerScreenParam::class.java.interfaces.any { it.simpleName == "ScreenParam" })
    }
}
