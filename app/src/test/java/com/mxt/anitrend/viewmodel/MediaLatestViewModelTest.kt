package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
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
import org.junit.Assert.assertSame
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
        val content = PageContainer<MediaBase>()
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
        assertSame(content, state.content)
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
        val content = PageContainer<MediaBase>()
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
        assertSame(content, state.content)
        verify(browseRepository).getMediaBrowse(
            page = 1,
            perPage = 10,
            type = MediaType.MANGA,
            sort = null,
            isAdult = null,
        )
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
}
