package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.domain.user.model.UserStatisticsRecord
import com.mxt.anitrend.model.entity.base.UserBase
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
class ProfileViewModelTest {

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
        assertEquals(ProfileViewModel.UiState.Loading, ProfileViewModel.UiState.Loading)
    }

    @Test
    fun `UiState Success holds user instance`() {
        val user = UserBase().apply {
            id = 1L
            name = "TestUser"
        }
        val state = ProfileViewModel.UiState.Success(user)
        assertEquals(1L, state.user.id)
        assertEquals("TestUser", state.user.name)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = ProfileViewModel.UiState.Error("Something went wrong")
        assertEquals("Something went wrong", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    // ── initial state ──

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = ProfileViewModel(userRepository = userRepository, ioDispatcher = testDispatcher)
        assertTrue(vm.state.value is ProfileViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val user = UserBase().apply { id = 1L }
        doReturn(Result.success(user))
            .`when`(userRepository)
            .getUserBase(id = 1L, userName = "profile")
        val vm = ProfileViewModel(userRepository = userRepository, ioDispatcher = testDispatcher)

        vm.load(userId = 1L, userName = "profile")

        val state = vm.state.value as ProfileViewModel.UiState.Success
        assertEquals(1L, state.user.id)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<UserBase>(RuntimeException("Profile failed")))
            .`when`(userRepository)
            .getUserBase(id = null, userName = "profile")
        val vm = ProfileViewModel(userRepository = userRepository, ioDispatcher = testDispatcher)

        vm.load(userId = 0L, userName = "profile")

        val state = vm.state.value as ProfileViewModel.UiState.Error
        assertEquals("Profile failed", state.message)
    }

    @Test
    fun `load skips after first success`() = runTest {
        val user = UserBase().apply { id = 1L }
        doReturn(Result.success(user))
            .`when`(userRepository)
            .getUserBase(id = 1L, userName = null)
        val vm = ProfileViewModel(userRepository = userRepository, ioDispatcher = testDispatcher)

        vm.load(userId = 1L, userName = null)
        vm.load(userId = 1L, userName = null)

        verify(userRepository, times(1)).getUserBase(id = 1L, userName = null)
    }

    // ── GraphQL error message extraction ──

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = ProfileViewModel.UiState.Error("User not found")
        assertEquals("User not found", state.message)
    }

    // ── loadStats ──

    @Test
    fun `loadStats returns the stats record from the repository`() = runTest {
        val stats = UserStatisticsRecord(
            anime = UserStatisticsRecord.Anime(count = 3, minutesWatched = 1_200),
            manga = UserStatisticsRecord.Manga(chaptersRead = 40),
        )
        doReturn(Result.success(stats))
            .`when`(userRepository)
            .getUserStats(id = 1L, userName = null)
        val vm = ProfileViewModel(userRepository = userRepository, ioDispatcher = testDispatcher)

        val result = vm.loadStats(userId = 1L, userName = null)

        assertTrue(result.isSuccess)
        assertEquals(stats, result.getOrThrow())
    }

    @Test
    fun `loadStats forwards a repository failure`() = runTest {
        doReturn(Result.failure<UserStatisticsRecord>(RuntimeException("Stats failed")))
            .`when`(userRepository)
            .getUserStats(id = null, userName = "profile")
        val vm = ProfileViewModel(userRepository = userRepository, ioDispatcher = testDispatcher)

        val result = vm.loadStats(userId = 0L, userName = "profile")

        assertTrue(result.isFailure)
        assertEquals("Stats failed", result.exceptionOrNull()?.message)
    }
}
