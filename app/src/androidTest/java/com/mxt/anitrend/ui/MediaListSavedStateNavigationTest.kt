@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.list.MediaListFragment
import com.mxt.anitrend.view.fragment.list.MediaListOrigin
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * NFR-007 regression test: drawer My Anime -> another root destination -> My
 * Manga (and the reverse). Every drawer root route uses
 * [com.mxt.anitrend.navigation.extension.NavigationDestinations] root options
 * (launchSingleTop + popUpTo animeFragment, inclusive = false, saveState =
 * true). The hazard under test: the second root media-list navigation can
 * restore the back stack entry saved at pop time, which carries the previous
 * media type instead of the newly selected one.
 *
 * The assertions deliberately expect the correct contract (the selected drawer
 * media type must win). The NFR-007 remediation makes the ROOT media-list
 * path use media-list-specific options with `restoreState(false)`, so the
 * restored stale entry is never applied and the fresh media type arguments
 * always win. These tests prove destination, ROOT origin, and selected media
 * type survive the restore-time switch in both directions.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class MediaListSavedStateNavigationTest {
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
        val context = ApplicationProvider.getApplicationContext<Context>()
        return ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java))
    }

    /**
     * Opens the production DrawerLayout and selects a visible drawer label
     * through Compose semantics, as a user does.
     */
    private fun selectDrawerItem(scenario: ActivityScenario<MainActivity>, itemId: Int) {
        val labelId = when (itemId) {
            R.id.nav_home_feed -> R.string.drawer_title_home
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

    private fun navController(activity: MainActivity): NavController {
        val host = activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        return host.navController
    }

    private fun assertMediaListEntry(
        controller: NavController,
        expectedMediaType: String,
        step: String,
    ) {
        assertEquals(
            "$step: current destination must be the media list",
            R.id.mediaListFragment,
            controller.currentDestination?.id,
        )
        assertEquals(
            "$step: media list origin must stay ROOT for the drawer producer",
            MediaListOrigin.ROOT.name,
            controller.currentBackStackEntry?.arguments?.getString(MediaListFragment.ARG_MEDIA_LIST_ORIGIN),
        )
        assertEquals(
            "$step: NFR-007 restored media list entry must carry the selected drawer media type",
            expectedMediaType,
            controller.currentBackStackEntry?.arguments?.getString(KeyUtil.arg_mediaType),
        )
    }

    @Suppress("DEPRECATION")
    @Test
    fun myAnimeThenRootSwitchThenMyMangaRestoresSelectedMediaType() {
        launchAuthenticatedMain().use { scenario ->
            selectDrawerItem(scenario, R.id.nav_myanime)
            scenario.onActivity { activity ->
                assertMediaListEntry(navController(activity), KeyUtil.ANIME, "after My Anime")
            }

            selectDrawerItem(scenario, R.id.nav_home_feed)
            scenario.onActivity { activity ->
                assertEquals(
                    "after Feed, the current destination must be the feed root",
                    R.id.feedFragment,
                    navController(activity).currentDestination?.id,
                )
            }

            selectDrawerItem(scenario, R.id.nav_mymanga)
            scenario.onActivity { activity ->
                assertMediaListEntry(navController(activity), KeyUtil.MANGA, "after My Manga via restore")
            }
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun myMangaThenRootSwitchThenMyAnimeRestoresSelectedMediaType() {
        launchAuthenticatedMain().use { scenario ->
            selectDrawerItem(scenario, R.id.nav_mymanga)
            scenario.onActivity { activity ->
                assertMediaListEntry(navController(activity), KeyUtil.MANGA, "after My Manga")
            }

            selectDrawerItem(scenario, R.id.nav_home_feed)
            scenario.onActivity { activity ->
                assertEquals(
                    "after Feed, the current destination must be the feed root",
                    R.id.feedFragment,
                    navController(activity).currentDestination?.id,
                )
            }

            selectDrawerItem(scenario, R.id.nav_myanime)
            scenario.onActivity { activity ->
                assertMediaListEntry(navController(activity), KeyUtil.ANIME, "after My Anime via restore")
            }
        }
    }
}
