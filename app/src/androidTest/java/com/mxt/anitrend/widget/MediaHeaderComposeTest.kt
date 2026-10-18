@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.widget

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.CharacterActorsAdapter
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil
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
                    contentType = KeyUtil.RECYCLER_TYPE_HEADER
                    subGroupTitle = "Supporting Role"
                }
            val adapter =
                CharacterActorsAdapter(
                    onMediaClick =
                    object : ItemClickListener<RecyclerItem> {
                        override fun onItemClick(
                            target: View,
                            data: IndexedValue<RecyclerItem>,
                        ) = Unit

                        override fun onItemLongClick(
                            target: View,
                            data: IndexedValue<RecyclerItem>,
                        ) = Unit
                    },
                    onStaffClick = { _, _ -> },
                )
            lateinit var recyclerView: RecyclerView
            runOnUiThread {
                recyclerView =
                    RecyclerView(hostActivity).apply {
                        setHasFixedSize(true)
                        layoutManager =
                            StaggeredGridLayoutManager(
                                hostActivity.resources.getInteger(R.integer.grid_giphy_x3),
                                StaggeredGridLayoutManager.VERTICAL,
                            )
                        itemAnimator = null
                        this.adapter = adapter
                    }
                hostActivity.setContentView(
                    ComposeView(hostActivity).apply {
                        id = View.generateViewId()
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
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
                adapter.submitList(listOf(media))
            }
            waitUntil("media header image shows local cover pixels", 60_000L) {
                val images = onAllNodesWithTag(MEDIA_HEADER_IMAGE_TAG, useUnmergedTree = true)
                val imageNodes = images.fetchSemanticsNodes()
                imageNodes.size == 1 && images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            waitForIdle()
            var statusColor = 0
            runOnUiThread {
                val row = requireNotNull(recyclerView.findViewHolderForAdapterPosition(0)).itemView
                val statusView = requireNotNull(findSeriesStatusWidget(row))
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
            runOnUiThread {
                media.coverImage = null
                adapter.notifyItemChanged(0)
            }
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

            runOnUiThread {
                media.coverImage = ImageBase(extraLarge = null, large = imageUrl, medium = null)
                adapter.notifyItemChanged(0)
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

            runOnUiThread { adapter.submitList(emptyList()) }
            waitUntil("removed media header image semantics are absent", 5_000L) {
                adapter.currentList.isEmpty() &&
                    onAllNodesWithTag(MEDIA_HEADER_IMAGE_TAG, useUnmergedTree = true)
                        .fetchSemanticsNodes()
                        .isEmpty()
            }
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
