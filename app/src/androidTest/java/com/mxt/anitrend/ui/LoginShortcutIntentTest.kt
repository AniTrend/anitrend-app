@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.ShortcutUtil
import com.mxt.anitrend.view.activity.index.LoginActivity
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.list.MediaListFragment
import com.mxt.anitrend.view.fragment.list.MediaListOrigin
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
 * the [MainActivity.EXTRA_ROUTE] wire value, and the legacy shortcut marker.
 * A cold launch of a produced intent lands on the pushed media list with the
 * host-reconstructed typed [UserScreenParam] on the destination arguments and
 * caller-back semantics (NFR-002). The bundles come from the same production
 * seam LoginActivity uses after a successful login.
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
        // Wire identity: the host's ROUTE_MEDIA_LIST branch falls back from the
        // typed parameter to these primitives and reconstructs the typed
        // UserScreenParam, so the produced intent carries identity, not just a
        // route. Extras must stay PersistableBundle-safe for ShortcutInfo.
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != 0)
        assertEquals(MainActivity.ROUTE_MEDIA_LIST, intent.getStringExtra(MainActivity.EXTRA_ROUTE))
        assertEquals(mediaType, intent.getStringExtra(KeyUtil.arg_mediaType))
        assertEquals(123L, intent.getLongExtra(KeyUtil.arg_id, 0L))
        assertEquals("raki", intent.getStringExtra(KeyUtil.arg_userName))
        assertEquals(shortcutType, intent.getIntExtra(KeyUtil.arg_shortcut_used, -1))
    }

    private fun assertColdLaunchLandsOnPushedTypedList(intent: Intent, mediaType: String) {
        // Cold launch of the produced shortcut intent: the media-list route
        // pushes by default (NFR-002), the typed identity is reconstructed on
        // the destination arguments, and back returns to the caller beneath
        // the list. Launched through the instrumentation directly instead of
        // ActivityScenario: the produced intent's CLEAR_TASK dispatch makes
        // each launch replace the previous activity, and the scenario's fixed
        // destroy timeout cannot stall the test on slow emulators.
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val launched = instrumentation.startActivitySync(
            Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        ) as MainActivity
        instrumentation.waitForIdleSync()
        val host = launched.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        val controller = host.navController
        assertEquals(R.id.mediaListFragment, controller.currentDestination?.id)
        val listArgs = controller.currentBackStackEntry?.arguments
        assertEquals(
            MediaListOrigin.PUSHED.name,
            listArgs?.getString(MediaListFragment.ARG_MEDIA_LIST_ORIGIN),
        )
        assertEquals(mediaType, listArgs?.getString(KeyUtil.arg_mediaType))
        assertEquals(
            UserScreenParam(userId = 123L, initialName = "raki"),
            listArgs?.screenParam<UserScreenParam>(),
        )
        assertEquals(123L, listArgs?.getLong(KeyUtil.arg_id))
        launched.finish()
        instrumentation.waitForIdleSync()
    }

    @Test
    fun myAnimeShortcutIntentCarriesTypedIdentityRouteAndPushedOrigin() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
        val intent = producedShortcutIntent(KeyUtil.SHORTCUT_MY_ANIME, KeyUtil.ANIME)
        assertShortcutIntentIdentityAndRoute(intent, KeyUtil.ANIME, KeyUtil.SHORTCUT_MY_ANIME)
        assertColdLaunchLandsOnPushedTypedList(intent, KeyUtil.ANIME)
    }

    @Test
    fun myMangaShortcutIntentCarriesTypedIdentityRouteAndPushedOrigin() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
        val intent = producedShortcutIntent(KeyUtil.SHORTCUT_MY_MANGA, KeyUtil.MANGA)
        assertShortcutIntentIdentityAndRoute(intent, KeyUtil.MANGA, KeyUtil.SHORTCUT_MY_MANGA)
        assertColdLaunchLandsOnPushedTypedList(intent, KeyUtil.MANGA)
    }
}
