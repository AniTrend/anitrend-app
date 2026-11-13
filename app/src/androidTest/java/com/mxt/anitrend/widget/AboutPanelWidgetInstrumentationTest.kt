@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.AboutPanelWidget
import com.mxt.anitrend.base.custom.view.widget.AboutPanelWidget.StatState
import com.mxt.anitrend.view.sheet.BottomSheetListUsers
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Click wiring for the profile about panel ([AboutPanelWidget]): a loaded stat count
 * (including zero) opens the normal list sheet, while not-yet-loaded and failed counts
 * keep the loading toast and must not open anything. The per-state click resolution
 * itself is unit-tested by AboutPanelWidgetStatStateTest; these tests verify the
 * container clicks drive the fragment-manager sheet.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class AboutPanelWidgetInstrumentationTest {

    @Test
    fun loadedZeroFollowers_opensListSheet() {
        assertListSheet(
            labelResource = R.string.followers,
            followers = StatState.Loaded(0),
            following = StatState.NotLoaded,
            shouldOpenSheet = true,
        )
    }

    @Test
    fun loadedNonzeroFollowers_opensListSheet() {
        assertListSheet(
            labelResource = R.string.followers,
            followers = StatState.Loaded(4),
            following = StatState.NotLoaded,
            shouldOpenSheet = true,
        )
    }

    @Test
    fun loadedZeroFollowing_opensListSheet() {
        assertListSheet(
            labelResource = R.string.following,
            followers = StatState.NotLoaded,
            following = StatState.Loaded(0),
            shouldOpenSheet = true,
        )
    }

    @Test
    fun notLoadedFollowers_keepsLoadingToastWithoutSheet() {
        assertListSheet(
            labelResource = R.string.followers,
            followers = StatState.NotLoaded,
            following = StatState.NotLoaded,
            shouldOpenSheet = false,
        )
    }

    @Test
    fun failedFollowers_keepsLoadingToastWithoutSheet() {
        assertListSheet(
            labelResource = R.string.followers,
            followers = StatState.Failed,
            following = StatState.NotLoaded,
            shouldOpenSheet = false,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    private fun assertListSheet(
        labelResource: Int,
        followers: StatState,
        following: StatState,
        shouldOpenSheet: Boolean,
    ) {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                val widget = AboutPanelWidget(hostActivity).apply {
                    setFragmentActivity(hostActivity)
                    setUserId(7L)
                    setStats(
                        followers = followers,
                        following = following,
                        favourites = StatState.NotLoaded,
                    )
                }
                installAboutPanelWidgetForComposeTest(hostActivity, widget)
            }
            waitForIdle()

            onNodeWithContentDescription(hostActivity.getString(labelResource)).performClick()
            runOnUiThread { hostActivity.supportFragmentManager.executePendingTransactions() }
            runOnIdle {
                assertEquals(
                    shouldOpenSheet,
                    hostActivity.supportFragmentManager.fragments.any { it is BottomSheetListUsers },
                )
            }
        }
    }
}
