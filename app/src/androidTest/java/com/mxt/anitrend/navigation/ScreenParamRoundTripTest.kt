@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.navigation

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.navigation.extension.putScreenParam
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.GiphyPreviewScreenParam
import com.mxt.anitrend.navigation.model.ImagePreviewScreenParam
import com.mxt.anitrend.navigation.model.VideoPlayerScreenParam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test that verifies the retained preview-activity
 * [com.mxt.anitrend.navigation.model.ScreenParam] parcel round trips on a
 * real Android runtime.
 *
 * A real [android.os.Parcel] is not available in JVM unit tests (the SDK is
 * stubbed with default return values), so the round trip is covered here
 * where parcelization is actually exercised. The in-app Nav3 destinations
 * consume primitive route extras; only the exported preview activities own
 * typed parameters on the wire.
 */
@RunWith(AndroidJUnit4::class)
class ScreenParamRoundTripTest {

    @Test
    fun imagePreviewScreenParamIntentRoundTripRetainsAllFields() {
        val original = ImagePreviewScreenParam(url = "https://example.com/image.png")
        val intent = Intent("com.mxt.anitrend.test").putScreenParam(original)
        assertEquals(original, intent.screenParam<ImagePreviewScreenParam>())
    }

    @Test
    fun giphyPreviewScreenParamIntentRoundTripRetainsAllFields() {
        val original = GiphyPreviewScreenParam(url = "https://example.com/preview.gif")
        val intent = Intent("com.mxt.anitrend.test").putScreenParam(original)
        assertEquals(original, intent.screenParam<GiphyPreviewScreenParam>())
    }

    @Test
    fun videoPlayerScreenParamIntentRoundTripRetainsAllFields() {
        val original = VideoPlayerScreenParam(url = "https://example.com/video.mp4")
        val intent = Intent("com.mxt.anitrend.test").putScreenParam(original)
        assertEquals(original, intent.screenParam<VideoPlayerScreenParam>())
    }

    @Test
    fun missingArgumentReturnsNull() {
        assertNull(Intent("com.mxt.anitrend.test").screenParam<ImagePreviewScreenParam>())
    }

    @Test
    fun wrongDestinationKeyReturnsNull() {
        val intent = Intent("com.mxt.anitrend.test").putScreenParam(VideoPlayerScreenParam(url = "https://example.com/video.mp4"))
        assertNull(intent.screenParam<ImagePreviewScreenParam>())
    }
}
