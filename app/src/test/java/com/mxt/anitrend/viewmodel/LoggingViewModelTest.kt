package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.log.LogEntry
import com.mxt.anitrend.model.entity.log.LogFilter
import com.mxt.anitrend.model.entity.log.LogUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.io.IOException
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class LoggingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("logvm_test_").toFile()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    // ── helpers ──

    private fun createViewModel(
        logFileProvider: () -> File,
        metadataProvider: MetadataProvider = { "# Test Metadata" },
    ): LoggingViewModel = LoggingViewModel(
        logFileProvider = logFileProvider,
        metadataProvider = metadataProvider,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    private fun createLogFile(content: String): File {
        val file = File(tempDir, "test_log_${System.nanoTime()}.txt")
        file.writeText(content)
        return file
    }

    private companion object {
        val SAMPLE_LOG = """
            07-19 09:15:30:100 I/Tag(1000) : Info message
            07-19 09:15:30:200 E/Tag(1000) : Error message
            07-19 09:15:30:300 W/Tag(1000) : Warning message
            07-19 09:15:30:400 D/Tag(1000) : Debug message
            07-19 09:15:30:500 V/Tag(1000) : Verbose message
        """.trimIndent()
    }

    // ── test 1: load parses valid log file and emits Success ──

    @Test
    fun `load parses valid log file and emits Success with newest-first entries`() {
        val logFile = createLogFile(SAMPLE_LOG)
        val viewModel = createViewModel(logFileProvider = { logFile })

        viewModel.load()

        val state = viewModel.state.value
        assertTrue("Expected Success state, was $state", state is LogUiState.Success)
        val success = state as LogUiState.Success
        assertEquals(LogFilter.All, success.filter)
        assertEquals(5, success.entries.size)

        // newest-first ordering
        assertEquals("Verbose message", success.entries[0].message)
        assertEquals(LogEntry.Level.VERBOSE, success.entries[0].level)
        assertEquals("Debug message", success.entries[1].message)
        assertEquals("Warning message", success.entries[2].message)
        assertEquals("Error message", success.entries[3].message)
        assertEquals("Info message", success.entries[4].message)
    }

    // ── test 2: load emits Error when logFileProvider throws ──

    @Test
    fun `load emits Error when logFileProvider throws`() {
        val viewModel = createViewModel(
            logFileProvider = { throw IOException("File not found") },
        )

        viewModel.load()

        val state = viewModel.state.value
        assertTrue("Expected Error state, was $state", state is LogUiState.Error)
        assertEquals("File not found", (state as LogUiState.Error).message)
    }

    // ── test 3: setFilter narrows from All to Error and back ──

    @Test
    fun `setFilter narrows from All to Error and back`() {
        val logFile = createLogFile(SAMPLE_LOG)
        val viewModel = createViewModel(logFileProvider = { logFile })
        viewModel.load()

        // start with All
        var state = viewModel.state.value
        assertTrue(state is LogUiState.Success)
        assertEquals(LogFilter.All, (state as LogUiState.Success).filter)
        assertEquals(5, state.entries.size)

        // narrow to Error
        viewModel.setFilter(LogFilter.Error)
        state = viewModel.state.value
        assertTrue(state is LogUiState.Success)
        val filtered = state as LogUiState.Success
        assertEquals(LogFilter.Error, filtered.filter)
        assertEquals(1, filtered.entries.size)
        assertEquals(LogEntry.Level.ERROR, filtered.entries[0].level)
        assertEquals("Error message", filtered.entries[0].message)

        // back to All
        viewModel.setFilter(LogFilter.All)
        state = viewModel.state.value
        assertTrue(state is LogUiState.Success)
        val restored = state as LogUiState.Success
        assertEquals(LogFilter.All, restored.filter)
        assertEquals(5, restored.entries.size)
    }

    // ── test 4: setFilter is a no-op when state is not Success ──

    @Test
    fun `setFilter is no-op during Loading state`() {
        val logFile = createLogFile(SAMPLE_LOG)
        val viewModel = createViewModel(logFileProvider = { logFile })

        // initial state is Loading
        assertTrue(viewModel.state.value is LogUiState.Loading)

        // setFilter should not change state from Loading to anything else
        viewModel.setFilter(LogFilter.Error)
        assertTrue(
            "State should remain Loading, was ${viewModel.state.value}",
            viewModel.state.value is LogUiState.Loading,
        )
    }

    @Test
    fun `setFilter is no-op during Error state`() {
        val viewModel = createViewModel(
            logFileProvider = { throw IOException("fail") },
        )
        viewModel.load()

        val before = viewModel.state.value
        assertTrue("Expected Error state, was $before", before is LogUiState.Error)
        val errorMessage = (before as LogUiState.Error).message

        // setFilter should not change Error state
        viewModel.setFilter(LogFilter.Debug)

        val after = viewModel.state.value
        assertTrue("State should remain Error, was $after", after is LogUiState.Error)
        assertEquals(errorMessage, (after as LogUiState.Error).message)
    }

    // ── test 5: clear truncates file and emits Success with empty entries ──

    @Test
    fun `clear truncates file and emits Success with empty entries`() {
        val logFile = createLogFile(SAMPLE_LOG)
        val viewModel = createViewModel(logFileProvider = { logFile })

        // load entries first
        viewModel.load()
        val before = viewModel.state.value
        assertTrue(before is LogUiState.Success)
        assertEquals(5, (before as LogUiState.Success).entries.size)

        // clear
        viewModel.clear()

        val after = viewModel.state.value
        assertTrue("Expected Success after clear, was $after", after is LogUiState.Success)
        val cleared = after as LogUiState.Success
        assertEquals(0, cleared.entries.size)
        assertEquals(LogFilter.All, cleared.filter)

        // file should be empty
        assertTrue("Log file should be empty after clear", logFile.readText().isEmpty())
    }

    // ── test 6: clear emits Error when file write fails ──

    @Test
    fun `clear emits Error when file write fails`() {
        // file in a non-existent directory -> FileWriter will throw
        val badDir = File(tempDir, "nonexistent_subdir")
        val badFile = File(badDir, "cant_truncate.txt")
        val viewModel = createViewModel(logFileProvider = { badFile })

        viewModel.clear()

        val state = viewModel.state.value
        assertTrue("Expected Error state, was $state", state is LogUiState.Error)
        assertNotNull((state as LogUiState.Error).message)
    }

    // ── test 7: buildShareFile creates temp file with metadata + log ──

    @Test
    fun `buildShareFile creates temp file with metadata header and log content`() = runTest {
        val content = "07-19 09:15:30:100 I/Tag(1) : Hello log\n"
        val logFile = createLogFile(content)
        val viewModel = createViewModel(logFileProvider = { logFile })

        val shareFile = viewModel.buildShareFile()

        assertTrue("Share file should exist", shareFile.exists())
        assertEquals(LoggingViewModel.SHARE_FILE_NAME, shareFile.name)

        val shareContent = shareFile.readText()
        assertTrue(
            "Share file should start with metadata",
            shareContent.startsWith("# Test Metadata"),
        )
        assertTrue(
            "Share file should contain log content",
            shareContent.contains("Hello log"),
        )
    }

    // ── test 8: buildShareFile when log file does not exist ──

    @Test
    fun `buildShareFile creates valid file even when log file does not exist`() = runTest {
        val nonexistentFile = File(tempDir, "does_not_exist.txt")
        val viewModel = createViewModel(
            logFileProvider = { nonexistentFile },
            metadataProvider = { "# Only Metadata" },
        )

        val shareFile = viewModel.buildShareFile()

        assertTrue("Share file should exist", shareFile.exists())
        assertEquals("# Only Metadata", shareFile.readText())
    }

    // ── test 9: state transitions ──

    @Test
    fun `state transitions Loading to Success after load`() {
        val logFile = createLogFile(SAMPLE_LOG)
        val viewModel = createViewModel(logFileProvider = { logFile })

        // initial
        assertTrue(viewModel.state.value is LogUiState.Loading)

        viewModel.load()

        assertTrue(
            "Expected Success after load, was ${viewModel.state.value}",
            viewModel.state.value is LogUiState.Success,
        )
    }

    @Test
    fun `state transitions Loading to Error on failure`() {
        val viewModel = createViewModel(
            logFileProvider = { throw IOException("fail") },
        )

        // initial
        assertTrue(viewModel.state.value is LogUiState.Loading)

        viewModel.load()

        assertTrue(
            "Expected Error after failed load, was ${viewModel.state.value}",
            viewModel.state.value is LogUiState.Error,
        )
    }

    // ── test 10: isLogLoadComplete ──

    @Test
    fun `isLogLoadComplete is true after Success`() {
        val logFile = createLogFile(SAMPLE_LOG)
        val viewModel = createViewModel(logFileProvider = { logFile })

        assertFalse("Should start false", viewModel.isLogLoadComplete)

        viewModel.load()

        assertTrue(
            "Expected isLogLoadComplete = true after Success",
            viewModel.isLogLoadComplete,
        )
    }

    @Test
    fun `isLogLoadComplete stays false after Error`() {
        val viewModel = createViewModel(
            logFileProvider = { throw IOException("fail") },
        )

        assertFalse("Should start false", viewModel.isLogLoadComplete)

        viewModel.load()

        assertFalse(
            "Expected isLogLoadComplete = false after Error",
            viewModel.isLogLoadComplete,
        )
    }

    // ── MainDispatcherRule ──

    class MainDispatcherRule(
        private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}
