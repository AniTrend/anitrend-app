@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetRenderState
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouriteToolbarActionComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun toolbarFavoriteExposesSelectionAndForwardsToggle() {
        val toggles = CopyOnWriteArrayList<Int>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: ToolbarFavoriteHost
            runOnUiThread {
                host = installToolbarFavorite(
                    activity = hostActivity,
                    state = FavouriteWidgetRenderState(isFavourite = false),
                    onToggle = { toggles.add(1) },
                )
            }
            waitForIdle()

            val addDescription = hostActivity.getString(R.string.description_favourite_action_add)
            val removeDescription = hostActivity.getString(R.string.description_favourite_action_remove)
            onNodeWithContentDescription(addDescription)
                .assertIsDisplayed()
                .assertIsNotSelected()
                .assertHasClickAction()
                .performClick()
            runOnIdle { assertEquals(listOf(1), toggles.toList()) }

            runOnUiThread {
                host.widget.render(FavouriteWidgetRenderState(isFavourite = true))
            }
            waitForIdle()
            onNodeWithContentDescription(removeDescription)
                .assertIsDisplayed()
                .assertIsSelected()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun loadingToolbarFavoriteShowsProgressAndDoesNotToggle() {
        val toggles = CopyOnWriteArrayList<Int>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: ToolbarFavoriteHost
            runOnUiThread {
                host = installToolbarFavorite(
                    activity = hostActivity,
                    state = FavouriteWidgetRenderState(isFavourite = true, isLoading = true),
                    onToggle = { toggles.add(1) },
                )
            }
            waitForIdle()

            val removeDescription = hostActivity.getString(R.string.description_favourite_action_remove)
            onNodeWithContentDescription(removeDescription).assertIsEnabled().performClick()
            onNodeWithContentDescription(hostActivity.getString(R.string.busy_please_wait), useUnmergedTree = true)
                .assertIsDisplayed()
                .assertHeightIsEqualTo(18.dp)
            runOnIdle { assertEquals(emptyList<Int>(), toggles.toList()) }

            runOnUiThread {
                host.widget.render(FavouriteWidgetRenderState(isFavourite = true, isLoading = false))
            }
            waitForIdle()
            onNodeWithContentDescription(removeDescription).assertIsSelected()
        }
    }

    private fun installToolbarFavorite(
        activity: ProgressLayoutTestActivity,
        state: FavouriteWidgetRenderState,
        onToggle: () -> Unit,
    ): ToolbarFavoriteHost {
        val toolbarRoot = activity.layoutInflater.inflate(R.layout.custom_toolbar, null, false)
        val toolbar = requireNotNull(toolbarRoot.findViewById<Toolbar>(R.id.toolbar))
        toolbar.inflateMenu(R.menu.media_base_menu)
        val menuItem = toolbar.menu.findItem(R.id.action_favourite)
        val widget = requireNotNull(menuItem.actionView as? FavouriteToolbarWidget)
        widget.setOnToggleAction(onToggle)
        widget.render(state)
        toolbar.title = activity.getString(R.string.title_activity_series)

        activity.setContentView(
            ComposeView(activity).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        AndroidView(
                            factory = { toolbarRoot },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            },
        )
        return ToolbarFavoriteHost(widget)
    }

    private data class ToolbarFavoriteHost(
        val widget: FavouriteToolbarWidget,
    )
}
