package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class MediaLatestViewModelTest {

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
        val vm = MediaLatestViewModel(browseRepository = browseRepository)
        assertTrue(vm.state.value is MediaLatestViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val item = MediaBase().apply { id = 3L }
        val content = PageContainer<MediaBase>().apply { pageData = listOf(item) }
        doReturn(Result.success(content))
            .`when`(browseRepository)
            .getMediaBrowse(
                page = 3,
                perPage = 25,
                type = MediaType.ANIME,
                sort = listOf(MediaSort.POPULARITY_DESC),
                isAdult = false,
            )
        val vm = MediaLatestViewModel(browseRepository = browseRepository)

        vm.load(type = MediaType.ANIME, page = 3, pageLimit = 25, sort = "POPULARITY_DESC", isAdult = false)

        val state = vm.state.value as MediaLatestViewModel.UiState.Success
        assertEquals(content.pageData, state.items)
        assertEquals(3, state.page)
        assertEquals(1, state.itemsInPage)
        assertTrue(state.hasMorePages)
        verify(browseRepository).getMediaBrowse(
            page = 3,
            perPage = 25,
            type = MediaType.ANIME,
            sort = listOf(MediaSort.POPULARITY_DESC),
            isAdult = false,
        )
    }

    @Test
    fun `load falls back to null sort for invalid enum`() = runTest {
        val content = PageContainer<MediaBase>().apply { pageData = listOf(MediaBase().apply { id = 1L }) }
        doReturn(Result.success(content))
            .`when`(browseRepository)
            .getMediaBrowse(
                page = 1,
                perPage = 10,
                type = MediaType.MANGA,
                sort = null,
                isAdult = null,
            )
        val vm = MediaLatestViewModel(browseRepository = browseRepository)

        vm.load(type = MediaType.MANGA, page = 1, pageLimit = 10, sort = "INVALID_SORT", isAdult = null)

        val state = vm.state.value as MediaLatestViewModel.UiState.Success
        assertEquals(content.pageData, state.items)
        verify(browseRepository).getMediaBrowse(
            page = 1,
            perPage = 10,
            type = MediaType.MANGA,
            sort = null,
            isAdult = null,
        )
    }

    @Test
    fun `later pages append to the accumulated snapshot`() = runTest {
        val first = MediaBase().apply { id = 1L }
        val second = MediaBase().apply { id = 2L }
        doReturn(Result.success(PageContainer<MediaBase>().apply { pageData = listOf(first) }))
            .`when`(browseRepository)
            .getMediaBrowse(page = 1, perPage = 10, type = null, sort = null, isAdult = false)
        doReturn(Result.success(PageContainer<MediaBase>().apply { pageData = listOf(second) }))
            .`when`(browseRepository)
            .getMediaBrowse(page = 2, perPage = 10, type = null, sort = null, isAdult = false)
        val vm = MediaLatestViewModel(browseRepository = browseRepository)

        vm.load(type = null, page = 1, pageLimit = 10, sort = null, isAdult = false)
        vm.load(type = null, page = 2, pageLimit = 10, sort = null, isAdult = false)

        val state = vm.state.value as MediaLatestViewModel.UiState.Success
        assertEquals(listOf(first, second), state.items)
    }

    @Test
    fun `page one replaces previously accumulated media`() = runTest {
        val stale = MediaBase().apply { id = 1L }
        val fresh = MediaBase().apply { id = 2L }
        doReturn(Result.success(PageContainer<MediaBase>().apply { pageData = listOf(stale) }))
            .`when`(browseRepository)
            .getMediaBrowse(page = 2, perPage = 10, type = null, sort = null, isAdult = false)
        doReturn(Result.success(PageContainer<MediaBase>().apply { pageData = listOf(fresh) }))
            .`when`(browseRepository)
            .getMediaBrowse(page = 1, perPage = 10, type = null, sort = null, isAdult = false)
        val vm = MediaLatestViewModel(browseRepository = browseRepository)

        vm.load(type = null, page = 2, pageLimit = 10, sort = null, isAdult = false)
        vm.load(type = null, page = 1, pageLimit = 10, sort = null, isAdult = false)

        val state = vm.state.value as MediaLatestViewModel.UiState.Success
        assertEquals(listOf(fresh), state.items)
    }

    @Test
    fun `latest media snapshots are unique and page contribution is stable on replay`() = runTest {
        val first = MediaBase().apply { id = 1L }
        val second = MediaBase().apply { id = 2L }
        val third = MediaBase().apply { id = 3L }
        val pageOne = PageContainer<MediaBase>().apply {
            pageData = listOf(first, first, second)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        val pageTwo = PageContainer<MediaBase>().apply {
            pageData = listOf(second, third)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        val duplicateOnlyPage = PageContainer<MediaBase>().apply {
            pageData = listOf(first, second)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        doReturn(Result.success(pageOne))
            .`when`(browseRepository)
            .getMediaBrowse(page = 1, perPage = 10, type = null, sort = null, isAdult = false)
        doReturn(Result.success(pageTwo), Result.success(pageTwo))
            .`when`(browseRepository)
            .getMediaBrowse(page = 2, perPage = 10, type = null, sort = null, isAdult = false)
        doReturn(Result.success(duplicateOnlyPage))
            .`when`(browseRepository)
            .getMediaBrowse(page = 3, perPage = 10, type = null, sort = null, isAdult = false)
        val viewModel = MediaLatestViewModel(browseRepository = browseRepository)

        viewModel.load(type = null, page = 1, pageLimit = 10, sort = null, isAdult = false)
        val pageOneState = viewModel.state.value as MediaLatestViewModel.UiState.Success
        assertEquals(2, pageOneState.itemsInPage)
        assertEquals(listOf(1L, 2L), pageOneState.items.map { it.id })
        viewModel.load(type = null, page = 2, pageLimit = 10, sort = null, isAdult = false)
        val pageTwoState = viewModel.state.value as MediaLatestViewModel.UiState.Success
        assertEquals(1, pageTwoState.itemsInPage)
        assertEquals(listOf(1L, 2L, 3L), pageTwoState.items.map { it.id })

        viewModel.load(type = null, page = 2, pageLimit = 10, sort = null, isAdult = false)
        val replayedPageTwo = viewModel.state.value as MediaLatestViewModel.UiState.Success
        assertEquals(1, replayedPageTwo.itemsInPage)
        assertEquals(listOf(1L, 2L, 3L), replayedPageTwo.items.map { it.id })

        viewModel.load(type = null, page = 3, pageLimit = 10, sort = null, isAdult = false)
        val duplicateOnly = viewModel.state.value as MediaLatestViewModel.UiState.Success
        assertEquals(0, duplicateOnly.itemsInPage)
        assertEquals(listOf(1L, 2L, 3L), duplicateOnly.items.map { it.id })
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<PageContainer<MediaBase>>(RuntimeException("Media failed")))
            .`when`(browseRepository)
            .getMediaBrowse(page = 1, perPage = 10, type = null, sort = null, isAdult = false)
        val vm = MediaLatestViewModel(browseRepository = browseRepository)

        vm.load(type = null, page = 1, pageLimit = 10, sort = null, isAdult = false)

        val state = vm.state.value as MediaLatestViewModel.UiState.Error
        assertEquals("Media failed", state.message)
    }

    @Test
    fun `same-id reload emits a fresh state instance`() = runTest {
        val first = MediaBase().apply { id = 3L }
        val second = MediaBase().apply { id = 3L }
        doReturn(
            Result.success(PageContainer<MediaBase>().apply { pageData = listOf(first) }),
            Result.success(PageContainer<MediaBase>().apply { pageData = listOf(second) }),
        )
            .`when`(browseRepository)
            .getMediaBrowse(page = 1, perPage = 10, type = null, sort = null, isAdult = false)
        val vm = MediaLatestViewModel(browseRepository = browseRepository)

        vm.load(type = null, page = 1, pageLimit = 10, sort = null, isAdult = false)
        val initial = vm.state.value
        assertTrue(initial is MediaLatestViewModel.UiState.Success)
        vm.load(type = null, page = 1, pageLimit = 10, sort = null, isAdult = false)

        assertNotSame(
            "a same-id reload must emit a fresh state instance",
            initial,
            vm.state.value,
        )
    }
}
