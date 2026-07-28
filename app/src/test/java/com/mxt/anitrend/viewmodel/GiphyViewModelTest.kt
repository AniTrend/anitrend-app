package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.model.api.retro.base.GiphyService
import com.mxt.anitrend.model.entity.giphy.GiphyContainer
import com.mxt.anitrend.util.KeyUtil
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
import org.mockito.Mockito.`when`
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class GiphyViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var service: GiphyService

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        service = mock(GiphyService::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(
            GiphyViewModel.UiState.Loading,
            GiphyViewModel.UiState.Loading,
        )
    }

    @Test
    fun `UiState Success wraps container`() {
        val container = GiphyContainer()
        val state = GiphyViewModel.UiState.Success(container)
        assertEquals(container, state.container)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = GiphyViewModel.UiState.Error("Failed")
        assertEquals("Failed", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = GiphyViewModel(
            giphyService = service,
            ioDispatcher = testDispatcher,
        )
        assertTrue(vm.state.value is GiphyViewModel.UiState.Loading)
    }

    @Test
    fun `loadTrending emits Success on successful response`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<GiphyContainer>
        val container = GiphyContainer()

        `when`(service.getTrending(BuildConfig.GIPHY_KEY, KeyUtil.PAGING_LIMIT, 0, "PG")).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(container))

        val vm = GiphyViewModel(
            giphyService = service,
            ioDispatcher = testDispatcher,
        )

        vm.loadTrending(0)

        val state = vm.state.value as GiphyViewModel.UiState.Success
        assertEquals(container, state.container)
    }

    @Test
    fun `search emits Error on request failure`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<GiphyContainer>

        `when`(
            service.findGif(
                BuildConfig.GIPHY_KEY,
                "naruto",
                KeyUtil.PAGING_LIMIT,
                25,
                "PG",
                "en",
            ),
        ).thenReturn(call)
        `when`(call.execute()).thenThrow(IOException("Network failed"))

        val vm = GiphyViewModel(
            giphyService = service,
            ioDispatcher = testDispatcher,
        )

        vm.search("naruto", 25)

        val state = vm.state.value as GiphyViewModel.UiState.Error
        assertEquals("Network failed", state.message)
    }
}
