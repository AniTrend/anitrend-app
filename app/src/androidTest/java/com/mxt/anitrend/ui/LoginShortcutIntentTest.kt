@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.navigation.AppNavKey
import com.mxt.anitrend.navigation.EXTRA_ROUTE
import com.mxt.anitrend.navigation.ROUTE_MEDIA_LIST
import com.mxt.anitrend.navigation.resolveExternalEntry
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.ShortcutUtil
import com.mxt.anitrend.view.activity.index.LoginActivity
import com.mxt.anitrend.view.activity.index.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Post-login dynamic shortcut contract (NFR-004): the produced shortcut
 * intents carry identity as PersistableBundle-safe wire primitives (the
 * platform rejects Parcelable extras in ShortcutInfo intents since API 26),
 * the [EXTRA_ROUTE] wire value, and the legacy shortcut marker. A cold launch
 * resolves to a pushed Profile → UserMediaList chain, keeping the list above
 * its caller rather than using it as a start destination.
 */
@RunWith(AndroidJUnit4::class)
class LoginShortcutIntentTest {

    @Before
    fun setUp() {
        TestSessionUtil.setAuthenticated(
            ApplicationProvider.getApplicationContext(),
            authenticated = false,
        )
    }

    private fun producedShortcutIntent(
        shortcutType: Int,
        mediaType: String,
    ): Intent {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val user = User().apply {
            id = 123L
            name = "raki"
        }
        // A produced shortcut always carries an intent; the platform API types
        // it nullable, so the non-null assertion documents the invariant.
        return ShortcutUtil.buildShortcutInfo(
            context,
            ShortcutUtil.ShortcutBuilder()
                .setShortcutType(shortcutType)
                .setShortcutParams(LoginActivity.userShortcutBundle(user, mediaType))
                .build(),
        )!!.intent!!
    }

    private fun assertShortcutIntentIdentityAndRoute(intent: Intent, mediaType: String, shortcutType: Int) {
        // The primitive owner/list extras and ROUTE_MEDIA_LIST form the
        // PersistableBundle-safe shortcut contract.
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != 0)
        assertEquals(ROUTE_MEDIA_LIST, intent.getStringExtra(EXTRA_ROUTE))
        assertEquals(mediaType, intent.getStringExtra(KeyUtil.arg_mediaType))
        assertEquals(123L, intent.getLongExtra(KeyUtil.arg_id, 0L))
        assertEquals("raki", intent.getStringExtra(KeyUtil.arg_userName))
        assertEquals(shortcutType, intent.getIntExtra(KeyUtil.arg_shortcut_used, -1))
    }

    private fun assertColdLaunchResolvesToPushedUserList(intent: Intent, mediaType: String) {
        // Launch the produced shortcut intent as a task root, then exercise
        // the same retained resolver used by MainActivity's ingress path.
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val launched = instrumentation.startActivitySync(
            Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        ) as MainActivity
        instrumentation.waitForIdleSync()

        val entry = requireNotNull(
            resolveExternalEntry(
                intent = intent,
                isTaskRoot = true,
                settings = Settings(launched),
            ),
        )
        assertEquals(null, entry.root)
        assertEquals(
            listOf(
                AppNavKey.Profile(userId = 123L, userName = "raki"),
                AppNavKey.UserMediaList(userId = 123L, userName = "raki", mediaType = mediaType),
            ),
            entry.keys,
        )
        assertEquals(false, entry.finishOnBack)

        launched.finish()
        instrumentation.waitForIdleSync()
    }

    @Test
    fun myAnimeShortcutIntentCarriesPrimitiveIdentityRouteAndPushedOwnerList() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
        val intent = producedShortcutIntent(KeyUtil.SHORTCUT_MY_ANIME, KeyUtil.ANIME)
        assertShortcutIntentIdentityAndRoute(intent, KeyUtil.ANIME, KeyUtil.SHORTCUT_MY_ANIME)
        assertColdLaunchResolvesToPushedUserList(intent, KeyUtil.ANIME)
    }

    @Test
    fun myMangaShortcutIntentCarriesPrimitiveIdentityRouteAndPushedOwnerList() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
        val intent = producedShortcutIntent(KeyUtil.SHORTCUT_MY_MANGA, KeyUtil.MANGA)
        assertShortcutIntentIdentityAndRoute(intent, KeyUtil.MANGA, KeyUtil.SHORTCUT_MY_MANGA)
        assertColdLaunchResolvesToPushedUserList(intent, KeyUtil.MANGA)
    }
}
