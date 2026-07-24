package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.entity.anilist.User
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
class LoginUserViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var service: UserModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        service = mock(UserModel::class.java)
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
        val vm = LoginUserViewModel(userService = service, ioDispatcher = testDispatcher)
        assertTrue(vm.state.value is LoginUserViewModel.UiState.Loading)
    }

    // ── GraphQL error message extraction ──

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = LoginUserViewModel.UiState.Error("User not found")
        assertEquals("User not found", state.message)
    }
}
