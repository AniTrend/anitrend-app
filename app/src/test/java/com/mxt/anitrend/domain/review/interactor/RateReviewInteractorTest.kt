package com.mxt.anitrend.domain.review.interactor

import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.data.store.review.InMemoryReviewStore
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.repository.BrowseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class RateReviewInteractorTest {

    @Test
    fun `successful rate commits ReviewRated with mapped record and revision`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryReviewStore()
        val rated = review(id = 42L, rating = 55, ratingAmount = 3, userRating = "UP_VOTE")
        doReturn(Result.success(rated))
            .`when`(repository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = RateReviewInteractor(
            browseRepository = repository,
            mutationExecutor = mutationExecutor(backgroundScope),
            reviewStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(42L, ReviewRating.UP_VOTE)

        assertEquals(MutationResult.Success, result)
        val record = store.state.value.reviewsById.getValue(42L).review
        assertEquals(42L, record.id)
        assertEquals(55, record.rating)
        assertEquals(3, record.ratingAmount)
        assertEquals("UP_VOTE", record.userRating)
        assertEquals(1L, record.revision)
        assertEquals(1L, store.state.value.reviewsById.getValue(42L).revision)
    }

    @Test
    fun `failed rate returns Failure and does not commit`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryReviewStore()
        doReturn(Result.failure<Review>(IllegalStateException("rate failed")))
            .`when`(repository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = RateReviewInteractor(
            browseRepository = repository,
            mutationExecutor = mutationExecutor(backgroundScope),
            reviewStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(42L, ReviewRating.UP_VOTE)

        assertTrue(result is MutationResult.Failure)
        assertEquals("rate failed", (result as MutationResult.Failure).message)
        assertTrue(store.state.value.reviewsById.isEmpty())
    }

    @Test
    fun `stale rate response is rejected by store`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryReviewStore()
        doReturn(Result.success(review(id = 42L, rating = 55)))
            .`when`(repository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = RateReviewInteractor(
            browseRepository = repository,
            mutationExecutor = mutationExecutor(backgroundScope),
            reviewStore = store,
            requestSequence = RequestSequence(),
        )

        interactor(42L, ReviewRating.UP_VOTE)
        store.apply(
            ReviewStoreChange.ReviewRated(
                review = store.state.value.reviewsById.getValue(42L).review.copy(rating = 10, revision = 0L),
                revision = 0L,
            ),
        )

        assertEquals(55, store.state.value.reviewsById.getValue(42L).review.rating)
        assertEquals(1L, store.state.value.reviewsById.getValue(42L).revision)
    }

    private fun mutationExecutor(scope: CoroutineScope) = DefaultMutationExecutor(
        applicationScope = scope,
        keyedMutex = KeyedMutex(scope),
        mutationRegistry = DefaultMutationRegistry(),
        operationIdGenerator = DefaultOperationIdGenerator(),
        sessionEpoch = SessionEpoch(),
    )

    private fun review(
        id: Long,
        rating: Int = 0,
        ratingAmount: Int = 0,
        userRating: String? = null,
    ): Review = Review().apply {
        this.id = id
        this.rating = rating
        this.ratingAmount = ratingAmount
        this.userRating = userRating
        media.id = 100L
        media.type = MediaType.ANIME.name
    }
}
