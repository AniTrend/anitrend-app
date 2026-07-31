package com.mxt.anitrend.data.store.mutation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class DefaultMutationExecutor(
    private val applicationScope: CoroutineScope,
    private val keyedMutex: KeyedMutex,
    private val mutationRegistry: MutationRegistry,
    private val operationIdGenerator: OperationIdGenerator,
    private val sessionEpoch: SessionEpoch,
) : MutationExecutor {

    override suspend fun execute(
        resourceKey: ResourceKey,
        operationKey: OperationKey,
        block: suspend (context: MutationContext) -> MutationResult,
    ): MutationResult {
        val deferred = applicationScope.async {
            val context = MutationContext(sessionEpoch, sessionEpoch.current())
            context.ensureSessionActive()

            keyedMutex.execute(resourceKey) {
                val operationId = operationIdGenerator.generate()
                mutationRegistry.markRunning(
                    operationKey = operationKey,
                    operationId = operationId,
                )

                try {
                    val result = block(context)
                    withContext(NonCancellable) {
                        when (result) {
                            MutationResult.Success ->
                                mutationRegistry.clear(
                                    operationKey = operationKey,
                                    operationId = operationId,
                                )
                            is MutationResult.Failure ->
                                mutationRegistry.markFailed(
                                    operationKey = operationKey,
                                    operationId = operationId,
                                    message = result.message,
                                )
                        }
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
        return deferred.await()
    }
}
