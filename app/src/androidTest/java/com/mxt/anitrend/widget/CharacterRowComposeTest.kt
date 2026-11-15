@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.MediaCharacterAdapter
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterRowComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun groupedRowsExposeCharacterNamesFavoriteStateImageAndClickableCard() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val rows =
                listOf(
                    characterFixture(hostActivity, 1, "Spike", "Spiegel", true),
                    characterFixture(hostActivity, 2, "Faye", "Valentine", false),
                )
            val adapter = GroupCharacterAdapter(hostActivity).apply { onItemsInserted(rows) }
            val recyclerView = characterRecyclerView(hostActivity, adapter)
            runOnUiThread { installComposeHost(hostActivity, recyclerView) }
            waitUntil("grouped character rows bind", 5_000L) {
                rows.indices.all { recyclerView.findViewHolderForAdapterPosition(it) != null }
            }
            waitForIdle()
            waitUntil("local character covers show visible pixels", 60_000L) {
                val images = onAllNodesWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)
                val imageNodes = images.fetchSemanticsNodes()
                imageNodes.size == 2 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels() &&
                    images[1].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }

            assertEquals(2, onAllNodesWithTag(CHARACTER_CARD_TAG, useUnmergedTree = true).fetchSemanticsNodes().size)
            assertEquals(2, onAllNodesWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true).fetchSemanticsNodes().size)
            assertEquals(1, onAllNodesWithTag(FAVORITE_INDICATOR_TAG, useUnmergedTree = true).fetchSemanticsNodes().size)
            onNodeWithText("Spike Spiegel", useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("Faye Valentine", useUnmergedTree = true).assertIsDisplayed()
            onAllNodesWithTag(CHARACTER_CARD_TAG, useUnmergedTree = true)[0].assertHasClickAction()
            assertTrue(
                "the first character cover should display local image pixels",
                onAllNodesWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)[0]
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
            assertTrue(
                "the second character cover should display local image pixels",
                onAllNodesWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)[1]
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun groupedRowRebindReflectsSameMutableCharacterChanges() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val character = characterFixture(hostActivity, 1, "Spike", "Spiegel", true)
            val adapter = GroupCharacterAdapter(hostActivity).apply { onItemsInserted(listOf(character)) }
            val recyclerView = characterRecyclerView(hostActivity, adapter)
            runOnUiThread { installComposeHost(hostActivity, recyclerView) }
            waitUntil("grouped character row binds", 5_000L) {
                recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            onNodeWithText("Spike Spiegel", useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(FAVORITE_INDICATOR_TAG, useUnmergedTree = true).assertIsDisplayed()

            character.name = TitleBase("Faye", "Valentine", null, null)
            character.isFavourite = false
            character.image = null
            runOnUiThread { adapter.onItemsInserted(listOf(character)) }
            waitForIdle()

            onNodeWithText("Faye Valentine", useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("Spike Spiegel", useUnmergedTree = true).assertDoesNotExist()
            onNodeWithTag(FAVORITE_INDICATOR_TAG, useUnmergedTree = true).assertDoesNotExist()
            waitUntil("rebound character image slot clears for a null model", 5_000L) {
                val images = onAllNodesWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasUniformImageSurface()
            }
            assertTrue(
                "a null character image must leave its slot empty",
                onNodeWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasUniformImageSurface(),
            )
            runOnUiThread { adapter.onItemsInserted(emptyList()) }
            waitUntil("recycled character image semantics are absent", 5_000L) {
                onAllNodesWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun mediaCharacterRowsExposeNameAndClickableCard() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val row = characterFixture(hostActivity, 3, "Edward", "Wong", false)
            val adapter = MediaCharacterAdapter { _, _ -> }.apply { submitList(listOf(row)) }
            val recyclerView = characterRecyclerView(hostActivity, adapter)
            runOnUiThread { installComposeHost(hostActivity, recyclerView) }
            waitUntil("media character row binds", 5_000L) {
                recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()
            waitUntil("local media character cover shows visible pixels", 60_000L) {
                val images = onAllNodesWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }

            onNodeWithTag(CHARACTER_CARD_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("Edward Wong", useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(CHARACTER_CARD_TAG, useUnmergedTree = true).assertHasClickAction()
            assertTrue(
                "the media character cover should display local image pixels",
                onNodeWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
            runOnUiThread { adapter.submitList(emptyList()) }
            waitUntil("removed media character image semantics are absent", 5_000L) {
                adapter.currentList.isEmpty() &&
                    onAllNodesWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)
                        .fetchSemanticsNodes()
                        .isEmpty()
            }
        }
    }

    private fun characterRecyclerView(
        activity: ProgressLayoutTestActivity,
        adapter: RecyclerView.Adapter<*>,
    ): RecyclerView = RecyclerView(activity).apply {
        setHasFixedSize(true)
        layoutManager =
            StaggeredGridLayoutManager(
                activity.resources.getInteger(R.integer.grid_giphy_x3),
                StaggeredGridLayoutManager.VERTICAL,
            )
        itemAnimator = null
        this.adapter = adapter
    }

    private fun installComposeHost(
        activity: ProgressLayoutTestActivity,
        recyclerView: RecyclerView,
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                id = android.view.View.generateViewId()
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
    }

    private fun characterFixture(
        activity: ProgressLayoutTestActivity,
        id: Long,
        first: String,
        last: String,
        isFavourite: Boolean,
    ): CharacterBase {
        val imageUrl = "android.resource://${activity.packageName}/${R.drawable.reg_bg}"
        return CharacterBase().apply {
            this.id = id
            name = TitleBase(first, last, null, null)
            image = ImageBase(extraLarge = null, large = imageUrl, medium = null)
            this.isFavourite = isFavourite
        }
    }

    private companion object {
        const val CHARACTER_CARD_TAG = "character-row-card"
        const val CHARACTER_IMAGE_TAG = "character-row-image"
        const val FAVORITE_INDICATOR_TAG = "character-row-favorite-indicator"
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
