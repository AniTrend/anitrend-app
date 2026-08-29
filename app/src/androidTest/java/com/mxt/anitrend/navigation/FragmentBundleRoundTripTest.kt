@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.navigation

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.navigation.extension.ARG_REVIEW_SCREEN
import com.mxt.anitrend.navigation.extension.ARG_USER_LIST_SCREEN
import com.mxt.anitrend.navigation.extension.asBundle
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.ReviewScreenParam
import com.mxt.anitrend.navigation.model.SettingsCategoryScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.navigation.model.TrailerScreenParam
import com.mxt.anitrend.navigation.model.UserListScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.detail.CharacterFragment
import com.mxt.anitrend.view.fragment.detail.MediaFragment
import com.mxt.anitrend.view.fragment.detail.StudioFragment
import com.mxt.anitrend.view.fragment.detail.StaffFragment
import com.mxt.anitrend.view.fragment.detail.ProfileFragment
import com.mxt.anitrend.view.fragment.settings.SettingsCategoryLegacyFragment
import com.mxt.anitrend.view.fragment.youtube.YouTubeEmbedFragment
import com.mxt.anitrend.view.sheet.BottomReviewReader
import com.mxt.anitrend.view.sheet.BottomSheetListUsers
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real-Bundle writer-reader round trips on an Android runtime: bundles built by
 * the production typed writers (asBundle / putScreenParam) or legacy writers are
 * read back through the production `fromBundle` parsers.
 */
@RunWith(AndroidJUnit4::class)
class FragmentBundleRoundTripTest {

    // ── typed writer → typed reader ──

    @Test
    fun mediaParamBundleRoundTripsThroughMediaOverviewParser() {
        val param = MediaScreenParam(mediaId = 21L, mediaType = "ANIME")
        assertEquals(param, MediaFragment.fromBundle(param.asBundle()))
    }

    @Test
    fun characterParamBundleRoundTripsThroughCharacterOverviewParser() {
        val param = CharacterScreenParam(characterId = 31L)
        assertEquals(param, CharacterFragment.fromBundle(param.asBundle()))
    }

    @Test
    fun studioParamBundleRoundTripsThroughStudioParser() {
        val param = StudioScreenParam(studioId = 41L)
        assertEquals(param, StudioFragment.fromBundle(param.asBundle()))
    }

    @Test
    fun staffParamBundleRoundTripsThroughStaffParser() {
        val param = StaffScreenParam(staffId = 45L)
        assertEquals(param, StaffFragment.fromBundle(param.asBundle()))
    }

    @Test
    fun userParamBundleRoundTripsThroughProfileParser() {
        val param = UserScreenParam(userId = 51L, initialName = "Raki")
        assertEquals(param, ProfileFragment.fromBundle(param.asBundle()))
    }

    @Test
    fun trailerParamBundleRoundTripsThroughYoutubeEmbedParser() {
        val param = TrailerScreenParam(trailerId = "abc", site = "youtube")
        assertEquals(param, YouTubeEmbedFragment.fromBundle(param.asBundle()))
    }

    @Test
    fun settingsCategoryParamBundleRoundTripsThroughSettingsParser() {
        val param = SettingsCategoryScreenParam(categoryId = "general")
        assertEquals(param, SettingsCategoryLegacyFragment.fromBundle(param.asBundle()))
    }

    @Test
    fun userListParamBundleRoundTripsThroughSheetParser() {
        val param = UserListScreenParam(userId = 71L, requestType = 2)
        assertEquals(param, BottomSheetListUsers.fromBundle(param.asBundle()))
    }

    @Test
    fun reviewParamBundleRoundTripsThroughReviewReaderParser() {
        val param = ReviewScreenParam(reviewId = 7L, mediaId = 100L, mediaType = "ANIME", userId = 42L)
        assertEquals(param, BottomReviewReader.fromBundle(param.asBundle()))
    }

    // ── legacy writer → typed reader (compatibility bridge) ──

    @Test
    fun legacyMediaExtrasAreBridgedByMediaOverviewParser() {
        val bundle = Bundle().apply {
            putLong(KeyUtil.arg_id, 5L)
            putString(KeyUtil.arg_mediaType, "MANGA")
        }
        assertEquals(MediaScreenParam(mediaId = 5L, mediaType = "MANGA"), MediaFragment.fromBundle(bundle))
    }

    @Test
    fun absentLegacyExtrasResolveToExactDefaults() {
        val empty = Bundle()
        // Pre-refactor getters: id 0, type null.
        assertEquals(null, MediaFragment.fromBundle(empty))
        assertEquals(null, StudioFragment.fromBundle(empty))
        // Negative ids pass through exactly.
        val negative = Bundle().apply { putLong(KeyUtil.arg_id, -3L) }
        assertEquals(null, MediaFragment.fromBundle(negative))
    }

