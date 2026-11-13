@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.widget

import android.graphics.Bitmap
import android.view.View
import android.widget.TextView
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
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaLatestAdapter
import com.mxt.anitrend.base.custom.view.text.RatingTextView
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.TestSessionUtil
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.minutes

private const val LATEST_MEDIA_ROW_TAG = "latest-media-row"
private const val LATEST_MEDIA_IMAGE_TAG = "latest-media-image"
private const val LATEST_MEDIA_STATUS_TAG = "latest-media-status"
private const val LATEST_MEDIA_RATING_TAG = "latest-media-rating"

@RunWith(AndroidJUnit4::class)
class MediaLatestRowComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun animeRowExposesAiringStatusRatingAndActionSemantics() {
        TestSessionUtil.setAuthenticated(InstrumentationRegistry.getInstrumentation().targetContext, authenticated = false)
        var host: LatestMediaHost? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val media = mediaFixture(hostActivity, isAnime = true)
            runOnUiThread {
                host = installLatestMediaHost(hostActivity, media)
            }
            val latestHost = requireNotNull(host)
            waitUntil("local latest anime image loads", 60_000L) {
                val images = onAllNodesWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true)
                val imageNodes = images.fetchSemanticsNodes()
                imageNodes.size == 1 && images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            waitForIdle()

