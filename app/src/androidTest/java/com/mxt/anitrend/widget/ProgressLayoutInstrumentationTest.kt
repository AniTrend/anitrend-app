package com.mxt.anitrend.widget

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.mxt.anitrend.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import com.mxt.anitrend.test.R as TestR

@LargeTest
@RunWith(AndroidJUnit4::class)
class ProgressLayoutInstrumentationTest {
    @Test
    fun initialState_isContent() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val contentChild = activity.findViewById<TextView>(TestR.id.contentChild)
                val loadingView = layout.findViewById<View>(R.id.progressStateLoading)
                val errorView = layout.findViewById<View>(R.id.progressStateError)

                assertTrue("Expected initial state to be CONTENT", layout.isContent)
                assertFalse("Expected initial state not to be LOADING", layout.isLoading)
                assertFalse("Expected initial state not to be ERROR", layout.isError)
                assertEquals(
                    "Content child should be VISIBLE initially",
                    View.VISIBLE,
                    contentChild.visibility,
                )
                assertEquals(
                    "Loading overlay should be GONE initially",
                    View.GONE,
                    loadingView.visibility,
                )
                assertEquals(
                    "Error overlay should be GONE initially",
                    View.GONE,
                    errorView.visibility,
                )
            }
        }
    }

    @Test
    fun showLoading_hidesContent_showsLoadingOverlay() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val contentChild = activity.findViewById<TextView>(TestR.id.contentChild)
                val loadingView = layout.findViewById<View>(R.id.progressStateLoading)
                val errorView = layout.findViewById<View>(R.id.progressStateError)

                layout.showLoading()

                assertTrue("Expected state to be LOADING", layout.isLoading)
                assertFalse("Expected state not to be CONTENT", layout.isContent)
                assertFalse("Expected state not to be ERROR", layout.isError)
                assertEquals(
                    "Content child should be GONE after showLoading",
                    View.GONE,
                    contentChild.visibility,
                )
                assertEquals(
                    "Loading overlay should be VISIBLE after showLoading",
                    View.VISIBLE,
                    loadingView.visibility,
                )
                assertEquals(
                    "Error overlay should be GONE after showLoading",
                    View.GONE,
                    errorView.visibility,
                )
            }
        }
    }

    @Test
    fun showContent_restoresContentVisibility() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val contentChild = activity.findViewById<TextView>(TestR.id.contentChild)
                val loadingView = layout.findViewById<View>(R.id.progressStateLoading)

                layout.showLoading()
                layout.showContent()

                assertTrue("Expected state to be CONTENT after showContent", layout.isContent)
                assertFalse("Expected state not to be LOADING after showContent", layout.isLoading)
                assertEquals(
                    "Content child should be VISIBLE after showContent",
                    View.VISIBLE,
                    contentChild.visibility,
                )
                assertEquals(
                    "Loading overlay should be GONE after showContent",
                    View.GONE,
                    loadingView.visibility,
                )
            }
        }
    }

    @Test
    fun showError_showsErrorOverlay() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val contentChild = activity.findViewById<TextView>(TestR.id.contentChild)
                val loadingView = layout.findViewById<View>(R.id.progressStateLoading)
                val errorView = layout.findViewById<View>(R.id.progressStateError)
                val errorIcon = layout.findViewById<ImageView>(R.id.progressStateErrorIcon)
                val errorText = layout.findViewById<TextView>(R.id.progressStateErrorText)
                val errorAction = layout.findViewById<Button>(R.id.progressStateErrorAction)

                val drawable = ColorDrawable(0xFF0000.toInt())
                var actionClicked = false
                val onClickListener = View.OnClickListener { actionClicked = true }

                layout.showError(
                    drawable = drawable,
                    message = "Something went wrong",
                    actionText = "Retry",
                    action = onClickListener,
                )

                assertTrue("Expected state to be ERROR after showError", layout.isError)
                assertFalse("Expected state not to be CONTENT", layout.isContent)
                assertFalse("Expected state not to be LOADING", layout.isLoading)
                assertEquals(
                    "Content child should be GONE after showError",
                    View.GONE,
                    contentChild.visibility,
                )
                assertEquals(
                    "Loading overlay should be GONE after showError",
                    View.GONE,
                    loadingView.visibility,
                )
                assertEquals(
                    "Error overlay should be VISIBLE after showError",
                    View.VISIBLE,
                    errorView.visibility,
                )
                assertEquals(
                    "Error icon should be VISIBLE",
                    View.VISIBLE,
                    errorIcon.visibility,
                )
                assertNotNull("Error icon drawable should not be null", errorIcon.drawable)
                assertEquals(
                    "Error message should be set",
                    "Something went wrong",
                    errorText.text.toString(),
                )
                assertEquals(
                    "Error action button should be VISIBLE",
                    View.VISIBLE,
                    errorAction.visibility,
                )
                assertEquals(
                    "Error action button text should be set",
                    "Retry",
                    errorAction.text.toString(),
                )

                // Verify action click works
                errorAction.performClick()
                assertTrue("Action click listener should have been invoked", actionClicked)
            }
        }
    }

    @Test
    fun showEmpty_showsErrorStateWithoutAction() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val errorView = layout.findViewById<View>(R.id.progressStateError)
                val errorIcon = layout.findViewById<ImageView>(R.id.progressStateErrorIcon)
                val errorText = layout.findViewById<TextView>(R.id.progressStateErrorText)
                val errorAction = layout.findViewById<Button>(R.id.progressStateErrorAction)

                val drawable = ColorDrawable(0xFFFF00.toInt())
                layout.showEmpty(drawable = drawable, message = "No data available")

                assertTrue("Expected state to be ERROR after showEmpty", layout.isError)
                assertEquals(
                    "Error overlay should be VISIBLE after showEmpty",
                    View.VISIBLE,
                    errorView.visibility,
                )
                assertEquals(
                    "Error icon should be VISIBLE",
                    View.VISIBLE,
                    errorIcon.visibility,
                )
                assertEquals(
                    "Error message should be set",
                    "No data available",
                    errorText.text.toString(),
                )
                assertEquals(
                    "Error action button should be GONE",
                    View.GONE,
                    errorAction.visibility,
                )
                assertEquals(
                    "Error action button text should be empty",
                    "",
                    errorAction.text.toString(),
                )
            }
        }
    }

    @Test
    fun showError_afterShowLoading_endsInErrorState() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val loadingView = layout.findViewById<View>(R.id.progressStateLoading)
                val errorView = layout.findViewById<View>(R.id.progressStateError)

                layout.showLoading()
                layout.showError(
                    drawable = ColorDrawable(0xFF0000.toInt()),
                    message = "Error after loading",
                    actionText = "OK",
                    action = View.OnClickListener { },
                )

                assertTrue(
                    "Expected state to be ERROR after showLoading -> showError",
                    layout.isError,
                )
                assertFalse(
                    "Expected state NOT to be LOADING after showLoading -> showError",
                    layout.isLoading,
                )
                assertEquals(
                    "Error overlay should be VISIBLE",
                    View.VISIBLE,
                    errorView.visibility,
                )
                assertEquals(
                    "Loading overlay should be GONE",
                    View.GONE,
                    loadingView.visibility,
                )
            }
        }
    }

    @Test
    fun showEmpty_afterShowLoading_endsInErrorState() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val loadingView = layout.findViewById<View>(R.id.progressStateLoading)
                val errorView = layout.findViewById<View>(R.id.progressStateError)
                val errorAction = layout.findViewById<Button>(R.id.progressStateErrorAction)

                layout.showLoading()
                layout.showEmpty(drawable = null, message = "Nothing here")

                assertTrue(
                    "Expected state to be ERROR after showLoading -> showEmpty",
                    layout.isError,
                )
                assertFalse(
                    "Expected state NOT to be LOADING after showLoading -> showEmpty",
                    layout.isLoading,
                )
                assertEquals(
                    "Error overlay should be VISIBLE",
                    View.VISIBLE,
                    errorView.visibility,
                )
                assertEquals(
                    "Loading overlay should be GONE",
                    View.GONE,
                    loadingView.visibility,
                )
                assertEquals(
                    "Error action button should be GONE",
                    View.GONE,
                    errorAction.visibility,
                )
            }
        }
    }

    @Test
    fun error_to_content_restoresChildren() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val contentChild = activity.findViewById<TextView>(TestR.id.contentChild)
                val errorView = layout.findViewById<View>(R.id.progressStateError)

                layout.showError(
                    drawable = ColorDrawable(0xFF0000.toInt()),
                    message = "Error",
                    actionText = null,
                    action = null,
                )
                layout.showContent()

                assertTrue(
                    "Expected state to be CONTENT after error -> showContent",
                    layout.isContent,
                )
                assertEquals(
                    "Content child should be VISIBLE after error -> showContent",
                    View.VISIBLE,
                    contentChild.visibility,
                )
                assertEquals(
                    "Error overlay should be GONE after error -> showContent",
                    View.GONE,
                    errorView.visibility,
                )
            }
        }
    }

    @Test
    fun showContent_withNullDrawable() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val errorIcon = layout.findViewById<ImageView>(R.id.progressStateErrorIcon)
                val errorText = layout.findViewById<TextView>(R.id.progressStateErrorText)
                val errorAction = layout.findViewById<Button>(R.id.progressStateErrorAction)

                layout.showError(
                    drawable = null,
                    message = "Info message",
                    actionText = null,
                    action = null,
                )

                assertTrue("Expected state to be ERROR", layout.isError)
                assertEquals(
                    "Error icon should be GONE when drawable is null",
                    View.GONE,
                    errorIcon.visibility,
                )
                assertEquals(
                    "Error action button should be GONE when action is null",
                    View.GONE,
                    errorAction.visibility,
                )
                assertEquals(
                    "Error message text should be set even with null drawable",
                    "Info message",
                    errorText.text.toString(),
                )
            }
        }
    }

    @Test
    fun multipleChildren_visibilityRestored() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val contentChild = activity.findViewById<TextView>(TestR.id.contentChild)
                val secondChild = activity.findViewById<Button>(TestR.id.secondContentChild)

                // Transition to LOADING
                layout.showLoading()
                assertEquals(
                    "Content child should be GONE during loading",
                    View.GONE,
                    contentChild.visibility,
                )
                assertEquals(
                    "Second child should be GONE during loading",
                    View.GONE,
                    secondChild.visibility,
                )

                // Transition back to CONTENT
                layout.showContent()
                assertEquals(
                    "Content child should be VISIBLE after returning to content",
                    View.VISIBLE,
                    contentChild.visibility,
                )
                assertEquals(
                    "Second child should be VISIBLE after returning to content",
                    View.VISIBLE,
                    secondChild.visibility,
                )
            }
        }
    }

    /**
     * Regression: multiple state transitions must not lose content visibility.
     * LOADING → ERROR → LOADING → CONTENT should restore all content children.
     */
    @Test
    fun multipleStateTransitions_restoresContentVisibility() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val contentChild = activity.findViewById<TextView>(TestR.id.contentChild)

                // Cycle 1: LOADING → ERROR → CONTENT
                layout.showLoading()
                layout.showError(
                    drawable = ColorDrawable(0xFF0000.toInt()),
                    message = "First error",
                    actionText = "Retry",
                    action = View.OnClickListener { },
                )
                assertTrue("Expected ERROR after showLoading → showError", layout.isError)
                layout.showContent()
                assertTrue("Expected CONTENT after error → showContent", layout.isContent)
                assertEquals(
                    "Content child should be VISIBLE after first cycle",
                    View.VISIBLE,
                    contentChild.visibility,
                )

                // Cycle 2: LOADING → ERROR → CONTENT (second time)
                layout.showLoading()
                layout.showError(
                    drawable = ColorDrawable(0xFF0000.toInt()),
                    message = "Second error",
                    actionText = "Retry",
                    action = View.OnClickListener { },
                )
                assertTrue("Expected ERROR after second showLoading → showError", layout.isError)
                layout.showContent()
                assertTrue("Expected CONTENT after second error → showContent", layout.isContent)
                assertEquals(
                    "Content child should be VISIBLE after second cycle",
                    View.VISIBLE,
                    contentChild.visibility,
                )

                // Cycle 3: Just LOADING → CONTENT
                layout.showLoading()
                assertTrue("Expected LOADING", layout.isLoading)
                layout.showContent()
                assertTrue("Expected CONTENT", layout.isContent)
                assertEquals(
                    "Content child should be VISIBLE after third cycle",
                    View.VISIBLE,
                    contentChild.visibility,
                )
            }
        }
    }

    /**
     * Regression: calling showError then showContent must restore content visibility
     * even when the content child was previously managed by the visibility map.
     */
    @Test
    fun errorThenContent_restoresChildren() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val contentChild = activity.findViewById<TextView>(TestR.id.contentChild)
                val errorView = layout.findViewById<View>(R.id.progressStateError)

                // Show error with null drawable (regression: icon GONE, content GONE)
                layout.showError(
                    drawable = null,
                    message = "Error without icon",
                    actionText = null,
                    action = null,
                )
                assertTrue("Expected ERROR", layout.isError)
                assertEquals("Content hidden during error", View.GONE, contentChild.visibility)
                assertEquals("Error overlay visible", View.VISIBLE, errorView.visibility)

                // Restore content
                layout.showContent()
                assertTrue("Expected CONTENT", layout.isContent)
                assertEquals(
                    "Content child should be VISIBLE after error → content",
                    View.VISIBLE,
                    contentChild.visibility,
                )
                assertEquals("Error overlay hidden", View.GONE, errorView.visibility)
            }
        }
    }

    /**
     * Regression: showContent must work immediately (not just after post),
     * verifying the synchronous visibility restoration path.
     */
    @Test
    fun contentVisibility_isSetSynchronously() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<ProgressLayout>(TestR.id.progressLayout)
                val contentChild = activity.findViewById<TextView>(TestR.id.contentChild)

                layout.showLoading()
                layout.showContent()
                // Synchronous check — content should be VISIBLE immediately,
                // not just after a post/layout pass
                assertEquals(
                    "Content child should be VISIBLE synchronously after showContent",
                    View.VISIBLE,
                    contentChild.visibility,
                )
            }
        }
    }
}
