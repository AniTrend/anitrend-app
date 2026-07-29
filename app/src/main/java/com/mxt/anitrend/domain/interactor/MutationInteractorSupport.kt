package com.mxt.anitrend.domain.interactor

import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.mutation.RevisionProvider

internal suspend fun executeMutation(
    mutationExecutor: MutationExecutor,
    revisionProvider: RevisionProvider,
    resourceKey: ResourceKey,
    operationKey: OperationKey,
    failureMessage: String,
    block: suspend (revision: Long) -> MutationResult,
): MutationResult = try {
    mutationExecutor.execute(
        resourceKey = resourceKey,
        operationKey = operationKey,
    ) {
        block(revisionProvider.nextRevision(resourceKey))
    }
} catch (throwable: Throwable) {
    MutationResult.Failure(
        message = throwable.message ?: failureMessage,
        cause = throwable,
    )
}
