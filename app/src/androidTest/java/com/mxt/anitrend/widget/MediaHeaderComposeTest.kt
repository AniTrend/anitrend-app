@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.widget

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.group.RecyclerHeaderItem
import com.mxt.anitrend.ui.detail.projectGroupedMediaSeriesRows
import com.mxt.anitrend.ui.mediasearch.MEDIA_SERIES_CARD_TAG
import com.mxt.anitrend.ui.character.CharacterMediaHeaderUiState
import com.mxt.anitrend.ui.detail.DetailGridRow
import com.mxt.anitrend.ui.detail.DetailListGrid
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.minutes

@RunWith(AndroidJUnit4::class)
class MediaHeaderComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun mediaHeaderExposesContentAndActions() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val imageUrl = "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}"
            val media =
                MediaBase().apply {
                    id = 501L
                    type = KeyUtil.ANIME
                    format = KeyUtil.TV
                    status = KeyUtil.RELEASING
                    title = MediaTitle(null, null, null, "The Alternate Story")
                    coverImage = ImageBase(null, imageUrl, null)
                }
            val callbackTarget = View(hostActivity).apply { id = R.id.container }
            var clickedTargetId = View.NO_ID
            var longClickedTargetId = View.NO_ID
            var clickedMedia: MediaBase? = null
            var longClickedMedia: MediaBase? = null
            val initialState = CharacterMediaHeaderUiState.from(media).copy(subGroupTitle = "Supporting Role")
            val headerRow =
                DetailGridRow.CharacterMediaHeader(
                    key = "actor:501:supporting:media",
                    state = initialState,
                    onClick = { target ->
                        clickedTargetId = target.id
                        clickedMedia = media
                    },
                    onLongClick = { target ->
                        longClickedTargetId = target.id
                        longClickedMedia = media
                    },
                )
            val rows = mutableStateOf(listOf(headerRow))
            hostActivity.installComposeContent {
                AniTrendTheme {
                    DetailListGrid(
                        rows = rows.value,
                        gridState = rememberLazyGridState(),
                        callbackTarget = callbackTarget,
                        onLoadMore = {},
                    )
                }
            }
            waitUntil("media header image shows local cover pixels", 60_000L) {
                val images = onAllNodesWithTag(MEDIA_HEADER_IMAGE_TAG, useUnmergedTree = true)
                val imageNodes = images.fetchSemanticsNodes()
                imageNodes.size == 1 && images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            waitForIdle()
            var statusColor = 0
            runOnUiThread {
                val statusView = requireNotNull(findSeriesStatusWidget(hostActivity.window.decorView))
                statusColor =
                    requireNotNull(statusView.background as? android.graphics.drawable.ColorDrawable).color
            }
            org.junit.Assert.assertEquals(
                androidx.core.content.ContextCompat.getColor(hostActivity, R.color.colorStateBlue),
                statusColor,
            )

            onNodeWithTag(MEDIA_HEADER_CARD_TAG, useUnmergedTree = true)
                .assertIsDisplayed()
                .assertHasClickAction()
                .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
                .performClick()
            onNodeWithTag(MEDIA_HEADER_CARD_TAG, useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.OnLongClick) { action -> action() }
            onNodeWithTag(MEDIA_HEADER_IMAGE_TAG, useUnmergedTree = true).assertIsDisplayed()
            assertTrue(
                "the media header cover should display local image pixels",
                onNodeWithTag(MEDIA_HEADER_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
            onNodeWithTag(MEDIA_HEADER_STATUS_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("The Alternate Story").assertIsDisplayed()
            onNodeWithText("Supporting Role", substring = true).assertIsDisplayed()
            onNodeWithText("TV", substring = true).assertIsDisplayed()
            assertTrue("media click retains the container target", clickedTargetId == R.id.container)
            assertTrue("media long-click retains the container target", longClickedTargetId == R.id.container)
            assertSame(media, clickedMedia)
            assertSame(media, longClickedMedia)

            runOnIdle { rows.value = listOf(headerRow.copy(state = initialState.copy(coverImageUrl = null))) }
            waitUntil("media header cover clears when its model becomes null", 5_000L) {
                val images = onAllNodesWithTag(MEDIA_HEADER_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasUniformImageSurface()
            }
            assertTrue(
                "a null media cover must leave the image slot empty",
                onNodeWithTag(MEDIA_HEADER_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasUniformImageSurface(),
            )

            runOnIdle {
                rows.value = listOf(headerRow.copy(state = initialState.copy(coverImageUrl = imageUrl)))
            }
            waitUntil("rebound media header cover shows local image pixels", 60_000L) {
                val images = onAllNodesWithTag(MEDIA_HEADER_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            assertTrue(
                "a rebound media cover must display local image pixels",
                onNodeWithTag(MEDIA_HEADER_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )

            runOnIdle { rows.value = emptyList() }
            waitUntil("removed media header image semantics are absent", 5_000L) {
                onAllNodesWithTag(MEDIA_HEADER_IMAGE_TAG, useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun groupedMediaSeriesForwardsBothActionsWithTheContainerTarget() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val media =
                MediaBase().apply {
                    id = 502L
                    type = KeyUtil.ANIME
                    format = KeyUtil.TV
                    status = KeyUtil.RELEASING
                    title = MediaTitle(null, null, null, "Detail media series")
                }
            val callbackTarget = View(hostActivity).apply { id = R.id.container }
            var clickedTargetId = View.NO_ID
            var longClickedTargetId = View.NO_ID
            var clickedMedia: MediaBase? = null
            var longClickedMedia: MediaBase? = null
            val rows =
                projectGroupedMediaSeriesRows(
                    context = hostActivity,
                    items = listOf(RecyclerHeaderItem("Relations", 1), media),
                    onMediaClick = { target, item ->
                        clickedTargetId = target.id
                        clickedMedia = item
                    },
                    onMediaLongClick = { target, item ->
                        longClickedTargetId = target.id
                        longClickedMedia = item
                    },
                )
            hostActivity.installComposeContent {
                AniTrendTheme {
                    DetailListGrid(
                        rows = rows,
                        gridState = rememberLazyGridState(),
                        callbackTarget = callbackTarget,
                        onLoadMore = {},
                    )
                }
            }

            waitForIdle()
            onNodeWithTag(MEDIA_SERIES_CARD_TAG, useUnmergedTree = true)
                .assertHasClickAction()
                .performClick()
            onNodeWithTag(MEDIA_SERIES_CARD_TAG, useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.OnLongClick) { action -> action() }
            assertTrue("media click keeps the container target", clickedTargetId == R.id.container)
            assertTrue("media long-click keeps the container target", longClickedTargetId == R.id.container)
            assertSame(media, clickedMedia)
            assertSame(media, longClickedMedia)
        }
    }

    private fun findSeriesStatusWidget(view: View): com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget? {
        if (view is com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findSeriesStatusWidget(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    private companion object {
        const val MEDIA_HEADER_CARD_TAG = "media-header-card"
        const val MEDIA_HEADER_IMAGE_TAG = "media-header-image"
        const val MEDIA_HEADER_STATUS_TAG = "media-header-status"
    }
}

private fun Bitmap.hasImageContentPixels(): Boolean {
    val colors = mutableSetOf<Int>()
    val startX = width / 5
    val endX = width - startX
    val startY = height / 5
    val endY = height - startY
    val stepX = maxOf(1, width / 12)
    val stepY = maxOf(1, height / 12)

    for (y in startY until endY step stepY) {
        for (x in startX until endX step stepX) {
            val color = getPixel(x, y)
            if (android.graphics.Color.alpha(color) > 240) {
                colors += color
                if (colors.size >= 8) return true
            }
        }
    }
    return false
}

private fun Bitmap.hasUniformImageSurface(): Boolean {
    val centerColor = getPixel(width / 2, height / 2)
    val startX = width / 5
    val endX = width - startX
    val startY = height / 5
    val endY = height - startY
    val stepX = maxOf(1, width / 40)
    val stepY = maxOf(1, height / 40)

    for (y in startY until endY step stepY) {
        for (x in startX until endX step stepX) {
            if (getPixel(x, y) != centerColor) return false
        }
    }
    return true
}
