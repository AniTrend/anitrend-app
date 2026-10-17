@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Intent
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.navigation.NavigationBarView
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
 * Primary My Anime / My Manga are the only [MediaListOrigin.ROOT] producers
 * (NFR-002): they land on the media list as the top-level destination, so the
 * first back press shows the root exit-confirm and only the second press
 * finishes the task. This is the root counterpart of the pushed route-ingress
 * tests, which prove caller-back semantics for every other producer.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class PrimaryMediaListNavigationTest {

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
     * Selects a primary destination through the real NavigationBarView
     * selection, which dispatches to the activity's shared production
     * selection handler exactly like a user tap.
     */
    private fun selectPrimaryItem(scenario: ActivityScenario<MainActivity>, itemId: Int) {
        scenario.onActivity { activity ->
            activity.findViewById<NavigationBarView>(R.id.primary_navigation).setSelectedItemId(itemId)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun navController(activity: MainActivity): androidx.navigation.NavController {
        val host = activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        return host.navController
    }

    @Suppress("DEPRECATION")
    @Test
    fun myAnimePrimaryEntryIsRootOriginAndRetainsRootExitConfirm() {
        launchAuthenticatedMain().use { scenario ->
            selectPrimaryItem(scenario, R.id.nav_myanime)
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
    fun myMangaPrimaryEntryIsRootOriginAndRetainsRootExitConfirm() {
        launchAuthenticatedMain().use { scenario ->
            selectPrimaryItem(scenario, R.id.nav_mymanga)
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
