package com.mxt.anitrend.data.store.mutation

class MutationContext(
    private val sessionEpoch: SessionEpoch,
    private val startEpoch: Long,
) {
    fun isSessionActive(): Boolean = sessionEpoch.current() == startEpoch

    fun ensureSessionActive() {
        if (!isSessionActive()) throw SessionInvalidatedException()
    }
}
