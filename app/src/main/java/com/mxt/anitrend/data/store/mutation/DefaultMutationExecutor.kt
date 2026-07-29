package com.mxt.anitrend.data.store.mutation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class DefaultMutationExecutor(
    private val keyedMutex: KeyedMutex,
    private val mutationRegistry: MutationRegistry,
    private val operationIdGenerator: OperationIdGenerator,
) : MutationExecutor {

    override suspend fun <T> execute(
        resourceKey: ResourceKey,
        operationKey: OperationKey,
        block: suspend () -> T,
    ): T = keyedMutex.execute(resourceKey) {
        val operationId = operationIdGenerator.generate()
        mutationRegistry.markRunning(
            operationKey = operationKey,
            operationId = operationId,
        )

        try {
            val result = block()
            withContext(NonCancellable) {
                mutationRegistry.clear(
                    operationKey = operationKey,
                    operationId = operationId,
                )
            }
            result
        } catch (cancellationException: CancellationException) {
            withContext(NonCancellable) {
                mutationRegistry.clear(
                    operationKey = operationKey,
                    operationId = operationId,
                )
            }
            throw cancellationException
        } catch (throwable: Throwable) {
            withContext(NonCancellable) {
                mutationRegistry.markFailed(
                    operationKey = operationKey,
                    operationId = operationId,
                    message = throwable.message ?: throwable.javaClass.simpleName,
                )
            }
            throw throwable
        }
    }
}
