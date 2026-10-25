@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.list.MediaListFragment
import com.mxt.anitrend.view.fragment.list.MediaListOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
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
    @get:Rule
    val composeRule = createEmptyComposeRule()

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
     * Opens the production DrawerLayout and selects a visible drawer label
     * through Compose semantics, as a user does.
     */
    private fun selectDrawerItem(scenario: ActivityScenario<MainActivity>, itemId: Int) {
        val labelId = when (itemId) {
            R.id.nav_myanime -> R.string.drawer_title_myanime
            R.id.nav_mymanga -> R.string.drawer_title_mymanga
            else -> error("Unsupported drawer item id: $itemId")
        }
        var label = ""
        scenario.onActivity { activity ->
            activity.findViewById<DrawerLayout>(R.id.drawer_layout)
                .openDrawer(GravityCompat.START, false)
            label = activity.getString(labelId)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(label).assertIsDisplayed().performClick()
        composeRule.waitUntil(5_000L) {
            var isDrawerOpen = true
            scenario.onActivity { activity ->
                isDrawerOpen = activity.findViewById<DrawerLayout>(R.id.drawer_layout)
                    .isDrawerOpen(GravityCompat.START)
            }
            !isDrawerOpen
        }
    }

    @Test
    fun signInDrawerItemLeavesDrawerOpen() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        try {
            ActivityScenario
                .launch<MainActivity>(Intent(context, MainActivity::class.java))
                .use { scenario ->
                    var label = ""
                    scenario.onActivity { activity ->
                        activity.findViewById<DrawerLayout>(R.id.drawer_layout)
                            .openDrawer(GravityCompat.START, false)
                        label = activity.getString(R.string.drawer_title_sign_in)
                    }
                    composeRule.waitForIdle()
                    composeRule.onNodeWithText(label).assertIsDisplayed().performClick()
                    composeRule.waitForIdle()
                    scenario.onActivity { activity ->
                        assertTrue(
                            "sign-in navigation must leave the drawer open",
                            activity.findViewById<DrawerLayout>(R.id.drawer_layout)
                                .isDrawerOpen(GravityCompat.START),
                        )
                    }
                }
        } finally {
            TestSessionUtil.setAuthenticated(context, authenticated = true)
        }
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
