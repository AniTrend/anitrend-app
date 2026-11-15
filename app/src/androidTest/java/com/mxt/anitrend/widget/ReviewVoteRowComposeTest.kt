@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.review.ReviewVoteRow
import com.mxt.anitrend.ui.review.ReviewVoteRowState
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewVoteRowComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun voteActionsExposeAccessibleDirectionCountAndSelectedState() {
        val upClicks = AtomicInteger()
        val downClicks = AtomicInteger()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installVoteRow(
                    activity = hostActivity,
                    state =
                    ReviewVoteRowState(
                        upVoteCount = 80,
                        downVoteCount = 20,
                        userRating = ReviewRating.UP_VOTE.name,
                    ),
                    onUpVoteClick = { upClicks.incrementAndGet() },
                    onDownVoteClick = { downClicks.incrementAndGet() },
                )
            }
            waitForIdle()

            val upVoteDescription = hostActivity.getString(R.string.description_review_vote_up)
            val downVoteDescription = hostActivity.getString(R.string.description_review_vote_down)
            onNodeWithContentDescription(upVoteDescription)
                .assertIsDisplayed()
                .assertIsSelected()
                .assertHasClickAction()
            onNodeWithContentDescription(downVoteDescription)
                .assertIsDisplayed()
                .assertIsNotSelected()
                .assertHasClickAction()
            onNodeWithText(" 80 ").assertIsDisplayed()
            onNodeWithText(" 20 ").assertIsDisplayed()

            onNodeWithContentDescription(downVoteDescription).performClick()
            runOnIdle {
                assertEquals(0, upClicks.get())
                assertEquals(1, downClicks.get())
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun zeroVoteCountsRemainVisibleWithTheLegacySpacing() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installVoteRow(
                    activity = hostActivity,
                    state = ReviewVoteRowState(upVoteCount = 0, downVoteCount = 0),
                    onUpVoteClick = {},
                    onDownVoteClick = {},
                )
            }
            waitForIdle()

            onAllNodesWithText(" 0 ").assertCountEquals(2)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun pendingVoteExposesAccessibleCompactProgressIndicator() {
        val upClicks = AtomicInteger()
        val downClicks = AtomicInteger()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            mainClock.autoAdvance = false
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installVoteRow(
                    activity = hostActivity,
                    state =
                    ReviewVoteRowState(
                        upVoteCount = 80,
                        downVoteCount = 20,
                        userRating = null,
                        upVoteLoading = true,
                    ),
                    onUpVoteClick = { upClicks.incrementAndGet() },
                    onDownVoteClick = { downClicks.incrementAndGet() },
                )
            }
            waitForIdle()

            val upVoteDescription = hostActivity.getString(R.string.description_review_vote_up)
            val busyDescription = hostActivity.getString(R.string.busy_please_wait)
            onNodeWithContentDescription(upVoteDescription).assertHasClickAction()
            onNodeWithContentDescription(busyDescription, useUnmergedTree = true)
                .assertIsDisplayed()
                .assertHeightIsEqualTo(18.dp)
            onNodeWithText(" 20 ").assertIsDisplayed()
            onNodeWithContentDescription(upVoteDescription).performClick()
            runOnIdle {
                assertEquals(1, upClicks.get())
                assertEquals(0, downClicks.get())
            }
        }
    }

    private fun installVoteRow(
        activity: ProgressLayoutTestActivity,
        state: ReviewVoteRowState,
        onUpVoteClick: () -> Unit,
        onDownVoteClick: () -> Unit,
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        ReviewVoteRow(
                            state = state,
                            onUpVoteClick = onUpVoteClick,
                            onDownVoteClick = onDownVoteClick,
                        )
                    }
                }
            },
        )
    }
}
