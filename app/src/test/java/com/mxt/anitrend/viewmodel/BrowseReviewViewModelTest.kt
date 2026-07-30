package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewSort
import com.mxt.anitrend.data.store.review.InMemoryReviewStore
import com.mxt.anitrend.data.store.review.ReviewQueryKey
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseReviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var browseRepository: BrowseRepository
    private lateinit var reviewStore: InMemoryReviewStore

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
    fun `initial state is Loading`() = runTest {
        val vm = BrowseReviewViewModel(browseRepository = browseRepository, reviewStore = reviewStore)
        assertTrue(vm.state.value is BrowseReviewViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success and defaults null sort`() = runTest {
        val review = Review().apply {
            id = 21L
            media.type = MediaType.ANIME.name
        }
        val content = PageContainer<Review>().apply { pageData = listOf(review) }
        val queryKey = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC)
        reviewStore.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                generation = 1,
                reviews = listOf(review),
                pageInfo = null,
            ),
        )
        doReturn(Result.success(content))
            .`when`(browseRepository)
            .getReviewBrowse(
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                type = MediaType.ANIME,
                sort = listOf(ReviewSort.CREATED_AT_DESC),
                asHtml = false,
                commitToStore = true,
                queryKey = queryKey,
                queryGeneration = 1,
            )
        val vm = BrowseReviewViewModel(browseRepository = browseRepository, reviewStore = reviewStore)
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(type = MediaType.ANIME, page = 1, sort = null)
        advanceUntilIdle()

        val state = vm.state.value as BrowseReviewViewModel.UiState.Success
        assertEquals(listOf(21L), state.content.pageData.map { it.id })
        verify(browseRepository).getReviewBrowse(
            page = 1,
            perPage = KeyUtil.PAGING_LIMIT,
            type = MediaType.ANIME,
            sort = listOf(ReviewSort.CREATED_AT_DESC),
            asHtml = false,
            commitToStore = true,
            queryKey = queryKey,
            queryGeneration = 1,
        )
        collector.cancel()
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        val queryKey = ReviewQueryKey(mediaId = null, mediaType = null, sort = ReviewSort.CREATED_AT_DESC)
        doReturn(Result.failure<PageContainer<Review>>(RuntimeException("Reviews failed")))
            .`when`(browseRepository)
            .getReviewBrowse(
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                type = null,
                sort = listOf(ReviewSort.CREATED_AT_DESC),
                asHtml = false,
                commitToStore = true,
                queryKey = queryKey,
                queryGeneration = 1,
            )
        val vm = BrowseReviewViewModel(browseRepository = browseRepository, reviewStore = reviewStore)
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(type = null, page = 1, sort = null)
        advanceUntilIdle()

        val state = vm.state.value as BrowseReviewViewModel.UiState.Error
        assertEquals("Reviews failed", state.message)
        collector.cancel()
    }

    @Test
    fun `rated review committed to store updates rendered state`() = runTest {
        val queryKey = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC)
        val review = Review().apply {
            id = 11L
            media.type = MediaType.ANIME.name
            rating = 10
        }
        val updatedReview = Review().apply {
            id = 11L
            media.type = MediaType.ANIME.name
            rating = 42
            ratingAmount = 5
        }
        val vm = BrowseReviewViewModel(browseRepository = browseRepository, reviewStore = reviewStore)
        val collector = backgroundScope.launch { vm.state.collect {} }

        reviewStore.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                generation = 1,
                reviews = listOf(review),
                pageInfo = null,
            ),
        )
        doReturn(Result.success(PageContainer<Review>().apply { pageData = listOf(review) }))
            .`when`(browseRepository)
            .getReviewBrowse(
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                type = MediaType.ANIME,
                sort = listOf(ReviewSort.CREATED_AT_DESC),
                asHtml = false,
                commitToStore = true,
                queryKey = queryKey,
                queryGeneration = 1,
            )

        vm.load(type = MediaType.ANIME, page = 1, sort = null)
        advanceUntilIdle()
        reviewStore.apply(ReviewStoreChange.ReviewRated(updatedReview, revision = 1L))
        advanceUntilIdle()

        val state = vm.state.value as BrowseReviewViewModel.UiState.Success
        assertEquals(42, state.content.pageData.first().rating)
        collector.cancel()
    }
}
