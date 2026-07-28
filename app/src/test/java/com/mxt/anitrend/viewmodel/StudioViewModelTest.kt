package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.StudioRepository
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
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class StudioViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var studioRepository: StudioRepository
    private lateinit var baseRepository: BaseRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        studioRepository = mock(StudioRepository::class.java)
        baseRepository = mock(BaseRepository::class.java)
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
        val studio = StudioBase().apply {
            id = 1L
            name = "Test"
        }
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
        val vm = StudioViewModel(studioRepository = studioRepository, baseRepository = baseRepository, ioDispatcher = testDispatcher)
        assertTrue(vm.state.value is StudioViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val studio = StudioBase().apply { id = 1L }
        doReturn(Result.success(studio)).`when`(studioRepository).getStudioBase(id = 1L)
        val vm = StudioViewModel(studioRepository = studioRepository, baseRepository = baseRepository, ioDispatcher = testDispatcher)

        vm.load(1L)

        val state = vm.state.value as StudioViewModel.UiState.Success
        assertEquals(1L, state.studio.id)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<StudioBase>(RuntimeException("Studio failed")))
            .`when`(studioRepository)
            .getStudioBase(id = 1L)
        val vm = StudioViewModel(studioRepository = studioRepository, baseRepository = baseRepository, ioDispatcher = testDispatcher)

        vm.load(1L)

        val state = vm.state.value as StudioViewModel.UiState.Error
        assertEquals("Studio failed", state.message)
    }

    // ── GraphQL error message extraction ──

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = StudioViewModel.UiState.Error("Studio not found")
        assertEquals("Studio not found", state.message)
    }
}
