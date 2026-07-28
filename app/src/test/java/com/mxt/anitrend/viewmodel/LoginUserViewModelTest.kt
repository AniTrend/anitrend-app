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
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class LoginUserViewModelTest {

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
        assertEquals(LoginUserViewModel.UiState.Loading, LoginUserViewModel.UiState.Loading)
    }

    @Test
    fun `UiState Success holds user instance`() {
        val user = User().apply {
            id = 1L
            name = "TestUser"
        }
        val state = LoginUserViewModel.UiState.Success(user)
        assertEquals(1L, state.user.id)
        assertEquals("TestUser", state.user.name)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = LoginUserViewModel.UiState.Error("Something went wrong")
        assertEquals("Something went wrong", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    // ── initial state ──

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = LoginUserViewModel(userRepository = userRepository)
        assertTrue(vm.state.value is LoginUserViewModel.UiState.Loading)
    }

    @Test
    fun `loadCurrentUser emits Success from repository result`() = runTest {
        val user = User().apply { id = 1L }
        doReturn(Result.success(user)).`when`(userRepository).getCurrentUser(asHtml = false)
        val vm = LoginUserViewModel(userRepository = userRepository)

        vm.loadCurrentUser()

        val state = vm.state.value as LoginUserViewModel.UiState.Success
        assertEquals(1L, state.user.id)
    }

    @Test
    fun `loadCurrentUser emits Error from repository failure`() = runTest {
        doReturn(Result.failure<User>(RuntimeException("User failed")))
            .`when`(userRepository)
            .getCurrentUser(asHtml = false)
        val vm = LoginUserViewModel(userRepository = userRepository)

        vm.loadCurrentUser()

        val state = vm.state.value as LoginUserViewModel.UiState.Error
        assertEquals("User failed", state.message)
    }

    @Test
    fun `loadCurrentUser skips after first success`() = runTest {
        val user = User().apply { id = 1L }
        doReturn(Result.success(user)).`when`(userRepository).getCurrentUser(asHtml = false)
        val vm = LoginUserViewModel(userRepository = userRepository)

        vm.loadCurrentUser()
        vm.loadCurrentUser()

        verify(userRepository, times(1)).getCurrentUser(asHtml = false)
    }

    // ── GraphQL error message extraction ──

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = LoginUserViewModel.UiState.Error("User not found")
        assertEquals("User not found", state.message)
    }
}
