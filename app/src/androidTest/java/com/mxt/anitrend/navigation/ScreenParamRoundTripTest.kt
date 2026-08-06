@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.navigation

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.navigation.extension.asBundle
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.navigation.model.GiphyPreviewScreenParam
import com.mxt.anitrend.navigation.model.ImagePreviewScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.ReviewScreenParam
import com.mxt.anitrend.navigation.model.ScreenParam
import com.mxt.anitrend.navigation.model.SettingsCategoryScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.TrailerScreenParam
import com.mxt.anitrend.navigation.model.UserListScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.navigation.model.VideoPlayerScreenParam
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
    fun characterScreenParamBundleRoundTripRetainsAllFields() {
        val original = CharacterScreenParam(characterId = 123L)
        assertEquals(original, original.asBundle().screenParam<CharacterScreenParam>())
    }

    @Test
    fun staffScreenParamBundleRoundTripRetainsAllFields() {
        val original = StaffScreenParam(staffId = 456L)
        assertEquals(original, original.asBundle().screenParam<StaffScreenParam>())
    }

    @Test
    fun imagePreviewScreenParamBundleRoundTripRetainsAllFields() {
        val original = ImagePreviewScreenParam(url = "https://example.com/image.png")
        assertEquals(original, original.asBundle().screenParam<ImagePreviewScreenParam>())
    }

    @Test
    fun giphyPreviewScreenParamBundleRoundTripRetainsAllFields() {
        val original = GiphyPreviewScreenParam(url = "https://example.com/preview.gif")
        assertEquals(original, original.asBundle().screenParam<GiphyPreviewScreenParam>())
    }

    @Test
    fun videoPlayerScreenParamBundleRoundTripRetainsAllFields() {
        val original = VideoPlayerScreenParam(url = "https://example.com/video.mp4")
        assertEquals(original, original.asBundle().screenParam<VideoPlayerScreenParam>())
    }

    @Test
    fun userListScreenParamBundleRoundTripRetainsAllFields() {
        val original = UserListScreenParam(userId = 15L, requestType = 2)
        assertEquals(original, original.asBundle().screenParam<UserListScreenParam>())
    }

    @Test
    fun trailerScreenParamBundleRoundTripRetainsAllFields() {
        val original = TrailerScreenParam(trailerId = "abc123", site = "youtube")
        assertEquals(original, original.asBundle().screenParam<TrailerScreenParam>())
    }

    @Test
    fun settingsCategoryScreenParamBundleRoundTripRetainsAllFields() {
        val original = SettingsCategoryScreenParam(categoryId = "general")
        assertEquals(original, original.asBundle().screenParam<SettingsCategoryScreenParam>())
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
