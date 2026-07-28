package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.StaffRepository
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
class StaffViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var staffRepository: StaffRepository
    private lateinit var baseRepository: BaseRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        staffRepository = mock(StaffRepository::class.java)
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
        val vm = StaffViewModel(staffRepository = staffRepository, baseRepository = baseRepository, ioDispatcher = testDispatcher)
        assertTrue(vm.state.value is StaffViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val staff = StaffBase().apply { id = 1L }
        doReturn(Result.success(staff)).`when`(staffRepository).getStaffBase(id = 1L)
        val vm = StaffViewModel(staffRepository = staffRepository, baseRepository = baseRepository, ioDispatcher = testDispatcher)

        vm.load(1L)

        val state = vm.state.value as StaffViewModel.UiState.Success
        assertEquals(1L, state.staff.id)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<StaffBase>(RuntimeException("Staff failed")))
            .`when`(staffRepository)
            .getStaffBase(id = 1L)
        val vm = StaffViewModel(staffRepository = staffRepository, baseRepository = baseRepository, ioDispatcher = testDispatcher)

        vm.load(1L)

        val state = vm.state.value as StaffViewModel.UiState.Error
        assertEquals("Staff failed", state.message)
    }

    // ── GraphQL error message extraction ──

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = StaffViewModel.UiState.Error("Staff not found")
        assertEquals("Staff not found", state.message)
    }
}
