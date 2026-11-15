@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.AboutPanelWidget
import com.mxt.anitrend.base.custom.view.widget.AboutPanelWidget.StatState
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.WidgetState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicLong

@RunWith(AndroidJUnit4::class)
class AboutPanelWidgetComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun statsExposeAccessibleLabelsFormattedCountsAndFavouriteNavigation() {
        val requestedUserId = AtomicLong(-1L)

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                val widget =
                    AboutPanelWidget(hostActivity).apply {
                        setFragmentActivity(hostActivity)
                        setUserId(7L)
                        setStats(
                            followers = StatState.Loaded(1200),
                            following = StatState.Loaded(36),
                            favourites = StatState.Loaded(0),
                        )
                        onFavouritesRequested = requestedUserId::set
                    }
                installAboutPanelWidgetForComposeTest(hostActivity, widget)
            }
            waitForIdle()

            listOf(R.string.following, R.string.followers, R.string.favourites).forEach { labelRes ->
                onNodeWithContentDescription(hostActivity.getString(labelRes))
                    .assertIsDisplayed()
                    .assertHasClickAction()
            }
            onNodeWithText(WidgetState.valueFormatter(1200)).assertIsDisplayed()
            onNodeWithText(WidgetState.valueFormatter(36)).assertIsDisplayed()
            onNodeWithText(WidgetState.valueFormatter(0)).assertIsDisplayed()

            onNodeWithContentDescription(hostActivity.getString(R.string.favourites)).performClick()
            runOnIdle { assertEquals(7L, requestedUserId.get()) }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun displayedCountsSurviveWidgetReattach() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            var widget: AboutPanelWidget? = null
            var container: FrameLayout? = null
            runOnUiThread {
                val aboutPanelWidget =
                    AboutPanelWidget(hostActivity).apply {
                        setFragmentActivity(hostActivity)
                        setUserId(7L)
                        setStats(
                            followers = StatState.Loaded(10),
                            following = StatState.NotLoaded,
                            favourites = StatState.NotLoaded,
                        )
                    }
                widget = aboutPanelWidget
                container = installAboutPanelWidgetForComposeTest(hostActivity, aboutPanelWidget)
            }
            waitForIdle()

            val followerCount = WidgetState.valueFormatter(10)
            onNodeWithText(followerCount).assertIsDisplayed()

            runOnUiThread {
                val aboutPanelWidget = requireNotNull(widget)
                val parent = requireNotNull(container)
                val layoutParams = aboutPanelWidget.layoutParams
                parent.removeView(aboutPanelWidget)
                parent.addView(aboutPanelWidget, layoutParams)
            }
            waitForIdle()
            onNodeWithText(followerCount).assertIsDisplayed()
        }
    }
}
internal fun installAboutPanelWidgetForComposeTest(
    activity: ProgressLayoutTestActivity,
    widget: AboutPanelWidget,
): FrameLayout {
    val container = FrameLayout(activity)
    container.addView(
        widget,
        FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ),
    )
    activity.setContentView(
        ComposeView(activity).apply {
            id = View.generateViewId()
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
            )
            setContent {
                AniTrendTheme {
                    AndroidView(
                        factory = { container },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        },
    )
    return container
}
