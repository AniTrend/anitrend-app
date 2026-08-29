package com.mxt.anitrend.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.GiphyPreviewScreenParam
import com.mxt.anitrend.navigation.model.ImagePreviewScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.navigation.model.VideoPlayerScreenParam
import com.mxt.anitrend.navigation.extension.putScreenParam
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
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_FEED)
        }),
        EntryPoint("AiringFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_AIRING)
        }),
        EntryPoint("TrendingFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_TRENDING)
        }),
        EntryPoint("LoginActivity", { Intent(it, LoginActivity::class.java) }),
        EntryPoint("SearchFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_SEARCH)
                .putExtra(KeyUtil.arg_search, "test")
        }),
        EntryPoint("LoggingFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_LOGGING)
        }),
        EntryPoint("WelcomeActivity", { Intent(it, WelcomeActivity::class.java) }),
        EntryPoint("FavouriteFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_FAVOURITES)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("MediaListFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_MEDIA_LIST)
                .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                .putExtra(KeyUtil.arg_userName, "test-user")
        }),
        EntryPoint("MediaFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_MEDIA)
                .putScreenParam(MediaScreenParam(1L, KeyUtil.ANIME))
        }),
        EntryPoint("ProfileFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_PROFILE)
                .putExtra(KeyUtil.arg_userName, "test-user")
        }),
        EntryPoint("CharacterFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_CHARACTER)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StaffFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_STAFF)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StudioFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_STUDIO)
                .putScreenParam(StudioScreenParam(1L))
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
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_FEED)
        }),
        EntryPoint("AiringFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_AIRING)
        }),
        EntryPoint("TrendingFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_TRENDING)
        }),
        EntryPoint("LoginActivity", { Intent(it, LoginActivity::class.java) }, assertUi = false),
        EntryPoint("SearchFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_SEARCH)
                .putExtra(KeyUtil.arg_search, "test")
        }),
        EntryPoint("LoggingFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_LOGGING)
        }),
        EntryPoint("WelcomeActivity", { Intent(it, WelcomeActivity::class.java) }),
        EntryPoint("FavouriteFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_FAVOURITES)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("MediaListFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_MEDIA_LIST)
                .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                .putExtra(KeyUtil.arg_userName, "test-user")
        }),
        EntryPoint("MediaFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_MEDIA)
                .putScreenParam(MediaScreenParam(1L, KeyUtil.ANIME))
        }),
        EntryPoint("ProfileFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_PROFILE)
                .putExtra(KeyUtil.arg_userName, "test-user")
        }),
        EntryPoint("CharacterFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_CHARACTER)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StaffFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_STAFF)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StudioFragment", {
            Intent(it, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_STUDIO)
                .putScreenParam(StudioScreenParam(1L))
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
     * Typed [ScreenParam] entry points for the Phase 1 migrated destinations.
     * The legacy-extra entries above stay in place to prove the fromIntent bridge;
     * these prove the typed navigation path end to end. Media browsing is
     * intentionally absent: it has no destination identity (its title and filters
     * stay on the legacy extras until Phase 2).
     */
    private fun typedMigratedEntries(context: Context): List<EntryPoint> = listOf(
        EntryPoint("ProfileFragment-typed", {
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_PROFILE)
                .putScreenParam(UserScreenParam(userId = 1L, initialName = "test-user"))
        }),
        EntryPoint("CharacterFragment-typed", {
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_CHARACTER)
                .putScreenParam(CharacterScreenParam(characterId = 1L))
        }),
        EntryPoint("StaffFragment-typed", {
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_STAFF)
                .putScreenParam(StaffScreenParam(staffId = 1L))
        }),
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

    private fun anilistIntent(context: Context, path: String): Intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://anilist.co$path"),
    ).setClass(context, MainActivity::class.java)
}
