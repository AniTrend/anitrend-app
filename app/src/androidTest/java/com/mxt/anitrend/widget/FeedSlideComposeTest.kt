@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.ImagePreviewAdapter
import com.mxt.anitrend.base.custom.view.widget.StatusContentWidget
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.FeedSlideRow
import com.mxt.anitrend.util.markdown.RegexUtil
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedSlideComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun imageSlideExposesVisibleImageAndClickableSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val imageUrl = "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}"
            val expectedCenterColor = resourceCenterPixel(hostActivity)
            lateinit var statusWidget: StatusContentWidget
            lateinit var recyclerView: RecyclerView
            runOnUiThread {
                statusWidget = StatusContentWidget(hostActivity)
                val margin = hostActivity.resources.getDimensionPixelSize(R.dimen.lg_margin)
                hostActivity.setContentView(
                    FrameLayout(hostActivity).apply {
                        addView(
                            statusWidget,
                            FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                            ).apply { setMargins(margin, margin, margin, margin) },
                        )
                    },
                )
                recyclerView = statusWidget.findViewById(R.id.widget_status_recycler)
                statusWidget.setTextData(RegexUtil.createImageStandard(imageUrl))
            }
            waitUntil("feed slide holder binds", 5_000L) {
                recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitUntil("local feed image pixels are visible", 5_000L) {
                val images =
                    onAllNodesWithTag(FEED_SLIDE_IMAGE_CONTENT_TAG, useUnmergedTree = true)
                        .fetchSemanticsNodes()
                images.size == 1 &&
                    isColorNear(
                        imageCenterPixel(onNodeWithTag(FEED_SLIDE_IMAGE_CONTENT_TAG, useUnmergedTree = true)),
                        expectedCenterColor,
                    )
            }

            assertEquals(1, onAllNodesWithTag(FEED_SLIDE_IMAGE_TAG, useUnmergedTree = true).fetchSemanticsNodes().size)
            onNodeWithTag(FEED_SLIDE_IMAGE_TAG, useUnmergedTree = true).assertIsDisplayed().assertHasClickAction()
            onNodeWithTag(FEED_SLIDE_IMAGE_CONTENT_TAG, useUnmergedTree = true).assertIsDisplayed()
            assertEquals(0, onAllNodesWithTag(FEED_SLIDE_PLAY_TAG, useUnmergedTree = true).fetchSemanticsNodes().size)
            var transitionName: String? = null
            runOnUiThread {
                transitionName =
                    requireNotNull(recyclerView.findViewHolderForAdapterPosition(0)?.itemView as? ComposeView)
                        .transitionName
            }
            assertEquals(imageUrl, transitionName)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun youtubeSlideShowsPlaybackOverlay() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val imageUrl = "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}"
            runOnUiThread {
                hostActivity.setContentView(
                    ComposeView(hostActivity).apply {
                        id = android.view.View.generateViewId()
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            AniTrendTheme {
                                FeedSlideRow(
                                    imageUrl = imageUrl,
                                    centerCrop = true,
                                    showPlayback = true,
                                    roundedCorners = false,
                                    onClick = {},
                                )
                            }
                        }
                    },
                )
            }
            waitForIdle()

            onNodeWithTag(FEED_SLIDE_IMAGE_TAG, useUnmergedTree = true).assertIsDisplayed().assertHasClickAction()
            onNodeWithTag(FEED_SLIDE_PLAY_TAG, useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun imageSlideUsesWideImageHeightFormula() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val imageUrl = "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}"
            lateinit var statusWidget: StatusContentWidget
            lateinit var recyclerView: RecyclerView
            runOnUiThread {
                statusWidget = StatusContentWidget(hostActivity)
                val margin = hostActivity.resources.getDimensionPixelSize(R.dimen.lg_margin)
                hostActivity.setContentView(
                    FrameLayout(hostActivity).apply {
                        addView(
                            statusWidget,
                            FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                            ).apply { setMargins(margin, margin, margin, margin) },
                        )
                    },
                )
                recyclerView = statusWidget.findViewById(R.id.widget_status_recycler)
                statusWidget.setTextData(RegexUtil.createImageStandard(imageUrl))
            }
            waitUntil("feed image row attaches", 5_000L) {
                recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.isShown == true &&
                    onAllNodesWithTag(FEED_SLIDE_IMAGE_CONTENT_TAG, useUnmergedTree = true)
                        .fetchSemanticsNodes()
                        .size == 1
            }
            waitForIdle()

            val expectedHeight = wideImageHeightPixels(hostActivity)
            val imageHeight =
                onNodeWithTag(FEED_SLIDE_IMAGE_CONTENT_TAG, useUnmergedTree = true)
                    .fetchSemanticsNode()
                    .boundsInRoot
                    .height
            assertEquals(expectedHeight.toFloat(), imageHeight, 1f)
            var rowHeight = 0
            runOnUiThread {
                rowHeight = requireNotNull(recyclerView.findViewHolderForAdapterPosition(0)).itemView.height
            }
            assertEquals("Feed row retains the legacy wide-image sizing formula", expectedHeight, rowHeight)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun imageSlideUsesInsideAndCropScalingWithRoundedCorners() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val imageFile = solidImageFile(hostActivity, Color.GREEN, width = 200, height = 400)
            val cropImageFile = solidImageFile(hostActivity, Color.GREEN, width = 200, height = 400)
            try {
                val imageUrl = mutableStateOf(Uri.fromFile(imageFile).toString())
                val centerCrop = mutableStateOf(false)
                runOnUiThread {
                    hostActivity.setContentView(
                        ComposeView(hostActivity).apply {
                            layoutParams =
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                            setContent {
                                AniTrendTheme {
                                    Box(Modifier.fillMaxSize().background(ComposeColor.Magenta)) {
                                        FeedSlideRow(
                                            imageUrl = imageUrl.value,
                                            centerCrop = centerCrop.value,
                                            showPlayback = false,
                                            roundedCorners = true,
                                            onClick = {},
                                        )
                                    }
                                }
                            }
                        },
                    )
                }
                waitUntil("local portrait image decodes", 5_000L) {
                    val images = onAllNodesWithTag(FEED_SLIDE_IMAGE_TAG, useUnmergedTree = true).fetchSemanticsNodes()
                    images.size == 1 && imageCenterPixel(onNodeWithTag(FEED_SLIDE_IMAGE_TAG, useUnmergedTree = true)) == Color.GREEN
                }

                val insideBitmap =
                    onNodeWithTag(FEED_SLIDE_IMAGE_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                val insideMidY = insideBitmap.height / 2
                assertEquals(Color.GREEN, insideBitmap.getPixel(insideBitmap.width / 2, insideMidY))
                assertTrue(
                    "ContentScale.Inside leaves the sides of a portrait image uncovered",
                    insideBitmap.getPixel(0, insideMidY) != Color.GREEN,
                )
                val fittedImageLeft =
                    (0 until insideBitmap.width).first { x -> insideBitmap.getPixel(x, insideMidY) == Color.GREEN }
                val fittedImageTop =
                    (0 until insideBitmap.height).first { y -> insideBitmap.getPixel(insideBitmap.width / 2, y) == Color.GREEN }
                val fittedCornerColor = insideBitmap.getPixel(fittedImageLeft, fittedImageTop)
                assertTrue(
                    "rounded slide corners clip the fitted inside image at ($fittedImageLeft,$fittedImageTop): ${Integer.toHexString(fittedCornerColor)}",
                    fittedCornerColor != Color.GREEN,
                )
                insideBitmap.recycle()

                runOnIdle {
                    imageUrl.value = Uri.fromFile(cropImageFile).toString()
                    centerCrop.value = true
                }
                waitForIdle()
                waitUntil("crop mode fills the slide width with local image pixels", 5_000L) {
                    val bitmap =
                        onNodeWithTag(FEED_SLIDE_IMAGE_TAG, useUnmergedTree = true)
                            .captureToImage()
                            .asAndroidBitmap()
                    try {
                        bitmap.getPixel(bitmap.width / 4, bitmap.height / 2) == Color.GREEN
                    } finally {
                        bitmap.recycle()
                    }
                }
                val cropBitmap =
                    onNodeWithTag(FEED_SLIDE_IMAGE_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                val cropMidY = cropBitmap.height / 2
                assertEquals(Color.GREEN, cropBitmap.getPixel(cropBitmap.width / 4, cropMidY))
                assertTrue(
                    "rounded slide corners clip the cropped image",
                    cropBitmap.getPixel(0, 0) != Color.GREEN,
                )
                cropBitmap.recycle()
            } finally {
                imageFile.delete()
                cropImageFile.delete()
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun reusedPreviewHostClearsTransitionNameForBlankImage() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val imageUrl = "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}"
            val expectedCenterColor = resourceCenterPixel(hostActivity)
            lateinit var recyclerView: RecyclerView
            lateinit var adapter: ImagePreviewAdapter
            runOnUiThread {
                adapter =
                    ImagePreviewAdapter(listOf(RegexUtil.KEY_IMG), hostActivity).apply {
                        onItemsInserted(listOf(imageUrl))
                    }
                recyclerView =
                    RecyclerView(hostActivity).apply {
                        layoutManager = LinearLayoutManager(hostActivity, LinearLayoutManager.HORIZONTAL, false)
                        itemAnimator = null
                        this.adapter = adapter
                    }
                hostActivity.setContentView(recyclerView)
            }
            waitUntil("image preview host receives its shared transition name", 5_000L) {
                val host = recyclerView.findViewHolderForAdapterPosition(0)?.itemView as? ComposeView
                host?.transitionName == imageUrl
            }
            val originalHost = requireNotNull(recyclerView.findViewHolderForAdapterPosition(0)?.itemView as? ComposeView)
            val imageContent = onNodeWithTag(FEED_SLIDE_IMAGE_CONTENT_TAG, useUnmergedTree = true)
            waitUntil("local image is visible before blank rebind", 5_000L) {
                onAllNodesWithTag(FEED_SLIDE_IMAGE_CONTENT_TAG, useUnmergedTree = true).fetchSemanticsNodes().size == 1 &&
                    isColorNear(imageCenterPixel(imageContent), expectedCenterColor)
            }
            val originalPixels = imageContent.captureToImage().asAndroidBitmap()
            try {
                runOnUiThread { adapter.onItemChanged("", 0) }
                waitUntil("reused image preview host clears its transition name", 5_000L) {
                    val host = recyclerView.findViewHolderForAdapterPosition(0)?.itemView as? ComposeView
                    host === originalHost && host.transitionName == null
                }
                waitForIdle()
                val blankImage = onNodeWithTag(FEED_SLIDE_IMAGE_CONTENT_TAG, useUnmergedTree = true)
                blankImage.assertIsDisplayed()
                val blankPixels = blankImage.captureToImage().asAndroidBitmap()
                try {
                    assertTrue("blank image binding removes the previous local pixels", !originalPixels.sameAs(blankPixels))
                    assertTrue("blank image binding leaves a uniform surface", blankPixels.isUniformColor())
                    assertEquals(
                        wideImageHeightPixels(hostActivity).toFloat(),
                        blankImage.fetchSemanticsNode().boundsInRoot.height,
                        1f,
                    )
                } finally {
                    blankPixels.recycle()
                }
            } finally {
                originalPixels.recycle()
            }
        }
    }

    private fun resourceCenterPixel(context: Context): Int {
        val bitmap = requireNotNull(BitmapFactory.decodeResource(context.resources, R.drawable.reg_bg))
        return try {
            bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        } finally {
            bitmap.recycle()
        }
    }

    private fun imageCenterPixel(node: SemanticsNodeInteraction): Int {
        val bitmap = node.captureToImage().asAndroidBitmap()
        return try {
            bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        } finally {
            bitmap.recycle()
        }
    }

    private fun Bitmap.isUniformColor(): Boolean {
        val firstPixel = getPixel(0, 0)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (getPixel(x, y) != firstPixel) return false
            }
        }
        return true
    }

    private fun wideImageHeightPixels(context: Context): Int {
        val maximumHeight = context.resources.getDimensionPixelSize(R.dimen.app_bar_height)
        val defaultMargin = context.resources.getDimensionPixelSize(R.dimen.sm_margin)
        return ((maximumHeight - defaultMargin) * 0.95f).toInt()
    }

    private fun isColorNear(
        actualColor: Int,
        expectedColor: Int,
        tolerance: Int = 16,
    ): Boolean = Color.alpha(actualColor) > 240 &&
        abs(Color.red(actualColor) - Color.red(expectedColor)) <= tolerance &&
        abs(Color.green(actualColor) - Color.green(expectedColor)) <= tolerance &&
        abs(Color.blue(actualColor) - Color.blue(expectedColor)) <= tolerance

    private companion object {
        const val FEED_SLIDE_IMAGE_TAG = "feed-slide-image"
        const val FEED_SLIDE_IMAGE_CONTENT_TAG = "feed-slide-image-content"
        const val FEED_SLIDE_PLAY_TAG = "feed-slide-playback"
    }
}
