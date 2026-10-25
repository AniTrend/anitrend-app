@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.ViewGroup
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.model.entity.anilist.MediaRank
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.RankingsGrid
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RankingsGridComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun rankingRowsExposeFormattedTextAndTypeIcons() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val rows = rankFixtures()
            runOnUiThread {
                hostActivity.setContentView(
                    ComposeView(hostActivity).apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            AniTrendTheme {
                                RankingsGrid(
                                    rankings = rows,
                                    onRankClick = {},
                                    onRankLongClick = {},
                                )
                            }
                        }
                    },
                )
            }
            waitForIdle()

            onAllNodesWithTag(RANK_CARD_TAG, useUnmergedTree = true)[0].assertIsDisplayed()
            onAllNodesWithTag(RANK_CARD_TAG, useUnmergedTree = true)[1].assertIsDisplayed()
            val richTextViews = findRichMarkdownTextViews(hostActivity.findViewById(android.R.id.content))
            assertEquals(2, richTextViews.size)
            assertEquals("#64 TOP RANK SUMMER 2015 (Manga )", richTextViews[0].text.toString().trim())
            assertTrue(richTextViews[0].isTextSelectable)
            assertEquals("#1 POPULAR (Anime )", richTextViews[1].text.toString().trim())
            assertTrue(richTextViews[1].isTextSelectable)
            onNodeWithTag(RATED_ICON_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(POPULAR_ICON_TAG, useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun rootTextAndLongClickForwardTheirExistingTargets() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val rows = rankFixtures()
            val clicks = mutableListOf<MediaRank>()
            val longClicks = mutableListOf<MediaRank>()
            runOnUiThread {
                installRankHost(
                    activity = hostActivity,
                    rows = rows,
                    onRankClick = { clicks += it },
                    onRankLongClick = { longClicks += it },
                )
            }
            waitForIdle()

            val rootCard = onAllNodesWithTag(RANK_CARD_TAG, useUnmergedTree = true)[0]
            rootCard.assertIsDisplayed().assertHasClickAction()
            rootCard.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
            rootCard.performTouchInput { click(Offset(x = 12.dp.toPx(), y = center.y)) }
            val rankTextView = findRichMarkdownTextViews(hostActivity.findViewById(android.R.id.content)).first()
            runOnUiThread { rankTextView.performClick() }
            rootCard.performTouchInput { longClick(Offset(x = 12.dp.toPx(), y = center.y)) }
            runOnIdle {
                assertEquals(listOf(rows.first(), rows.first()), clicks)
                assertEquals(listOf(rows.first()), longClicks)
            }
        }
    }

    private fun installRankHost(
        activity: ProgressLayoutTestActivity,
        rows: List<MediaRank>,
        onRankClick: (MediaRank) -> Unit,
        onRankLongClick: (MediaRank) -> Unit,
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        RankingsGrid(
                            rankings = rows,
                            onRankClick = onRankClick,
                            onRankLongClick = onRankLongClick,
                        )
                    }
                }
            },
        )
    }

    private fun findRichMarkdownTextViews(parent: ViewGroup): List<RichMarkdownTextView> = buildList {
        for (index in 0 until parent.childCount) {
            when (val child = parent.getChildAt(index)) {
                is RichMarkdownTextView -> add(child)
                is ViewGroup -> addAll(findRichMarkdownTextViews(child))
            }
        }
    }

    private fun rankFixtures(): List<MediaRank> = listOf(
        MediaRank(
            rank = 64,
            type = KeyUtil.RATED,
            format = KeyUtil.MANGA,
            year = 2015,
            season = KeyUtil.SUMMER,
            context = "Top Rank",
        ),
        MediaRank(
            rank = 1,
            type = KeyUtil.POPULAR,
            format = KeyUtil.ANIME,
            context = "Popular",
        ),
    )

    private companion object {
        const val RANK_CARD_TAG = "rank-row-card"
        const val RATED_ICON_TAG = "rank-row-rated-icon"
        const val POPULAR_ICON_TAG = "rank-row-popular-icon"
    }
}
