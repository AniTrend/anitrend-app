package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewSort
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
class BrowseReviewViewModelTest {

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
        val vm = BrowseReviewViewModel(browseRepository = browseRepository)
        assertTrue(vm.state.value is BrowseReviewViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success and defaults null sort`() = runTest {
        val content = PageContainer<Review>()
        doReturn(Result.success(content))
            .`when`(browseRepository)
            .getReviewBrowse(
                page = 2,
                perPage = KeyUtil.PAGING_LIMIT,
                type = MediaType.ANIME,
                sort = listOf(ReviewSort.CREATED_AT_DESC),
                asHtml = false,
            )
        val vm = BrowseReviewViewModel(browseRepository = browseRepository)

        vm.load(type = MediaType.ANIME, page = 2, sort = null)

        val state = vm.state.value as BrowseReviewViewModel.UiState.Success
        assertSame(content, state.content)
        verify(browseRepository).getReviewBrowse(
            page = 2,
            perPage = KeyUtil.PAGING_LIMIT,
            type = MediaType.ANIME,
            sort = listOf(ReviewSort.CREATED_AT_DESC),
            asHtml = false,
        )
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<PageContainer<Review>>(RuntimeException("Reviews failed")))
            .`when`(browseRepository)
            .getReviewBrowse(
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                type = null,
                sort = listOf(ReviewSort.CREATED_AT_DESC),
                asHtml = false,
            )
        val vm = BrowseReviewViewModel(browseRepository = browseRepository)

        vm.load(type = null, page = 1, sort = null)

        val state = vm.state.value as BrowseReviewViewModel.UiState.Error
        assertEquals("Reviews failed", state.message)
    }
}
