package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
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
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class UserListViewModelTest {

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
            pageData = listOf(
                UserBase().apply {
                    id = 1L
                    name = "Test User"
                },
            )
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
            userRepository = userRepository,
        )
        assertTrue(vm.state.value is UserListViewModel.UiState.Loading)
    }

    @Test
    fun `loadFollowers emits Success from repository result`() = runTest {
        val page = PageContainer<UserBase>().apply {
            pageData = listOf(
                UserBase().apply {
                    id = 1L
                    name = "Follower"
                },
            )
        }
        doReturn(Result.success(page))
            .`when`(userRepository)
            .getFollowers(id = 9L, page = 1, perPage = 50)

        val vm = UserListViewModel(
            userRepository = userRepository,
        )

        vm.loadFollowers(userId = 9L, page = 1, perPage = 50)

        val state = vm.state.value as UserListViewModel.UiState.Success
        assertEquals(1L, state.container.pageData.first().id)
        verify(userRepository).getFollowers(id = 9L, page = 1, perPage = 50)
    }

    @Test
    fun `loadFollowing emits Success from repository result`() = runTest {
        val page = PageContainer<UserBase>().apply {
            pageData = listOf(
                UserBase().apply {
                    id = 2L
                    name = "Following"
                },
            )
        }
        doReturn(Result.success(page))
            .`when`(userRepository)
            .getFollowing(id = 9L, page = 2, perPage = 50)

        val vm = UserListViewModel(
            userRepository = userRepository,
        )

        vm.loadFollowing(userId = 9L, page = 2, perPage = 50)

        val state = vm.state.value as UserListViewModel.UiState.Success
        assertEquals(2L, state.container.pageData.first().id)
        verify(userRepository).getFollowing(id = 9L, page = 2, perPage = 50)
    }

    @Test
    fun `loadFollowers emits Error from repository failure`() = runTest {
        doReturn(Result.failure<PageContainer<UserBase>>(RuntimeException("Followers failed")))
            .`when`(userRepository)
            .getFollowers(id = 9L, page = 1, perPage = 50)

        val vm = UserListViewModel(
            userRepository = userRepository,
        )

        vm.loadFollowers(userId = 9L, page = 1, perPage = 50)

        val state = vm.state.value as UserListViewModel.UiState.Error
        assertEquals("Followers failed", state.message)
    }

    @Test
    fun `loadFollowing emits Error from repository failure`() = runTest {
        doReturn(Result.failure<PageContainer<UserBase>>(RuntimeException("Network failed")))
            .`when`(userRepository)
            .getFollowing(id = 9L, page = 1, perPage = 50)

        val vm = UserListViewModel(
            userRepository = userRepository,
        )

        vm.loadFollowing(userId = 9L, page = 1, perPage = 50)

        val state = vm.state.value as UserListViewModel.UiState.Error
        assertEquals("Network failed", state.message)
    }
}
