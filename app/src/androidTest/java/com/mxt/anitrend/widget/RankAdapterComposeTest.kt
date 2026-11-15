@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.RankAdapter
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.model.entity.anilist.MediaRank
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RankAdapterComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun rankRowsExposeFormattedTextAndTypeIcons() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val rows = rankFixtures()
            lateinit var recyclerView: RecyclerView
            runOnUiThread { recyclerView = installRankHost(hostActivity, rows) }
            waitUntil("ranking rows bind", 5_000L) {
                rows.indices.all { recyclerView.findViewHolderForAdapterPosition(it) != null }
            }
            waitForIdle()

            lateinit var richTextViews: List<RichMarkdownTextView>
            runOnUiThread {
                richTextViews =
                    rows.indices.map { position ->
                        val row = requireNotNull(recyclerView.findViewHolderForAdapterPosition(position)?.itemView)
                        requireNotNull(row.findViewById(R.id.sub_container))
                    }
            }
            onAllNodesWithTag(RANK_CARD_TAG, useUnmergedTree = true)[0].assertIsDisplayed()
            onAllNodesWithTag(RANK_CARD_TAG, useUnmergedTree = true)[1].assertIsDisplayed()
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
            val clicks = mutableListOf<Pair<Int, MediaRank>>()
            val longClicks = mutableListOf<Pair<Int, MediaRank>>()
            lateinit var recyclerView: RecyclerView
            runOnUiThread {
                recyclerView =
                    installRankHost(hostActivity, rows) { adapter ->
                        adapter.setClickListener(
                            object : ItemClickListener<MediaRank> {
                                override fun onItemClick(
                                    target: View,
                                    data: IndexedValue<MediaRank>,
                                ) {
                                    clicks += target.id to data.value
                                }

                                override fun onItemLongClick(
                                    target: View,
                                    data: IndexedValue<MediaRank>,
                                ) {
                                    longClicks += target.id to data.value
                                }
                            },
                        )
                    }
            }
            waitUntil("ranking rows bind", 5_000L) {
                recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val rootCard = onAllNodesWithTag(RANK_CARD_TAG)[0]
            rootCard.performTouchInput { click(Offset(x = 12.dp.toPx(), y = center.y)) }
            lateinit var rankTextView: View
            runOnUiThread {
                rankTextView =
                    requireNotNull(
                        recyclerView
                            .findViewHolderForAdapterPosition(0)
                            ?.itemView
                            ?.findViewById(R.id.sub_container),
                    )
            }
            runOnUiThread { rankTextView.performClick() }
            rootCard.performTouchInput { longClick(Offset(x = 12.dp.toPx(), y = center.y)) }
            runOnIdle {
                assertEquals(
                    listOf(R.id.container to rows.first(), R.id.sub_container to rows.first()),
                    clicks,
                )
                assertEquals(listOf(R.id.container to rows.first()), longClicks)
            }
        }
    }

    private fun installRankHost(
        activity: ProgressLayoutTestActivity,
        rows: List<MediaRank>,
        configure: (RankAdapter) -> Unit = {},
    ): RecyclerView {
        val adapter = RankAdapter(activity).apply {
            onItemsInserted(rows)
            configure(this)
        }
        val recyclerView =
            RecyclerView(activity).apply {
                setHasFixedSize(true)
                layoutManager =
                    StaggeredGridLayoutManager(
                        activity.resources.getInteger(R.integer.grid_list_x2),
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
        return recyclerView
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
