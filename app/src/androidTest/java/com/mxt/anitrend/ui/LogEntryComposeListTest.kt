package com.mxt.anitrend.ui

import android.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.model.entity.log.LogEntry
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.logging.LogEntryList
import com.mxt.anitrend.ui.logging.LOG_ENTRY_ROW_TAG
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogEntryComposeListTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun composeLogRowsRenderReversedOrderAndExposeLevelColors() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val entries = listOf(
                LogEntry(date = "2026-01-01", time = "00:00:00.000", level = LogEntry.Level.ERROR, message = "first error"),
                LogEntry(date = "2026-01-01", time = "00:00:01.000", level = LogEntry.Level.INFO, message = "first info"),
            )
            val state = androidx.compose.runtime.mutableStateOf(entries)
            runOnUiThread {
                hostActivity.setContentView(
                    androidx.compose.ui.platform.ComposeView(hostActivity).apply {
                        setViewCompositionStrategy(
                            androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
                        )
                        setContent {
                            AniTrendTheme {
                                LogEntryList(entries = state.value)
                            }
                        }
                    },
                )
            }
            waitForIdle()

            assertEquals(listOf("first info", "first error"), visibleLogMessages())
            assertTrue(rowContainsColor(LogEntry.Level.ERROR, severityColors.getValue(LogEntry.Level.ERROR)))
            assertTrue(rowContainsColor(LogEntry.Level.INFO, severityColors.getValue(LogEntry.Level.INFO)))
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun composeLogRowsRenderAndTrackStateUpdates() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val entries = listOf(
                LogEntry(date = "2026-01-01", time = "00:00:00.000", level = LogEntry.Level.ERROR, message = "first error"),
                LogEntry(date = "2026-01-01", time = "00:00:01.000", level = LogEntry.Level.INFO, message = "first info"),
            )
            val replacement = listOf(
                LogEntry(date = "2026-01-01", time = "00:00:02.000", level = LogEntry.Level.WARNING, message = "replacement warning"),
            )
            val state = androidx.compose.runtime.mutableStateOf(entries)
            runOnUiThread {
                hostActivity.setContentView(
                    androidx.compose.ui.platform.ComposeView(hostActivity).apply {
                        setViewCompositionStrategy(
                            androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
                        )
                        setContent {
                            AniTrendTheme {
                                LogEntryList(entries = state.value)
                            }
                        }
                    },
                )
            }
            waitForIdle()

            onNodeWithText("first error").assertIsDisplayed()
            onNodeWithText("first info").assertIsDisplayed()
            assertEquals(2, onAllNodesWithTag(LOG_ENTRY_ROW_TAG).fetchSemanticsNodes().size)
            assertEquals(listOf("first info", "first error"), visibleLogMessages())

            runOnUiThread { state.value = replacement }
            waitForIdle()

            onNodeWithText("replacement warning").assertIsDisplayed()
            assertEquals(1, onAllNodesWithTag(LOG_ENTRY_ROW_TAG).fetchSemanticsNodes().size)
            assertEquals(replacement, state.value)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun composeLogRowsKeepEverySeverityColor() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val entries =
                LogEntry.Level.entries.map { level ->
                    LogEntry(
                        date = "2026-01-01",
                        time = "00:00:00.000",
                        level = level,
                        message = "${level.name} message",
                    )
                }
            runOnUiThread {
                hostActivity.setContentView(
                    androidx.compose.ui.platform.ComposeView(hostActivity).apply {
                        setViewCompositionStrategy(
                            androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
                        )
                        setContent {
                            AniTrendTheme {
                                LogEntryList(entries = entries)
                            }
                        }
                    },
                )
            }
            waitForIdle()

            entries.forEach { entry ->
                assertTrue(
                    "${entry.level} should keep its severity color",
                    rowContainsColor(entry.level, severityColors.getValue(entry.level)),
                )
            }
        }
    }

    private fun SemanticsNodeInteractionsProvider.visibleLogMessages(): List<String> = onAllNodesWithTag(LOG_ENTRY_ROW_TAG, useUnmergedTree = true)
        .fetchSemanticsNodes()
        .sortedBy { it.boundsInRoot.top }
        .map { row -> row.rowTexts().last() }

    private fun SemanticsNodeInteractionsProvider.rowContainsColor(level: LogEntry.Level, expected: Int): Boolean {
        val rows = onAllNodesWithTag(LOG_ENTRY_ROW_TAG, useUnmergedTree = true)
        val index = rows.fetchSemanticsNodes().indexOfFirst { row -> row.rowTexts().firstOrNull() == level.name }
        if (index < 0) return false
        val bitmap = rows[index].captureToImage().asAndroidBitmap()
        return try {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            pixels.contains(expected)
        } finally {
            bitmap.recycle()
        }
    }

    private fun SemanticsNode.rowTexts(): List<String> = buildList {
        config.getOrNull(SemanticsProperties.Text)
            ?.let { text -> add(text.joinToString(separator = "") { it.text }) }
        children.forEach { child -> addAll(child.rowTexts()) }
    }

    private val severityColors =
        mapOf(
            LogEntry.Level.ERROR to Color.rgb(213, 0, 0),
            LogEntry.Level.WARNING to Color.rgb(255, 109, 0),
            LogEntry.Level.INFO to Color.rgb(0, 145, 234),
            LogEntry.Level.DEBUG to Color.rgb(84, 165, 250),
            LogEntry.Level.VERBOSE to Color.rgb(158, 158, 158),
        )
}
