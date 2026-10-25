package com.mxt.anitrend.ui

import android.content.Intent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.chip.ChipGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.view.activity.index.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Live smoke for the episodes feed screen. The airing screen's feed chip navigates
 * to WatchListFragment, whose rows now render through the Compose EpisodeList host.
 * The shared empty/error state is a View-hosted ProgressLayout surface, so it is
 * asserted with Espresso while rows are asserted through Compose semantics.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class WatchListComposeTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun watchListFeedRendersRowsOrSharedSettledState() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_AIRING)
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            // AiringFragment's feed chip is the second chip of its section selector.
            scenario.onActivity { activity ->
                val sectionSelector = activity.findViewById<ChipGroup>(R.id.media_section_selector)
                sectionSelector.getChildAt(1).performClick()
            }
            composeRule.waitForIdle()
            composeRule.waitUntil(30_000L) {
                val rows = composeRule.onAllNodesWithTag(EPISODE_IMAGE_TAG, useUnmergedTree = true)
                    .fetchSemanticsNodes()
                rows.isNotEmpty() || sharedStateVisible()
            }
        }
    }

    /**
     * The shared empty/error state is a View-hosted ProgressLayout surface outside
     * Compose semantics, so it settles through Espresso checks.
     */
    private fun sharedStateVisible(): Boolean = try {
        onView(withText(R.string.layout_empty_response)).check(matches(isDisplayed()))
        true
    } catch (emptyMissed: Throwable) {
        try {
            onView(withText(R.string.button_try_again)).check(matches(isDisplayed()))
            true
        } catch (errorMissed: Throwable) {
            false
        }
    }

    private companion object {
        const val EPISODE_IMAGE_TAG = "episode-row-image"
    }
}
