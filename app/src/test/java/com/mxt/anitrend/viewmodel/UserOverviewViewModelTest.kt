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
class UserOverviewViewModelTest {

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
    fun `initial state is Loading`() = runTest {
        val vm = UserOverviewViewModel(userRepository = userRepository)
        assertTrue(vm.state.value is UserOverviewViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val user = User().apply { id = 1L }
        doReturn(Result.success(user))
            .`when`(userRepository)
            .getUserOverview(id = 1L, userName = "user", asHtml = false)
        val vm = UserOverviewViewModel(userRepository = userRepository)

        vm.load(userId = 1L, userName = "user")

        val state = vm.state.value as UserOverviewViewModel.UiState.Success
        assertEquals(1L, state.user.id)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<User>(RuntimeException("Overview failed")))
            .`when`(userRepository)
            .getUserOverview(id = null, userName = "user", asHtml = false)
        val vm = UserOverviewViewModel(userRepository = userRepository)

        vm.load(userId = 0L, userName = "user")

        val state = vm.state.value as UserOverviewViewModel.UiState.Error
        assertEquals("Overview failed", state.message)
    }

    @Test
    fun `load skips after first success`() = runTest {
        val user = User().apply { id = 1L }
        doReturn(Result.success(user))
            .`when`(userRepository)
            .getUserOverview(id = 1L, userName = null, asHtml = false)
        val vm = UserOverviewViewModel(userRepository = userRepository)

        vm.load(userId = 1L, userName = "")
        vm.load(userId = 1L, userName = "")

        verify(userRepository, times(1)).getUserOverview(id = 1L, userName = null, asHtml = false)
    }

    @Test
    fun `default params pass null id and username`() = runTest {
        val user = User().apply { id = 2L }
        doReturn(Result.success(user))
            .`when`(userRepository)
            .getUserOverview(id = null, userName = null, asHtml = false)
        val vm = UserOverviewViewModel(userRepository = userRepository)

        vm.load()

        verify(userRepository).getUserOverview(id = null, userName = null, asHtml = false)
    }
}
