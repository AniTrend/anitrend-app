package com.mxt.anitrend.data.store.mutation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultMutationRegistry : MutationRegistry {
    private val updateMutex = Mutex()
    private val mutableState = MutableStateFlow<Map<OperationKey, OperationStatus>>(emptyMap())

    override val state: StateFlow<Map<OperationKey, OperationStatus>> = mutableState.asStateFlow()

    override suspend fun markRunning(
        operationKey: OperationKey,
        operationId: String,
    ) {
        updateState { currentState ->
            currentState + (operationKey to OperationStatus.Running(operationId))
        }
    }

    override suspend fun markFailed(
        operationKey: OperationKey,
        operationId: String,
        message: String,
    ) {
        updateState { currentState ->
            currentState + (operationKey to OperationStatus.Failed(operationId, message))
        }
    }

    override suspend fun clear(
        operationKey: OperationKey,
        operationId: String,
    ) {
        updateState { currentState ->
            val currentOperation = currentState[operationKey]
            if (currentOperation?.operationIdOrNull() == operationId) {
                currentState - operationKey
            } else {
                currentState
            }
        }
    }

    private suspend fun updateState(
        transform: (Map<OperationKey, OperationStatus>) -> Map<OperationKey, OperationStatus>,
    ) {
        updateMutex.withLock {
            mutableState.value = transform(mutableState.value)
        }
    }

    private fun OperationStatus.operationIdOrNull(): String? = when (this) {
        OperationStatus.Idle -> null
        is OperationStatus.Running -> operationId
        is OperationStatus.Failed -> operationId
    }
}
