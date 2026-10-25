package com.mxt.anitrend.widget

import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.ui.detail.DetailGridRow
import com.mxt.anitrend.ui.detail.DetailListGrid
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.staff.StaffRowUiState
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val STAFF_ROW_TAG = "staff-row-card"
private const val STAFF_IMAGE_TAG = "staff-row-image"
private const val STAFF_FAVORITE_TAG = "staff-row-favorite"

@RunWith(AndroidJUnit4::class)
class StaffRowHostComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun characterActorsStaffRowShowsCoverAndSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val staff = staffFixture(hostActivity, language = "japanese", isFavourite = true)
            val callbackTarget = View(hostActivity).apply { id = R.id.container }
            var clickedStaff: StaffBase? = null
            var clickedTargetId = View.NO_ID
            hostActivity.installComposeContent {
                AniTrendTheme {
                    DetailListGrid(
                        rows =
                        listOf(
                            DetailGridRow.Staff(
                                key = "actor:501:role:staff:${staff.id}",
                                state = StaffRowUiState.from(staff),
                                onClick = { target ->
                                    clickedStaff = staff
                                    clickedTargetId = target.id
                                },
                            ),
                        ),
                        gridState = rememberLazyGridState(),
                        callbackTarget = callbackTarget,
                        onLoadMore = {},
                    )
                }
            }

            waitUntil("character staff cover shows local image pixels", 30_000L) {
                val images = onAllNodesWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            assertTrue(
                "the character staff row should show image pixels",
                onNodeWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
            onNodeWithText("Akira Toriyama").assertIsDisplayed()
            onNodeWithTag(STAFF_FAVORITE_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(STAFF_ROW_TAG, useUnmergedTree = true)
                .assertHasClickAction()
                .performClick()
            assertSame(staff, clickedStaff)
            assertTrue("the actor staff callback keeps its container target", clickedTargetId == R.id.container)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun mediaStaffRoleRowShowsCoverAndSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val staff = staffFixture(hostActivity, language = "japanese", isFavourite = true)
            val callbackTarget = View(hostActivity).apply { id = R.id.container }
            var clickedStaff: StaffBase? = null
            var clickedTargetId = View.NO_ID
            hostActivity.installComposeContent {
                AniTrendTheme {
                    DetailListGrid(
                        rows =
                        listOf(
                            DetailGridRow.Header(
                                key = "group:voice-actor:header",
                                title = "Voice Actor",
                                count = 1,
                            ),
                            DetailGridRow.Staff(
                                key = "group:voice-actor:staff:${staff.id}",
                                state = StaffRowUiState.from(staff),
                                onClick = { target ->
                                    clickedStaff = staff
                                    clickedTargetId = target.id
                                },
                            ),
                        ),
                        gridState = rememberLazyGridState(),
                        callbackTarget = callbackTarget,
                        onLoadMore = {},
                    )
                }
            }

            waitUntil("media staff role cover shows local image pixels", 30_000L) {
                val images = onAllNodesWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            assertTrue(
                "the media staff role row should show image pixels",
                onNodeWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
            onNodeWithText("Voice Actor").assertIsDisplayed()
            onNodeWithText("Akira Toriyama").assertIsDisplayed()
            onNodeWithTag(STAFF_FAVORITE_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(STAFF_ROW_TAG, useUnmergedTree = true)
                .assertHasClickAction()
                .performClick()
            assertSame(staff, clickedStaff)
            assertTrue("the media staff callback keeps its container target", clickedTargetId == R.id.container)
        }
    }

    private fun staffFixture(
        activity: ProgressLayoutTestActivity,
        language: String?,
        isFavourite: Boolean,
    ): StaffBase {
        val imageUrl = "android.resource://${activity.packageName}/${R.drawable.reg_bg}"
        return StaffBase().apply {
            id = 501L
            name = TitleBase(first = "Akira", last = "Toriyama", original = null, alternative = null)
            image = ImageBase(extraLarge = null, large = imageUrl, medium = null)
            this.language = language
            this.isFavourite = isFavourite
        }
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
