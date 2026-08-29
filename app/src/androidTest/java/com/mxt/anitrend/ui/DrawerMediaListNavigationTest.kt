@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Intent
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.navigation.NavigationView
import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.list.MediaListFragment
import com.mxt.anitrend.view.fragment.list.MediaListOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Drawer My Anime / My Manga are the only [MediaListOrigin.ROOT] producers
 * (NFR-002): they land on the media list as the top-level destination, so the
 * first back press shows the root exit-confirm and only the second press
 * finishes the task. This is the root counterpart of the pushed route-ingress
 * tests, which prove caller-back semantics for every other producer.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class DrawerMediaListNavigationTest {

    @Before
    fun setUp() {
        TestSessionUtil.setAuthenticated(
            ApplicationProvider.getApplicationContext(),
            authenticated = true,
        )
    }

    private fun launchAuthenticatedMain(): ActivityScenario<MainActivity> {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java))
    }

    /**
     * Selects a drawer destination through the real NavigationView menu, which
     * dispatches to the activity's OnNavigationItemSelectedListener exactly
     * like a user tap. The drawer is closed explicitly afterwards so the back
     * presses under test exercise the destination back policy, not the drawer
     * close animation.
     */
    @Suppress("DEPRECATION")
    private fun selectDrawerItem(scenario: ActivityScenario<MainActivity>, itemId: Int) {
        scenario.onActivity { activity ->
            activity.findViewById<NavigationView>(R.id.nav_view).menu.performIdentifierAction(itemId, 0)
            activity.findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun navController(activity: MainActivity): androidx.navigation.NavController {
        val host = activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        return host.navController
    }

    @Suppress("DEPRECATION")
    @Test
    fun myAnimeDrawerEntryIsRootOriginAndRetainsRootExitConfirm() {
        launchAuthenticatedMain().use { scenario ->
            selectDrawerItem(scenario, R.id.nav_myanime)
            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.mediaListFragment, controller.currentDestination?.id)
                assertEquals(
                    MediaListOrigin.ROOT.name,
                    controller.currentBackStackEntry?.arguments?.getString(MediaListFragment.ARG_MEDIA_LIST_ORIGIN),
                )
                assertEquals(KeyUtil.ANIME, controller.currentBackStackEntry?.arguments?.getString(KeyUtil.arg_mediaType))
                // Root media list: the first back press shows the exit-confirm
                // and must not finish the task.
                activity.onBackPressed()
                assertFalse("first back from the root media list must not finish the task", activity.isFinishing)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                // The second back press accepts the root exit-confirm.
                activity.onBackPressed()
                assertTrue("second back from the root media list must finish the task", activity.isFinishing)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun myMangaDrawerEntryIsRootOriginAndRetainsRootExitConfirm() {
        launchAuthenticatedMain().use { scenario ->
            selectDrawerItem(scenario, R.id.nav_mymanga)
            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.mediaListFragment, controller.currentDestination?.id)
                assertEquals(
                    MediaListOrigin.ROOT.name,
                    controller.currentBackStackEntry?.arguments?.getString(MediaListFragment.ARG_MEDIA_LIST_ORIGIN),
                )
                assertEquals(KeyUtil.MANGA, controller.currentBackStackEntry?.arguments?.getString(KeyUtil.arg_mediaType))
                activity.onBackPressed()
                assertFalse("first back from the root media list must not finish the task", activity.isFinishing)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                activity.onBackPressed()
                assertTrue("second back from the root media list must finish the task", activity.isFinishing)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }
}
