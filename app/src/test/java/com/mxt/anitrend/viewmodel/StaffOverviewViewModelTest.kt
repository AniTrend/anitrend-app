package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.StaffBase
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
class StaffOverviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var staffRepository: StaffRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        staffRepository = mock(StaffRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(
            StaffOverviewViewModel.UiState.Loading,
            StaffOverviewViewModel.UiState.Loading,
        )
    }

    @Test
    fun `UiState Success holds staff instance`() {
        val title = TitleBase("Test", "Staff", null, null)
        val staff = StaffBase().apply {
            id = 1L
            name = title
        }
        val state = StaffOverviewViewModel.UiState.Success(staff)
        assertEquals(1L, state.staff.id)
        assertEquals("Test Staff", state.staff.name?.fullName)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = StaffOverviewViewModel.UiState.Error("Failed")
        assertEquals("Failed", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = StaffOverviewViewModel(
            staffRepository = staffRepository,
        )
        assertTrue(vm.state.value is StaffOverviewViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val title = TitleBase("Test", "Staff", null, null)
        val staff = StaffBase().apply {
            id = 1L
            name = title
        }
        doReturn(Result.success(staff))
            .`when`(staffRepository)
            .getStaffOverview(id = 1L, asHtml = false)

        val vm = StaffOverviewViewModel(
            staffRepository = staffRepository,
        )

        vm.load(1L)

        val state = vm.state.value as StaffOverviewViewModel.UiState.Success
        assertEquals(1L, state.staff.id)
        assertEquals("Test Staff", state.staff.name?.fullName)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<StaffBase>(RuntimeException("Network failed")))
            .`when`(staffRepository)
            .getStaffOverview(id = 1L, asHtml = false)

        val vm = StaffOverviewViewModel(
            staffRepository = staffRepository,
        )

        vm.load(1L)

        val state = vm.state.value as StaffOverviewViewModel.UiState.Error
        assertEquals("Network failed", state.message)
    }
}
