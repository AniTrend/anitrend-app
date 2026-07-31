package com.mxt.anitrend.data.store.mutation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KeyedMutexTest {

    @Test
    fun `commands on the same resource execute sequentially`() = runTest {
        val keyedMutex = KeyedMutex(backgroundScope)
        val resourceKey = ResourceKey.Feed(1L)
        val firstStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val secondStarted = CompletableDeferred<Unit>()
        val events = mutableListOf<String>()

        val firstJob = backgroundScope.launch {
            keyedMutex.execute(resourceKey) {
                events += "first-start"
                firstStarted.complete(Unit)
                releaseFirst.await()
                events += "first-end"
            }
        }

        firstStarted.await()

        val secondJob = backgroundScope.launch {
            keyedMutex.execute(resourceKey) {
                events += "second-start"
                secondStarted.complete(Unit)
                events += "second-end"
            }
        }

        runCurrent()

        assertFalse(secondStarted.isCompleted)

        releaseFirst.complete(Unit)
        advanceUntilIdle()
        firstJob.join()
        secondJob.join()

        assertTrue(secondStarted.isCompleted)
        assertEquals(
            listOf("first-start", "first-end", "second-start", "second-end"),
            events,
        )
    }

    @Test
    fun `commands on different resources execute concurrently`() = runTest {
        val keyedMutex = KeyedMutex(backgroundScope)
        val firstStarted = CompletableDeferred<Unit>()
        val secondStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val releaseSecond = CompletableDeferred<Unit>()

        val firstJob = backgroundScope.launch {
            keyedMutex.execute(ResourceKey.Feed(1L)) {
                firstStarted.complete(Unit)
                releaseFirst.await()
            }
        }

        firstStarted.await()

        val secondJob = backgroundScope.launch {
            keyedMutex.execute(ResourceKey.Review(2L)) {
                secondStarted.complete(Unit)
                releaseSecond.await()
            }
        }

        runCurrent()

        assertTrue(secondStarted.isCompleted)
        assertEquals(
            setOf(ResourceKey.Feed(1L), ResourceKey.Review(2L)),
            keyedMutex.trackedKeys(),
        )

        releaseFirst.complete(Unit)
        releaseSecond.complete(Unit)
        advanceUntilIdle()

        firstJob.join()
        secondJob.join()
    }

    @Test
    fun `cancellation while waiting for a lock does not leak the lock`() = runTest {
        val keyedMutex = KeyedMutex(backgroundScope)
        val resourceKey = ResourceKey.Reply(3L)
        val firstStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val thirdStarted = CompletableDeferred<Unit>()

        val firstJob = backgroundScope.launch {
            keyedMutex.execute(resourceKey) {
                firstStarted.complete(Unit)
                releaseFirst.await()
            }
        }

        firstStarted.await()

        val cancelledWaiter = backgroundScope.launch {
            keyedMutex.execute(resourceKey) {
                error("Cancelled waiter must never acquire the lock")
            }
        }

        runCurrent()
        cancelledWaiter.cancel()
        cancelledWaiter.join()

        val thirdJob = backgroundScope.launch {
            keyedMutex.execute(resourceKey) {
                thirdStarted.complete(Unit)
            }
        }

        runCurrent()
        assertFalse(thirdStarted.isCompleted)

        releaseFirst.complete(Unit)
        advanceUntilIdle()
        firstJob.join()
        thirdJob.join()

        assertTrue(thirdStarted.isCompleted)
        assertEquals(0, keyedMutex.trackedKeyCount())
    }

    @Test
    fun `unused mutex entries are removed after completion`() = runTest {
        val keyedMutex = KeyedMutex(backgroundScope)

        keyedMutex.execute(ResourceKey.MediaListById(4L)) { Unit }

        assertTrue(keyedMutex.trackedKeys().isEmpty())
        assertEquals(0, keyedMutex.trackedKeyCount())
    }

    @Test
    fun `never holds map lock during execution`() = runTest {
        val keyedMutex = KeyedMutex(backgroundScope)
        val firstStarted = CompletableDeferred<Unit>()
        val secondStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val releaseSecond = CompletableDeferred<Unit>()

        val firstJob = backgroundScope.launch {
            keyedMutex.execute(ResourceKey.Feed(5L)) {
                firstStarted.complete(Unit)
                releaseFirst.await()
            }
        }

        firstStarted.await()

        val secondJob = backgroundScope.launch {
            keyedMutex.execute(ResourceKey.MediaListByMedia(6L)) {
                secondStarted.complete(Unit)
                releaseSecond.await()
            }
        }

        runCurrent()

        assertTrue(secondStarted.isCompleted)

        releaseFirst.complete(Unit)
        releaseSecond.complete(Unit)
        advanceUntilIdle()

        firstJob.join()
        secondJob.join()
    }
}
