package com.mxt.anitrend.ui

import android.content.Context
import android.content.Intent
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.GiphyPreviewScreenParam
import com.mxt.anitrend.navigation.model.ImagePreviewScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.navigation.model.VideoPlayerScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.base.AboutActivity
import com.mxt.anitrend.view.activity.base.GiphyPreviewActivity
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity
import com.mxt.anitrend.view.activity.base.LoggingActivity
import com.mxt.anitrend.view.activity.base.SettingsActivity
import com.mxt.anitrend.view.activity.base.SharedContentActivity
import com.mxt.anitrend.view.activity.base.VideoPlayerActivity
import com.mxt.anitrend.view.activity.base.WelcomeActivity
import com.mxt.anitrend.view.activity.detail.CharacterActivity
import com.mxt.anitrend.view.activity.detail.CommentActivity
import com.mxt.anitrend.view.activity.detail.FavouriteActivity
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.MediaBrowseActivity
import com.mxt.anitrend.view.activity.detail.MediaListActivity
import com.mxt.anitrend.view.activity.detail.MessageActivity
import com.mxt.anitrend.view.activity.detail.NotificationActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.activity.detail.StaffActivity
import com.mxt.anitrend.view.activity.detail.StudioActivity
import com.mxt.anitrend.view.activity.index.LoginActivity
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.activity.index.SearchActivity

internal data class EntryPoint(
    val name: String,
    val intentProvider: (Context) -> Intent,
    val assertUi: Boolean = true,
)

internal object EntryPointFixtures {
    fun unauthenticated(context: Context): List<EntryPoint> = listOf(
        // SplashActivity is intentionally excluded from render smoke tests because it immediately routes onward and performs startup side effects rather than exposing a stable UI surface.
        EntryPoint("MainActivity", { Intent(it, MainActivity::class.java) }),
        EntryPoint("LoginActivity", { Intent(it, LoginActivity::class.java) }),
        EntryPoint("SearchActivity", { Intent(it, SearchActivity::class.java) }),
        EntryPoint("SettingsActivity", { Intent(it, SettingsActivity::class.java) }),
        EntryPoint("AboutActivity", { Intent(it, AboutActivity::class.java) }),
        EntryPoint("LoggingActivity", { Intent(it, LoggingActivity::class.java) }),
        EntryPoint("WelcomeActivity", { Intent(it, WelcomeActivity::class.java) }),
        EntryPoint("NotificationActivity", { Intent(it, NotificationActivity::class.java) }),
        EntryPoint("MessageActivity", { Intent(it, MessageActivity::class.java) }),
        EntryPoint("FavouriteActivity", { Intent(it, FavouriteActivity::class.java) }),
        EntryPoint("MediaListActivity", {
            Intent(it, MediaListActivity::class.java)
                .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
        }),
        EntryPoint("MediaBrowseActivity", {
            Intent(it, MediaBrowseActivity::class.java)
                .putExtra(KeyUtil.arg_activity_tag, "Test")
        }),
        EntryPoint("MediaActivity", {
            Intent(it, MediaActivity::class.java)
                .putExtra(KeyUtil.arg_id, 1L)
                .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
        }),
        EntryPoint("ProfileActivity", {
            Intent(it, ProfileActivity::class.java)
                .putExtra(KeyUtil.arg_userName, "test-user")
        }),
        EntryPoint("CharacterActivity", {
            Intent(it, CharacterActivity::class.java)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StaffActivity", {
            Intent(it, StaffActivity::class.java)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StudioActivity", {
            Intent(it, StudioActivity::class.java)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("CommentActivity", {
            Intent(it, CommentActivity::class.java)
                .putExtra(KeyUtil.arg_id, 1L)
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
        EntryPoint("SharedContentActivity", {
            Intent(Intent.ACTION_SEND)
                .setClass(it, SharedContentActivity::class.java)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "https://example.com")
        }),
    ) + typedMigratedEntries(context)

    fun authenticated(context: Context): List<EntryPoint> = listOf(
        // SplashActivity is intentionally excluded from render smoke tests because it immediately routes onward and performs startup side effects rather than exposing a stable UI surface.
        EntryPoint("MainActivity", { Intent(it, MainActivity::class.java) }),
        EntryPoint("LoginActivity", { Intent(it, LoginActivity::class.java) }, assertUi = false),
        EntryPoint("SearchActivity", { Intent(it, SearchActivity::class.java) }),
        EntryPoint("SettingsActivity", { Intent(it, SettingsActivity::class.java) }),
        EntryPoint("AboutActivity", { Intent(it, AboutActivity::class.java) }),
        EntryPoint("LoggingActivity", { Intent(it, LoggingActivity::class.java) }),
        EntryPoint("WelcomeActivity", { Intent(it, WelcomeActivity::class.java) }),
        EntryPoint("NotificationActivity", { Intent(it, NotificationActivity::class.java) }),
        EntryPoint("MessageActivity", { Intent(it, MessageActivity::class.java) }),
        EntryPoint("FavouriteActivity", { Intent(it, FavouriteActivity::class.java) }),
        EntryPoint("MediaListActivity", {
            Intent(it, MediaListActivity::class.java)
                .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
        }),
        EntryPoint("MediaBrowseActivity", {
            Intent(it, MediaBrowseActivity::class.java)
                .putExtra(KeyUtil.arg_activity_tag, "Test")
        }),
        EntryPoint("MediaActivity", {
            Intent(it, MediaActivity::class.java)
                .putExtra(KeyUtil.arg_id, 1L)
                .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
        }),
        EntryPoint("ProfileActivity", {
            Intent(it, ProfileActivity::class.java)
                .putExtra(KeyUtil.arg_userName, "test-user")
        }),
        EntryPoint("CharacterActivity", {
            Intent(it, CharacterActivity::class.java)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StaffActivity", {
            Intent(it, StaffActivity::class.java)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("StudioActivity", {
            Intent(it, StudioActivity::class.java)
                .putExtra(KeyUtil.arg_id, 1L)
        }),
        EntryPoint("CommentActivity", {
            Intent(it, CommentActivity::class.java)
                .putExtra(KeyUtil.arg_id, 1L)
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
        EntryPoint("SharedContentActivity", {
            Intent(Intent.ACTION_SEND)
                .setClass(it, SharedContentActivity::class.java)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "https://example.com")
        }),
    ) + typedMigratedEntries(context)

    /**
     * Typed [ScreenParam] entry points for the Phase 1 migrated destinations.
     * The legacy-extra entries above stay in place to prove the fromIntent bridge;
     * these prove the typed navigation path end to end. MediaBrowseActivity is
     * intentionally absent: it has no destination identity (its title and filters
     * stay on the legacy extras until Phase 2).
     */
    private fun typedMigratedEntries(context: Context): List<EntryPoint> = listOf(
        EntryPoint("ProfileActivity-typed", {
            ProfileActivity.newIntent(context, UserScreenParam(userId = 1L, initialName = "test-user"))
        }),
        EntryPoint("CharacterActivity-typed", {
            CharacterActivity.newIntent(context, CharacterScreenParam(characterId = 1L))
        }),
        EntryPoint("StaffActivity-typed", {
            StaffActivity.newIntent(context, StaffScreenParam(staffId = 1L))
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
}
