package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.model.api.retro.base.GiphyService
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.model.entity.giphy.GiphyContainer
import com.mxt.anitrend.model.entity.giphy.Pagination
import com.mxt.anitrend.util.KeyUtil
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class GiphyViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var service: GiphyService

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        service = mock(GiphyService::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `overlapping pages keep unique ids and stop at server total`() = runTest {
        `when`(service.getTrending(BuildConfig.GIPHY_KEY, KeyUtil.PAGING_LIMIT, 0, "PG"))
            .thenReturn(Response.success(page(0, 4, "a", "b")))
        `when`(service.getTrending(BuildConfig.GIPHY_KEY, KeyUtil.PAGING_LIMIT, 2, "PG"))
            .thenReturn(Response.success(page(2, 4, "b", "c")))
        val viewModel = GiphyViewModel(service, dispatcher)

        viewModel.refresh(null)
        viewModel.loadMore()
        viewModel.loadMore()

        val state = viewModel.state.value as GiphyViewModel.UiState.Success
        assertEquals(listOf("a", "b", "c"), state.container.data.map { it.id })
        verify(service, times(1)).getTrending(BuildConfig.GIPHY_KEY, KeyUtil.PAGING_LIMIT, 2, "PG")
    }

    @Test
    fun `failed append cannot advance and refresh replaces with empty result`() = runTest {
        `when`(service.getTrending(BuildConfig.GIPHY_KEY, KeyUtil.PAGING_LIMIT, 0, "PG"))
            .thenReturn(Response.success(page(0, 4, "a", "b")))
            .thenReturn(Response.success(page(0, 0)))
        `when`(service.getTrending(BuildConfig.GIPHY_KEY, KeyUtil.PAGING_LIMIT, 2, "PG"))
            .thenAnswer { throw IOException("Offline") }
        val viewModel = GiphyViewModel(service, dispatcher)

        viewModel.refresh(null)
        viewModel.loadMore()
        assertEquals("Offline", (viewModel.state.value as GiphyViewModel.UiState.Error).message)
        viewModel.loadMore()
        verify(service, times(1)).getTrending(BuildConfig.GIPHY_KEY, KeyUtil.PAGING_LIMIT, 2, "PG")
        viewModel.refresh(null)

        assertTrue((viewModel.state.value as GiphyViewModel.UiState.Success).container.data.isEmpty())
    }

    @Test
    fun `query submission resets offset and replaces trending rows`() = runTest {
        `when`(service.getTrending(BuildConfig.GIPHY_KEY, KeyUtil.PAGING_LIMIT, 0, "PG"))
            .thenReturn(Response.success(page(0, 10, "old")))
        `when`(service.findGif(BuildConfig.GIPHY_KEY, "cat", KeyUtil.PAGING_LIMIT, 0, "PG", "en"))
            .thenReturn(Response.success(page(0, 1, "cat")))
        val viewModel = GiphyViewModel(service, dispatcher)

        viewModel.refresh(null)
        viewModel.refresh("cat")

        assertEquals(listOf("cat"), (viewModel.state.value as GiphyViewModel.UiState.Success).container.data.map { it.id })
    }

    private fun page(offset: Int, total: Int, vararg ids: String): GiphyContainer = GiphyContainer(
        data = ids.map { Giphy(id = it, title = it) },
        pagination = Pagination(total_count = total, count = ids.size, offset = offset),
    )
}
