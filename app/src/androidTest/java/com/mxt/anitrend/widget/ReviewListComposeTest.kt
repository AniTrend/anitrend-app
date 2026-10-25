@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.review.ReviewImageAction
import com.mxt.anitrend.ui.review.ReviewList
import com.mxt.anitrend.viewmodel.ReviewVotePendingState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewListComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun bannerReviewListKeepsVoteStateAcrossRowRemovalAndForwardsTheHostedReadMoreView() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val review = review(42L, ReviewRating.UP_VOTE.name)
            val rows = mutableStateOf(listOf(review))
            val voteStates = mutableStateOf(emptyMap<Long, ReviewVotePendingState>())
            var requestedVote: Pair<Long, ReviewRating>? = null
            val imageActions = mutableListOf<ReviewImageAction>()
            var readMoreViewId = View.NO_ID
            var hostedReadMoreView: View? = null
            val readMoreSummaries = mutableListOf<String?>()

            hostActivity.installComposeContent {
                AniTrendTheme {
                    ReviewList(
                        reviews = rows.value,
                        isBanner = true,
                        voteStates = voteStates.value,
                        listState = rememberLazyListState(),
                        onVote = { item, rating ->
                            if (voteStates.value[item.id]?.isLoading(rating) != true) {
                                requestedVote = item.id to rating
                            }
                        },
                        onImageAction = { action, _ -> imageActions += action },
                        onReviewClick = { view, item ->
                            hostedReadMoreView = view
                            readMoreViewId = view.id
                            readMoreSummaries += item.summary
                        },
                        onLoadMore = {},
                    )
                }
            }

            val upVoteDescription = hostActivity.getString(R.string.description_review_vote_up)
            val readMoreLabel = hostActivity.getString(R.string.text_read_more)
            onNodeWithText("Preferred").assertIsDisplayed()
            onNodeWithText("summary").assertIsDisplayed()
            onNodeWithContentDescription(upVoteDescription).assertIsSelected().assertHasClickAction()
            onNodeWithContentDescription("Preferred").performClick()
            onNodeWithContentDescription("Preferred").performSemanticsAction(SemanticsActions.OnLongClick) { action -> action() }

            onNodeWithContentDescription(upVoteDescription).performClick()
            onNodeWithContentDescription(readMoreLabel).performClick()
            runOnIdle {
                assertEquals(42L to ReviewRating.UP_VOTE, requestedVote)
                assertEquals(R.id.review_read_more, readMoreViewId)
                assertEquals(listOf("summary"), readMoreSummaries)
                assertEquals(
                    listOf(ReviewImageAction.OPEN_MEDIA, ReviewImageAction.LONG_PRESS_MEDIA),
                    imageActions,
                )
                voteStates.value = mapOf(42L to ReviewVotePendingState(upVoteToken = 7L))
            }
            onNodeWithContentDescription(hostActivity.getString(R.string.busy_please_wait), useUnmergedTree = true)
                .assertIsDisplayed()

            runOnIdle { rows.value = listOf(review.copy(summary = "updated summary")) }
            onNodeWithText("updated summary").assertIsDisplayed()
            runOnIdle {
                requireNotNull(hostedReadMoreView).performClick()
                assertEquals("updated summary", readMoreSummaries.last())
            }
            runOnIdle { rows.value = emptyList() }
            onNodeWithText("Preferred").assertDoesNotExist()
            runOnIdle { rows.value = listOf(review.copy(summary = "updated summary")) }
            onNodeWithContentDescription(hostActivity.getString(R.string.busy_please_wait), useUnmergedTree = true)
                .assertIsDisplayed()

            runOnIdle { voteStates.value = emptyMap() }
            onNodeWithText(" 80 ").assertIsDisplayed()
            onNodeWithText(" 20 ").assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun seriesReviewListDispatchesProfileAndCoverActionsAndUsesItsOwnRowVariant() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val review = review(51L, null)
            val actions = mutableListOf<ReviewImageAction>()
            var readMoreViewId = View.NO_ID

            hostActivity.installComposeContent {
                AniTrendTheme {
                    ReviewList(
                        reviews = listOf(review),
                        isBanner = false,
                        voteStates = emptyMap(),
                        listState = rememberLazyListState(),
                        onVote = { _, _ -> },
                        onImageAction = { action, _ -> actions += action },
                        onReviewClick = { view, _ -> readMoreViewId = view.id },
                        onLoadMore = {},
                    )
                }
            }

            val profileDescription = hostActivity.getString(R.string.description_review_open_profile, "alice")
            val readMoreLabel = hostActivity.getString(R.string.text_read_more)
            onNodeWithText("alice").assertIsDisplayed()
            onNodeWithContentDescription(profileDescription).assertIsDisplayed().performClick()
            onNodeWithContentDescription("Preferred").performSemanticsAction(SemanticsActions.OnLongClick) { action -> action() }
            onNodeWithContentDescription(readMoreLabel).performClick()
            runOnIdle {
                assertEquals(
                    listOf(ReviewImageAction.OPEN_PROFILE, ReviewImageAction.LONG_PRESS_MEDIA),
                    actions,
                )
                assertEquals(R.id.review_read_more, readMoreViewId)
            }
        }
    }

    private fun review(id: Long, userRating: String?) = ReviewRecord(
        id = id,
        summary = "summary",
        mediaType = "ANIME",
        body = "body",
        rating = 80,
        ratingAmount = 100,
        userRating = userRating,
        score = 80,
        isPrivate = false,
        createdAt = 1_600_000_000L,
        user = UserSummaryRecord(
            id = 7L,
            name = "alice",
            avatar = null,
            siteUrl = null,
        ),
        media = MediaSummaryRecord(
            id = 44L,
            titleUserPreferred = "Preferred",
            titleRomaji = null,
            titleEnglish = null,
            titleOriginal = null,
            coverImage = null,
            bannerImage = null,
            type = "ANIME",
            format = null,
            episodes = 12,
            chapters = 0,
            volumes = 0,
            status = null,
            siteUrl = null,
        ),
        revision = 1L,
    )
}
