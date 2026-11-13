package com.mxt.anitrend.widget

import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.StaffAdapter
import com.mxt.anitrend.adapter.recycler.detail.CharacterActorsAdapter
import com.mxt.anitrend.adapter.recycler.detail.MediaStaffRoleAdapter
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.minutes

private const val STAFF_ROW_TAG = "staff-row-card"
private const val STAFF_IMAGE_TAG = "staff-row-image"
private const val STAFF_FAVORITE_TAG = "staff-row-favorite"

@RunWith(AndroidJUnit4::class)
class StaffAdapterComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun favoriteStaffRowExposesContentImageAndClickSemantics() {
        var host: StaffTestHost? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val staff = staffFixture(hostActivity, language = "japanese", isFavourite = true)
            runOnUiThread {
                host = installStaffHost(hostActivity, staff)
            }
            val staffHost = requireNotNull(host)
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
        var host: StaffTestHost? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val staff = staffFixture(hostActivity, language = null, isFavourite = false)
            val imageUrl = requireNotNull(staff.image?.large)
            runOnUiThread {
                host = installStaffHost(hostActivity, staff)
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
                staffHost.adapter.onItemChanged(staff, 0)
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
                staffHost.adapter.onItemChanged(staff, 0)
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

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun removingStaffRowReleasesImageAndComposeContent() {
        var host: StaffTestHost? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val staff = staffFixture(hostActivity, language = "japanese", isFavourite = true)
            runOnUiThread { host = installStaffHost(hostActivity, staff) }
            val staffHost = requireNotNull(host)

            waitUntil("local staff image shows visible pixels", 30_000L) {
                val images = onAllNodesWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            waitForIdle()

            val recycledRow = requireNotNull(staffHost.recyclerView.findViewHolderForAdapterPosition(0)).itemView
            val viewType = staffHost.adapter.getItemViewType(0)
            runOnUiThread {
                staffHost.recyclerView.setItemViewCacheSize(0)
                staffHost.recyclerView.recycledViewPool.setMaxRecycledViews(viewType, 0)
                staffHost.adapter.onItemRemoved(0)
            }

            waitUntil("staff row detaches after removal", 5_000L) {
                staffHost.adapter.itemCount == 0 && recycledRow.parent == null
            }
            waitUntil("recycled staff image semantics are removed", 5_000L) {
                onAllNodesWithTag(STAFF_IMAGE_TAG, useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
            waitUntil("removed staff Compose content is absent", 5_000L) {
                onAllNodesWithTag(STAFF_ROW_TAG, useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun characterActorsStaffRowShowsCoverAndSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val staff = staffFixture(hostActivity, language = "japanese", isFavourite = true)
            runOnUiThread { installCharacterStaffHost(hostActivity, staff) }

            waitUntil("character staff row is composed", 5_000L) {
                onAllNodesWithTag(STAFF_ROW_TAG, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
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
            onNodeWithTag(STAFF_ROW_TAG, useUnmergedTree = true).assertHasClickAction()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun mediaStaffRoleRowShowsCoverAndSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val staff = staffFixture(hostActivity, language = "japanese", isFavourite = true)
            runOnUiThread { installMediaStaffHost(hostActivity, staff) }

            waitUntil("media staff row is composed", 5_000L) {
                onAllNodesWithTag(STAFF_ROW_TAG, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
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
            onNodeWithText("Akira Toriyama").assertIsDisplayed()
            onNodeWithTag(STAFF_FAVORITE_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(STAFF_ROW_TAG, useUnmergedTree = true).assertHasClickAction()
        }
    }

    private fun installStaffHost(
        activity: ProgressLayoutTestActivity,
        staff: StaffBase,
    ): StaffTestHost {
        val adapter = StaffAdapter(activity).apply { onItemsInserted(listOf(staff)) }
        return StaffTestHost(adapter, installRecyclerHost(activity, adapter))
    }

    private fun installCharacterStaffHost(
        activity: ProgressLayoutTestActivity,
        staff: StaffBase,
    ) {
        val adapter =
            CharacterActorsAdapter(
                onMediaClick =
                object : ItemClickListener<RecyclerItem> {
                    override fun onItemClick(target: View, data: IndexedValue<RecyclerItem>) = Unit

                    override fun onItemLongClick(target: View, data: IndexedValue<RecyclerItem>) = Unit
                },
                onStaffClick = { _, _ -> },
            ).apply { submitList(listOf(staff)) }
        installRecyclerHost(activity, adapter)
    }

    private fun installMediaStaffHost(
        activity: ProgressLayoutTestActivity,
        staff: StaffBase,
    ) {
        val adapter = MediaStaffRoleAdapter { _, _ -> }
        adapter.submitList(listOf(staff))
        installRecyclerHost(activity, adapter)
    }

    private fun installRecyclerHost(
        activity: ProgressLayoutTestActivity,
        adapter: RecyclerView.Adapter<*>,
    ): RecyclerView {
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
        return recyclerView
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

    private data class StaffTestHost(
        val adapter: StaffAdapter,
        val recyclerView: RecyclerView,
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
