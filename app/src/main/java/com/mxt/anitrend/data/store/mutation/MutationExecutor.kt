package com.mxt.anitrend.data.store.mutation

interface MutationExecutor {
    suspend fun <T> execute(
        resourceKey: ResourceKey,
        operationKey: OperationKey,
        block: suspend () -> T,
    ): T
}
