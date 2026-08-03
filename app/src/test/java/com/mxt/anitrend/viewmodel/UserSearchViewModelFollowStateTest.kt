package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.data.store.user.InMemoryUserStore
import com.mxt.anitrend.data.store.user.UserStoreChange
import com.mxt.anitrend.domain.model.ToggleUserFollowCommand
import com.mxt.anitrend.domain.model.UserRecord
import com.mxt.anitrend.domain.user.interactor.ToggleUserFollowInteractor
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.repository.SearchRepository
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Verifies UserSearchViewModel follow-state convergence through the canonical
 * [com.mxt.anitrend.data.store.user.UserStore]: a successful toggle converges the
 * observed follow state, a failed toggle leaves it unchanged, and the legacy
 * repository result callback is no longer used.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserSearchViewModelFollowStateTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var searchRepository: SearchRepository
    private lateinit var userRepository: UserRepository
    private lateinit var userStore: InMemoryUserStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        searchRepository = mock(SearchRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        userStore = InMemoryUserStore()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful toggle commits to store and converges observed follow state`() = runTest(testDispatcher) {
        doReturn(Result.success(createUser(7L, isFollowing = true)))
            .`when`(userRepository)
            .toggleFollow(7L)

        val viewModel = viewModel()

        viewModel.toggleFollow(7L)
        advanceUntilIdle()

        assertTrue(viewModel.followStates.value.getValue(7L))
        assertTrue(userStore.state.value.usersById.getValue(7L).isFollowing)
    }

    @Test
    fun `failed toggle does not commit and leaves observed follow state unchanged`() = runTest(testDispatcher) {
        doReturn(Result.failure<UserBase>(IllegalStateException("boom")))
            .`when`(userRepository)
            .toggleFollow(7L)

        val viewModel = viewModel()

        viewModel.toggleFollow(7L)
        advanceUntilIdle()

        assertFalse(viewModel.followStates.value.containsKey(7L))
        assertFalse(userStore.state.value.usersById.containsKey(7L))
    }

    @Test
    fun `failed toggle preserves previously committed follow state`() = runTest(testDispatcher) {
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
        doReturn(Result.failure<UserBase>(IllegalStateException("boom")))
            .`when`(userRepository)
            .toggleFollow(7L)

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.toggleFollow(7L)
        advanceUntilIdle()

        assertTrue(viewModel.followStates.value.getValue(7L))
        assertTrue(userStore.state.value.usersById.getValue(7L).isFollowing)
    }

    @Test
    fun `toggleFollow routes through interactor with the user command`() = runTest(testDispatcher) {
        val interactor = mock(ToggleUserFollowInteractor::class.java)
        doReturn(MutationResult.Success)
            .`when`(interactor)
            .invoke(ToggleUserFollowCommand(userId = 7L))
        val viewModel = UserSearchViewModel(
            searchRepository = searchRepository,
            toggleUserFollowInteractor = interactor,
            userStore = userStore,
        )

        viewModel.toggleFollow(7L)
        advanceUntilIdle()

        verify(interactor).invoke(ToggleUserFollowCommand(userId = 7L))
    }

    private fun TestScope.viewModel(): UserSearchViewModel {
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
        return UserSearchViewModel(
            searchRepository = searchRepository,
            toggleUserFollowInteractor = interactor,
            userStore = userStore,
        )
    }

    private fun createUser(
        id: Long,
        isFollowing: Boolean = false,
    ): UserBase = UserBase(name = "user-$id", isFollowing = isFollowing).apply {
        this.id = id
    }
}
