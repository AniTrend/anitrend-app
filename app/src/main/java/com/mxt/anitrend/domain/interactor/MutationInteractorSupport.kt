package com.mxt.anitrend.domain.interactor

import com.mxt.anitrend.data.store.mutation.MutationContext
import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.mutation.RequestSequence
import kotlin.coroutines.cancellation.CancellationException

internal suspend fun executeMutation(
    mutationExecutor: MutationExecutor,
    requestSequence: RequestSequence,
    resourceKey: ResourceKey,
    operationKey: OperationKey,
    failureMessage: String,
    block: suspend (revision: Long, context: MutationContext) -> MutationResult,
): MutationResult = try {
    mutationExecutor.execute(
        resourceKey = resourceKey,
        operationKey = operationKey,
    ) { context ->
        block(requestSequence.next(), context)
    }
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (throwable: Throwable) {
    MutationResult.Failure(
        message = throwable.message ?: failureMessage,
        cause = throwable,
    )
}
