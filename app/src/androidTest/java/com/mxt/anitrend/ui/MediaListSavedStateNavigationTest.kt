@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.mxt.anitrend.R
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.index.MainActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Library's Anime and Manga choices are in-screen presets rather than drawer
 * destinations. This verifies that the selected preset remains visible after
 * switching to Social and restoring the saved Library root.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class MediaListSavedStateNavigationTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = true)
        Settings(context).edit {
            putString(context.getString(R.string.pref_key_startup_page), "0")
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun libraryPresetsRemainSelectedAcrossSocialRootSwitches() {
        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            val menuLabel = mainActivity.getString(R.string.action_menu)
            val libraryLabel = mainActivity.getString(R.string.drawer_title_library)
            val socialLabel = mainActivity.getString(R.string.drawer_title_home)
            val myAnimeLabel = mainActivity.getString(R.string.library_preset_my_anime)
            val myMangaLabel = mainActivity.getString(R.string.library_preset_my_manga)

            // The shell menu affordance is an icon with a content description.
            onNodeWithContentDescription(menuLabel).performClick()
            waitForIdle()
            onNodeWithText(libraryLabel).performClick()
            waitForIdle()

            onNodeWithText(myAnimeLabel).assertIsSelected()
            onNodeWithText(myMangaLabel).assertIsNotSelected()
            onNodeWithText(myMangaLabel).performClick()
            waitForIdle()
            onNodeWithText(myMangaLabel).assertIsSelected()

            onAllNodesWithContentDescription(menuLabel)[0].performClick()
            waitForIdle()
            onNodeWithText(socialLabel).performClick()
            waitForIdle()
            onNodeWithContentDescription(menuLabel).performClick()
            waitForIdle()
            onNodeWithText(libraryLabel).performClick()
            waitForIdle()

            onNodeWithText(myMangaLabel).assertIsSelected()
            onNodeWithText(myAnimeLabel).assertIsNotSelected()
            onNodeWithText(myAnimeLabel).performClick()
            waitForIdle()

            onAllNodesWithContentDescription(menuLabel)[0].performClick()
            waitForIdle()
            onNodeWithText(socialLabel).performClick()
            waitForIdle()
            onNodeWithContentDescription(menuLabel).performClick()
            waitForIdle()
            onNodeWithText(libraryLabel).performClick()
            waitForIdle()

            onNodeWithText(myAnimeLabel).assertIsSelected()
            onNodeWithText(myMangaLabel).assertIsNotSelected()
        }
    }
}
