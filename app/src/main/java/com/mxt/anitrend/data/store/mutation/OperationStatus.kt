package com.mxt.anitrend.data.store.mutation

sealed interface OperationStatus {
    data object Idle : OperationStatus

    data class Running(val operationId: String) : OperationStatus

    data class Failed(
        val operationId: String,
        val message: String,
    ) : OperationStatus
}
