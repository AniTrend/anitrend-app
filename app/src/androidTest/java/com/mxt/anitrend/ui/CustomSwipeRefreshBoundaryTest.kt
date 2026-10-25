package com.mxt.anitrend.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The refresh container gates its gestures on the target's
 * `canScrollVertically`, which Compose hosts cannot answer. The optional
 * scroll-boundary oracle keeps mid-list drags from being hijacked as refresh
 * gestures, and the target's own answer stays the fallback.
 */
@RunWith(AndroidJUnit4::class)
class CustomSwipeRefreshBoundaryTest {

    @Test
    fun scrollBoundaryOracleDrivesTheGestureGates() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val layout = CustomSwipeRefreshLayout(context)

        assertFalse(layout.canChildScrollUp())
        assertFalse(layout.canChildScrollDown())

        var canScrollBack = true
        var canScrollForward = false
        layout.scrollBoundary = { direction ->
            if (direction < 0) canScrollBack else canScrollForward
        }

        assertTrue(layout.canChildScrollUp())
        assertFalse(layout.canChildScrollDown())

        canScrollBack = false
        canScrollForward = true
        assertFalse(layout.canChildScrollUp())
        assertTrue(layout.canChildScrollDown())
    }

    @Test
    fun targetScrollAnswerRemainsTheFallback() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val layout = CustomSwipeRefreshLayout(context)
        var asked = false
        val scrollingChild =
            object : android.view.View(context) {
                override fun canScrollVertically(direction: Int): Boolean {
                    asked = true
                    return direction < 0
                }
            }
        layout.addView(scrollingChild)

        // Force target resolution through a measure pass.
        layout.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(100, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(100, android.view.View.MeasureSpec.EXACTLY),
        )

        assertTrue(layout.canChildScrollUp())
        assertFalse(layout.canChildScrollDown())
        assertTrue(asked)
    }
}
