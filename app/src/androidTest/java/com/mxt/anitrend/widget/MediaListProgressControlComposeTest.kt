@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.MediaListProgressControl
import com.mxt.anitrend.ui.media.MediaListProgressState
import com.mxt.anitrend.util.KeyUtil
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

private const val MEDIA_LIST_PROGRESS_CONTROL_TAG = "media-list-progress-control"

@RunWith(AndroidJUnit4::class)
class MediaListProgressControlComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun progressLabelsAndClickActionsFollowState() {
        val state = mutableStateOf(progressState())
        val incrementCalls = AtomicInteger()
        val limitCalls = AtomicInteger()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installProgressControl(
                    hostActivity,
                    state,
                    onIncrement = { incrementCalls.incrementAndGet() },
                    onBusyClick = {},
                    onLimitReached = { limitCalls.incrementAndGet() },
                )
            }
            waitForIdle()

            val control = onNodeWithTag(MEDIA_LIST_PROGRESS_CONTROL_TAG, useUnmergedTree = true)
            control
                .assertIsDisplayed()
                .assertHasClickAction()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            onNodeWithText("3/12 +").assertIsDisplayed()
            control.performClick()
            runOnIdle { assertEquals(1, incrementCalls.get()) }

            runOnUiThread { state.value = progressState(maxProgress = 0, isEnabled = false) }
            waitForIdle()
            control.assertIsEnabled()
            onNodeWithText("3/? +").assertIsDisplayed()
            control.performClick()
            runOnIdle {
                assertEquals(1, incrementCalls.get())
                assertEquals(0, limitCalls.get())
            }

            runOnUiThread {
                state.value = progressState(progress = 0, isEnabled = false, status = KeyUtil.NOT_YET_RELEASED)
            }
            waitForIdle()
            onNodeWithText(hostActivity.getString(R.string.TBA)).assertIsDisplayed()
            onNodeWithText("0/12 +").assertDoesNotExist()
            control.performClick()
            runOnIdle {
                assertEquals(1, incrementCalls.get())
                assertEquals(0, limitCalls.get())
            }

            runOnUiThread { state.value = progressState(progress = 12, isEnabled = false) }
            waitForIdle()
            onNodeWithText("12/12").assertIsDisplayed()
            onNodeWithText("12/12 +").assertDoesNotExist()
            control.performClick()
            runOnIdle { assertEquals(1, limitCalls.get()) }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun loadingStateShowsAccessibleIndicatorAndBusyAction() {
        val busyCalls = AtomicInteger()
        val incrementCalls = AtomicInteger()
        val state = mutableStateOf(progressState())

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            mainClock.autoAdvance = false
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installProgressControl(
                    hostActivity,
                    state,
                    onIncrement = { incrementCalls.incrementAndGet() },
                    onBusyClick = { busyCalls.incrementAndGet() },
                    onLimitReached = {},
                )
            }
            waitForIdle()

            val control = onNodeWithTag(MEDIA_LIST_PROGRESS_CONTROL_TAG, useUnmergedTree = true)
            control
                .assertIsDisplayed()
                .assertHasClickAction()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            val activeBounds = control.fetchSemanticsNode().boundsInRoot

            runOnUiThread { state.value = progressState(isEnabled = false, isLoading = true) }
            mainClock.advanceTimeBy(1_000L)
            waitForIdle()

            val loadingBounds = control.fetchSemanticsNode().boundsInRoot
            assertEquals(activeBounds.width, loadingBounds.width, 0.1f)
            assertEquals(activeBounds.height, loadingBounds.height, 0.1f)
            onNodeWithContentDescription(hostActivity.getString(R.string.busy_please_wait), useUnmergedTree = true)
                .assertIsDisplayed()
            control.performClick()
            runOnIdle {
                assertEquals(1, busyCalls.get())
                assertEquals(0, incrementCalls.get())
            }
        }
    }
}

private fun installProgressControl(
    activity: ProgressLayoutTestActivity,
    state: MutableState<MediaListProgressState>,
    onIncrement: () -> Unit,
    onBusyClick: () -> Unit,
    onLimitReached: () -> Unit,
) {
    activity.setContentView(
        ComposeView(activity).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AniTrendTheme("light") {
                    Box(
                        modifier =
                        Modifier
                            .size(width = 80.dp, height = 48.dp)
                            .background(Color.DarkGray),
                    ) {
                        MediaListProgressControl(
                            state = state.value,
                            onIncrement = onIncrement,
                            onBusyClick = onBusyClick,
                            onLimitReached = onLimitReached,
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                    }
                }
            }
        },
    )
}

private fun progressState(
    progress: Int = 3,
    maxProgress: Int = 12,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    status: String? = "RELEASING",
    mediaType: String? = KeyUtil.ANIME,
): MediaListProgressState = MediaListProgressState(
    progress = progress,
    maxProgress = maxProgress,
    isEnabled = isEnabled,
    isLoading = isLoading,
    status = status,
    mediaType = mediaType,
)
