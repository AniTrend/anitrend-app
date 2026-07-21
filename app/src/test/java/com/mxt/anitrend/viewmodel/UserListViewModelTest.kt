package com.mxt.anitrend.viewmodel

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.UserFollowers
import com.mxt.anitrend.graphql.generated.UserFollowing
import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.DataContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
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
class UserListViewModelTest {

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

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(
            UserListViewModel.UiState.Loading,
            UserListViewModel.UiState.Loading,
        )
    }

    @Test
    fun `UiState Success wraps a container`() {
        val container = PageContainer<UserBase>().apply {
            pageData = listOf(UserBase().apply { id = 1L; name = "Test User" })
        }
        val state = UserListViewModel.UiState.Success(container)
        assertEquals(1L, state.container.pageData.first().id)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = UserListViewModel.UiState.Error("Failed")
        assertEquals("Failed", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = UserListViewModel(
            userService = service,
            ioDispatcher = testDispatcher,
        )
        assertTrue(vm.state.value is UserListViewModel.UiState.Loading)
    }

    @Test
    fun `loadFollowers emits Success on successful response`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<PageContainer<UserBase>>>
        val page = PageContainer<UserBase>().apply {
            pageData = listOf(UserBase().apply { id = 1L; name = "Follower" })
        }
        val container = AniListContainer(data = DataContainer(result = page), errors = null)
        val request = UserFollowers.request(id = 9, page = 1, perPage = 50)

        `when`(service.getFollowers(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(container))

        val vm = UserListViewModel(
            userService = service,
            ioDispatcher = testDispatcher,
        )

        vm.loadFollowers(userId = 9L, page = 1, perPage = 50)

        val state = vm.state.value as UserListViewModel.UiState.Success
        assertEquals(1L, state.container.pageData.first().id)
    }

    @Test
    fun `loadFollowing emits Success on successful response`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<PageContainer<UserBase>>>
        val page = PageContainer<UserBase>().apply {
            pageData = listOf(UserBase().apply { id = 2L; name = "Following" })
        }
        val container = AniListContainer(data = DataContainer(result = page), errors = null)
        val request = UserFollowing.request(id = 9, page = 2, perPage = 50)

        `when`(service.getFollowing(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(container))

        val vm = UserListViewModel(
            userService = service,
            ioDispatcher = testDispatcher,
        )

        vm.loadFollowing(userId = 9L, page = 2, perPage = 50)

        val state = vm.state.value as UserListViewModel.UiState.Success
        assertEquals(2L, state.container.pageData.first().id)
    }

    @Test
    fun `loadFollowers emits Error on GraphQL error`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<PageContainer<UserBase>>>
        val graphError = mock(GraphError::class.java)
        val container = AniListContainer<PageContainer<UserBase>>(data = null, errors = listOf(graphError))
        val request = UserFollowers.request(id = 9, page = 1, perPage = 50)

        `when`(graphError.message).thenReturn("Followers failed")
        `when`(service.getFollowers(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(container))

        val vm = UserListViewModel(
            userService = service,
            ioDispatcher = testDispatcher,
        )

        vm.loadFollowers(userId = 9L, page = 1, perPage = 50)

        val state = vm.state.value as UserListViewModel.UiState.Error
        assertEquals("Followers failed", state.message)
    }

    @Test
    fun `loadFollowing emits Error on request failure`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<PageContainer<UserBase>>>
        val request = UserFollowing.request(id = 9, page = 1, perPage = 50)

        `when`(service.getFollowing(request)).thenReturn(call)
        `when`(call.execute()).thenThrow(IOException("Network failed"))

        val vm = UserListViewModel(
            userService = service,
            ioDispatcher = testDispatcher,
        )

        vm.loadFollowing(userId = 9L, page = 1, perPage = 50)

        val state = vm.state.value as UserListViewModel.UiState.Error
        assertEquals("Network failed", state.message)
    }
}
