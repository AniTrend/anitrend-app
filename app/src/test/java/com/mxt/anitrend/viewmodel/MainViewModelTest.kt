package com.mxt.anitrend.viewmodel

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.CurrentUser
import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.DataContainer
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
class MainViewModelTest {

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
        val vm = MainViewModel(userService = service, ioDispatcher = testDispatcher)
        assertTrue(vm.state.value is MainViewModel.UiState.Loading)
    }

    @Test
    fun `loadCurrentUser emits Success on successful response`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<User>>
        val user = User().apply {
            id = 1L
            name = "MainUser"
        }
        val container = AniListContainer(data = DataContainer(result = user), errors = null)
        val request = CurrentUser.request(asHtml = false)

        `when`(service.getCurrentUser(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(container))

        val vm = MainViewModel(userService = service, ioDispatcher = testDispatcher)
        vm.loadCurrentUser()

        val state = vm.state.value as MainViewModel.UiState.Success
        assertEquals(1L, state.user.id)
        assertEquals("MainUser", state.user.name)
    }

    @Test
    fun `loadCurrentUser emits Error on GraphQL error`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<User>>
        val graphError = mock(GraphError::class.java)
        val container = AniListContainer<User>(data = null, errors = listOf(graphError))
        val request = CurrentUser.request(asHtml = false)

        `when`(graphError.message).thenReturn("User not found")
        `when`(service.getCurrentUser(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(container))

        val vm = MainViewModel(userService = service, ioDispatcher = testDispatcher)
        vm.loadCurrentUser()

        val state = vm.state.value as MainViewModel.UiState.Error
        assertEquals("User not found", state.message)
    }

    @Test
    fun `loadCurrentUser emits Error on request failure`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<User>>
        val request = CurrentUser.request(asHtml = false)

        `when`(service.getCurrentUser(request)).thenReturn(call)
        `when`(call.execute()).thenThrow(IOException("Network failed"))

        val vm = MainViewModel(userService = service, ioDispatcher = testDispatcher)
        vm.loadCurrentUser()

        val state = vm.state.value as MainViewModel.UiState.Error
        assertEquals("Network failed", state.message)
    }
}
