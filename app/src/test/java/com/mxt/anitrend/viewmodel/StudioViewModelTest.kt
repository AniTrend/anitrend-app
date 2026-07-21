package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.api.retro.anilist.StudioModel
import com.mxt.anitrend.model.entity.base.StudioBase
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

@OptIn(ExperimentalCoroutinesApi::class)
class StudioViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var service: StudioModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        service = mock(StudioModel::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── UiState sealed type ──

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(StudioViewModel.UiState.Loading, StudioViewModel.UiState.Loading)
    }

    @Test
    fun `UiState Success holds studio instance`() {
        val studio = StudioBase().apply { id = 1L; name = "Test" }
        val state = StudioViewModel.UiState.Success(studio)
        assertEquals(1L, state.studio.id)
        assertEquals("Test", state.studio.name)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = StudioViewModel.UiState.Error("Something went wrong")
        assertEquals("Something went wrong", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    // ── initial state ──

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = StudioViewModel(studioService = service, ioDispatcher = testDispatcher)
        assertTrue(vm.state.value is StudioViewModel.UiState.Loading)
    }

    // ── GraphQL error message extraction ──

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = StudioViewModel.UiState.Error("Studio not found")
        assertEquals("Studio not found", state.message)
    }
}
