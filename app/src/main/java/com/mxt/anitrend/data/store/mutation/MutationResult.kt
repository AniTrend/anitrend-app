package com.mxt.anitrend.data.store.mutation

sealed interface MutationResult {
    data object Success : MutationResult

    data class Failure(
        val message: String,
        val cause: Throwable? = null,
    ) : MutationResult
}
