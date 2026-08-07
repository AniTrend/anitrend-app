@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.AboutPanelWidget
import com.mxt.anitrend.base.custom.view.widget.AboutPanelWidget.StatState
import com.mxt.anitrend.view.sheet.BottomSheetListUsers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val widget = attachWidget(activity)
                widget.setStats(StatState.Loaded(0), StatState.NotLoaded, StatState.NotLoaded)

                widget.findViewById<View>(R.id.user_followers_container).performClick()
                activity.supportFragmentManager.executePendingTransactions()

                assertTrue(
                    "loaded zero followers must open the empty list sheet",
                    activity.supportFragmentManager.fragments.any { it is BottomSheetListUsers },
                )
            }
        }
    }

    @Test
    fun loadedNonzeroFollowers_opensListSheet() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val widget = attachWidget(activity)
                widget.setStats(StatState.Loaded(4), StatState.NotLoaded, StatState.NotLoaded)

                widget.findViewById<View>(R.id.user_followers_container).performClick()
                activity.supportFragmentManager.executePendingTransactions()

                assertTrue(
                    "loaded nonzero followers must open the list sheet",
                    activity.supportFragmentManager.fragments.any { it is BottomSheetListUsers },
                )
            }
        }
    }

    @Test
    fun loadedZeroFollowing_opensListSheet() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val widget = attachWidget(activity)
                widget.setStats(StatState.NotLoaded, StatState.Loaded(0), StatState.NotLoaded)

                widget.findViewById<View>(R.id.user_following_container).performClick()
                activity.supportFragmentManager.executePendingTransactions()

                assertTrue(
                    "loaded zero following must open the empty list sheet",
                    activity.supportFragmentManager.fragments.any { it is BottomSheetListUsers },
                )
            }
        }
    }

    @Test
    fun notLoadedFollowers_keepsLoadingToastWithoutSheet() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val widget = attachWidget(activity)

                widget.findViewById<View>(R.id.user_followers_container).performClick()
                activity.supportFragmentManager.executePendingTransactions()

                assertFalse(
                    "not-yet-loaded followers must not open the list sheet",
                    activity.supportFragmentManager.fragments.any { it is BottomSheetListUsers },
                )
            }
        }
    }

    @Test
    fun failedFollowers_keepsLoadingToastWithoutSheet() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val widget = attachWidget(activity)
                widget.setStats(StatState.Failed, StatState.NotLoaded, StatState.NotLoaded)

                widget.findViewById<View>(R.id.user_followers_container).performClick()
                activity.supportFragmentManager.executePendingTransactions()

                assertFalse(
                    "failed followers must not open the list sheet",
                    activity.supportFragmentManager.fragments.any { it is BottomSheetListUsers },
                )
            }
        }
    }

    private fun attachWidget(activity: androidx.appcompat.app.AppCompatActivity): AboutPanelWidget {
        val container = FrameLayout(activity)
        activity.setContentView(container)
        val widget = AboutPanelWidget(activity)
        container.addView(widget)
        widget.setFragmentActivity(activity)
        widget.setUserId(7L, activity.lifecycle)
        return widget
    }
}
