package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.graphql.generated.MediaType
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
class ReviewViewModelTest {

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
        val vm = ReviewViewModel(browseRepository = browseRepository, reviewStore = reviewStore)
        assertTrue(vm.state.value is ReviewViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success and passes media id`() = runTest {
        val review = Review().apply {
            id = 33L
            media.id = 100L
            media.type = MediaType.ANIME.name
        }
        val content = PageContainer<Review>().apply { pageData = listOf(review) }
        val queryKey = ReviewQueryKey(mediaId = 100L, mediaType = MediaType.ANIME, sort = null)
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
                mediaId = 100L,
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                type = MediaType.ANIME,
                asHtml = false,
                commitToStore = true,
                queryKey = queryKey,
                queryGeneration = 1,
            )
        val vm = ReviewViewModel(browseRepository = browseRepository, reviewStore = reviewStore)
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(mediaId = 100L, type = MediaType.ANIME, page = 1)
        advanceUntilIdle()

        val state = vm.state.value as ReviewViewModel.UiState.Success
        assertEquals(listOf(33L), state.content.pageData.map { it.id })
        verify(browseRepository).getReviewBrowse(
            mediaId = 100L,
            page = 1,
            perPage = KeyUtil.PAGING_LIMIT,
            type = MediaType.ANIME,
            asHtml = false,
            commitToStore = true,
            queryKey = queryKey,
            queryGeneration = 1,
        )
        collector.cancel()
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        val queryKey = ReviewQueryKey(mediaId = 100L, mediaType = null, sort = null)
        doReturn(Result.failure<PageContainer<Review>>(RuntimeException("Review failed")))
            .`when`(browseRepository)
            .getReviewBrowse(
                mediaId = 100L,
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                type = null,
                asHtml = false,
                commitToStore = true,
                queryKey = queryKey,
                queryGeneration = 1,
            )
        val vm = ReviewViewModel(browseRepository = browseRepository, reviewStore = reviewStore)
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(mediaId = 100L, type = null, page = 1)
        advanceUntilIdle()

        val state = vm.state.value as ReviewViewModel.UiState.Error
        assertEquals("Review failed", state.message)
        collector.cancel()
    }

    @Test
    fun `store delete removes review from rendered state`() = runTest {
        val queryKey = ReviewQueryKey(mediaId = 100L, mediaType = MediaType.ANIME, sort = null)
        val review = Review().apply {
            id = 7L
            media.id = 100L
            media.type = MediaType.ANIME.name
        }
        val vm = ReviewViewModel(browseRepository = browseRepository, reviewStore = reviewStore)
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
                mediaId = 100L,
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                type = MediaType.ANIME,
                asHtml = false,
                commitToStore = true,
                queryKey = queryKey,
                queryGeneration = 1,
            )

        vm.load(mediaId = 100L, type = MediaType.ANIME, page = 1)
        advanceUntilIdle()
        reviewStore.apply(ReviewStoreChange.ReviewDeleted(reviewId = 7L, revision = 1L))
        advanceUntilIdle()

        val state = vm.state.value as ReviewViewModel.UiState.Success
        assertTrue(state.content.pageData.isEmpty())
        collector.cancel()
    }
}
