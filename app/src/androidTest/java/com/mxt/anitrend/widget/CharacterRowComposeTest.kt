@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.ui.character.CharacterRowUiState
import com.mxt.anitrend.ui.detail.DetailGridRow
import com.mxt.anitrend.ui.detail.DetailListGrid
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterRowComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun mediaCharacterRowsExposeNameAndClickableCard() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val row = characterFixture(hostActivity, 3, "Edward", "Wong", false)
            val callbackTarget = View(hostActivity).apply { id = R.id.container }
            var clickTargetId = View.NO_ID
            val rows = mutableStateOf(
                listOf(
                    DetailGridRow.Character(
                        key = "role:character:${row.id}",
                        state = CharacterRowUiState.from(row),
                        onClick = { target -> clickTargetId = target.id },
                    ),
                ),
            )
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
            waitUntil("local media character cover shows visible pixels", 60_000L) {
                val images = onAllNodesWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }

            onNodeWithTag(CHARACTER_CARD_TAG, useUnmergedTree = true)
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
            onNodeWithText("Edward Wong", useUnmergedTree = true).assertIsDisplayed()
            assertTrue("detail actions receive the container host", clickTargetId == R.id.container)
            assertTrue(
                "the media character cover should display local image pixels",
                onNodeWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
            runOnIdle { rows.value = emptyList() }
            waitUntil("removed media character image semantics are absent", 5_000L) {
                onAllNodesWithTag(CHARACTER_IMAGE_TAG, useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
        }
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
