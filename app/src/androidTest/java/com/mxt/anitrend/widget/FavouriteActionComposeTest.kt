@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.favourite.FavouriteAction
import com.mxt.anitrend.ui.favourite.FavouriteActionState
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouriteActionComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun favouriteActionReflectsCanonicalStateCountAndEnabledBehavior() {
        val state =
            mutableStateOf(
                FavouriteActionState(count = 5, isLiked = false, isEnabled = true, isLoading = false),
            )
        val toggles = AtomicInteger()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installFavouriteAction(hostActivity, state) { toggles.incrementAndGet() }
            }
            waitForIdle()

            val addDescription = hostActivity.getString(R.string.description_favourite_action_add)
            val removeDescription = hostActivity.getString(R.string.description_favourite_action_remove)
            onNodeWithContentDescription(addDescription)
                .assertIsDisplayed()
                .assertIsNotSelected()
                .assertHasClickAction()
            onNodeWithText(" 5 ").assertIsDisplayed()

            onNodeWithContentDescription(addDescription).performClick()
            runOnIdle { assertEquals(1, toggles.get()) }
            runOnUiThread { state.value = state.value.copy(count = 6, isLiked = true) }
            waitForIdle()
            onNodeWithContentDescription(removeDescription)
                .assertIsDisplayed()
                .assertIsSelected()
            onNodeWithText(" 6 ").assertIsDisplayed()

            runOnUiThread { state.value = state.value.copy(count = 0, isEnabled = false) }
            waitForIdle()
            onNodeWithContentDescription(removeDescription).assertIsNotEnabled()
            onNodeWithText(" 0 ").assertIsDisplayed()
            runOnIdle { assertEquals(1, toggles.get()) }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun pendingFavouriteExposesAccessibleCompactProgressIndicator() {
        val state =
            mutableStateOf(
                FavouriteActionState(count = 5, isLiked = true, isEnabled = true, isLoading = true),
            )
        val toggles = AtomicInteger()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            mainClock.autoAdvance = false
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installFavouriteAction(hostActivity, state) { toggles.incrementAndGet() }
            }
            waitForIdle()

            val removeDescription = hostActivity.getString(R.string.description_favourite_action_remove)
            onNodeWithContentDescription(removeDescription).assertHasClickAction()
            onNodeWithContentDescription(hostActivity.getString(R.string.busy_please_wait), useUnmergedTree = true)
                .assertIsDisplayed()
                .assertHeightIsEqualTo(18.dp)
            onNodeWithContentDescription(removeDescription).performClick()
            runOnIdle { assertEquals(1, toggles.get()) }
        }
    }

    private fun installFavouriteAction(
        activity: ProgressLayoutTestActivity,
        state: MutableState<FavouriteActionState>,
        onClick: () -> Unit,
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        FavouriteAction(state = state.value, onClick = onClick)
                    }
                }
            },
        )
    }
}
