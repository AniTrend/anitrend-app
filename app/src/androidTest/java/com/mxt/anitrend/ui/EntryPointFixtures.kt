package com.mxt.anitrend.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mxt.anitrend.navigation.EXTRA_ROUTE
import com.mxt.anitrend.navigation.ROUTE_AIRING
import com.mxt.anitrend.navigation.ROUTE_CHARACTER
import com.mxt.anitrend.navigation.ROUTE_FAVOURITES
import com.mxt.anitrend.navigation.ROUTE_FEED
import com.mxt.anitrend.navigation.ROUTE_LOGGING
import com.mxt.anitrend.navigation.ROUTE_MEDIA
import com.mxt.anitrend.navigation.ROUTE_MEDIA_LIST
import com.mxt.anitrend.navigation.ROUTE_PROFILE
import com.mxt.anitrend.navigation.ROUTE_SEARCH
import com.mxt.anitrend.navigation.ROUTE_STAFF
import com.mxt.anitrend.navigation.ROUTE_STUDIO
import com.mxt.anitrend.navigation.ROUTE_TRENDING
import com.mxt.anitrend.navigation.model.GiphyPreviewScreenParam
import com.mxt.anitrend.navigation.model.ImagePreviewScreenParam
import com.mxt.anitrend.navigation.model.VideoPlayerScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.base.GiphyPreviewActivity
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity
import com.mxt.anitrend.view.activity.base.VideoPlayerActivity
import com.mxt.anitrend.view.activity.base.WelcomeActivity
import com.mxt.anitrend.view.activity.index.LoginActivity
import com.mxt.anitrend.view.activity.index.MainActivity

internal data class EntryPoint(
    val name: String,
    val intentProvider: (Context) -> Intent,
    val assertUi: Boolean = true,
)

