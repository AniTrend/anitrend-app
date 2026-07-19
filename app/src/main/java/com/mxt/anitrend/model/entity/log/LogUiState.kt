package com.mxt.anitrend.model.entity.log

/**
 * UI state emitted by [com.mxt.anitrend.viewmodel.LoggingViewModel].
 *
 * States are mutually exclusive: the ViewModel is always in exactly one of
 * [Loading], [Success], or [Error].
 */
sealed interface LogUiState {
    /** Log entries are being loaded from the persisted file. */
    data object Loading : LogUiState

    /** Log entries are loaded and filtered. */
    data class Success(
        val entries: List<LogEntry>,
        val filter: LogFilter,
    ) : LogUiState

    /** An error occurred during loading or processing. */
    data class Error(
        val message: String,
    ) : LogUiState
}
