@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.github.rahatarmanahmed.cpv.CircularProgressView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidget
import com.mxt.anitrend.base.custom.view.widget.VoteWidget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class WidgetSpinnerDimensionInstrumentationTest {
    private val context: Context
        get() = ContextThemeWrapper(
            ApplicationProvider.getApplicationContext(),
            R.style.AppTheme,
        )

    @Test
    fun favouriteLayouts_useCompactSpinnerDimensions() {
        assertSpinnerDimensions(
            label = "widget_favourite",
            root = inflate(R.layout.widget_favourite),
            expectedCount = 1,
        )
        assertSpinnerDimensions(
            label = "widget_toolbar_favourite",
            root = inflate(R.layout.widget_toolbar_favourite),
            expectedCount = 1,
        )
    }

    @Test
    fun voteLayout_usesCompactSpinnerDimensionsForBothStates() {
        assertSpinnerDimensions(
            label = "widget_vote",
            root = inflate(R.layout.widget_vote),
            expectedCount = 2,
        )
    }

    @Test
    fun hostSurfaces_renderOwnedWidgetsWithCompactSpinners() {
        val feedProgress = inflate(R.layout.adapter_feed_progress)
        val favouriteWidget = feedProgress.findViewById<FavouriteWidget>(R.id.widget_favourite)
        assertNotNull("adapter_feed_progress should contain widget_favourite", favouriteWidget)
        assertSpinnerDimensions(
            label = "adapter_feed_progress.widget_favourite",
            root = favouriteWidget,
            expectedCount = 1,
        )

        val review = inflate(R.layout.adapter_review)
        val voteWidget = review.findViewById<VoteWidget>(R.id.review_vote)
        assertNotNull("adapter_review should contain review_vote", voteWidget)
        assertSpinnerDimensions(
            label = "adapter_review.review_vote",
            root = voteWidget,
            expectedCount = 2,
        )
    }

    private fun inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, null, false)

    private fun assertSpinnerDimensions(
        label: String,
        root: View,
        expectedCount: Int,
    ) {
        val spinners = root.findViewsOfType<CircularProgressView>()
        val expectedSize = context.resources.getDimensionPixelSize(R.dimen.widget_spinner_compact_size)

        assertEquals("Unexpected spinner count in $label", expectedCount, spinners.size)

        spinners.forEachIndexed { index, spinner ->
            val width = spinner.layoutParams?.width
            val height = spinner.layoutParams?.height
            assertEquals("Spinner #$index width should use widget_spinner_compact_size in $label", expectedSize, width)
            assertEquals("Spinner #$index height should use widget_spinner_compact_size in $label", expectedSize, height)
        }
    }

    private inline fun <reified T : View> View.findViewsOfType(): List<T> {
        val matches = mutableListOf<T>()
        collectViews(this, T::class.java, matches)
        return matches
    }

    private fun <T : View> collectViews(
        view: View,
        klass: Class<T>,
        matches: MutableList<T>,
    ) {
        if (klass.isInstance(view)) {
            matches += klass.cast(view)
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                collectViews(view.getChildAt(index), klass, matches)
            }
        }
    }
}
