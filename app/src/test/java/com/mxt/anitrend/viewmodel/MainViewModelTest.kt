package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.repository.UserRepository
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
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mock(UserRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── UiState sealed type ──

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(MainViewModel.UiState.Loading, MainViewModel.UiState.Loading)
    }

    @Test
    fun `UiState Success holds user instance`() {
        val user = User().apply {
            id = 1L
            name = "TestUser"
        }
        val state = MainViewModel.UiState.Success(user)
        assertEquals(1L, state.user.id)
        assertEquals("TestUser", state.user.name)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = MainViewModel.UiState.Error("Something went wrong")
        assertEquals("Something went wrong", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    // ── initial state ──

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = MainViewModel(userRepository = userRepository)
        assertTrue(vm.state.value is MainViewModel.UiState.Loading)
    }

    @Test
    fun `loadCurrentUser emits Success from repository result`() = runTest {
        val user = User().apply {
            id = 1L
            name = "MainUser"
        }
        doReturn(Result.success(user)).`when`(userRepository).getCurrentUser(asHtml = false)

        val vm = MainViewModel(userRepository = userRepository)
        vm.loadCurrentUser()

        val state = vm.state.value as MainViewModel.UiState.Success
        assertEquals(1L, state.user.id)
        assertEquals("MainUser", state.user.name)
    }

    @Test
    fun `loadCurrentUser emits Error from repository failure`() = runTest {
        doReturn(Result.failure<User>(RuntimeException("Network failed")))
            .`when`(userRepository)
            .getCurrentUser(asHtml = false)

        val vm = MainViewModel(userRepository = userRepository)
        vm.loadCurrentUser()

        val state = vm.state.value as MainViewModel.UiState.Error
        assertEquals("Network failed", state.message)
    }
}
