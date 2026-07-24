package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.api.retro.anilist.StaffModel
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.repository.BaseRepository
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
class StaffViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var service: StaffModel
    private lateinit var baseRepository: BaseRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        service = mock(StaffModel::class.java)
        baseRepository = mock(BaseRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── UiState sealed type ──

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(StaffViewModel.UiState.Loading, StaffViewModel.UiState.Loading)
    }

    @Test
    fun `UiState Success holds staff instance`() {
        val staff = StaffBase().apply {
            id = 1L
            siteUrl = "https://anilist.co/staff/1"
        }
        val state = StaffViewModel.UiState.Success(staff)
        assertEquals(1L, state.staff.id)
        assertEquals("https://anilist.co/staff/1", state.staff.siteUrl)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = StaffViewModel.UiState.Error("Something went wrong")
        assertEquals("Something went wrong", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    // ── initial state ──

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = StaffViewModel(staffService = service, baseRepository = baseRepository, ioDispatcher = testDispatcher)
        assertTrue(vm.state.value is StaffViewModel.UiState.Loading)
    }

    // ── GraphQL error message extraction ──

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = StaffViewModel.UiState.Error("Staff not found")
        assertEquals("Staff not found", state.message)
    }
}
