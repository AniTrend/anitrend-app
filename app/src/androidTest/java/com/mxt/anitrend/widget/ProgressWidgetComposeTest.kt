@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UnstableApiUsage")

package com.mxt.anitrend.widget

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.NestedScrollView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.ProgressWidget
import com.mxt.anitrend.base.custom.view.widget.ScoreWidget
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.TestSessionUtil
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.sheet.BottomSheetSeriesManage
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ProgressWidgetComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun boundedProgressExposesEditableValueAndAccessibleSteppers() {
        prepareUnauthenticatedSession()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var widget: ProgressWidget
            runOnUiThread {
                widget = ProgressWidget(hostActivity).apply {
                    setProgressMaximum(2)
                    setProgressCurrent(1)
                }
                installWidget(hostActivity, widget)
            }
            waitForIdle()

            val input = onNodeWithText("1", useUnmergedTree = true)
            input.assertIsDisplayed().assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))
            onNodeWithText("/ 2", useUnmergedTree = true).assertIsDisplayed()

            val increment = onNodeWithContentDescription(hostActivity.getString(R.string.action_increment))
            increment
                .assertIsDisplayed()
                .assertIsEnabled()
                .assertHasClickAction()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            increment.performClick()
            runOnIdle { assertEquals(2, widget.progressCurrent) }
            increment.performClick()
            runOnIdle { assertEquals(2, widget.progressCurrent) }

            val decrement = onNodeWithContentDescription(hostActivity.getString(R.string.action_decrement))
            decrement.assertIsDisplayed().assertIsEnabled().assertHasClickAction()
            decrement.performClick()
            runOnIdle { assertEquals(1, widget.progressCurrent) }
            runOnUiThread { widget.setProgressCurrent(0) }
            waitForIdle()
            decrement.performClick()
            runOnIdle { assertEquals(0, widget.progressCurrent) }

            val zeroInput = onNodeWithText("0", useUnmergedTree = true).assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))
            zeroInput.performTextInputSelection(TextRange(0, 1))
            zeroInput.performTextInput("2")
            runOnIdle { assertEquals(2, widget.progressCurrent) }
            val maxInput = onNodeWithText("2", useUnmergedTree = true)
                .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))
            maxInput.performTextInputSelection(TextRange(0, 1))
            maxInput.performTextInput("3")
            runOnIdle { assertEquals(2, widget.progressCurrent) }
            onNodeWithText("2", useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun unboundedProgressHidesMaximumAndDoesNotDecrementBelowZero() {
        prepareUnauthenticatedSession()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var widget: ProgressWidget
            runOnUiThread {
                widget = ProgressWidget(hostActivity).apply { setProgressCurrent(0) }
                installWidget(hostActivity, widget)
            }
            waitForIdle()

            onNodeWithText("0", useUnmergedTree = true)
                .assertIsDisplayed()
                .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))
            onNodeWithText("/ 0", useUnmergedTree = true).assertDoesNotExist()
            val decrement = onNodeWithContentDescription(hostActivity.getString(R.string.action_decrement))
            decrement.assertIsDisplayed().assertIsEnabled().performClick()
            runOnIdle { assertEquals(0, widget.progressCurrent) }

            onNodeWithContentDescription(hostActivity.getString(R.string.action_increment))
                .performClick()
            runOnIdle { assertEquals(1, widget.progressCurrent) }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun scoreFormatsKeepTheirMaximumAndIncrementStep() {
        prepareUnauthenticatedSession()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var widget: ScoreWidget
            runOnUiThread {
                widget = ScoreWidget(hostActivity)
                installWidget(hostActivity, widget)
            }
            waitForIdle()

            val increment = onNodeWithContentDescription(hostActivity.getString(R.string.action_increment))
            val formats =
                listOf(
                    ScoreCase(KeyUtil.POINT_100, 99f, "99", "/ 100", 100f, "100"),
                    ScoreCase(
                        KeyUtil.POINT_10_DECIMAL,
                        7.5f,
                        formatDecimal(7.5f),
                        String.format(Locale.getDefault(), "/ %.1f", 10f),
                        7.6f,
                        formatDecimal(7.6f),
                    ),
                    ScoreCase(KeyUtil.POINT_10, 9f, "9", "/ 10", 10f, "10"),
                    ScoreCase(KeyUtil.POINT_5, 4f, "4", "/ 5", 5f, "5"),
                    ScoreCase(KeyUtil.POINT_3, 2f, "2", "/ 3", 3f, "3"),
                )

            for (scoreCase in formats) {
                runOnUiThread {
                    widget.setScoreFormat(scoreCase.format)
                    widget.setScoreCurrent(scoreCase.initialValue)
                }
                waitForIdle()

                val input = onNodeWithText(scoreCase.initialText, useUnmergedTree = true)
                input.assertIsDisplayed().assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))
                onNodeWithText(scoreCase.maximumText, useUnmergedTree = true).assertIsDisplayed()
                increment
                    .assertIsDisplayed()
                    .assertIsEnabled()
                    .assertHasClickAction()
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                increment.performClick()
                runOnIdle { assertEquals(scoreCase.afterIncrement, widget.scoreCurrent, 0.001f) }
                onNodeWithText(scoreCase.afterIncrementText, useUnmergedTree = true).assertIsDisplayed()

                runOnUiThread { widget.setScoreCurrent(scoreCase.maximumValue) }
                waitForIdle()
                increment.performClick()
                runOnIdle { assertEquals(scoreCase.maximumValue, widget.scoreCurrent, 0.001f) }
            }

            runOnUiThread {
                widget.setScoreFormat(KeyUtil.POINT_10_DECIMAL)
                widget.setScoreCurrent(7.5f)
            }
            waitForIdle()
            val decimalInput = onNodeWithText(formatDecimal(7.5f), useUnmergedTree = true)
                .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))
            decimalInput.performTextInputSelection(TextRange(0, formatDecimal(7.5f).length))
            decimalInput.performTextInput(formatDecimal(8.2f))
            runOnIdle { assertEquals(8.2f, widget.scoreCurrent, 0.001f) }
            val percentInput = onNodeWithText(formatDecimal(8.2f), useUnmergedTree = true)
                .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))
            percentInput.performTextInputSelection(TextRange(0, formatDecimal(8.2f).length))
            percentInput.performTextInput("80")
            runOnIdle { assertEquals(8f, widget.scoreCurrent, 0.001f) }
            onNodeWithText(formatDecimal(8f), useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun editingManageSheetProgressDoesNotMutateCommittedEntryWhenCancelled() {
        prepareUnauthenticatedSession()
        val committedEntry = mediaListEntry(progress = 5)
        val media = mediaBase(committedEntry)

        var progressWidget: ProgressWidget? = null
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val sheet = BottomSheetSeriesManage.newInstance(media)
            runOnUiThread {
                sheet.show(hostActivity.supportFragmentManager, "progress-control-draft-test")
                hostActivity.supportFragmentManager.executePendingTransactions()
                val dialog = requireNotNull(sheet.dialog)
                val scrollView = requireNotNull(dialog.findViewById<NestedScrollView>(R.id.sheet_manage_scroll_view))
                val progressCard = requireNotNull(dialog.findViewById<View>(R.id.sheet_manage_progress_card))
                scrollView.scrollTo(0, progressCard.top)
                progressWidget = requireNotNull(dialog.findViewById<ProgressWidget>(R.id.sheet_manage_progress))
            }
            waitForIdle()

            val input = onNodeWithText("5", useUnmergedTree = true)
            input.assertIsDisplayed().assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))
            input.performTextInputSelection(TextRange(0, 1))
            input.performTextInput("8")
            runOnIdle { assertEquals(8, requireNotNull(progressWidget).progressCurrent) }

            runOnUiThread {
                sheet.dismiss()
                hostActivity.supportFragmentManager.executePendingTransactions()
            }
            waitForIdle()
        }

        assertEquals(5, committedEntry.progress)
    }

    private fun prepareUnauthenticatedSession() {
        TestSessionUtil.setAuthenticated(InstrumentationRegistry.getInstrumentation().targetContext, authenticated = false)
    }

    private fun installWidget(
        activity: ProgressLayoutTestActivity,
        widget: View,
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
                setContent {
                    AniTrendTheme {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            AndroidView(
                                factory = { widget },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(Alignment.Center),
                            )
                        }
                    }
                }
            },
        )
    }

    private fun mediaBase(entry: MediaList): MediaBase = MediaBase().apply {
        id = 1L
        type = KeyUtil.ANIME
        status = KeyUtil.FINISHED
        episodes = 12
        mediaListEntry = entry
    }

    private fun mediaListEntry(progress: Int): MediaList = MediaList().apply {
        id = 2L
        mediaId = 1L
        status = KeyUtil.CURRENT
        score = 80f
        this.progress = progress
    }

    private fun formatDecimal(value: Float): String = String.format(Locale.getDefault(), "%.1f", value)

    private data class ScoreCase(
        val format: String,
        val initialValue: Float,
        val initialText: String,
        val maximumText: String,
        val afterIncrement: Float,
        val afterIncrementText: String,
        val maximumValue: Float = when (format) {
            KeyUtil.POINT_100 -> 100f
            KeyUtil.POINT_10_DECIMAL -> 10f
            KeyUtil.POINT_10 -> 10f
            KeyUtil.POINT_5 -> 5f
            KeyUtil.POINT_3 -> 3f
            else -> 0f
        },
    )
}
