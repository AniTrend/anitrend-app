@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.adapter.recycler.detail

import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagAdapterComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun tagRowsExposeNameAndRankSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val tags =
                listOf(
                    MediaTag(name = "High School", rank = 80),
                    MediaTag(name = "Mystery Story", rank = 45, isMediaSpoiler = true),
                )

            runOnUiThread {
                val adapter = TagAdapter(hostActivity).apply { onItemsInserted(tags) }
                val recyclerView =
                    RecyclerView(hostActivity).apply {
                        layoutManager =
                            StaggeredGridLayoutManager(
                                hostActivity.resources.getInteger(R.integer.grid_list_x2),
                                StaggeredGridLayoutManager.VERTICAL,
                            )
                        isNestedScrollingEnabled = false
                        setHasFixedSize(true)
                        this.adapter = adapter
                    }
                hostActivity.setContentView(
                    ComposeView(hostActivity).apply {
                        id = View.generateViewId()
                        setViewCompositionStrategy(
                            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                        )
                        setContent {
                            AniTrendTheme {
                                AndroidView(
                                    factory = { recyclerView },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    },
                )
            }
            waitForIdle()

            onNodeWithText("High School").assertIsDisplayed()
            onNodeWithText("80%").assertIsDisplayed()
            onNodeWithText("Mystery Story").assertIsDisplayed()
            onNodeWithText("45%").assertIsDisplayed()
            assertTrue(
                "normal tag name should use the theme subtitle color",
                onNodeWithText("High School")
                    .captureToImage()
                    .asAndroidBitmap()
                    .containsColorNear(hostActivity.getCompatColorAttr(R.attr.subtitleColor)),
            )
            assertTrue(
                "tag rank should use the theme accent color",
                onNodeWithText("80%")
                    .captureToImage()
                    .asAndroidBitmap()
                    .containsColorNear(hostActivity.getCompatColorAttr(R.attr.colorAccent)),
            )
            assertTrue(
                "spoiler tag name should use the orange state color",
                onNodeWithText("Mystery Story")
                    .captureToImage()
                    .asAndroidBitmap()
                    .containsColorNear(ContextCompat.getColor(hostActivity, R.color.colorStateOrange)),
            )
            val tagNameBounds = onNodeWithText("Mystery Story").fetchSemanticsNode().boundsInRoot
            val tagRankBounds = onNodeWithText("45%").fetchSemanticsNode().boundsInRoot
            assertTrue("tag name must not overlap rank", tagNameBounds.right < tagRankBounds.left)
        }
    }
}

private fun Bitmap.containsColorNear(expectedColor: Int): Boolean {
    val expectedRed = android.graphics.Color.red(expectedColor)
    val expectedGreen = android.graphics.Color.green(expectedColor)
    val expectedBlue = android.graphics.Color.blue(expectedColor)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = getPixel(x, y)
            if (
                android.graphics.Color.alpha(color) > 240 &&
                abs(android.graphics.Color.red(color) - expectedRed) <= 12 &&
                abs(android.graphics.Color.green(color) - expectedGreen) <= 12 &&
                abs(android.graphics.Color.blue(color) - expectedBlue) <= 12
            ) {
                return true
            }
        }
    }
    return false
}
