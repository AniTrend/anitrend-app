package com.mxt.anitrend.data.store.mutation

import kotlinx.coroutines.flow.StateFlow

interface MutationRegistry {
    val state: StateFlow<Map<OperationKey, OperationStatus>>

    suspend fun markRunning(operationKey: OperationKey, operationId: String)

    suspend fun markFailed(operationKey: OperationKey, operationId: String, message: String)

    suspend fun clear(operationKey: OperationKey, operationId: String)
}
