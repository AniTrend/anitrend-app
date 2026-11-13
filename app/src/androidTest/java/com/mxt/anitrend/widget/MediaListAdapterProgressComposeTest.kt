@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.widget

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaListAdapter
import com.mxt.anitrend.domain.model.MediaListItemUiModel
import com.mxt.anitrend.ui.TestSessionUtil
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

private const val MEDIA_LIST_PROGRESS_CONTROL_TAG = "media-list-progress-control"

@RunWith(AndroidJUnit4::class)
class MediaListAdapterProgressComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun expandedRowRecyclesAndRoutesIncrementToCurrentModel() {
        TestSessionUtil.setAuthenticated(InstrumentationRegistry.getInstrumentation().targetContext, authenticated = false)
        val initialItem = mediaListItem(id = 1L, progress = 3, title = "Initial progress")
        val updatedItem = initialItem.copy(progress = 8, mediaTitle = "Updated progress", mediaTitleEnglish = "Updated progress")
        val deliveredItem = AtomicReference<MediaListItemUiModel?>()
        val committed = AtomicBoolean(false)
        lateinit var host: MediaListAdapterHost

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                host =
                    installMediaListHost(
                        activity = hostActivity,
                        style = KeyUtil.LIST_VIEW_STYLE_DEFAULT,
                        onIncrement = { deliveredItem.set(it) },
                    )
                host.adapter.submitItems(listOf(initialItem)) { committed.set(true) }
            }
            waitUntil("expanded row binds its initial progress", 5_000L) {
                committed.get() &&
                    host.adapter.currentList.singleOrNull() == initialItem &&
                    onAllNodesWithText("3/12 +").fetchSemanticsNodes().isNotEmpty()
            }

            val control = onNodeWithTag(MEDIA_LIST_PROGRESS_CONTROL_TAG, useUnmergedTree = true)
            control.assertIsDisplayed().assertHasClickAction()
            control.performClick()
            runOnIdle { assertSame(initialItem, deliveredItem.get()) }

            val holder = AtomicReference<RecyclerView.ViewHolder?>()
            runOnUiThread { holder.set(host.recyclerView.findViewHolderForAdapterPosition(0)) }
            runOnUiThread { host.adapter.onViewRecycled(requireNotNull(holder.get())) }
            waitForIdle()
            onAllNodesWithTag(MEDIA_LIST_PROGRESS_CONTROL_TAG, useUnmergedTree = true).assertCountEquals(0)

            committed.set(false)
            runOnUiThread { host.adapter.submitItems(listOf(updatedItem)) { committed.set(true) } }
            waitUntil("recycled row binds the updated progress", 5_000L) {
                committed.get() &&
                    host.adapter.currentList.singleOrNull() == updatedItem &&
                    onAllNodesWithText("8/12 +").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag(MEDIA_LIST_PROGRESS_CONTROL_TAG, useUnmergedTree = true).performClick()
            runOnIdle { assertSame(updatedItem, deliveredItem.get()) }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun compactX1HostsProgressControl() {
        TestSessionUtil.setAuthenticated(InstrumentationRegistry.getInstrumentation().targetContext, authenticated = false)
        val item = mediaListItem(id = 2L, progress = 4, title = "Compact progress")
        val committed = AtomicBoolean(false)
        lateinit var host: MediaListAdapterHost

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                host =
                    installMediaListHost(
                        activity = hostActivity,
                        style = KeyUtil.LIST_VIEW_STYLE_COMPACT_X1,
                        onIncrement = {},
                    )
                host.adapter.submitItems(listOf(item)) { committed.set(true) }
            }
            waitUntil("compact X1 row exposes progress", 5_000L) {
                committed.get() &&
                    host.adapter.currentList.singleOrNull() == item &&
                    onAllNodesWithText("4/12 +").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag(MEDIA_LIST_PROGRESS_CONTROL_TAG, useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun compactX2HidesProgressControl() {
        TestSessionUtil.setAuthenticated(InstrumentationRegistry.getInstrumentation().targetContext, authenticated = false)
        val item = mediaListItem(id = 3L, progress = 4, title = "Compact progress")
        val committed = AtomicBoolean(false)
        lateinit var host: MediaListAdapterHost

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                host =
                    installMediaListHost(
                        activity = hostActivity,
                        style = KeyUtil.LIST_VIEW_STYLE_COMPACT_X2,
                        onIncrement = {},
                    )
                host.adapter.submitItems(listOf(item)) { committed.set(true) }
            }
            waitUntil("compact X2 row is committed", 5_000L) {
                committed.get() && host.adapter.currentList.singleOrNull() == item
            }
            waitForIdle()

            val progressVisibility = AtomicReference<Int?>()
            runOnUiThread {
                val row = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
                progressVisibility.set(row.findViewById<View>(R.id.series_episodes).visibility)
            }
            runOnIdle { assertEquals(View.GONE, progressVisibility.get()) }
        }
    }
}

private fun installMediaListHost(
    activity: ProgressLayoutTestActivity,
    style: Int,
    onIncrement: (MediaListItemUiModel) -> Unit,
): MediaListAdapterHost {
    val adapter =
        MediaListAdapter(
            context = activity,
            mediaListStyle = style,
            onIncrement = onIncrement,
            onOpenMedia = { _, _ -> },
            onOpenManage = {},
        )
    val recyclerView =
        RecyclerView(activity).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
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

    return MediaListAdapterHost(adapter, recyclerView)
}

private fun mediaListItem(
    id: Long,
    progress: Int,
    title: String,
): MediaListItemUiModel = MediaListItemUiModel(
    id = id,
    mediaId = id + 100L,
    status = "CURRENT",
    progress = progress,
    progressVolumes = 0,
    score = 8.0,
    repeat = 0,
    mediaTitle = title,
    mediaTitleEnglish = title,
    mediaTitleOriginal = null,
    mediaCoverImage = null,
    mediaType = KeyUtil.ANIME,
    mediaFormat = "TV",
    mediaStatus = "RELEASING",
    mediaEpisodes = 12,
    mediaChapters = 0,
    mediaVolumes = 0,
    mediaStartDate = null,
    nextAiringEpisode = null,
    mediaIsFavourite = false,
    isIncrementPending = false,
    isDeletePending = false,
    canIncrement = true,
)

private data class MediaListAdapterHost(
    val adapter: MediaListAdapter,
    val recyclerView: RecyclerView,
)
