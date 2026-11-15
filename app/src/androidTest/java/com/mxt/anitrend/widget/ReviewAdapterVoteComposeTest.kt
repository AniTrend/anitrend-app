@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.base.custom.view.widget.CustomRatingBar
import com.mxt.anitrend.adapter.recycler.index.ReviewAdapter
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val REVIEW_SERIES_COVER_TAG = "review-series-cover"

@RunWith(AndroidJUnit4::class)
class ReviewAdapterVoteComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun browseBannerExposesAccessibleContentAndActionSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val imageUrl = "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}"
            val initialReview = review(userRating = ReviewRating.UP_VOTE.name)
            val bannerReview =
                initialReview.copy(
                    media = requireNotNull(initialReview.media).copy(bannerImage = imageUrl),
                )
            lateinit var host: ReviewAdapterHost
            runOnUiThread {
                host = installReviewAdapter(
                    activity = hostActivity,
                    isMediaType = false,
                    currentUser = currentUser(),
                    review = bannerReview,
                    onRateReview = { _, _ -> },
                )
            }
            waitUntil("review banner binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val bannerImage = onNodeWithContentDescription("Preferred")
            bannerImage
                .assertIsDisplayed()
                .assertHasClickAction()
                .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
            waitUntil("local review banner displays image pixels", 5_000L) {
                bannerImage.hasImageContentPixels()
            }
            onNodeWithText("Preferred").assertIsDisplayed()
            onNodeWithText("summary").assertIsDisplayed()
            val readMoreLabel = hostActivity.getString(R.string.text_read_more)
            onNodeWithContentDescription(readMoreLabel)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun browseBannerClearsStaleContentWhenMediaAndSummaryAreMissing() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: ReviewAdapterHost
            runOnUiThread {
                host = installReviewAdapter(
                    activity = hostActivity,
                    isMediaType = false,
                    currentUser = currentUser(),
                    review = review(userRating = null),
                    onRateReview = { _, _ -> },
                )
            }
            waitUntil("initial review banner binds", 5_000L) {
                host.adapter.currentList.singleOrNull()?.id == 42L &&
                    host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()
            onNodeWithText("Preferred").assertIsDisplayed()
            onNodeWithText("summary").assertIsDisplayed()
            val bannerImage = onNodeWithContentDescription("Preferred")
            bannerImage.assertIsDisplayed().assertHasClickAction()
            waitUntil("review banner fallback displays image pixels", 5_000L) {
                bannerImage.hasImageContentPixels()
            }

            val missingContent = review(userRating = null).copy(
                id = 43L,
                summary = null,
                user = null,
                media = null,
                rating = 0,
                ratingAmount = 0,
                score = 0,
            )
            runOnUiThread { host.adapter.submitList(listOf(missingContent)) }
            waitUntil("rebound review banner updates its model", 5_000L) {
                host.adapter.currentList.singleOrNull()?.id == 43L
            }
            waitForIdle()

            onNodeWithText("Preferred").assertDoesNotExist()
            onNodeWithText("summary").assertDoesNotExist()
            onNodeWithContentDescription("Preferred").assertDoesNotExist()
            val missingMediaBanner =
                onNodeWithContentDescription(hostActivity.getString(R.string.drawer_title_reviews))
            missingMediaBanner.assertIsDisplayed().assertHasClickAction()
            waitUntil("missing-media banner fallback stays visible", 5_000L) {
                missingMediaBanner.hasImageContentPixels()
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun failedBrowseBannerUsesSharedErrorDrawable() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val initialReview = review(userRating = ReviewRating.UP_VOTE.name)
            val failedBanner =
                initialReview.copy(
                    media = requireNotNull(initialReview.media).copy(
                        bannerImage = "android.resource://${hostActivity.packageName}/0",
                    ),
                )
            lateinit var host: ReviewAdapterHost
            runOnUiThread {
                host =
                    installReviewAdapter(
                        activity = hostActivity,
                        isMediaType = false,
                        currentUser = currentUser(),
                        review = failedBanner,
                        onRateReview = { _, _ -> },
                    )
            }
            waitUntil("failed review banner binds", 5_000L) {
                host.adapter.currentList.singleOrNull() == failedBanner &&
                    host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val bannerImage = onNodeWithContentDescription("Preferred")
            bannerImage.assertIsDisplayed()
            // At 10% height the overlay alpha is about 5%; allow the resulting color tint.
            waitUntil("failed banner uses the shared error drawable", 5_000L) {
                bannerImage.upperCenterColorMatches(Color.rgb(255, 224, 74))
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun seriesReviewRowExposesAccessibleActionsAndContent() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val imageUrl = "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}"
            val initialReview = review(userRating = ReviewRating.UP_VOTE.name)
            val record =
                initialReview.copy(
                    media = requireNotNull(initialReview.media).copy(coverImage = imageUrl),
                )
            lateinit var host: ReviewAdapterHost
            runOnUiThread {
                host =
                    installReviewAdapter(
                        activity = hostActivity,
                        isMediaType = true,
                        currentUser = currentUser(),
                        review = record,
                        onRateReview = { _, _ -> },
                    )
            }
            waitUntil("series review holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val profileDescription =
                hostActivity.getString(
                    R.string.description_review_open_profile,
                    requireNotNull(record.user).name,
                )
            val readMoreDescription = hostActivity.getString(R.string.text_read_more)
            val coverDescription = requireNotNull(record.media).titleUserPreferred.orEmpty()
            val upVoteDescription = hostActivity.getString(R.string.description_review_vote_up)
            val downVoteDescription = hostActivity.getString(R.string.description_review_vote_down)

            val profileNode = onNodeWithContentDescription(profileDescription)
            profileNode.assertIsDisplayed().assertHasClickAction()
            waitUntil("missing review avatar displays fallback pixels", 5_000L) {
                profileNode.centerColorMatches(Color.rgb(255, 224, 74))
            }
            onNodeWithContentDescription(readMoreDescription).assertIsDisplayed().assertHasClickAction()
            val coverNode = onNodeWithContentDescription(coverDescription)
            coverNode
                .assertIsDisplayed()
                .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
            waitUntil("local series-review cover displays image pixels", 5_000L) {
                coverNode.hasImageContentPixels()
            }
            onNodeWithContentDescription(upVoteDescription).assertIsSelected()
            onNodeWithContentDescription(downVoteDescription).assertIsNotSelected()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun seriesReviewRebindUpdatesMarkdownRatingAndClearsCover() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val initialReview = review(userRating = ReviewRating.UP_VOTE.name)
            val coverUrl = "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}"
            val avatarUrl = "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}"
            val expectedAvatarColor = resourceCenterColor(hostActivity, R.drawable.reg_bg)
            val populatedReview =
                initialReview.copy(
                    summary = "A **marked** review with [a link](https://anilist.co).",
                    user = requireNotNull(initialReview.user).copy(avatar = avatarUrl),
                    media = requireNotNull(initialReview.media).copy(coverImage = coverUrl),
                )
            lateinit var host: ReviewAdapterHost
            runOnUiThread {
                host =
                    installReviewAdapter(
                        activity = hostActivity,
                        isMediaType = true,
                        currentUser = currentUser(),
                        review = populatedReview,
                        onRateReview = { _, _ -> },
                    )
            }
            waitUntil("local series-review content binds", 5_000L) {
                host.adapter.currentList.singleOrNull() == populatedReview &&
                    host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val coverDescription = requireNotNull(populatedReview.media).titleUserPreferred.orEmpty()
            val profileDescription =
                hostActivity.getString(
                    R.string.description_review_open_profile,
                    requireNotNull(populatedReview.user).name,
                )
            val coverNode = onNodeWithContentDescription(coverDescription)
            val profileNode = onNodeWithContentDescription(profileDescription)
            val coverSlot = onNodeWithTag(REVIEW_SERIES_COVER_TAG, useUnmergedTree = true)
            coverNode.assertIsDisplayed()
            profileNode.assertIsDisplayed()
            waitUntil("local series-review cover renders", 5_000L) {
                coverSlot.hasImageContentPixels()
            }
            waitUntil("local series-review avatar renders", 5_000L) {
                profileNode.centerColorMatches(expectedAvatarColor)
            }

            var summaryText = ""
            var linkCount = 0
            var score = 0f
            var scoreIsIndicator = false
            var ratingDescription = ""
            var ratingIsAccessible = false
            runOnUiThread {
                val row = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
                val summary = row.findViewById<RichMarkdownTextView>(R.id.review_summary)
                summaryText = summary.text.toString()
                linkCount =
                    (summary.text as Spanned)
                        .getSpans(0, summary.text.length, ClickableSpan::class.java)
                        .size
                val rating = row.findViewById<CustomRatingBar>(R.id.series_rating)
                score = rating.rating
                scoreIsIndicator = rating.isIndicator
                ratingDescription = rating.contentDescription?.toString().orEmpty()
                ratingIsAccessible = rating.importantForAccessibility == View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }
            assertEquals("A marked review with a link.", summaryText)
            assertTrue(linkCount > 0)
            assertEquals(4f, score, 0.01f)
            assertTrue(scoreIsIndicator)
            assertEquals(hostActivity.getString(R.string.description_review_rating, 4.0f), ratingDescription)
            assertTrue("rating remains accessible to TalkBack", ratingIsAccessible)

            val failedReview =
                populatedReview.copy(
                    media = requireNotNull(populatedReview.media).copy(
                        coverImage = "android.resource://${hostActivity.packageName}/0",
                    ),
                )
            runOnUiThread { host.adapter.submitList(listOf(failedReview)) }
            waitUntil("failed cover request shows the shared error drawable", 5_000L) {
                coverSlot.centerColorMatches(Color.rgb(255, 224, 74))
            }

            val missingReview =
                populatedReview.copy(
                    summary = null,
                    user = null,
                    media = null,
                    rating = 0,
                    ratingAmount = 0,
                    score = 0,
                    userRating = null,
                )
            runOnUiThread { host.adapter.submitList(listOf(missingReview)) }
            waitUntil("rebound review updates its empty content", 5_000L) {
                host.adapter.currentList.singleOrNull() == missingReview &&
                    host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            var emptySummary = ""
            var emptyScore = -1f
            var emptyRatingDescription = ""
            runOnUiThread {
                val row = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
                emptySummary = row.findViewById<RichMarkdownTextView>(R.id.review_summary).text.toString()
                val rating = row.findViewById<CustomRatingBar>(R.id.series_rating)
                emptyScore = rating.rating
                emptyRatingDescription = rating.contentDescription?.toString().orEmpty()
                ratingIsAccessible = rating.importantForAccessibility == View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }
            assertEquals("No content available", emptySummary)
            assertEquals(0f, emptyScore, 0.01f)
            assertEquals(hostActivity.getString(R.string.description_review_rating, 0.0f), emptyRatingDescription)
            assertTrue("rating remains accessible to TalkBack after rebind", ratingIsAccessible)
            onNodeWithContentDescription(coverDescription).assertDoesNotExist()
            waitUntil("rebound review cover slot has no stale image pixels", 5_000L) {
                !coverSlot.hasImageContentPixels()
            }
            onNodeWithContentDescription(profileDescription).assertDoesNotExist()
            val fallbackAvatar = onNodeWithContentDescription(hostActivity.getString(R.string.profile_avatar_description))
            fallbackAvatar.assertIsDisplayed()
            waitUntil("missing reviewer displays avatar fallback pixels", 5_000L) {
                fallbackAvatar.centerColorMatches(Color.rgb(255, 224, 74))
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun browseVoteClearsActiveRatingAndResetsWithoutOptimisticCounts() {
        val ratings = CopyOnWriteArrayList<Pair<Long, ReviewRating?>>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: ReviewAdapterHost
            runOnUiThread {
                host = installReviewAdapter(
                    activity = hostActivity,
                    isMediaType = false,
                    currentUser = currentUser(),
                    review = review(userRating = ReviewRating.UP_VOTE.name),
                    onRateReview = { id, rating -> ratings.add(id to rating) },
                )
            }
            waitUntil("browse review holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val upVoteDescription = hostActivity.getString(R.string.description_review_vote_up)
            val busyDescription = hostActivity.getString(R.string.busy_please_wait)
            onNodeWithContentDescription(upVoteDescription).assertIsSelected().performClick()
            runOnIdle { assertEquals(listOf(42L to ReviewRating.NO_VOTE), ratings.toList()) }
            onNodeWithContentDescription(busyDescription, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithContentDescription(upVoteDescription).performClick()
            runOnIdle { assertEquals(listOf(42L to ReviewRating.NO_VOTE), ratings.toList()) }

            runOnUiThread { host.adapter.onRateReviewResult(42L, MutationResult.Success) }
            waitForIdle()
            onNodeWithContentDescription(upVoteDescription)
                .assertIsDisplayed()
                .assertIsSelected()
            onNodeWithText(" 80 ").assertIsDisplayed()
            onNodeWithText(" 20 ").assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun mediaReviewVoteDispatchesInactiveRatingAndFailureResetsLoading() {
        val ratings = CopyOnWriteArrayList<Pair<Long, ReviewRating?>>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: ReviewAdapterHost
            runOnUiThread {
                host = installReviewAdapter(
                    activity = hostActivity,
                    isMediaType = true,
                    currentUser = currentUser(),
                    review = review(userRating = null),
                    onRateReview = { id, rating -> ratings.add(id to rating) },
                )
            }
            waitUntil("media review holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val downVoteDescription = hostActivity.getString(R.string.description_review_vote_down)
            val busyDescription = hostActivity.getString(R.string.busy_please_wait)
            onNodeWithContentDescription(downVoteDescription).assertIsNotSelected().performClick()
            runOnIdle { assertEquals(listOf(42L to ReviewRating.DOWN_VOTE), ratings.toList()) }
            onNodeWithContentDescription(busyDescription, useUnmergedTree = true).assertIsDisplayed()

            runOnUiThread {
                host.adapter.onRateReviewResult(
                    42L,
                    MutationResult.Failure(message = "rate failed"),
                )
            }
            waitForIdle()
            onNodeWithContentDescription(downVoteDescription)
                .assertIsDisplayed()
                .assertIsNotSelected()
            onNodeWithText(" 80 ").assertIsDisplayed()
            onNodeWithText(" 20 ").assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun voteTargetsSwitchAndClearTheCurrentDirection() {
        val ratings = CopyOnWriteArrayList<Pair<Long, ReviewRating?>>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: ReviewAdapterHost
            runOnUiThread {
                host = installReviewAdapter(
                    activity = hostActivity,
                    isMediaType = true,
                    currentUser = currentUser(),
                    review = review(userRating = ReviewRating.UP_VOTE.name),
                    onRateReview = { id, rating -> ratings.add(id to rating) },
                )
            }
            waitUntil("vote transition holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val upVoteDescription = hostActivity.getString(R.string.description_review_vote_up)
            val downVoteDescription = hostActivity.getString(R.string.description_review_vote_down)
            val scenarios =
                listOf(
                    Triple(ReviewRating.UP_VOTE.name, downVoteDescription, ReviewRating.DOWN_VOTE),
                    Triple(ReviewRating.DOWN_VOTE.name, upVoteDescription, ReviewRating.UP_VOTE),
                    Triple(ReviewRating.DOWN_VOTE.name, downVoteDescription, ReviewRating.NO_VOTE),
                    Triple(ReviewRating.NO_VOTE.name, upVoteDescription, ReviewRating.UP_VOTE),
                    Triple(ReviewRating.NO_VOTE.name, downVoteDescription, ReviewRating.DOWN_VOTE),
                    Triple(null, upVoteDescription, ReviewRating.UP_VOTE),
                    Triple(null, downVoteDescription, ReviewRating.DOWN_VOTE),
                )
            for ((currentRating, description, expected) in scenarios) {
                runOnUiThread { host.adapter.submitList(listOf(review(userRating = currentRating))) }
                waitUntil("vote row receives current rating", 5_000L) {
                    host.adapter.currentList.singleOrNull()?.userRating == currentRating
                }
                waitForIdle()

                ratings.clear()
                onNodeWithContentDescription(description).performClick()
                runOnIdle { assertEquals(listOf(42L to expected), ratings.toList()) }
                runOnUiThread { host.adapter.onRateReviewResult(42L, MutationResult.Success) }
                waitForIdle()
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun downVoteCountClampsToZeroWhenRatingExceedsAmount() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: ReviewAdapterHost
            runOnUiThread {
                host = installReviewAdapter(
                    activity = hostActivity,
                    isMediaType = true,
                    currentUser = currentUser(),
                    review = review(userRating = null, rating = 80, ratingAmount = 50),
                    onRateReview = { _, _ -> },
                )
            }
            waitUntil("clamped-vote holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()
            onNodeWithText(" 0 ").assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun loggedOutReviewVoteDoesNotDispatchMutation() {
        val ratings = CopyOnWriteArrayList<Pair<Long, ReviewRating?>>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: ReviewAdapterHost
            runOnUiThread {
                host = installReviewAdapter(
                    activity = hostActivity,
                    isMediaType = true,
                    currentUser = null,
                    review = review(userRating = null),
                    onRateReview = { id, rating -> ratings.add(id to rating) },
                )
            }
            waitUntil("logged-out review holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            onNodeWithContentDescription(hostActivity.getString(R.string.description_review_vote_up))
                .performClick()
            runOnIdle { assertEquals(emptyList<Pair<Long, ReviewRating?>>(), ratings.toList()) }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun recycledVoteHolderCannotDispatchAgainstItsFormerReview() {
        val ratings = CopyOnWriteArrayList<Pair<Long, ReviewRating?>>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: ReviewAdapterHost
            runOnUiThread {
                host = installReviewAdapter(
                    activity = hostActivity,
                    isMediaType = false,
                    currentUser = currentUser(),
                    review = review(userRating = null),
                    onRateReview = { id, rating -> ratings.add(id to rating) },
                )
            }
            waitUntil("recycled review holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val upVoteDescription = hostActivity.getString(R.string.description_review_vote_up)
            onNodeWithContentDescription(upVoteDescription).performClick()
            runOnIdle { assertEquals(listOf(42L to ReviewRating.UP_VOTE), ratings.toList()) }

            runOnUiThread {
                host.adapter.onViewRecycled(
                    requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)),
                )
            }
            waitForIdle()
            onNodeWithContentDescription(upVoteDescription).performClick()
            runOnIdle { assertEquals(listOf(42L to ReviewRating.UP_VOTE), ratings.toList()) }
        }
    }

    private fun installReviewAdapter(
        activity: ProgressLayoutTestActivity,
        isMediaType: Boolean,
        currentUser: UserBase?,
        review: ReviewRecord,
        onRateReview: (Long, ReviewRating?) -> Unit,
    ): ReviewAdapterHost {
        val adapter = ReviewAdapter(
            currentUser = currentUser,
            onRateReviewAction = onRateReview,
            onReviewImageAction = { _, _ -> },
            isMediaType = isMediaType,
        )
        val recyclerView = RecyclerView(activity).apply {
            // Avoid duplicate Compose semantics from RecyclerView change animations during rebinds.
            itemAnimator = null
            layoutManager = LinearLayoutManager(activity)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
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
        adapter.submitList(listOf(review))
        return ReviewAdapterHost(adapter, recyclerView)
    }

    private fun currentUser(): UserBase = UserBase(name = "current-user").apply { id = 1L }

    private fun review(
        userRating: String?,
        rating: Int = 80,
        ratingAmount: Int = 100,
    ): ReviewRecord = ReviewRecord(
        id = 42L,
        summary = "summary",
        mediaType = "ANIME",
        body = "body",
        rating = rating,
        ratingAmount = ratingAmount,
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

    private data class ReviewAdapterHost(
        val adapter: ReviewAdapter,
        val recyclerView: RecyclerView,
    )
}

private fun resourceCenterColor(
    activity: ProgressLayoutTestActivity,
    resourceId: Int,
): Int {
    val image = requireNotNull(BitmapFactory.decodeResource(activity.resources, resourceId))
    return try {
        image.getPixel(image.width / 2, image.height / 2)
    } finally {
        image.recycle()
    }
}

private fun SemanticsNodeInteraction.hasImageContentPixels(): Boolean {
    val image = captureToImage().asAndroidBitmap()
    return try {
        image.hasImageContentPixels()
    } finally {
        image.recycle()
    }
}

private fun SemanticsNodeInteraction.centerColorMatches(expectedColor: Int): Boolean {
    val image = captureToImage().asAndroidBitmap()
    return try {
        image.centerColorMatches(expectedColor)
    } finally {
        image.recycle()
    }
}

private fun SemanticsNodeInteraction.upperCenterColorMatches(expectedColor: Int): Boolean {
    val image = captureToImage().asAndroidBitmap()
    return try {
        image.upperCenterColorMatches(expectedColor)
    } finally {
        image.recycle()
    }
}

private fun Bitmap.upperCenterColorMatches(expectedColor: Int): Boolean {
    val actualColor = getPixel(width / 2, height / 10)
    val tolerance = 32
    return abs(Color.red(actualColor) - Color.red(expectedColor)) <= tolerance &&
        abs(Color.green(actualColor) - Color.green(expectedColor)) <= tolerance &&
        abs(Color.blue(actualColor) - Color.blue(expectedColor)) <= tolerance
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
            if (Color.alpha(color) > 240) {
                colors += color
                if (colors.size >= 16) return true
            }
        }
    }
    return false
}

private fun Bitmap.centerColorMatches(expectedColor: Int): Boolean {
    val actualColor = getPixel(width / 2, height / 2)
    val tolerance = 24
    return abs(Color.red(actualColor) - Color.red(expectedColor)) <= tolerance &&
        abs(Color.green(actualColor) - Color.green(expectedColor)) <= tolerance &&
        abs(Color.blue(actualColor) - Color.blue(expectedColor)) <= tolerance
}
