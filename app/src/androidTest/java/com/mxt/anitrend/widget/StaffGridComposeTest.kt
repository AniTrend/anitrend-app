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
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.staff.StaffGrid
import com.mxt.anitrend.ui.staff.StaffRowUiState
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val STAFF_ROW_TAG = "staff-row-card"
private const val STAFF_IMAGE_TAG = "staff-row-image"
private const val STAFF_FAVORITE_TAG = "staff-row-favorite"

@RunWith(AndroidJUnit4::class)
class StaffGridComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun favoriteStaffRowExposesContentImageAndClickSemantics() {
        var host: StaffGridHost? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val staff = staffFixture(hostActivity, language = "japanese", isFavourite = true)
            runOnUiThread {
                host = installStaffGridHost(hostActivity, listOf(StaffRowUiState.from(staff)))
            }
            requireNotNull(host)
            waitUntil("local staff image shows visible pixels", 30_000L) {
                val images = onAllNodesWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            waitForIdle()

            onNodeWithTag(STAFF_ROW_TAG, useUnmergedTree = true)
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithText("Akira Toriyama").assertIsDisplayed()
            onNodeWithText("Japanese", substring = true).assertIsDisplayed()
            onNodeWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true).assertIsDisplayed()
            assertTrue(
                "the staff cover should display local image pixels",
                onNodeWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
            onNodeWithTag(STAFF_FAVORITE_TAG, useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun nonFavoriteStaffRowHidesOptionalContentAndRebindsMutableState() {
        var host: StaffGridHost? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val staff = staffFixture(hostActivity, language = null, isFavourite = false)
            val imageUrl = requireNotNull(staff.image?.large)
            runOnUiThread {
                host = installStaffGridHost(hostActivity, listOf(StaffRowUiState.from(staff)))
            }
            val staffHost = requireNotNull(host)
            waitUntil("local staff image shows visible pixels", 30_000L) {
                val images = onAllNodesWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            waitForIdle()

            onNodeWithTag(STAFF_ROW_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("Akira Toriyama").assertIsDisplayed()
            onNodeWithText("Japanese").assertDoesNotExist()
            onNodeWithTag(STAFF_FAVORITE_TAG, useUnmergedTree = true).assertDoesNotExist()

            runOnUiThread {
                staff.language = "japanese"
                staff.isFavourite = true
                staff.image = null
                staffHost.items.value = listOf(StaffRowUiState.from(staff))
            }
            waitUntil("favorite and language rebind", 5_000L) {
                onAllNodesWithTag(STAFF_FAVORITE_TAG, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithText("Japanese", substring = true).fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag(STAFF_FAVORITE_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("Japanese", substring = true).assertIsDisplayed()
            waitUntil("staff image slot clears when its model becomes null", 5_000L) {
                val images = onAllNodesWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasUniformImageSurface()
            }
            assertTrue(
                "a null staff image must leave its slot empty",
                onNodeWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasUniformImageSurface(),
            )

            runOnUiThread {
                staff.image = ImageBase(extraLarge = null, large = imageUrl, medium = null)
                staffHost.items.value = listOf(StaffRowUiState.from(staff))
            }
            waitUntil("rebound staff image shows local pixels", 30_000L) {
                val images = onAllNodesWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            assertTrue(
                "a rebound staff image must display local image pixels",
                onNodeWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
        }
    }

    /** Removing a lazy row releases its image and composed content. */
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun removingStaffRowReleasesImageAndComposeContent() {
        var host: StaffGridHost? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val staff = staffFixture(hostActivity, language = "japanese", isFavourite = true)
            runOnUiThread {
                host = installStaffGridHost(hostActivity, listOf(StaffRowUiState.from(staff)))
            }
            val staffHost = requireNotNull(host)

            waitUntil("local staff image shows visible pixels", 30_000L) {
                val images = onAllNodesWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            waitForIdle()

            runOnUiThread { staffHost.items.value = emptyList() }
            waitUntil("removed staff row and image semantics disappear", 5_000L) {
                onAllNodesWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true).fetchSemanticsNodes().isEmpty() &&
                    onAllNodesWithTag(STAFF_ROW_TAG, useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true).assertDoesNotExist()
            onNodeWithTag(STAFF_ROW_TAG, useUnmergedTree = true).assertDoesNotExist()
        }
    }

    private fun installStaffGridHost(
        activity: ProgressLayoutTestActivity,
        initialItems: List<StaffRowUiState>,
    ): StaffGridHost {
        val items = mutableStateOf(initialItems)
        activity.setContentView(
            ComposeView(activity).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        StaffGrid(
                            items = items.value,
                            modifier = Modifier.fillMaxSize(),
                            onStaffClick = {},
                            onLoadMore = {},
                        )
                    }
                }
            },
        )
        return StaffGridHost(items)
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

    private data class StaffGridHost(
        val items: MutableState<List<StaffRowUiState>>,
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
