package com.mxt.anitrend.data.store.mutation

interface MutationExecutor {
    suspend fun execute(
        resourceKey: ResourceKey,
        operationKey: OperationKey,
        block: suspend (context: MutationContext) -> MutationResult,
    ): MutationResult
}
