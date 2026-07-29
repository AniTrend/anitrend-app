package com.mxt.anitrend.data.store.mutation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MutationExecutorTest {

    @Test
    fun `successful operation clears registry state`() = runTest {
        val registry = DefaultMutationRegistry()
        val executor = DefaultMutationExecutor(
            keyedMutex = KeyedMutex(backgroundScope),
            mutationRegistry = registry,
            operationIdGenerator = FixedOperationIdGenerator("success-id"),
        )
        val operationKey = OperationKey.feedLike(1L)

        val result = executor.execute(
            resourceKey = ResourceKey.Feed(1L),
            operationKey = operationKey,
        ) {
            "done"
        }

        assertEquals("done", result)
        assertTrue(registry.state.value.isEmpty())
    }

    @Test
    fun `failed operation records failure in registry`() = runTest {
        val registry = DefaultMutationRegistry()
        val executor = DefaultMutationExecutor(
            keyedMutex = KeyedMutex(backgroundScope),
            mutationRegistry = registry,
            operationIdGenerator = FixedOperationIdGenerator("failure-id"),
        )
        val operationKey = OperationKey.replyDelete(2L)
        val failure = IllegalStateException("boom")

        val thrown = runCatching {
            executor.execute(
                resourceKey = ResourceKey.Reply(2L),
                operationKey = operationKey,
            ) {
                throw failure
            }
        }.exceptionOrNull()

        assertSame(failure, thrown)
        assertEquals(
            OperationStatus.Failed(operationId = "failure-id", message = "boom"),
            registry.state.value[operationKey],
        )
    }

    @Test
    fun `cancellation does not mark as failed`() = runTest {
        val registry = DefaultMutationRegistry()
        val executor = DefaultMutationExecutor(
            keyedMutex = KeyedMutex(backgroundScope),
            mutationRegistry = registry,
            operationIdGenerator = FixedOperationIdGenerator("cancel-id"),
        )
        val operationKey = OperationKey.mediaListSave(3L)
        val started = CompletableDeferred<Unit>()

        val job = backgroundScope.launch {
            executor.execute(
                resourceKey = ResourceKey.MediaListByMedia(3L),
                operationKey = operationKey,
            ) {
                started.complete(Unit)
                awaitCancellation()
            }
        }

        started.await()
        job.cancel()
        job.join()

        assertTrue(registry.state.value.isEmpty())
    }

    @Test
    fun `operation ID is generated and passed to registry`() = runTest {
        val registry = DefaultMutationRegistry()
        val executor = DefaultMutationExecutor(
            keyedMutex = KeyedMutex(backgroundScope),
            mutationRegistry = registry,
            operationIdGenerator = FixedOperationIdGenerator("generated-id"),
        )
        val operationKey = OperationKey.reviewDelete(4L)

        executor.execute(
            resourceKey = ResourceKey.Review(4L),
            operationKey = operationKey,
        ) {
            assertEquals(
                OperationStatus.Running(operationId = "generated-id"),
                registry.state.value[operationKey],
            )
        }

        advanceUntilIdle()
        assertTrue(registry.state.value.isEmpty())
    }

    private class FixedOperationIdGenerator(
        private vararg val operationIds: String,
    ) : OperationIdGenerator {
        private var index = 0

        override fun generate(): String {
            return operationIds.getOrNull(index++)
                ?: error("No operation ID configured for index $index")
        }
    }
}