            val row = onNodeWithTag(LATEST_MEDIA_ROW_TAG, useUnmergedTree = true)
            row.assertIsDisplayed()
                .assertHasClickAction()
                .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
            onNodeWithText("Bocchi the Rock!").assertIsDisplayed()
            onNodeWithText("EP 8:", substring = true).assertIsDisplayed()
            onNodeWithText("2024 - 12 Episodes").assertIsDisplayed()
            onNodeWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true).assertIsDisplayed()
            assertTrue(
                "the anime cover should display local image pixels",
                onNodeWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
            onNodeWithTag(LATEST_MEDIA_STATUS_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(LATEST_MEDIA_RATING_TAG, useUnmergedTree = true).assertIsDisplayed()

            val statusBitmap = onNodeWithTag(LATEST_MEDIA_STATUS_TAG, useUnmergedTree = true).captureToImage().asAndroidBitmap()
            assertEquals(
                ContextCompat.getColor(hostActivity, R.color.colorStateBlue),
                statusBitmap.getPixel(statusBitmap.width / 2, statusBitmap.height / 2),
            )
            val rating = requireNotNull(latestHost.recyclerView.findViewHolderForAdapterPosition(0)?.itemView)
                .findViewById<RatingTextView>(R.id.custom_rating_widget)
            assertEquals("87", rating.findViewById<TextView>(R.id.rating_value).text.toString())
            assertEquals(View.VISIBLE, rating.findViewById<View>(R.id.rating_list_status).visibility)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun mangaRowExposesReleaseLabelChapterMetadataAndStatus() {
        TestSessionUtil.setAuthenticated(InstrumentationRegistry.getInstrumentation().targetContext, authenticated = false)
        var host: LatestMediaHost? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val media = mediaFixture(hostActivity, isAnime = false)
            runOnUiThread {
                host = installLatestMediaHost(hostActivity, media)
            }
            val latestHost = requireNotNull(host)
            waitUntil("local latest manga image loads", 60_000L) {
                val images = onAllNodesWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true)
                val imageNodes = images.fetchSemanticsNodes()
                imageNodes.size == 1 && images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            waitForIdle()

            onNodeWithTag(LATEST_MEDIA_ROW_TAG, useUnmergedTree = true)
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithText("Dandadan").assertIsDisplayed()
            onNodeWithText(hostActivity.getString(R.string.label_latest_release)).assertIsDisplayed()
            onNodeWithText("2020 - 105 Chapters").assertIsDisplayed()
            onNodeWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true).assertIsDisplayed()
            assertTrue(
                "the manga cover should display local image pixels",
                onNodeWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
            onNodeWithTag(LATEST_MEDIA_STATUS_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(LATEST_MEDIA_RATING_TAG, useUnmergedTree = true).assertIsDisplayed()

            val statusBitmap = onNodeWithTag(LATEST_MEDIA_STATUS_TAG, useUnmergedTree = true).captureToImage().asAndroidBitmap()
            assertEquals(
                ContextCompat.getColor(hostActivity, R.color.colorStateGreen),
                statusBitmap.getPixel(statusBitmap.width / 2, statusBitmap.height / 2),
            )
            val rating = requireNotNull(latestHost.recyclerView.findViewHolderForAdapterPosition(0)?.itemView)
                .findViewById<RatingTextView>(R.id.custom_rating_widget)
            assertEquals("92", rating.findViewById<TextView>(R.id.rating_value).text.toString())
            assertEquals(View.GONE, rating.findViewById<View>(R.id.rating_list_status).visibility)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun animeRowRebindsSameIdMutableMedia() {
        TestSessionUtil.setAuthenticated(InstrumentationRegistry.getInstrumentation().targetContext, authenticated = false)
        var host: LatestMediaHost? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val model = mediaFixture(hostActivity, isAnime = true)
            val imageUrl = requireNotNull(model.coverImage?.large)
            runOnUiThread {
                host = installLatestMediaHost(hostActivity, model)
            }
            val latestHost = requireNotNull(host)
            waitUntil("local latest anime image loads", 60_000L) {
                val images = onAllNodesWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true)
                val imageNodes = images.fetchSemanticsNodes()
                imageNodes.size == 1 && images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            waitForIdle()
            onNodeWithTag(LATEST_MEDIA_ROW_TAG, useUnmergedTree = true).assertIsDisplayed()

            runOnUiThread {
                model.title = MediaTitle(null, null, null, "Bocchi Updated")
                model.status = KeyUtil.FINISHED
                model.nextAiringEpisode = null
                model.startDate = FuzzyDate(day = 1, month = 4, year = 2025)
                model.averageScore = 75
                model.isFavourite = false
                model.coverImage = null
                model.mediaListEntry = null
                latestHost.adapter.onItemChanged(model, 0)
            }

            waitUntil("same-ID anime row shows its updated values", 5_000L) {
                onAllNodesWithText("Bocchi Updated").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithText("Finished", substring = true).fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithText("2025 - 12 Episodes").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("Bocchi Updated").assertIsDisplayed()
            onNodeWithText("Finished", substring = true).assertIsDisplayed()
            onNodeWithText("2025 - 12 Episodes").assertIsDisplayed()

            val row = requireNotNull(latestHost.recyclerView.findViewHolderForAdapterPosition(0)?.itemView)
            val rating = row.findViewById<RatingTextView>(R.id.custom_rating_widget)
            assertEquals("75", rating.findViewById<TextView>(R.id.rating_value).text.toString())
            assertEquals(View.GONE, rating.findViewById<View>(R.id.rating_list_status).visibility)

            val statusBitmap = onNodeWithTag(LATEST_MEDIA_STATUS_TAG, useUnmergedTree = true).captureToImage().asAndroidBitmap()
            assertEquals(
                ContextCompat.getColor(hostActivity, R.color.colorStateGreen),
                statusBitmap.getPixel(statusBitmap.width / 2, statusBitmap.height / 2),
            )
            waitUntil("anime cover clears when the model becomes null", 5_000L) {
                val images = onAllNodesWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasUniformImageSurface()
            }
            assertTrue(
                "a null cover must leave the image slot empty",
                onNodeWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasUniformImageSurface(),
            )

            runOnUiThread {
                model.coverImage = ImageBase(extraLarge = null, large = imageUrl, medium = null)
                latestHost.adapter.onItemChanged(model, 0)
            }
            waitUntil("rebound anime cover shows local image pixels", 60_000L) {
                val images = onAllNodesWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true)
                images.fetchSemanticsNodes().size == 1 &&
                    images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            assertTrue(
                "the rebound anime cover should display local image pixels",
                onNodeWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true)
                    .captureToImage()
                    .asAndroidBitmap()
                    .hasImageContentPixels(),
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun removingMediaRowDropsCoverContent() {
        TestSessionUtil.setAuthenticated(InstrumentationRegistry.getInstrumentation().targetContext, authenticated = false)
        var host: LatestMediaHost? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val media = mediaFixture(hostActivity, isAnime = true)
            runOnUiThread {
                host = installLatestMediaHost(hostActivity, media)
            }
            val latestHost = requireNotNull(host)
            waitUntil("local latest media image loads", 60_000L) {
                val images = onAllNodesWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true)
                val imageNodes = images.fetchSemanticsNodes()
                imageNodes.size == 1 && images[0].captureToImage().asAndroidBitmap().hasImageContentPixels()
            }
            waitForIdle()

            val recycledRow = requireNotNull(latestHost.recyclerView.findViewHolderForAdapterPosition(0)).itemView
            val viewType = latestHost.adapter.getItemViewType(0)
            runOnUiThread {
                latestHost.recyclerView.setItemViewCacheSize(0)
                latestHost.recyclerView.recycledViewPool.setMaxRecycledViews(viewType, 0)
                latestHost.adapter.onItemRemoved(0)
            }

            waitUntil("latest media row detaches after removal", 5_000L) {
                latestHost.adapter.itemCount == 0 && recycledRow.parent == null
            }
            waitUntil("recycled latest media image semantics are removed", 5_000L) {
                onAllNodesWithTag(LATEST_MEDIA_IMAGE_TAG, useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
        }
    }

    private fun mediaFixture(
        activity: ProgressLayoutTestActivity,
        isAnime: Boolean,
    ): MediaBase {
        val imageUrl = "android.resource://${activity.packageName}/${R.drawable.reg_bg}"
        return MediaBase().apply {
            id = if (isAnime) 501L else 502L
            type = if (isAnime) KeyUtil.ANIME else KeyUtil.MANGA
            title = MediaTitle(null, null, null, if (isAnime) "Bocchi the Rock!" else "Dandadan")
            format = if (isAnime) KeyUtil.TV else KeyUtil.MANGA
            status = if (isAnime) KeyUtil.RELEASING else KeyUtil.FINISHED
            startDate = FuzzyDate(day = 1, month = 4, year = if (isAnime) 2024 else 2020)
            averageScore = if (isAnime) 87 else 92
            coverImage = ImageBase(extraLarge = null, large = imageUrl, medium = null)
            episodes = if (isAnime) 12 else 0
            chapters = if (isAnime) 0 else 105
            nextAiringEpisode = if (isAnime) AiringSchedule(timeUntilAiring = 4L * 24 * 60 * 60, episode = 8) else null
            isFavourite = isAnime
            mediaListEntry = if (isAnime) MediaList().apply { status = KeyUtil.CURRENT } else null
        }
    }

    private fun installLatestMediaHost(
        activity: ProgressLayoutTestActivity,
        media: MediaBase,
    ): LatestMediaHost {
        val adapter =
            MediaLatestAdapter(activity).apply {
                onItemsInserted(listOf(media))
            }
        val recyclerView =
            RecyclerView(activity).apply {
                setHasFixedSize(true)
                layoutManager =
                    StaggeredGridLayoutManager(
                        activity.resources.getInteger(R.integer.single_list_x1),
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
                        AndroidView(factory = { recyclerView }, modifier = Modifier.fillMaxSize())
                    }
                }
            },
        )
        return LatestMediaHost(adapter, recyclerView)
    }

    private data class LatestMediaHost(
        val adapter: MediaLatestAdapter,
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