internal object EntryPointFixtures {
    fun unauthenticated(context: Context): List<EntryPoint> = listOf(
        // SplashActivity is intentionally excluded from render smoke tests because it immediately routes onward and performs startup side effects rather than exposing a stable UI surface.
        EntryPoint("MainActivity", { Intent(it, MainActivity::class.java) }),
        EntryPoint("FeedFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_FEED)
        }),
        EntryPoint("AiringFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_AIRING)
        }),
        EntryPoint("TrendingFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_TRENDING)
        }),
        EntryPoint("LoginActivity", { Intent(it, LoginActivity::class.java) }),
        EntryPoint("SearchFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_SEARCH)
                .putExtra(KeyUtil.arg_search, "test")
        }),
        EntryPoint("LoggingFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_LOGGING)
        }),
        EntryPoint("WelcomeActivity", { Intent(it, WelcomeActivity::class.java) }),
        EntryPoint("FavouriteFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_FAVOURITES)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("MediaListFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_MEDIA_LIST)
                .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                .putExtra(KeyUtil.arg_userName, "test-user")
        }),
        EntryPoint("MediaFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_MEDIA)
                .putExtra(KeyUtil.arg_id, 1L)
                .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
        }),
        EntryPoint("ProfileFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_PROFILE)
                .putExtra(KeyUtil.arg_userName, "test-user")
        }),
        EntryPoint("CharacterFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_CHARACTER)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StaffFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_STAFF)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StudioFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_STUDIO)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("CommentRoute", {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://anilist.co/activity/1"))
                .setClass(it, MainActivity::class.java)
        }),
        EntryPoint("ImagePreviewActivity", {
            Intent(it, ImagePreviewActivity::class.java)
                .putExtra(KeyUtil.arg_model, "https://example.com/image.png")
        }),
        EntryPoint("GiphyPreviewActivity", {
            Intent(it, GiphyPreviewActivity::class.java)
                .putExtra(KeyUtil.arg_model, "https://example.com/preview.gif")
        }),
        EntryPoint("VideoPlayerActivity", {
            Intent(it, VideoPlayerActivity::class.java)
                .putExtra(KeyUtil.arg_model, "https://example.com/video.mp4")
        }),
        EntryPoint("SharedContentFragment", {
            Intent(Intent.ACTION_SEND)
                .setClass(it, MainActivity::class.java)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "https://example.com")
        }),
    ) + typedMigratedEntries(context) + externalIngressEntries(context)

    fun authenticated(context: Context): List<EntryPoint> = listOf(
        // SplashActivity is intentionally excluded from render smoke tests because it immediately routes onward and performs startup side effects rather than exposing a stable UI surface.
        EntryPoint("MainActivity", { Intent(it, MainActivity::class.java) }),
        EntryPoint("FeedFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_FEED)
        }),
        EntryPoint("AiringFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_AIRING)
        }),
        EntryPoint("TrendingFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_TRENDING)
        }),
        EntryPoint("LoginActivity", { Intent(it, LoginActivity::class.java) }, assertUi = false),
        EntryPoint("SearchFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_SEARCH)
                .putExtra(KeyUtil.arg_search, "test")
        }),
        EntryPoint("LoggingFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_LOGGING)
        }),
        EntryPoint("WelcomeActivity", { Intent(it, WelcomeActivity::class.java) }),
        EntryPoint("FavouriteFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_FAVOURITES)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("MediaListFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_MEDIA_LIST)
                .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                .putExtra(KeyUtil.arg_userName, "test-user")
        }),
        EntryPoint("MediaFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_MEDIA)
                .putExtra(KeyUtil.arg_id, 1L)
                .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
        }),
        EntryPoint("ProfileFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_PROFILE)
                .putExtra(KeyUtil.arg_userName, "test-user")
        }),
        EntryPoint("CharacterFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_CHARACTER)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StaffFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_STAFF)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StudioFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, ROUTE_STUDIO)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("CommentRoute", {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://anilist.co/activity/1"))
                .setClass(it, MainActivity::class.java)
        }),
        EntryPoint("ImagePreviewActivity", {
            Intent(it, ImagePreviewActivity::class.java)
                .putExtra(KeyUtil.arg_model, "https://example.com/image.png")
        }),
        EntryPoint("GiphyPreviewActivity", {
            Intent(it, GiphyPreviewActivity::class.java)
                .putExtra(KeyUtil.arg_model, "https://example.com/preview.gif")
        }),
        EntryPoint("VideoPlayerActivity", {
            Intent(it, VideoPlayerActivity::class.java)
                .putExtra(KeyUtil.arg_model, "https://example.com/video.mp4")
        }),
        EntryPoint("SharedContentFragment", {
            Intent(Intent.ACTION_SEND)
                .setClass(it, MainActivity::class.java)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "https://example.com")
        }),
    ) + typedMigratedEntries(context) + externalIngressEntries(context)

    /**
     * Typed [ScreenParam] entry points for the exported preview activities.
     * The in-app destinations consume primitive route extras; only the three
     * preview activities still own typed parameters on the wire.
     */
    private fun typedMigratedEntries(context: Context): List<EntryPoint> = listOf(
        EntryPoint("ImagePreviewActivity-typed", {
            ImagePreviewActivity.newIntent(context, ImagePreviewScreenParam(url = "https://example.com/image.png"))
        }),
        EntryPoint("GiphyPreviewActivity-typed", {
            GiphyPreviewActivity.newIntent(context, GiphyPreviewScreenParam(url = "https://example.com/preview.gif"))
        }),
        EntryPoint("VideoPlayerActivity-typed", {
            VideoPlayerActivity.newIntent(context, VideoPlayerScreenParam(url = "https://example.com/video.mp4"))
        }),
    )

    /**
     * Cold-start fixtures for every manifest-owned AniList URI route. These
     * intentionally exercise MainActivity ingress rather than launching a
     * destination Activity directly.
     */
    private fun externalIngressEntries(context: Context): List<EntryPoint> = listOf(
        EntryPoint("AniListActivityLink", { anilistIntent(context, "/activity/1") }),
        EntryPoint("AniListStudioLink", { anilistIntent(context, "/studio/1") }),
        EntryPoint("AniListCharacterLink", { anilistIntent(context, "/character/1") }),
        EntryPoint("AniListStaffLink", { anilistIntent(context, "/staff/1") }),
        EntryPoint("AniListActorLink", { anilistIntent(context, "/actor/1") }),
        EntryPoint("AniListAnimeLink", { anilistIntent(context, "/anime/1") }),
        EntryPoint("AniListMangaLink", { anilistIntent(context, "/manga/1") }),
        EntryPoint("AniListUserIdLink", { anilistIntent(context, "/user/1") }),
        EntryPoint("AniListUserNameLink", { anilistIntent(context, "/user/test-user") }),
    )

    private fun anilistIntent(
        context: Context,
        path: String,
    ): Intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://anilist.co$path"),
    ).setClass(context, MainActivity::class.java)
}
