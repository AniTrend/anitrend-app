package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.DataContainer
import com.mxt.anitrend.model.api.retro.anilist.StaffModel
import com.mxt.anitrend.graphql.generated.StaffOverview
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.StaffBase
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
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class StaffOverviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var service: StaffModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        service = mock(StaffModel::class.java)
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
        val staff = StaffBase().apply { id = 1L; name = title }
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
            staffService = service,
            ioDispatcher = testDispatcher,
        )
        assertTrue(vm.state.value is StaffOverviewViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success on successful response`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<StaffBase>>
        val title = TitleBase("Test", "Staff", null, null)
        val staff = StaffBase().apply { id = 1L; name = title }
        val container = AniListContainer(data = DataContainer(result = staff), errors = null)
        val request = StaffOverview.request(1, asHtml = false)

        `when`(service.getStaffOverview(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(container))

        val vm = StaffOverviewViewModel(
            staffService = service,
            ioDispatcher = testDispatcher,
        )

        vm.load(1L)

        val state = vm.state.value as StaffOverviewViewModel.UiState.Success
        assertEquals(1L, state.staff.id)
        assertEquals("Test Staff", state.staff.name?.fullName)
    }

    @Test
    fun `load emits Error on request failure`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<StaffBase>>
        val request = StaffOverview.request(1, asHtml = false)

        `when`(service.getStaffOverview(request)).thenReturn(call)
        `when`(call.execute()).thenThrow(IOException("Network failed"))

        val vm = StaffOverviewViewModel(
            staffService = service,
            ioDispatcher = testDispatcher,
        )

        vm.load(1L)

        val state = vm.state.value as StaffOverviewViewModel.UiState.Error
        assertEquals("Network failed", state.message)
    }
}
