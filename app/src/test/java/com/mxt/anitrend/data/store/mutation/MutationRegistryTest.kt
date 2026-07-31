package com.mxt.anitrend.data.store.mutation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MutationRegistryTest {

    @Test
    fun `markRunning sets Running status`() = runTest {
        val registry = DefaultMutationRegistry()
        val operationKey = OperationKey.feedLike(1L)

        registry.markRunning(operationKey = operationKey, operationId = "running-id")

        assertEquals(
            OperationStatus.Running(operationId = "running-id"),
            registry.state.value[operationKey],
        )
    }

    @Test
    fun `markFailed sets Failed status`() = runTest {
        val registry = DefaultMutationRegistry()
        val operationKey = OperationKey.replyDelete(2L)

        registry.markFailed(
            operationKey = operationKey,
            operationId = "failed-id",
            message = "failure",
        )

        assertEquals(
            OperationStatus.Failed(operationId = "failed-id", message = "failure"),
            registry.state.value[operationKey],
        )
    }

    @Test
    fun `clear removes the key`() = runTest {
        val registry = DefaultMutationRegistry()
        val operationKey = OperationKey.mediaListDelete(3L)

        registry.markRunning(operationKey = operationKey, operationId = "clear-id")
        registry.clear(operationKey = operationKey, operationId = "clear-id")

        assertTrue(registry.state.value.isEmpty())
    }

    @Test
    fun `clear with stale operationId does not overwrite a newer operation`() = runTest {
        val registry = DefaultMutationRegistry()
        val operationKey = OperationKey.reviewRate(4L)

        registry.markRunning(operationKey = operationKey, operationId = "old-id")
        registry.markRunning(operationKey = operationKey, operationId = "new-id")
        registry.clear(operationKey = operationKey, operationId = "old-id")

        assertEquals(
            OperationStatus.Running(operationId = "new-id"),
            registry.state.value[operationKey],
        )
    }

    @Test
    fun `clearAll removes every tracked operation`() = runTest {
        val registry = DefaultMutationRegistry()

        registry.markRunning(OperationKey.feedLike(1L), "feed-like")
        registry.markFailed(OperationKey.reviewRate(2L), "review-rate", "failed")

        registry.clearAll()

        assertTrue(registry.state.value.isEmpty())
    }

    @Test
    fun `state is observable via StateFlow`() = runTest {
        val registry = DefaultMutationRegistry()
        val operationKey = OperationKey.feedDelete(5L)
        val observedStates = mutableListOf<Map<OperationKey, OperationStatus>>()

        val collectionJob = backgroundScope.launch {
            registry.state.take(3).toList(observedStates)
        }

        runCurrent()

        registry.markRunning(operationKey = operationKey, operationId = "observe-id")
        runCurrent()
        registry.clear(operationKey = operationKey, operationId = "observe-id")

        advanceUntilIdle()
        collectionJob.join()

        assertEquals(
            listOf(
                emptyMap(),
                mapOf(operationKey to OperationStatus.Running(operationId = "observe-id")),
                emptyMap(),
            ),
            observedStates,
        )
    }
}
