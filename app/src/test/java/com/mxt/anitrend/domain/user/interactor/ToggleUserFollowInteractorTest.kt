package com.mxt.anitrend.domain.user.interactor

import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.data.store.mutation.SessionInvalidatedException
import com.mxt.anitrend.data.store.user.InMemoryUserStore
import com.mxt.anitrend.domain.model.ToggleUserFollowCommand
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.repository.UserRepository
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class ToggleUserFollowInteractorTest {

    @Test
    fun `successful toggle commits revisioned UserRecord to store`() = runTest {
        val repository = mock(UserRepository::class.java)
        val store = InMemoryUserStore()
        doReturn(Result.success(createUser(7L, isFollowing = true)))
            .`when`(repository)
            .toggleFollow(7L)

        val interactor = interactor(repository, store)

        val result = interactor(ToggleUserFollowCommand(userId = 7L))

        assertEquals(MutationResult.Success, result)
        val committed = store.state.value.usersById.getValue(7L)
        assertEquals("user-7", committed.name)
        assertTrue(committed.isFollowing)
        assertEquals(1L, committed.revision)
    }

    @Test
    fun `failed toggle returns MutationResult Failure and does not commit`() = runTest {
        val repository = mock(UserRepository::class.java)
        val store = InMemoryUserStore()
        doReturn(Result.failure<UserBase>(IllegalStateException("boom")))
            .`when`(repository)
            .toggleFollow(7L)

        val interactor = interactor(repository, store)

        val result = interactor(ToggleUserFollowCommand(userId = 7L))

        assertTrue(result is MutationResult.Failure)
        assertFalse(store.state.value.usersById.containsKey(7L))
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `two toggles on the same user execute sequentially`() = runTest {
        val repository = mock(UserRepository::class.java)
        val store = InMemoryUserStore()
        val firstStarted = CompletableDeferred<Unit>()
        val allowFirstReturn = CompletableDeferred<Unit>()
        val secondStarted = CompletableDeferred<Unit>()
        val activeCalls = AtomicInteger(0)
        val maxActiveCalls = AtomicInteger(0)

        doAnswer { invocation ->
            val current = activeCalls.incrementAndGet()
            maxActiveCalls.updateAndGet { previous -> maxOf(previous, current) }
            try {
                firstStarted.complete(Unit)
                val continuation =
                    invocation.rawArguments.last() as Continuation<Result<UserBase>>
                backgroundScope.launch {
                    allowFirstReturn.await()
                    continuation.resume(Result.success(createUser(7L, isFollowing = true)))
                }
                COROUTINE_SUSPENDED
            } finally {
                activeCalls.decrementAndGet()
            }
        }.doAnswer { invocation ->
            val current = activeCalls.incrementAndGet()
            maxActiveCalls.updateAndGet { previous -> maxOf(previous, current) }
            try {
                secondStarted.complete(Unit)
                createUser(7L, isFollowing = false)
            } finally {
                activeCalls.decrementAndGet()
            }
        }.`when`(repository)
            .toggleFollow(7L)

        val interactor = interactor(repository, store)

        val first = backgroundScope.launch {
            interactor(ToggleUserFollowCommand(userId = 7L))
        }
        runCurrent()
        firstStarted.await()

        val second = backgroundScope.launch {
            interactor(ToggleUserFollowCommand(userId = 7L))
        }
        advanceUntilIdle()
        assertTrue(!secondStarted.isCompleted)

        allowFirstReturn.complete(Unit)
        advanceUntilIdle()

        first.join()
        second.join()
        assertTrue(secondStarted.isCompleted)
        assertEquals(1, maxActiveCalls.get())
    }

    @Test
    fun `session invalidation before commit rejects the response and leaves store unchanged`() = runTest {
        val repository = mock(UserRepository::class.java)
        val store = InMemoryUserStore()
        val registry = DefaultMutationRegistry()
        val sessionEpoch = SessionEpoch()
        doAnswer {
            sessionEpoch.bump()
            createUser(7L, isFollowing = true)
        }.`when`(repository)
            .toggleFollow(7L)

        val interactor = ToggleUserFollowInteractor(
            userRepository = repository,
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = registry, operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = sessionEpoch),
            userStore = store,
            requestSequence = RequestSequence(),
        )

        val thrown = try {
            interactor(ToggleUserFollowCommand(userId = 7L))
            null
        } catch (expected: SessionInvalidatedException) {
            expected
        }

        assertNotNull(thrown)
        assertFalse(store.state.value.usersById.containsKey(7L))
        assertTrue(registry.state.value.isEmpty())
    }

    private fun TestScope.interactor(
        repository: UserRepository,
        store: InMemoryUserStore,
    ): ToggleUserFollowInteractor = ToggleUserFollowInteractor(
        userRepository = repository,
        mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = DefaultMutationRegistry(), operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch()),
        userStore = store,
        requestSequence = RequestSequence(),
    )

    private fun createUser(
        id: Long,
        isFollowing: Boolean = false,
    ): UserBase = UserBase(name = "user-$id", isFollowing = isFollowing).apply {
        this.id = id
    }
}