    @Test
    fun typedParamWinsOverLegacyExtras() {
        val bundle = MediaScreenParam(mediaId = 77L, mediaType = "ANIME").asBundle().apply {
            putLong(KeyUtil.arg_id, 5L)
            putString(KeyUtil.arg_mediaType, "MANGA")
        }
        assertEquals(MediaScreenParam(mediaId = 77L, mediaType = "ANIME"), MediaFragment.fromBundle(bundle))
    }

    @Test
    fun invalidTypedParamFallsBackToLegacyExtras() {
        val bundle = MediaScreenParam(mediaId = 0L).asBundle().apply {
            putLong(KeyUtil.arg_id, 5L)
        }
        assertEquals(MediaScreenParam(mediaId = 5L, mediaType = null), MediaFragment.fromBundle(bundle))
    }

    @Test
    fun profileLegacyIdentityBridgeWorks() {
        val byId = Bundle().apply { putLong(KeyUtil.arg_id, 9L) }
        assertEquals(UserScreenParam(userId = 9L), ProfileFragment.fromBundle(byId))

        val byName = Bundle().apply { putString(KeyUtil.arg_userName, "Raki") }
        assertEquals(UserScreenParam(userId = 0L, initialName = "Raki"), ProfileFragment.fromBundle(byName))
    }

    @Test
    fun reviewReaderLegacyArgModelBridgeWorks() {
        val param = ReviewScreenParam(reviewId = 9L)
        val bundle = Bundle().apply {
            putParcelable(KeyUtil.arg_model, param)
        }
        assertEquals(param, BottomReviewReader.fromBundle(bundle))
    }

    // ── production Builder outputs: stable and legacy keys + parsing ──

    @Test
    fun userListBuilderWritesStableAndLegacyKeysAndParsesBack() {
        val sheet = BottomSheetListUsers.Builder()
            .setUserId(5L)
            .setModelCount(3)
            .setRequestType(2)
            .build()
        val bundle = sheet.arguments ?: error("missing arguments")

        // Stable typed key written by the production builder.
        assertEquals(UserListScreenParam(userId = 5L, requestType = 2), bundle.screenParam<UserListScreenParam>())
        assertEquals(ARG_USER_LIST_SCREEN, screenParamKey<UserListScreenParam>())
        // Legacy keys retained for pre-migration readers.
        assertEquals(5L, bundle.getLong(KeyUtil.arg_userId))
        assertEquals(2, bundle.getInt(KeyUtil.arg_request_type))
        assertEquals(3, bundle.getInt(KeyUtil.arg_model))
        // Production reader parses the builder output.
        assertEquals(UserListScreenParam(userId = 5L, requestType = 2), BottomSheetListUsers.fromBundle(bundle))
    }

    @Test
    fun userListBuilderNegativeUserIdSurvivesThroughReader() {
        val sheet = BottomSheetListUsers.Builder()
            .setUserId(-5L)
            .setRequestType(1)
            .build()
        val bundle = sheet.arguments ?: error("missing arguments")

        // The builder-derived typed param is invalid (non-positive id), so the reader
        // must fall back to the exact raw legacy value, negative id included.
        assertEquals(-5L, bundle.getLong(KeyUtil.arg_userId))
        assertEquals(UserListScreenParam(userId = -5L, requestType = 1), BottomSheetListUsers.fromBundle(bundle))
    }

    @Test
    fun userListValidTypedParamTakesPrecedenceOverLegacy() {
        val bundle = Bundle().apply {
            putParcelable(screenParamKey<UserListScreenParam>(), UserListScreenParam(userId = 9L, requestType = 1))
            putLong(KeyUtil.arg_userId, 5L)
            putInt(KeyUtil.arg_request_type, 2)
        }
        assertEquals(UserListScreenParam(userId = 9L, requestType = 1), BottomSheetListUsers.fromBundle(bundle))
    }

    @Test
    fun reviewReaderBuilderWritesStableAndLegacyKeysAndParsesBack() {
        val review = ReviewRecord(
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
        val expected = ReviewScreenParam(reviewId = 7L, mediaId = 100L, mediaType = "ANIME", userId = 42L)
        val reader = BottomReviewReader.Builder().setReview(review).build()
        val bundle = reader.arguments ?: error("missing arguments")

        // Stable key written by the production builder.
        assertEquals(expected, bundle.screenParam<ReviewScreenParam>())
        assertEquals(ARG_REVIEW_SCREEN, screenParamKey<ReviewScreenParam>())
        // Legacy bridge key retained.
        assertEquals(expected, bundle.parcelable<ReviewScreenParam>(KeyUtil.arg_model))
        // Production reader parses the builder output.
        assertEquals(expected, BottomReviewReader.fromBundle(bundle))
    }
}
