package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.ReviewRating
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewVoteStateOwnerTest {

    @Test
    fun `loading is keyed by review and vote direction`() = runTest {
        val owner = ReviewVoteStateOwner(backgroundScope)

        val upVote = requireNotNull(owner.beginVote(review(42L), ReviewRating.UP_VOTE))
        assertNull(owner.beginVote(review(42L), ReviewRating.UP_VOTE))
        val downVote = requireNotNull(owner.beginVote(review(42L), ReviewRating.DOWN_VOTE))
        val otherReviewVote = requireNotNull(owner.beginVote(review(43L), ReviewRating.UP_VOTE))

        assertTrue(owner.state.value.getValue(42L).upVoteLoading)
        assertTrue(owner.state.value.getValue(42L).downVoteLoading)
        assertTrue(owner.state.value.getValue(43L).upVoteLoading)

        owner.complete(upVote)
        assertFalse(owner.state.value.getValue(42L).upVoteLoading)
        assertTrue(owner.state.value.getValue(42L).downVoteLoading)
        assertTrue(owner.state.value.getValue(43L).upVoteLoading)

        owner.complete(downVote)
        owner.complete(otherReviewVote)
        assertFalse(owner.state.value.containsKey(42L))
        assertFalse(owner.state.value.containsKey(43L))
    }

    @Test
    fun `vote loading falls back after ten seconds of fake time`() = runTest {
        val owner = ReviewVoteStateOwner(backgroundScope)
        owner.beginVote(review(42L), ReviewRating.UP_VOTE)

        advanceTimeBy(9_999L)
        runCurrent()
        assertTrue(owner.state.value.getValue(42L).upVoteLoading)

        advanceTimeBy(1L)
        runCurrent()
        assertFalse(owner.state.value.containsKey(42L))
    }

    @Test
    fun `late result for expired request cannot clear a newer request for the same review`() = runTest {
        val owner = ReviewVoteStateOwner(backgroundScope)
        val expiredVote = requireNotNull(owner.beginVote(review(42L), ReviewRating.UP_VOTE))

        advanceTimeBy(10_000L)
        runCurrent()
        val currentVote = requireNotNull(owner.beginVote(review(42L), ReviewRating.UP_VOTE))

        owner.complete(expiredVote)
        assertTrue(owner.state.value.getValue(42L).upVoteLoading)

        owner.complete(currentVote)
        assertFalse(owner.state.value.containsKey(42L))
    }

    @Test
    fun `voting for the active rating submits no vote`() = runTest {
        val owner = ReviewVoteStateOwner(backgroundScope)
        val review = review(42L, userRating = ReviewRating.DOWN_VOTE.name)

        val ticket = owner.beginVote(review, ReviewRating.DOWN_VOTE)

        assertEquals(ReviewRating.NO_VOTE, ticket?.rating)
    }

    private fun review(id: Long, userRating: String? = null) = ReviewRecord(
        id = id,
        summary = "summary",
        mediaType = "ANIME",
        body = "body",
        rating = 80,
        ratingAmount = 100,
        userRating = userRating,
        score = 90,
        isPrivate = false,
        createdAt = 1_600_000_000L,
        user = null,
        media = null,
        revision = 1L,
    )
}
