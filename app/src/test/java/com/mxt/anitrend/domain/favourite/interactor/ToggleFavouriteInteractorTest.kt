package com.mxt.anitrend.domain.favourite.interactor

import com.mxt.anitrend.data.store.favourite.FavouriteStoreChange
import com.mxt.anitrend.data.store.favourite.InMemoryFavouriteStore
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.data.store.mutation.SessionInvalidatedException
import com.mxt.anitrend.domain.favourite.model.FavouriteKey
import com.mxt.anitrend.domain.model.ToggleFavouriteCommand
import com.mxt.anitrend.repository.BaseRepository
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
class ToggleFavouriteInteractorTest {

    @Test
    fun `successful toggle infers the flipped flag from committed store state`() = runTest {
        val repository = mock(BaseRepository::class.java)
        val store = InMemoryFavouriteStore()
        store.apply(
            FavouriteStoreChange.FavouriteFlagReplaced(
                key = FavouriteKey.Studio(7L),
                isFavourite = true,
                revision = 0L,
            ),
        )
        doReturn(Result.success(Unit))
            .`when`(repository)
            .toggleFavourite(null, null, null, null, 7, null, null)

        val interactor = interactor(repository, store)

        val result = interactor(ToggleFavouriteCommand(FavouriteKey.Studio(7L)))

        assertEquals(MutationResult.Success, result)
        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Studio(7L))
        assertFalse(committed.isFavourite)
        assertEquals(1L, committed.revision)
    }

    @Test
    fun `inferred flip treats a missing committed value as not favourite`() = runTest {
        val repository = mock(BaseRepository::class.java)
        val store = InMemoryFavouriteStore()
        doReturn(Result.success(Unit))
            .`when`(repository)
            .toggleFavourite(null, null, null, null, 7, null, null)

        val interactor = interactor(repository, store)

        val result = interactor(ToggleFavouriteCommand(FavouriteKey.Studio(7L)))

        assertEquals(MutationResult.Success, result)
        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Studio(7L))
        assertTrue(committed.isFavourite)
        assertEquals(1L, committed.revision)
    }

    @Test
    fun `failed toggle returns Failure and leaves the committed store unchanged`() = runTest {
        val repository = mock(BaseRepository::class.java)
        val store = InMemoryFavouriteStore()
        store.apply(
            FavouriteStoreChange.FavouriteFlagReplaced(
                key = FavouriteKey.Studio(7L),
                isFavourite = false,
                revision = 1L,
            ),
        )
        doReturn(Result.failure<Unit>(IllegalStateException("boom")))
            .`when`(repository)
            .toggleFavourite(null, null, null, null, 7, null, null)

        val interactor = interactor(repository, store)

        val result = interactor(ToggleFavouriteCommand(FavouriteKey.Studio(7L)))

        assertTrue(result is MutationResult.Failure)
        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Studio(7L))
        assertFalse(committed.isFavourite)
        assertEquals(1L, committed.revision)
    }

    @Test
    fun `different favourite keys commit to their own store entries`() = runTest {
        val repository = mock(BaseRepository::class.java)
        val store = InMemoryFavouriteStore()
        doReturn(Result.success(Unit))
            .`when`(repository)
            .toggleFavourite(9, null, null, null, null, null, null)
        doReturn(Result.success(Unit))
            .`when`(repository)
            .toggleFavourite(null, null, null, null, 7, null, null)

        val interactor = interactor(repository, store)

        interactor(ToggleFavouriteCommand(FavouriteKey.Anime(9L)))
        interactor(ToggleFavouriteCommand(FavouriteKey.Studio(7L)))

        assertTrue(store.state.value.flagsByKey.getValue(FavouriteKey.Anime(9L)).isFavourite)
        assertTrue(store.state.value.flagsByKey.getValue(FavouriteKey.Studio(7L)).isFavourite)
    }

    @Test
    fun `stale revision is rejected and the newer committed value is preserved`() = runTest {
        val repository = mock(BaseRepository::class.java)
        val store = InMemoryFavouriteStore()
        // Simulate a commit from another screen that already carries a newer revision.
        store.apply(
            FavouriteStoreChange.FavouriteFlagReplaced(
                key = FavouriteKey.Studio(7L),
                isFavourite = true,
                revision = 5L,
            ),
        )
        doReturn(Result.success(Unit))
            .`when`(repository)
            .toggleFavourite(null, null, null, null, 7, null, null)

        val interactor = interactor(repository, store)

        val result = interactor(ToggleFavouriteCommand(FavouriteKey.Studio(7L)))

        assertEquals(MutationResult.Success, result)
        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Studio(7L))
        assertTrue(committed.isFavourite)
        assertEquals(5L, committed.revision)
    }

    @Test
    fun `session invalidation before commit rejects the response and leaves the store unchanged`() = runTest {
        val repository = mock(BaseRepository::class.java)
        val store = InMemoryFavouriteStore()
        val registry = DefaultMutationRegistry()
        val sessionEpoch = SessionEpoch()
        doAnswer {
            sessionEpoch.bump()
            Unit
        }.`when`(repository)
            .toggleFavourite(null, null, null, null, 7, null, null)

        val interactor = ToggleFavouriteInteractor(
            baseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(
                applicationScope = backgroundScope,
                keyedMutex = KeyedMutex(backgroundScope),
                mutationRegistry = registry,
                operationIdGenerator = DefaultOperationIdGenerator(),
                sessionEpoch = sessionEpoch,
            ),
            favouriteStore = store,
            requestSequence = RequestSequence(),
        )

        val thrown = try {
            interactor(ToggleFavouriteCommand(FavouriteKey.Studio(7L)))
            null
        } catch (expected: SessionInvalidatedException) {
            expected
        }

        assertNotNull(thrown)
        assertTrue(store.state.value.flagsByKey.isEmpty())
        assertTrue(registry.state.value.isEmpty())
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `two toggles on the same studio execute sequentially`() = runTest {
        val repository = mock(BaseRepository::class.java)
        val store = InMemoryFavouriteStore()
        val firstStarted = CompletableDeferred<Unit>()
        val allowFirstReturn = CompletableDeferred<Unit>()
        val secondStarted = CompletableDeferred<Unit>()
        val activeCalls = AtomicInteger(0)
        val maxActiveCalls = AtomicInteger(0)
        val callCount = AtomicInteger(0)

        doAnswer { invocation ->
            val current = activeCalls.incrementAndGet()
            maxActiveCalls.updateAndGet { previous -> maxOf(previous, current) }
            try {
                if (callCount.incrementAndGet() == 1) {
                    firstStarted.complete(Unit)
                    val continuation = invocation.rawArguments.last() as Continuation<Result<Unit>>
                    backgroundScope.launch {
                        allowFirstReturn.await()
                        continuation.resume(Result.success(Unit))
                    }
                    COROUTINE_SUSPENDED
                } else {
                    secondStarted.complete(Unit)
                    Unit
                }
            } finally {
                activeCalls.decrementAndGet()
            }
        }.`when`(repository)
            .toggleFavourite(null, null, null, null, 7, null, null)

        val interactor = interactor(repository, store)

        val first = backgroundScope.launch {
            interactor(ToggleFavouriteCommand(FavouriteKey.Studio(7L)))
        }
        runCurrent()
        firstStarted.await()

        val second = backgroundScope.launch {
            interactor(ToggleFavouriteCommand(FavouriteKey.Studio(7L)))
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

    private fun TestScope.interactor(
        repository: BaseRepository,
        store: InMemoryFavouriteStore,
    ): ToggleFavouriteInteractor = ToggleFavouriteInteractor(
        baseRepository = repository,
        mutationExecutor = DefaultMutationExecutor(
            applicationScope = backgroundScope,
            keyedMutex = KeyedMutex(backgroundScope),
            mutationRegistry = DefaultMutationRegistry(),
            operationIdGenerator = DefaultOperationIdGenerator(),
            sessionEpoch = SessionEpoch(),
        ),
        favouriteStore = store,
        requestSequence = RequestSequence(),
    )
}
