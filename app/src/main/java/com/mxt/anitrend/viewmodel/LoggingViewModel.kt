package com.mxt.anitrend.viewmodel

import android.os.Environment
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.log.LogEntry
import com.mxt.anitrend.model.entity.log.LogFilter
import com.mxt.anitrend.model.entity.log.LogParser
import com.mxt.anitrend.model.entity.log.LogUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter

/**
 * Produces a formatted metadata block describing the device, build, and app
 * context for support sharing.
 */
typealias MetadataProvider = () -> String

/**
 * Owns loading, parsing, filtering, clearing, and share-file building for the
 * local persisted application log.
 */
class LoggingViewModel(
    private val logFileProvider: () -> File,
    private val metadataProvider: MetadataProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _state = MutableStateFlow<LogUiState>(LogUiState.Loading)
    val state: StateFlow<LogUiState> = _state.asStateFlow()

    @VisibleForTesting
    @Volatile
    internal var isLogLoadComplete = false

    /** All parsed entries (unfiltered) cached after the most recent load. */
    private var masterEntries: List<LogEntry> = emptyList()

    /** The currently active filter; drives re-filtering after load or filter change. */
    private var currentFilter: LogFilter = LogFilter.All

    /**
     * Reads the persisted log file, parses it, applies the current filter, and
     * emits [LogUiState.Success] or [LogUiState.Error].
     */
    fun load() {
        viewModelScope.launch {
            _state.value = LogUiState.Loading
            isLogLoadComplete = false

            runCatching {
                withContext(ioDispatcher) {
                    logFileProvider()
                        .inputStream()
                        .use { inputStream ->
                            LogParser.parse(inputStream)
                        }
                }
            }.onSuccess { entries ->
                masterEntries = entries
                _state.value = LogUiState.Success(
                    entries = applyFilter(entries, currentFilter),
                    filter = currentFilter,
                )
                isLogLoadComplete = true
            }.onFailure { throwable ->
                Timber.e(throwable)
                _state.value = LogUiState.Error(
                    message = throwable.message ?: "Failed to load log file",
                )
            }
        }
    }

    /**
     * Updates the active filter and re-emits the filtered entries.
     */
    fun setFilter(filter: LogFilter) {
        currentFilter = filter
        val currentState = _state.value
        if (currentState is LogUiState.Success) {
            _state.value = currentState.copy(
                entries = applyFilter(masterEntries, filter),
                filter = filter,
            )
        }
    }

    /**
     * Truncates the persisted log file and clears the in-memory entries.
     */
    fun clear() {
        viewModelScope.launch {
            runCatching {
                withContext(ioDispatcher) {
                    FileWriter(logFileProvider()).use { writer ->
                        writer.write("")
                    }
                }
                masterEntries = emptyList()
                _state.value = LogUiState.Success(
                    entries = emptyList(),
                    filter = currentFilter,
                )
            }.onFailure {
                Timber.e(it)
                _state.value = LogUiState.Error(
                    message = it.message ?: "Failed to clear log file",
                )
            }
        }
    }

    /**
     * Builds a temporary share file by prepending support metadata as comment
     * lines, then appending the original log content.
     *
     * The temp file is written to the same directory as the log file so it is
     * covered by the existing [androidx.core.content.FileProvider] path
     * declaration. Callers should delete this file after sharing.
     *
     * @return the temp file ready for sharing
     */
    suspend fun buildShareFile(): File {
        val metadata = metadataProvider()
        val logFile = logFileProvider()
        val parentDirectory = requireNotNull(logFile.parentFile) {
            "Log file must have a parent directory"
        }
        val shareFile = File(parentDirectory, SHARE_FILE_NAME)

        return withContext(ioDispatcher) {
            shareFile.bufferedWriter().use { writer ->
                writer.write(metadata)
                if (logFile.exists()) {
                    writer.newLine()
                    logFile.forEachLine { line ->
                        writer.write(line)
                        writer.newLine()
                    }
                }
            }
            shareFile
        }
    }

    /**
     * Copies the log file to the device Downloads directory.
     * Callers should show user-facing feedback on success/failure.
     */
    suspend fun saveToDownloads(): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS,
            )
            val destination = File(downloadsDir, DOWNLOADS_FILE_NAME)
            destination.parentFile?.mkdirs()
            logFileProvider().copyTo(destination, overwrite = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        runCatching {
            val shareFile = File(
                logFileProvider().parentFile,
                SHARE_FILE_NAME,
            )
            if (shareFile.exists()) shareFile.delete()
        }
    }

    // ── private helpers ──

    private fun applyFilter(
        entries: List<LogEntry>,
        filter: LogFilter,
    ): List<LogEntry> = when (filter) {
        LogFilter.All -> entries
        LogFilter.Error -> entries.filter { it.level == LogEntry.Level.ERROR }
        LogFilter.Warning -> entries.filter { it.level == LogEntry.Level.WARNING }
        LogFilter.Info -> entries.filter { it.level == LogEntry.Level.INFO }
        LogFilter.Debug -> entries.filter {
            it.level == LogEntry.Level.DEBUG || it.level == LogEntry.Level.VERBOSE
        }
    }

    companion object {
        const val SHARE_FILE_NAME = "anitrend-log-share.tmp"
        private const val DOWNLOADS_FILE_NAME = "AniTrend Logcat.txt"
    }
}
