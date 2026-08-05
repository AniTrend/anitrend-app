package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.data.store.user.InMemoryUserStore
import com.mxt.anitrend.data.store.user.UserStore
import com.mxt.anitrend.data.store.user.UserStoreChange
import com.mxt.anitrend.domain.model.ToggleUserFollowCommand
import com.mxt.anitrend.domain.model.UserRecord
import com.mxt.anitrend.domain.user.interactor.ToggleUserFollowInteractor
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class UserOverviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: UserRepository
    private lateinit var toggleUserFollowInteractor: ToggleUserFollowInteractor
    private lateinit var userStore: InMemoryUserStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mock(UserRepository::class.java)
        toggleUserFollowInteractor = mock(ToggleUserFollowInteractor::class.java)
        userStore = InMemoryUserStore()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): UserOverviewViewModel = UserOverviewViewModel(
        userRepository = userRepository,
        toggleUserFollowInteractor = toggleUserFollowInteractor,
        userStore = userStore,
    )

    private suspend fun stubOverview(userId: Long) {
        doReturn(Result.success(User().apply { id = userId }))
            .`when`(userRepository)
            .getUserOverview(id = userId, userName = null, asHtml = false)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = viewModel()
        assertTrue(vm.state.value is UserOverviewViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val user = User().apply { id = 1L }
        doReturn(Result.success(user))
            .`when`(userRepository)
            .getUserOverview(id = 1L, userName = "user", asHtml = false)
        val vm = viewModel()

        vm.load(userId = 1L, userName = "user")

        val state = vm.state.value as UserOverviewViewModel.UiState.Success
        assertEquals(1L, state.user.id)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<User>(RuntimeException("Overview failed")))
            .`when`(userRepository)
            .getUserOverview(id = null, userName = "user", asHtml = false)
        val vm = viewModel()

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
        val vm = viewModel()

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
        val vm = viewModel()

        vm.load()

        verify(userRepository).getUserOverview(id = null, userName = null, asHtml = false)
    }

    @Test
    fun `toggleFollow delegates exact command for displayed profile`() = runTest(testDispatcher) {
        stubOverview(userId = 7L)
        val vm = viewModel()

        vm.load(userId = 7L, userName = "")
        vm.toggleFollow(7L)
        advanceUntilIdle()

        verify(toggleUserFollowInteractor).invoke(ToggleUserFollowCommand(userId = 7L))
    }

    @Test
    fun `toggleFollow ignores user id that does not match displayed profile`() = runTest(testDispatcher) {
        stubOverview(userId = 7L)
        val vm = viewModel()

        vm.load(userId = 7L, userName = "")
        vm.toggleFollow(8L)
        advanceUntilIdle()

        verifyNoInteractions(toggleUserFollowInteractor)
    }

    @Test
    fun `toggleFollow ignores call before any profile is loaded`() = runTest(testDispatcher) {
        val vm = viewModel()

        vm.toggleFollow(7L)
        advanceUntilIdle()

        verifyNoInteractions(toggleUserFollowInteractor)
    }

    @Test
    fun `matching user record updates exposed committed follow state`() = runTest(testDispatcher) {
        stubOverview(userId = 7L)
        val vm = viewModel()

        vm.load(userId = 7L, userName = "")
        advanceUntilIdle()
        assertNull(vm.isFollowing.value)

        userStore.apply(
            UserStoreChange.UserUpserted(
                UserRecord(
                    id = 7L,
                    name = "user-7",
                    avatar = null,
                    banner = null,
                    isFollowing = true,
                    revision = 1L,
                ),
            ),
        )
        advanceUntilIdle()

        assertEquals(true, vm.isFollowing.value)
    }

    @Test
    fun `preloaded committed record wins over server overview value`() = runTest(testDispatcher) {
        val overviewUser = User().apply { id = 7L }
        assertFalse(overviewUser.isFollowing)
        doReturn(Result.success(overviewUser))
            .`when`(userRepository)
            .getUserOverview(id = 7L, userName = null, asHtml = false)
        userStore.apply(
            UserStoreChange.UserUpserted(
                UserRecord(
                    id = 7L,
                    name = "user-7",
                    avatar = null,
                    banner = null,
                    isFollowing = true,
                    revision = 1L,
                ),
            ),
        )
        val vm = viewModel()

        vm.load(userId = 7L, userName = "")
        advanceUntilIdle()

        assertEquals(true, vm.isFollowing.value)
    }

    @Test
    fun `failed toggle keeps exposed follow state unchanged`() = runTest(testDispatcher) {
        stubOverview(userId = 7L)
        val interactor = ToggleUserFollowInteractor(
            userRepository = userRepository,
            mutationExecutor = DefaultMutationExecutor(
                applicationScope = backgroundScope,
                keyedMutex = KeyedMutex(backgroundScope),
                mutationRegistry = DefaultMutationRegistry(),
                operationIdGenerator = DefaultOperationIdGenerator(),
                sessionEpoch = SessionEpoch(),
            ),
            userStore = userStore,
            requestSequence = RequestSequence(),
        )
        val vm = UserOverviewViewModel(
            userRepository = userRepository,
            toggleUserFollowInteractor = interactor,
            userStore = userStore,
        )
        doReturn(Result.failure<UserBase>(IllegalStateException("boom")))
            .`when`(userRepository)
            .toggleFollow(7L)

        vm.load(userId = 7L, userName = "")
        vm.toggleFollow(7L)
        advanceUntilIdle()

        assertNull(vm.isFollowing.value)
        assertTrue(userStore.state.value.usersById.isEmpty())
    }

    @Test
    fun `null record is ignored so server loaded follow value stays the fallback`() = runTest(testDispatcher) {
        stubOverview(userId = 7L)
        val store = mock(UserStore::class.java)
        doReturn(flowOf(null))
            .`when`(store)
            .observeUser(7L)
        val vm = UserOverviewViewModel(
            userRepository = userRepository,
            toggleUserFollowInteractor = toggleUserFollowInteractor,
            userStore = store,
        )

        vm.load(userId = 7L, userName = "")
        advanceUntilIdle()

        assertNull(vm.isFollowing.value)
    }

    @Test
    fun `record for another user is ignored by follow state observation`() = runTest(testDispatcher) {
        stubOverview(userId = 7L)
        val store = mock(UserStore::class.java)
        doReturn(
            flowOf(
                UserRecord(
                    id = 8L,
                    name = "user-8",
                    avatar = null,
                    banner = null,
                    isFollowing = true,
                    revision = 1L,
                ),
            ),
        )
            .`when`(store)
            .observeUser(7L)
        val vm = UserOverviewViewModel(
            userRepository = userRepository,
            toggleUserFollowInteractor = toggleUserFollowInteractor,
            userStore = store,
        )

        vm.load(userId = 7L, userName = "")
        advanceUntilIdle()

        assertNull(vm.isFollowing.value)
    }

    @Test
    fun `current user snapshot comes from repository cache`() = runTest {
        val currentUser = User().apply { id = 42L }
        doReturn(currentUser).`when`(userRepository).cachedCurrentUser

        val vm = viewModel()

        assertEquals(42L, vm.currentUserSnapshot?.id)
    }
}
