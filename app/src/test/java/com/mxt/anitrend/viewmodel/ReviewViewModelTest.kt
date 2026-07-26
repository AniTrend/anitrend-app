package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        browseRepository = mock(BrowseRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = ReviewViewModel(browseRepository = browseRepository)
        assertTrue(vm.state.value is ReviewViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success and passes media id`() = runTest {
        val content = PageContainer<Review>()
        doReturn(Result.success(content))
            .`when`(browseRepository)
            .getReviewBrowse(
                mediaId = 100L,
                page = 2,
                perPage = KeyUtil.PAGING_LIMIT,
                type = MediaType.ANIME,
                asHtml = false,
            )
        val vm = ReviewViewModel(browseRepository = browseRepository)

        vm.load(mediaId = 100L, type = MediaType.ANIME, page = 2)

        val state = vm.state.value as ReviewViewModel.UiState.Success
        assertSame(content, state.content)
        verify(browseRepository).getReviewBrowse(
            mediaId = 100L,
            page = 2,
            perPage = KeyUtil.PAGING_LIMIT,
            type = MediaType.ANIME,
            asHtml = false,
        )
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<PageContainer<Review>>(RuntimeException("Review failed")))
            .`when`(browseRepository)
            .getReviewBrowse(
                mediaId = 100L,
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                type = null,
                asHtml = false,
            )
        val vm = ReviewViewModel(browseRepository = browseRepository)

        vm.load(mediaId = 100L, type = null, page = 1)

        val state = vm.state.value as ReviewViewModel.UiState.Error
        assertEquals("Review failed", state.message)
    }
}
