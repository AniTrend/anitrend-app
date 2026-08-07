package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.mapper.toReviewRecord
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.data.store.review.InMemoryReviewStore
import com.mxt.anitrend.data.store.review.ReviewQueryKey
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.domain.review.interactor.RateReviewInteractor
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.graphql.generated.ReviewSort
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

/**
 * Verifies BrowseReviewViewModel rate-mutation behavior: the outcome is observable as
 * a UI effect through [BrowseReviewViewModel.rateReviewEvents] while the canonical
 * ReviewStore remains the sole committed-state owner, the existing store StateFlow
 * rebinding stays intact, and failures never commit.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BrowseReviewViewModelRateTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var browseRepository: BrowseRepository
    private lateinit var reviewStore: InMemoryReviewStore

    private val queryKey = ReviewQueryKey(
        mediaId = null,
        mediaType = null,
        sort = ReviewSort.ID_DESC,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        browseRepository = mock(BrowseRepository::class.java)
        reviewStore = InMemoryReviewStore()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful rate commits to store, emits Success outcome and rebinds state`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        val outcomes = mutableListOf<ReviewRateOutcome>()
        backgroundScope.launch { viewModel.state.collect {} }
        backgroundScope.launch { viewModel.rateReviewEvents.collect { outcomes += it } }

        stubPage()
        viewModel.load(type = null, page = 1, sort = "ID_DESC")
        advanceUntilIdle()
        assertEquals(0, (viewModel.state.value as BrowseReviewViewModel.UiState.Success).content.pageData.single().rating)

        doReturn(Result.success(review(id = 42L, rating = 55, ratingAmount = 3, userRating = "UP_VOTE")))
            .`when`(browseRepository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        viewModel.rateReview(42L, ReviewRating.UP_VOTE)
        advanceUntilIdle()

        assertEquals(1, outcomes.size)
        assertEquals(42L, outcomes.single().reviewId)
        assertEquals(MutationResult.Success, outcomes.single().result)
        val committed = reviewStore.state.value.reviewsById.getValue(42L).review
        assertEquals(55, committed.rating)
        assertEquals("UP_VOTE", committed.userRating)
        assertEquals(55, (viewModel.state.value as BrowseReviewViewModel.UiState.Success).content.pageData.single().rating)
    }

    @Test
    fun `failed rate emits Failure outcome and does not commit`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        val outcomes = mutableListOf<ReviewRateOutcome>()
        backgroundScope.launch { viewModel.state.collect {} }
        backgroundScope.launch { viewModel.rateReviewEvents.collect { outcomes += it } }

        stubPage()
        viewModel.load(type = null, page = 1, sort = "ID_DESC")
        advanceUntilIdle()

        doReturn(Result.failure<Review>(IllegalStateException("rate failed")))
            .`when`(browseRepository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        viewModel.rateReview(42L, ReviewRating.UP_VOTE)
        advanceUntilIdle()

        assertEquals(1, outcomes.size)
        assertTrue(outcomes.single().result is MutationResult.Failure)
        assertEquals("rate failed", (outcomes.single().result as MutationResult.Failure).message)
        assertEquals(0, reviewStore.state.value.reviewsById.getValue(42L).review.rating)
        assertEquals(0, (viewModel.state.value as BrowseReviewViewModel.UiState.Success).content.pageData.single().rating)
    }

    @Test
    fun `failed rate emitted with no active collector is delivered to a later collector`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }

        stubPage()
        viewModel.load(type = null, page = 1, sort = "ID_DESC")
        advanceUntilIdle()

        doReturn(Result.failure<Review>(IllegalStateException("rate failed")))
            .`when`(browseRepository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        // No outcome collector is active while the mutation runs (the fragment view is
        // not in STARTED): the buffered channel must retain the outcome.
        viewModel.rateReview(42L, ReviewRating.UP_VOTE)
        advanceUntilIdle()

        val outcomes = mutableListOf<ReviewRateOutcome>()
        backgroundScope.launch { viewModel.rateReviewEvents.collect { outcomes += it } }
        advanceUntilIdle()

        assertEquals(1, outcomes.size)
        assertTrue(outcomes.single().result is MutationResult.Failure)
        assertEquals("rate failed", (outcomes.single().result as MutationResult.Failure).message)
        assertEquals(0, reviewStore.state.value.reviewsById.getValue(42L).review.rating)
    }

    private fun TestScope.viewModel(): BrowseReviewViewModel = BrowseReviewViewModel(
        browseRepository = browseRepository,
        reviewStore = reviewStore,
        requestSequence = RequestSequence(),
        rateReviewInteractor = RateReviewInteractor(
            browseRepository = browseRepository,
            mutationExecutor = DefaultMutationExecutor(
                applicationScope = backgroundScope,
                keyedMutex = KeyedMutex(backgroundScope),
                mutationRegistry = DefaultMutationRegistry(),
                operationIdGenerator = DefaultOperationIdGenerator(),
                sessionEpoch = SessionEpoch(),
            ),
            reviewStore = reviewStore,
            requestSequence = RequestSequence(),
        ),
    )

    private fun stubPage() {
        val content = PageContainer<Review>().apply {
            pageData = listOf(review(id = 42L))
        }
        runBlocking {
            reviewStore.apply(
                ReviewStoreChange.PageLoaded(
                    queryKey = queryKey,
                    page = 1,
                    token = 1L,
                    reviews = listOf(review(id = 42L).toReviewRecord(revision = 1L)),
                    pageInfo = null,
                ),
            )
        }
        runBlocking {
            doReturn(Result.success(content))
                .`when`(browseRepository)
                .getReviewBrowse(
                    page = 1,
                    perPage = KeyUtil.PAGING_LIMIT,
                    type = null,
                    sort = listOf(ReviewSort.ID_DESC),
                    asHtml = false,
                    queryKey = queryKey,
                    readToken = 1L,
                )
        }
    }

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
