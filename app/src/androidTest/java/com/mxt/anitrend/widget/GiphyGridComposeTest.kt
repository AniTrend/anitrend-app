@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.giphy.GiphyGrid
import com.mxt.anitrend.model.entity.giphy.Gif
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil
import java.io.File
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GiphyGridComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun giphyRowsExposeLabelsAndImageSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val gifFile = writeAnimatedGifFixture(hostActivity)
            val originalDecoyFile = solidImageFile(hostActivity, Color.GREEN)
            try {
                val animatedGifUrl = Uri.fromFile(gifFile).toString()
                val originalDecoyUrl = Uri.fromFile(originalDecoyFile).toString()
                val resourceImageUrl = "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}"
                val titledGiphy =
                    giphyFixture(
                        id = "preview-gif",
                        title = "Dancing cat GIF",
                        previewUrl = animatedGifUrl,
                        originalUrl = originalDecoyUrl,
                        originalWidth = "4",
                        originalHeight = "3",
                    )
                val untitledGiphy =
                    giphyFixture(
                        id = "original-resource",
                        title = null,
                        previewUrl = null,
                        originalUrl = resourceImageUrl,
                    )
                val expectedResourceColor = resourceCenterPixel(hostActivity)
                var selected: String? = null
                var previewed: String? = null
                runOnUiThread {
                    installGiphyHost(
                        activity = hostActivity,
                        giphys = listOf(titledGiphy, untitledGiphy),
                        onSelect = { selected = it.id },
                        onPreview = { previewed = it.id },
                    )
                }
                waitForIdle()

                val titleRow = onNodeWithContentDescription("Dancing cat GIF")
                val fallback = hostActivity.getString(R.string.title_giphy_preview)
                val fallbackRow = onNodeWithContentDescription(fallback)
                onAllNodesWithContentDescription("Dancing cat GIF").assertCountEquals(1)
                onAllNodesWithContentDescription(fallback).assertCountEquals(1)
                titleRow.assertIsDisplayed().assertHasClickAction()
                titleRow.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
                fallbackRow.assertIsDisplayed()
                val titleBounds = titleRow.fetchSemanticsNode().boundsInRoot
                assertEquals(4f / 3.3f, titleBounds.width / titleBounds.height, 0.01f)
                var firstGifFrame = Color.TRANSPARENT
                waitUntil("local animated GIF preview is decoded before the original model", 5_000L) {
                    val frameColor = imageCenterPixel(titleRow)
                    if (isGifFrame(frameColor)) {
                        firstGifFrame = frameColor
                        true
                    } else {
                        false
                    }
                }
                val nextGifFrame = if (firstGifFrame == Color.RED) Color.BLUE else Color.RED
                waitUntil("local animated GIF advances to its other frame", 5_000L) {
                    imageCenterPixel(titleRow) == nextGifFrame
                }
                waitUntil("original Android resource is decoded", 5_000L) {
                    isColorNear(imageCenterPixel(fallbackRow), expectedResourceColor)
                }

                val badge = onNodeWithTag(giphyBadgeTag(titledGiphy), useUnmergedTree = true)
                badge.assertIsDisplayed()
                val badgeBitmap = badge.captureToImage().asAndroidBitmap()
                try {
                    val badgeBackground = badgeBitmap.getPixel(0, badgeBitmap.height / 2)
                    assertTrue(
                        "GIPHY badge keeps an opaque background over its image",
                        Color.alpha(badgeBackground) > 240 && badgeBackground != Color.RED && badgeBackground != Color.BLUE,
                    )
                } finally {
                    badgeBitmap.recycle()
                }
                onNodeWithTag(giphyBadgeTag(untitledGiphy), useUnmergedTree = true).assertIsDisplayed()
                titleRow.performClick()
                fallbackRow.performSemanticsAction(SemanticsActions.OnLongClick) { action -> action() }
                assertEquals("preview-gif", selected)
                assertEquals("original-resource", previewed)
            } finally {
                gifFile.delete()
                originalDecoyFile.delete()
            }
        }
    }

    private fun giphyFixture(
        id: String,
        title: String?,
        previewUrl: String?,
        originalUrl: String?,
        originalWidth: String = "1024",
        originalHeight: String = "300",
    ): Giphy {
        val images = hashMapOf<String, Gif>()
        previewUrl?.let { images[KeyUtil.GIPHY_PREVIEW] = Gif(url = it, width = "4", height = "3") }
        originalUrl?.let {
            images[KeyUtil.GIPHY_ORIGINAL_ANIMATED] = Gif(url = it, width = originalWidth, height = originalHeight)
        }
        return Giphy(id = id, title = title, images = images)
    }

    private fun installGiphyHost(
        activity: ProgressLayoutTestActivity,
        giphys: List<Giphy>,
        onSelect: (Giphy) -> Unit,
        onPreview: (Giphy) -> Unit,
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        GiphyGrid(
                            items = giphys,
                            modifier = Modifier.fillMaxSize(),
                            onSelect = onSelect,
                            onPreview = onPreview,
                            onLoadMore = {},
                        )
                    }
                }
            },
        )
    }

    private fun writeAnimatedGifFixture(activity: ProgressLayoutTestActivity): File = File.createTempFile("compose-giphy-fixture-", ".gif", activity.cacheDir).apply {
        writeBytes(Base64.decode(ANIMATED_GIF_FIXTURE_BASE64, Base64.DEFAULT))
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

    private fun isGifFrame(color: Int): Boolean = color == Color.RED || color == Color.BLUE

    private fun isColorNear(
        actualColor: Int,
        expectedColor: Int,
        tolerance: Int = 16,
    ): Boolean = Color.alpha(actualColor) > 240 &&
        abs(Color.red(actualColor) - Color.red(expectedColor)) <= tolerance &&
        abs(Color.green(actualColor) - Color.green(expectedColor)) <= tolerance &&
        abs(Color.blue(actualColor) - Color.blue(expectedColor)) <= tolerance

    private fun giphyBadgeTag(giphy: Giphy): String = "$GIPHY_BADGE_TAG_PREFIX-${giphy.id}"

    private companion object {
        const val GIPHY_BADGE_TAG_PREFIX = "giphy-result-badge"
        const val ANIMATED_GIF_FIXTURE_BASE64 =
            "R0lGODlhBAADAIEAAP8AAAAAAAAAAAAAACH/C05FVFNDQVBFMi4wAwEAAAAh+QQIFAAAACwAAAAABAAD" +
                "AAAICAABCBxIUGBAACH5BAgUAAAALAAAAAAEAAMAgQAA/wAAAAAAAAAAAAgIAAEIHEhQYEAAOw=="
    }
}
