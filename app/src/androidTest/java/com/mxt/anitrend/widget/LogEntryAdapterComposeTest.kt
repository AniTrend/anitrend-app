@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.adapter.recycler.detail.LogEntryAdapter
import com.mxt.anitrend.model.entity.log.LogEntry
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogEntryAdapterComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun logRowsExposeEveryLevelAndKeepItsSeverityColor() {
        val entries =
            LogEntry.Level.entries.map { level ->
                LogEntry(
                    date = "2026-10-14",
                    time = "09:15:30",
                    level = level,
                    message = "${level.name} log line",
                )
            }
        val severityColors =
            mapOf(
                LogEntry.Level.ERROR to Color.rgb(213, 0, 0),
                LogEntry.Level.WARNING to Color.rgb(255, 109, 0),
                LogEntry.Level.INFO to Color.rgb(0, 145, 234),
                LogEntry.Level.DEBUG to Color.rgb(84, 165, 250),
                LogEntry.Level.VERBOSE to Color.rgb(158, 158, 158),
            )

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: LogHost
            runOnUiThread { host = installLogHost(hostActivity, entries) }
            waitUntil("all log rows bind", 5_000L) {
                entries.indices.all { host.recyclerView.findViewHolderForAdapterPosition(it) != null }
            }
            waitForIdle()

            entries.forEach { entry ->
                onAllNodesWithText(entry.level.name, useUnmergedTree = true).assertCountEquals(1)
                onNodeWithText(entry.level.name, useUnmergedTree = true).assertIsDisplayed()
                onAllNodesWithText(entry.message, useUnmergedTree = true).assertCountEquals(1)
                onNodeWithText(entry.message, useUnmergedTree = true).assertIsDisplayed()
            }
            runOnUiThread {
                entries.forEachIndexed { index, entry ->
                    val bitmap = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(index)).itemView.drawToBitmap()
                    try {
                        assertTrue(
                            "${entry.level} should retain its severity color",
                            bitmap.containsColor(severityColors.getValue(entry.level)),
                        )
                    } finally {
                        bitmap.recycle()
                    }
                }
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun logMessageSelectionIsScopedToItsRowAndClearedAfterRebind() {
        val firstMessage = "select this log"
        val replacementMessage = "replacement log entry"
        val firstEntry =
            LogEntry(
                date = "2026-10-14",
                time = "09:15:30",
                level = LogEntry.Level.ERROR,
                message = firstMessage,
            )
        val replacementEntry =
            LogEntry(
                date = "2026-10-14",
                time = "09:15:31",
                level = LogEntry.Level.INFO,
                message = replacementMessage,
            )

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: LogHost
            runOnUiThread { host = installLogHost(hostActivity, listOf(firstEntry)) }
            waitUntil("initial log row binds", 5_000L) {
                host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val messageNode = onNodeWithText(firstMessage, useUnmergedTree = true)
            messageNode.assertIsDisplayed()
            val firstHolder = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)) as LogEntryAdapter.LogEntryHolder
            val firstSelectionState = firstHolder.selectionState

            val levelNode = onNodeWithText("ERROR", useUnmergedTree = true)
            val levelSize = levelNode.fetchSemanticsNode().size
            levelNode.performTouchInput { longClick(Offset(20f, levelSize.height / 2f)) }
            waitForIdle()
            assertTrue(firstSelectionState.selectedTexts.isEmpty())

            val messageSize = messageNode.fetchSemanticsNode().size
            messageNode.performTouchInput { longClick(Offset(20f, messageSize.height / 2f)) }
            waitForIdle()
            val selectedMessage = firstSelectionState.selectedTexts.joinToString(separator = "") { it.text }
            assertTrue(selectedMessage.isNotBlank() && firstMessage.contains(selectedMessage))

            runOnUiThread { host.adapter.onItemsInserted(listOf(replacementEntry)) }
            waitUntil("replacement message is composed", 5_000L) {
                onAllNodesWithText(replacementMessage, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
            }
            waitForIdle()

            val replacementNode = onNodeWithText(replacementMessage, useUnmergedTree = true)
            replacementNode.assertIsDisplayed()
            val replacementHolder = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)) as LogEntryAdapter.LogEntryHolder
            assertTrue(replacementHolder.selectionState.selectedTexts.isEmpty())

            val replacementSize = replacementNode.fetchSemanticsNode().size
            replacementNode.performTouchInput { longClick(Offset(20f, replacementSize.height / 2f)) }
            waitForIdle()
            val selectedReplacement = replacementHolder.selectionState.selectedTexts.joinToString(separator = "") { it.text }
            assertTrue(selectedReplacement.isNotBlank() && replacementMessage.contains(selectedReplacement))
        }
    }

    private fun installLogHost(
        activity: ProgressLayoutTestActivity,
        entries: List<LogEntry>,
    ): LogHost {
        val adapter = LogEntryAdapter(activity).apply { onItemsInserted(entries) }
        val recyclerView =
            RecyclerView(activity).apply {
                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)
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
        return LogHost(adapter, recyclerView)
    }

    private fun Bitmap.containsColor(expected: Int): Boolean {
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)
        return pixels.contains(expected)
    }

    private data class LogHost(
        val adapter: LogEntryAdapter,
        val recyclerView: RecyclerView,
    )
}
