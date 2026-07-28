package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.repository.StaffRepository
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
class MediaAnimeRoleViewModelTest {

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
    fun `initial state is Loading`() = runTest {
        val vm = MediaAnimeRoleViewModel(staffRepository = staffRepository)
        assertTrue(vm.state.value is MediaAnimeRoleViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val content = ConnectionContainer<EdgeContainer<MediaEdge>>()
        doReturn(Result.success(content))
            .`when`(staffRepository)
            .getStaffCharacters(id = 1L, onList = true, page = 2)
        val vm = MediaAnimeRoleViewModel(staffRepository = staffRepository)

        vm.load(id = 1L, onList = true, page = 2)

        val state = vm.state.value as MediaAnimeRoleViewModel.UiState.Success
        assertSame(content, state.content)
        verify(staffRepository).getStaffCharacters(id = 1L, onList = true, page = 2)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<ConnectionContainer<EdgeContainer<MediaEdge>>>(RuntimeException("Role failed")))
            .`when`(staffRepository)
            .getStaffCharacters(id = 1L, onList = false, page = 3)
        val vm = MediaAnimeRoleViewModel(staffRepository = staffRepository)

        vm.load(id = 1L, onList = false, page = 3)

        val state = vm.state.value as MediaAnimeRoleViewModel.UiState.Error
        assertEquals("Role failed", state.message)
    }
}
