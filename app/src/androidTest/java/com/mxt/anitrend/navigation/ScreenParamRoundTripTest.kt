@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.navigation

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.navigation.extension.asBundle
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.navigation.model.FeedComposerScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.ReviewScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test that verifies [com.mxt.anitrend.navigation.model.ScreenParam]
 * parcel/bundle round trips on a real Android runtime.
 *
 * A real [android.os.Parcel] is not available in JVM unit tests (the SDK is stubbed
 * with default return values), so the round trip is covered here where parcelization
 * is actually exercised.
 */
@RunWith(AndroidJUnit4::class)
class ScreenParamRoundTripTest {

    @Test
    fun userScreenParamBundleRoundTripRetainsAllFields() {
        val original = UserScreenParam(userId = 42L, initialName = "Raki")
        assertEquals(original, original.asBundle().screenParam<UserScreenParam>())
    }

    @Test
    fun userScreenParamNullFieldsSurviveRoundTrip() {
        val original = UserScreenParam(userId = 42L, initialName = null)
        val restored = original.asBundle().screenParam<UserScreenParam>()
        assertEquals(original, restored)
        assertNull(restored?.initialName)
    }

    @Test
    fun mediaScreenParamBundleRoundTripRetainsAllFields() {
        val original = MediaScreenParam(mediaId = 21L, mediaType = "ANIME")
        assertEquals(original, original.asBundle().screenParam<MediaScreenParam>())
    }

    @Test
    fun commentScreenParamBundleRoundTripRetainsAllFields() {
        val original = CommentScreenParam(feedId = 7L)
        assertEquals(original, original.asBundle().screenParam<CommentScreenParam>())
    }

    @Test
    fun studioScreenParamBundleRoundTripRetainsAllFields() {
        val original = StudioScreenParam(studioId = 99L)
        assertEquals(original, original.asBundle().screenParam<StudioScreenParam>())
    }

    @Test
    fun feedComposerScreenParamBundleRoundTripRetainsAllFields() {
        val original = FeedComposerScreenParam(
            feedId = 12L,
            draftText = "draft",
            recipientId = 8L,
            recipientName = "Raki",
        )
        assertEquals(original, original.asBundle().screenParam<FeedComposerScreenParam>())
    }

    @Test
    fun feedComposerScreenParamNullFieldsSurviveRoundTrip() {
        val original = FeedComposerScreenParam()
        val restored = original.asBundle().screenParam<FeedComposerScreenParam>()
        assertEquals(original, restored)
        assertNull(restored?.feedId)
        assertNull(restored?.draftText)
        assertNull(restored?.recipientId)
        assertNull(restored?.recipientName)
    }

    @Test
    fun reviewScreenParamBundleRoundTripRetainsAllFields() {
        val original =
            ReviewScreenParam(
                reviewId = 7L,
                mediaId = 100L,
                mediaType = "ANIME",
                userId = 42L,
            )
        assertEquals(original, original.asBundle().screenParam<ReviewScreenParam>())
    }

    @Test
    fun reviewScreenParamNullFieldsSurviveRoundTrip() {
        val original = ReviewScreenParam(reviewId = 5L)
        val restored = original.asBundle().screenParam<ReviewScreenParam>()
        assertEquals(original, restored)
        assertNull(restored?.mediaId)
        assertNull(restored?.mediaType)
        assertNull(restored?.userId)
    }

    @Test
    fun missingArgumentReturnsNull() {
        assertNull(Bundle().screenParam<UserScreenParam>())
    }

    @Test
    fun wrongDestinationKeyReturnsNull() {
        val bundle = StudioScreenParam(studioId = 5L).asBundle()
        assertNull(bundle.screenParam<UserScreenParam>())
    }
}
