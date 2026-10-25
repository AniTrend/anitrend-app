@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.ui.character.CharacterGrid
import com.mxt.anitrend.ui.character.CharacterRowUiState
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val CHARACTER_CARD_TAG = "character-row-card"
private const val CHARACTER_IMAGE_TAG = "character-row-image"
private const val FAVORITE_INDICATOR_TAG = "character-row-favorite-indicator"

@RunWith(AndroidJUnit4::class)
class CharacterGridComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun groupedRowsExposeCharacterNamesFavoriteStateImageAndClickableCard() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val characters =
                listOf(
                    characterFixture(hostActivity, 1L, "Spike", "Spiegel", true),
                    characterFixture(hostActivity, 2L, "Faye", "Valentine", false),
                )
            runOnUiThread {
                installCharacterGridHost(
                    hostActivity,
                    characters.map { CharacterRowUiState.from(it) },
                )
            }
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
        var host: CharacterGridHost? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val character = characterFixture(hostActivity, 1L, "Spike", "Spiegel", true)
            runOnUiThread {
                host = installCharacterGridHost(hostActivity, listOf(CharacterRowUiState.from(character)))
            }
            val characterHost = requireNotNull(host)
            waitUntil("grouped character row binds", 5_000L) {
                onAllNodesWithTag(CHARACTER_CARD_TAG, useUnmergedTree = true).fetchSemanticsNodes().size == 1
            }
            waitForIdle()

            onNodeWithText("Spike Spiegel", useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(FAVORITE_INDICATOR_TAG, useUnmergedTree = true).assertIsDisplayed()

            character.name = TitleBase("Faye", "Valentine", null, null)
            character.isFavourite = false
            character.image = null
            runOnUiThread {
                characterHost.items.value = listOf(CharacterRowUiState.from(character))
            }
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
            runOnUiThread { characterHost.items.value = emptyList() }
            waitUntil("removed character image semantics are absent", 5_000L) {
                onAllNodesWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
        }
    }

    private fun installCharacterGridHost(
        activity: ProgressLayoutTestActivity,
        initialItems: List<CharacterRowUiState>,
    ): CharacterGridHost {
        val items = mutableStateOf(initialItems)
        activity.setContentView(
            ComposeView(activity).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        CharacterGrid(
                            items = items.value,
                            modifier = Modifier.fillMaxSize(),
                            onCharacterClick = {},
                            onLoadMore = {},
                        )
                    }
                }
            },
        )
        return CharacterGridHost(items)
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

    private data class CharacterGridHost(
        val items: MutableState<List<CharacterRowUiState>>,
    )
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
