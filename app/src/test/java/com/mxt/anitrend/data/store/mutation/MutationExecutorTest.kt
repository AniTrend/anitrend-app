package com.mxt.anitrend.data.store.mutation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MutationExecutorTest {

    @Test
    fun `block success clears registry state`() = runTest {
        val applicationScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        try {
            val registry = DefaultMutationRegistry()
            val operationKey = OperationKey.feedLike(1L)
            val executor = createExecutor(
                applicationScope = applicationScope,
                registry = registry,
                operationIdGenerator = FixedOperationIdGenerator("success-id"),
            )

            val result = executor.execute(
                resourceKey = ResourceKey.Feed(1L),
                operationKey = operationKey,
            ) { context ->
                assertTrue(context.isSessionActive())
                assertEquals(
                    OperationStatus.Running(operationId = "success-id"),
                    registry.state.value[operationKey],
                )
                MutationResult.Success
            }

            assertEquals(MutationResult.Success, result)
            assertTrue(registry.state.value.isEmpty())
        } finally {
            applicationScope.cancel()
        }
    }

    @Test
    fun `block failure result leaves failed registry state`() = runTest {
        val applicationScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        try {
            val registry = DefaultMutationRegistry()
            val operationKey = OperationKey.replyDelete(2L)
            val executor = createExecutor(
                applicationScope = applicationScope,
                registry = registry,
                operationIdGenerator = FixedOperationIdGenerator("failure-id"),
            )
            val failure = MutationResult.Failure(message = "boom")

            val result = executor.execute(
                resourceKey = ResourceKey.Reply(2L),
                operationKey = operationKey,
            ) {
                failure
            }

            assertEquals(
                failure,
                result,
            )
            assertEquals(
                OperationStatus.Failed(operationId = "failure-id", message = "boom"),
                registry.state.value[operationKey],
            )
        } finally {
            applicationScope.cancel()
        }
    }

    @Test
    fun `throwable marks registry failed and propagates`() = runTest {
        val applicationScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        try {
            val registry = DefaultMutationRegistry()
            val operationKey = OperationKey.mediaListSave(3L)
            val executor = createExecutor(
                applicationScope = applicationScope,
                registry = registry,
                operationIdGenerator = FixedOperationIdGenerator("throw-id"),
            )

            val thrown = runCatching {
                executor.execute(
                    resourceKey = ResourceKey.MediaListByMedia(3L),
                    operationKey = operationKey,
                ) {
                    throw IllegalStateException("boom")
                }
            }.exceptionOrNull()

            assertTrue(thrown is IllegalStateException)
            assertEquals("boom", thrown?.message)
            assertEquals(
                OperationStatus.Failed(operationId = "throw-id", message = "boom"),
                registry.state.value[operationKey],
            )
        } finally {
            applicationScope.cancel()
        }
    }

    @Test
    fun `cancellation clears registry and propagates`() = runTest {
        val applicationScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        try {
            val registry = DefaultMutationRegistry()
            val executor = createExecutor(
                applicationScope = applicationScope,
                registry = registry,
                operationIdGenerator = FixedOperationIdGenerator("cancel-id"),
            )

            val thrown = runCatching {
                executor.execute(
                    resourceKey = ResourceKey.MediaListByMedia(4L),
                    operationKey = OperationKey.mediaListSave(4L),
                ) {
                    throw CancellationException("cancelled")
                }
            }.exceptionOrNull()

            assertTrue(thrown is CancellationException)
            assertEquals("cancelled", thrown?.message)
            assertTrue(registry.state.value.isEmpty())
        } finally {
            applicationScope.cancel()
        }
    }

    @Test
    fun `caller cancellation does not cancel application scoped work`() = runTest {
        val applicationScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val callerScope = CoroutineScope(Job() + StandardTestDispatcher(testScheduler))
        try {
            val registry = DefaultMutationRegistry()
            val executor = createExecutor(
                applicationScope = applicationScope,
                registry = registry,
                operationIdGenerator = FixedOperationIdGenerator("async-id"),
            )
            val started = CompletableDeferred<Unit>()
            val allowCompletion = CompletableDeferred<Unit>()
            val blockCompleted = CompletableDeferred<Unit>()

            val callerJob = callerScope.async {
                executor.execute(
                    resourceKey = ResourceKey.Feed(5L),
                    operationKey = OperationKey.feedSave(5L),
                ) {
                    started.complete(Unit)
                    allowCompletion.await()
                    blockCompleted.complete(Unit)
                    MutationResult.Success
                }
            }

            started.await()
            callerScope.cancel()
            allowCompletion.complete(Unit)
            blockCompleted.await()
            callerJob.join()

            assertTrue(callerJob.isCancelled)
            assertTrue(registry.state.value.isEmpty())
        } finally {
            callerScope.cancel()
            applicationScope.cancel()
        }
    }

    private fun createExecutor(
        applicationScope: CoroutineScope,
        registry: MutationRegistry,
        operationIdGenerator: OperationIdGenerator,
    ) = DefaultMutationExecutor(
        applicationScope = applicationScope,
        keyedMutex = KeyedMutex(applicationScope),
        mutationRegistry = registry,
        operationIdGenerator = operationIdGenerator,
        sessionEpoch = SessionEpoch(),
    )

    private class FixedOperationIdGenerator(
        private vararg val operationIds: String,
    ) : OperationIdGenerator {
        private var index = 0

        override fun generate(): String = operationIds.getOrNull(index++)
            ?: error("No operation ID configured for index $index")
    }
}
