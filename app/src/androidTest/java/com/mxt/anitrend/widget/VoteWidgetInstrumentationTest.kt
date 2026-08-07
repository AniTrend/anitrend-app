@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.widget.ViewFlipper
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.VoteWidget
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.model.entity.base.UserBase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Vote control convergence for review rating mutations ([VoteWidget]): the tapped thumb
 * stays in the loading state while the mutation is in flight (no optimistic store
 * mutation), a failure outcome resets the loading state and surfaces the message, and a
 * success outcome resets the loading state while the canonical store rebinding renders
 * the committed rating.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class VoteWidgetInstrumentationTest {

    @Test
    fun clickKeepsLoadingUntilOutcome_failureResetsAndDeliversRating() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val widget = VoteWidget(activity)
                widget.setCurrentUser(
                    UserBase(name = "current-user").apply {
                        id = 1L
                    },
                )
                widget.setModel(review(), 0)

                val deliveredRatings = mutableListOf<Pair<Long, ReviewRating?>>()
                widget.setListener(
                    object : VoteWidget.Listener {
                        override fun onRateReview(
                            id: Long,
                            rating: ReviewRating?,
                        ) {
                            deliveredRatings.add(id to rating)
                        }
                    },
                )

                val flipper = widget.findViewById<ViewFlipper>(R.id.widget_thumb_up_flipper)
                flipper.performClick()

                // No optimistic mutation: the thumb stays in the loading state in flight.
                assertEquals(listOf(42L to ReviewRating.UP_VOTE), deliveredRatings)
                assertEquals(VoteWidget.LOADING_STATE, flipper.displayedChild)

                widget.onRateReviewResult(MutationResult.Failure(message = "rate failed"))

                assertEquals(VoteWidget.CONTENT_STATE, flipper.displayedChild)
            }
        }
    }

    @Test
    fun successOutcomeResetsLoadingState() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val widget = VoteWidget(activity)
                widget.setCurrentUser(
                    UserBase(name = "current-user").apply {
                        id = 1L
                    },
                )
                widget.setModel(review(), 0)
                widget.setListener(
                    object : VoteWidget.Listener {
                        override fun onRateReview(
                            id: Long,
                            rating: ReviewRating?,
                        ) = Unit
                    },
                )

                val flipper = widget.findViewById<ViewFlipper>(R.id.widget_thumb_up_flipper)
                flipper.performClick()
                assertEquals(VoteWidget.LOADING_STATE, flipper.displayedChild)

                widget.onRateReviewResult(MutationResult.Success)

                assertEquals(VoteWidget.CONTENT_STATE, flipper.displayedChild)
            }
        }
    }

    private fun review(): ReviewRecord = ReviewRecord(
        id = 42L,
        summary = "summary",
        mediaType = "ANIME",
        body = "body",
        rating = 10,
        ratingAmount = 20,
        userRating = null,
        score = 80,
        isPrivate = false,
        createdAt = 1_600_000_000L,
        user =
        UserSummaryRecord(
            id = 7L,
            name = "alice",
            avatar = null,
            siteUrl = null,
        ),
        media =
        MediaSummaryRecord(
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
