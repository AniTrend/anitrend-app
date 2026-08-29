package com.mxt.anitrend.ui

import android.content.Intent
import android.net.Uri
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.list.MediaListFragment
import com.mxt.anitrend.view.fragment.list.MediaListOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityExternalIngressTest {

    @Before
    fun setUp() {
        TestSessionUtil.setAuthenticated(
            ApplicationProvider.getApplicationContext(),
            authenticated = false,
        )
    }

    private fun anilistIntent(context: android.content.Context, path: String): Intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://anilist.co$path"),
    ).setClass(context, MainActivity::class.java)

    @Test
    fun warmSingleTopIntentRoutesThroughTheExistingRootHost() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = Intent(context, MainActivity::class.java)
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity ->
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://anilist.co/anime/1"),
                ).setClass(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                // Seam note: ActivityScenario cannot deliver a warm singleTop
                // intent through the real onNewIntent dispatch, so the harness
                // invokes the production override directly. The activity is a
                // task root, which is exactly the singleTop delivery state the
                // platform would produce.
                activity.javaClass.getDeclaredMethod("onNewIntent", Intent::class.java).apply {
                    isAccessible = true
                }.invoke(activity, intent)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                val host = activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
                assertEquals(R.id.mediaFragment, host.navController.currentDestination?.id)
                activity.setIntent(launchIntent)
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    @Test
    fun warmShareIntentRoutesThroughTheSharedContentDestination() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = Intent(context, MainActivity::class.java)
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity ->
                val intent = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, "https://example.com")
                // Seam note: same harness limitation as the singleTop test
                // above; ActivityScenario cannot deliver a warm ACTION_SEND
                // intent through the platform dispatch, so the production
                // override is invoked directly on the task-root activity.
                activity.javaClass.getDeclaredMethod("onNewIntent", Intent::class.java).apply {
                    isAccessible = true
                }.invoke(activity, intent)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                val host = activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
                assertEquals(R.id.sharedContentFragment, host.navController.currentDestination?.id)
                activity.setIntent(launchIntent)
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    // ── NFR-001: /user/<name>/animelist and /mangalist land on Profile then push a typed media list ──

    @Test
    fun coldStartUserAnimeListLinkLandsOnProfileThenPushesTypedMediaList() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = anilistIntent(context, "/user/raki/animelist")
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.mediaListFragment, controller.currentDestination?.id)
                // Profile is preserved beneath the typed list.
                assertEquals(R.id.profileFragment, controller.previousBackStackEntry?.destination?.id)
                // The pushed list carries the media type from the deep link and
                // the pushed origin contract.
                assertEquals(KeyUtil.ANIME, controller.currentBackStackEntry?.arguments?.getString(KeyUtil.arg_mediaType))
                assertEquals(
                    MediaListOrigin.PUSHED.name,
                    controller.currentBackStackEntry?.arguments?.getString(MediaListFragment.ARG_MEDIA_LIST_ORIGIN),
                )
                activity.setIntent(launchIntent)
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    @Test
    fun coldStartUserMangaListLinkPushesTypedMediaList() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = anilistIntent(context, "/user/raki/mangalist")
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.mediaListFragment, controller.currentDestination?.id)
                assertEquals(R.id.profileFragment, controller.previousBackStackEntry?.destination?.id)
                assertEquals(KeyUtil.MANGA, controller.currentBackStackEntry?.arguments?.getString(KeyUtil.arg_mediaType))
                activity.setIntent(launchIntent)
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    @Test
    fun coldStartPlainUserLinkLandsOnProfileOnly() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = anilistIntent(context, "/user/raki")
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.profileFragment, controller.currentDestination?.id)
                assertEquals(R.id.animeFragment, controller.previousBackStackEntry?.destination?.id)
                activity.setIntent(launchIntent)
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    // ── Numeric user identity survives the optional list suffix (NFR-003) ──

    @Test
    fun coldStartNumericUserAnimeListLinkPreservesNumericUserId() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = anilistIntent(context, "/user/123/animelist")
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.mediaListFragment, controller.currentDestination?.id)
                assertEquals(R.id.profileFragment, controller.previousBackStackEntry?.destination?.id)
                val profileArgs = controller.previousBackStackEntry?.arguments
                // The first user path segment is numeric, so it is a user id,
                // not a username, even though the animelist suffix follows.
                val profileParam = profileArgs?.screenParam<UserScreenParam>()
                assertEquals(123L, profileParam?.userId)
                assertNull(profileParam?.initialName)
                assertEquals(123L, profileArgs?.getLong(KeyUtil.arg_id))
                val listArgs = controller.currentBackStackEntry?.arguments
                assertEquals(KeyUtil.ANIME, listArgs?.getString(KeyUtil.arg_mediaType))
                assertEquals(123L, listArgs?.screenParam<UserScreenParam>()?.userId)
                activity.setIntent(launchIntent)
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    @Test
    fun coldStartNumericUserMangaListLinkPreservesNumericUserId() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = anilistIntent(context, "/user/123/mangalist")
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.mediaListFragment, controller.currentDestination?.id)
                assertEquals(R.id.profileFragment, controller.previousBackStackEntry?.destination?.id)
                val profileArgs = controller.previousBackStackEntry?.arguments
                val profileParam = profileArgs?.screenParam<UserScreenParam>()
                assertEquals(123L, profileParam?.userId)
                assertNull(profileParam?.initialName)
                assertEquals(123L, profileArgs?.getLong(KeyUtil.arg_id))
                val listArgs = controller.currentBackStackEntry?.arguments
                assertEquals(KeyUtil.MANGA, listArgs?.getString(KeyUtil.arg_mediaType))
                assertEquals(123L, listArgs?.screenParam<UserScreenParam>()?.userId)
                activity.setIntent(launchIntent)
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    // ── NFR-003: external user-list → Profile → MediaList back chain ──

    @Suppress("DEPRECATION")
    @Test
    fun externalUserListBackChainReturnsToProfileThenRootWithoutFinishingTask() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = anilistIntent(context, "/user/raki/animelist")
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                assertEquals(R.id.mediaListFragment, navController(activity).currentDestination?.id)
                // Internal navigation into the media list cleared the external
                // finish semantics, so back returns to the caller (Profile).
                activity.onBackPressed()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                assertEquals(R.id.profileFragment, navController(activity).currentDestination?.id)
                assertFalse("back from MediaList must not finish the task", activity.isFinishing)
                // A further back leaves the pushed chain and reaches the root
                // destination; the task is still not finished.
                activity.onBackPressed()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                assertEquals(R.id.animeFragment, navController(activity).currentDestination?.id)
                assertFalse("back from Profile must not finish the task", activity.isFinishing)
                activity.setIntent(launchIntent)
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    // ── NFR-003: direct external ingress keeps finish-on-back semantics ──

    @Suppress("DEPRECATION")
    @Test
    fun coldStartAnimeLinkBackFinishesExternalTask() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = anilistIntent(context, "/anime/1")
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.mediaFragment, controller.currentDestination?.id)
                // A direct external ingress dispatches exactly one route (the
                // initial ingress), so back from the ingress destination
                // finishes the external task instead of navigating up.
                activity.onBackPressed()
                assertTrue("back from the external anime link must finish the task", activity.isFinishing)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun coldStartActivityLinkBackFinishesExternalTask() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = anilistIntent(context, "/activity/1")
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.commentFragment, controller.currentDestination?.id)
                activity.onBackPressed()
                assertTrue("back from the external activity link must finish the task", activity.isFinishing)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    // ── NFR-002: ROUTE_MEDIA_LIST ingress (shortcuts and route extras) is pushed by default ──

    @Suppress("DEPRECATION")
    @Test
    fun coldStartMediaListRouteEntryIsPushedAndBacksUpWithoutFinishing() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_MEDIA_LIST)
            .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)
            .putExtra(KeyUtil.arg_userName, "raki")
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.mediaListFragment, controller.currentDestination?.id)
                // The post-login shortcut continuation inherits this contract:
                // the media-list route never re-routes as root.
                assertEquals(
                    MediaListOrigin.PUSHED.name,
                    controller.currentBackStackEntry?.arguments?.getString(MediaListFragment.ARG_MEDIA_LIST_ORIGIN),
                )
                activity.onBackPressed()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                assertEquals(R.id.animeFragment, navController(activity).currentDestination?.id)
                assertFalse("pushed media list back must not finish the task", activity.isFinishing)
                activity.setIntent(launchIntent)
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    private fun navController(activity: MainActivity): androidx.navigation.NavController {
        val host = activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        return host.navController
    }
}
