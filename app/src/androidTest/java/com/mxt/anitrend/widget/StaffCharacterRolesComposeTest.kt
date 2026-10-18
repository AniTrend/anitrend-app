@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.StaffCharacterRolesAdapter
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.CharacterStaffBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StaffCharacterRolesComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun roleRowsExposeCharacterAndMediaTitles() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val rows =
                listOf(
                    staffFixture(hostActivity, 101, "Compose staff character", "Compose staff media", true),
                    staffFixture(hostActivity, 102, "Standard staff character", "Standard staff media", false),
                )
            lateinit var host: StaffRoleHost
            runOnUiThread { host = installStaffHost(hostActivity, rows) }
            waitUntil("staff role rows bind", 5_000L) {
                (0..1).all { host.recyclerView.findViewHolderForAdapterPosition(it) != null }
            }
            waitForIdle()
            waitUntil("staff role covers show local image pixels", 60_000L) {
                val images = onAllNodesWithTag(STAFF_ROLE_IMAGE_TAG, useUnmergedTree = true)
                val imageNodes = images.fetchSemanticsNodes()
                imageNodes.size == 2 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels() &&
                    images[1].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }

            assertTrue(
                "the first staff role cover should show local image pixels",
                onAllNodesWithTag(STAFF_ROLE_IMAGE_TAG, useUnmergedTree = true)[0]
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
            assertTrue(
                "the second staff role cover should show local image pixels",
                onAllNodesWithTag(STAFF_ROLE_IMAGE_TAG, useUnmergedTree = true)[1]
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
            onAllNodesWithText("Compose staff character").assertCountEquals(1)
            onNodeWithText("Compose staff character").assertIsDisplayed().assertHasClickAction()
            onAllNodesWithText("Compose staff media").assertCountEquals(1)
            onNodeWithText("Compose staff media").assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun favoriteIndicatorFollowsCharacterFavoriteState() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val standard =
                staffFixture(
                    hostActivity,
                    101,
                    "Standard character",
                    "Standard media",
                    false,
                )
            val favorite =
                staffFixture(
                    hostActivity,
                    101,
                    "Favorite character",
                    "Favorite media",
                    true,
                )
            val missingCover =
                staffFixture(
                    hostActivity,
                    101,
                    "Favorite character",
                    "Favorite media",
                    false,
                    hasImage = false,
                )
            val updated =
                staffFixture(
                    hostActivity,
                    101,
                    "Updated character",
                    "Updated media",
                    false,
                )
            lateinit var host: StaffRoleHost
            runOnUiThread { host = installStaffHost(hostActivity, listOf(standard)) }
            waitUntil("standard staff row binds", 5_000L) {
                host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitUntil("initial role cover shows local image pixels", 10_000L) {
                val images = onAllNodesWithTag(STAFF_ROLE_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            onNodeWithTag(FAVORITE_INDICATOR_TAG, useUnmergedTree = true).assertDoesNotExist()

            runOnUiThread { host.adapter.submitList(listOf(favorite)) }
            waitUntil("same character rebind shows favorite state", 10_000L) {
                onAllNodesWithTag(FAVORITE_INDICATOR_TAG, useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .size == 1
            }
            waitUntil("same character keeps its cover", 10_000L) {
                val imageNodes = onAllNodesWithTag(STAFF_ROLE_IMAGE_TAG, useUnmergedTree = true)
                imageNodes.fetchSemanticsNodes().size == 1 &&
                    imageNodes[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            onNodeWithTag(FAVORITE_INDICATOR_TAG, useUnmergedTree = true).assertIsDisplayed()

            runOnUiThread { host.adapter.submitList(listOf(missingCover)) }
            waitUntil("same character rebind clears its old cover", 5_000L) {
                val favoriteNodes = onAllNodesWithTag(FAVORITE_INDICATOR_TAG, useUnmergedTree = true)
                val imageNodes = onAllNodesWithTag(STAFF_ROLE_IMAGE_TAG, useUnmergedTree = true)
                favoriteNodes.fetchSemanticsNodes().isEmpty() &&
                    imageNodes.fetchSemanticsNodes().size == 1 &&
                    imageNodes[0].captureToImage().asAndroidBitmap().hasUniformImageSurface()
            }
            onNodeWithTag(FAVORITE_INDICATOR_TAG, useUnmergedTree = true).assertDoesNotExist()
            assertTrue(
                "same-character rebind clears the previous cover pixels",
                onNodeWithTag(STAFF_ROLE_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasUniformImageSurface(),
            )

            runOnUiThread { host.adapter.submitList(listOf(updated)) }
            waitUntil("same character rebind restores its cover", 10_000L) {
                val images = onAllNodesWithTag(STAFF_ROLE_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            assertTrue(
                "same-character rebind restores local cover pixels",
                onNodeWithTag(STAFF_ROLE_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun removingRoleRowRemovesImageSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val row = staffFixture(hostActivity, 101, "Staff character", "Staff media", true)
            lateinit var host: StaffRoleHost
            runOnUiThread { host = installStaffHost(hostActivity, listOf(row)) }
            waitUntil("staff role cover shows local image pixels", 60_000L) {
                val images = onAllNodesWithTag(STAFF_ROLE_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }

            runOnUiThread { host.adapter.submitList(emptyList()) }
            waitUntil("removed staff role image semantics are absent", 5_000L) {
                host.adapter.currentList.isEmpty() &&
                    onAllNodesWithTag(STAFF_ROLE_IMAGE_TAG, useUnmergedTree = true)
                        .fetchSemanticsNodes()
                        .isEmpty()
            }
        }
    }

    private fun installStaffHost(
        activity: ProgressLayoutTestActivity,
        rows: List<CharacterStaffBase>,
    ): StaffRoleHost {
        val adapter = StaffCharacterRolesAdapter { _, _ -> }.apply { submitList(rows) }
        val recyclerView =
            RecyclerView(activity).apply {
                setHasFixedSize(true)
                layoutManager =
                    StaggeredGridLayoutManager(
                        activity.resources.getInteger(R.integer.grid_giphy_x3),
                        StaggeredGridLayoutManager.VERTICAL,
                    )
                itemAnimator = null
                this.adapter = adapter
            }
        activity.setContentView(
            ComposeView(activity).apply {
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
        return StaffRoleHost(adapter, recyclerView)
    }

    private data class StaffRoleHost(
        val adapter: StaffCharacterRolesAdapter,
        val recyclerView: RecyclerView,
    )

    private companion object {
        const val FAVORITE_INDICATOR_TAG = "staff-role-favorite-indicator"
        const val STAFF_ROLE_IMAGE_TAG = "staff-role-image"
    }

    private fun staffFixture(
        activity: ProgressLayoutTestActivity,
        characterId: Long,
        characterName: String,
        mediaTitle: String,
        isCharacterFavourite: Boolean,
        hasImage: Boolean = true,
    ): CharacterStaffBase {
        val imageUri = if (hasImage) "android.resource://${activity.packageName}/${R.drawable.reg_bg}" else null
        val character =
            CharacterBase().apply {
                id = characterId
                name = TitleBase(characterName, null, null, null)
                image = imageUri?.let { ImageBase(it, it, it) }
                this.isFavourite = isCharacterFavourite
            }
        val media =
            MediaBase().apply {
                id = characterId + 1000
                title = MediaTitle(null, null, null, mediaTitle)
            }
        return CharacterStaffBase(character, media)
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
