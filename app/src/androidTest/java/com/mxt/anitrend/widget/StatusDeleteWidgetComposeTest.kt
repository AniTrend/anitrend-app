@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.content.DialogInterface
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidget
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidgetState
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.DialogUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class StatusDeleteWidgetComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun deleteActionExposesAccessibleStateForIdleDisabledAndLoading() {
        val callbackCount = AtomicInteger()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            mainClock.autoAdvance = false
            val hostActivity = requireNotNull(activity)
            var widget: StatusDeleteWidget? = null
            runOnUiThread {
                widget =
                    installDeleteWidget(
                        activity = hostActivity,
                        state = StatusDeleteWidgetState(isEnabled = true, isLoading = false),
                        onDelete = { callbackCount.incrementAndGet() },
                    )
            }
            waitForIdle()

            val deleteDescription = hostActivity.getString(R.string.dialog_title_delete_activity)
            val busyDescription = hostActivity.getString(R.string.busy_please_wait)
            onNodeWithContentDescription(deleteDescription)
                .assertIsDisplayed()
                .assertIsEnabled()
                .assertHasClickAction()

            runOnUiThread {
                requireNotNull(widget).render(
                    StatusDeleteWidgetState(isEnabled = false, isLoading = false),
                )
            }
            mainClock.advanceTimeByFrame()
            waitForIdle()
            onNodeWithContentDescription(deleteDescription)
                .assertIsDisplayed()
                .assertIsNotEnabled()

            runOnUiThread {
                requireNotNull(widget).render(
                    StatusDeleteWidgetState(isEnabled = false, isLoading = true),
                )
            }
            mainClock.advanceTimeBy(250)
            waitForIdle()
            onNodeWithContentDescription(deleteDescription)
                .assertIsDisplayed()
                .assertIsNotEnabled()
            onNodeWithContentDescription(busyDescription).assertIsDisplayed()
            onNodeWithContentDescription(deleteDescription).performTouchInput { click() }
            runOnIdle { assertEquals(0, callbackCount.get()) }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun recycledDeleteConfirmationCannotTargetAnotherListener() {
        val oldListenerCalls = AtomicInteger()
        val currentListenerCalls = AtomicInteger()
        val confirmationCallback = slot<DialogInterface.OnClickListener>()

        mockkStatic(DialogUtil::class)
        try {
            every {
                DialogUtil.createMessage(any(), any(), any(), capture(confirmationCallback))
            } returns Unit

            runAndroidComposeUiTest<ProgressLayoutTestActivity> {
                val hostActivity = requireNotNull(activity)
                val deleteDescription = hostActivity.getString(R.string.dialog_title_delete_activity)
                var widget: StatusDeleteWidget? = null
                runOnUiThread {
                    widget =
                        installDeleteWidget(
                            activity = hostActivity,
                            state = StatusDeleteWidgetState(isEnabled = true, isLoading = false),
                            onDelete = { oldListenerCalls.incrementAndGet() },
                        )
                }
                waitForIdle()

                onNodeWithContentDescription(deleteDescription).performClick()
                runOnUiThread {
                    confirmationCallback.captured.onClick(
                        mockk(relaxed = true),
                        DialogInterface.BUTTON_POSITIVE,
                    )
                }
                runOnIdle { assertEquals(1, oldListenerCalls.get()) }

                onNodeWithContentDescription(deleteDescription).performClick()
                val staleCallback = confirmationCallback.captured
                runOnUiThread { requireNotNull(widget).onViewRecycled() }
                onNodeWithContentDescription(deleteDescription).performClick()
                verify(exactly = 2) {
                    DialogUtil.createMessage(any(), any(), any(), any())
                }

                runOnUiThread {
                    requireNotNull(widget).apply {
                        render(StatusDeleteWidgetState(isEnabled = true, isLoading = false))
                        setOnDeleteListener { currentListenerCalls.incrementAndGet() }
                    }
                    staleCallback.onClick(
                        mockk(relaxed = true),
                        DialogInterface.BUTTON_POSITIVE,
                    )
                }
                runOnIdle {
                    assertEquals(1, oldListenerCalls.get())
                    assertEquals(0, currentListenerCalls.get())
                }

                onNodeWithContentDescription(deleteDescription).performClick()
                runOnUiThread {
                    confirmationCallback.captured.onClick(
                        mockk(relaxed = true),
                        DialogInterface.BUTTON_POSITIVE,
                    )
                }
                runOnIdle {
                    assertEquals(1, oldListenerCalls.get())
                    assertEquals(1, currentListenerCalls.get())
                }
                verify(exactly = 3) {
                    DialogUtil.createMessage(any(), any(), any(), any())
                }
            }
        } finally {
            unmockkStatic(DialogUtil::class)
        }
    }

    private fun installDeleteWidget(
        activity: ProgressLayoutTestActivity,
        state: StatusDeleteWidgetState,
        onDelete: () -> Unit,
    ): StatusDeleteWidget {
        val widget = StatusDeleteWidget(activity).apply {
            render(state)
            setOnDeleteListener(onDelete)
        }
        activity.setContentView(
            ComposeView(activity).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
                setContent {
                    AniTrendTheme {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            AndroidView(
                                factory = { widget },
                                modifier = Modifier.wrapContentSize(),
                            )
                        }
                    }
                }
            },
        )
        return widget
    }
}
