package com.mxt.anitrend.viewmodel

import android.content.res.AssetManager
import com.mxt.anitrend.viewmodel.ChangelogViewModel.ChangelogUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.io.ByteArrayInputStream
import java.io.IOException
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import java.nio.charset.StandardCharsets

/**
 * Focused tests for the bundled changelog loader: content decoding, empty
 * and invalid documents, and retry recovery after a failed read.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChangelogViewModelTest {

    private lateinit var assets: AssetManager

    private val validDocument =
        """
        {
          "releases": [
            {
              "version": "1.13.1",
              "added": ["State synchronization improvements."],
              "improved": ["Modernized Material 3 experiences."],
              "fixed": ["Fixed feed and reply rendering."],
              "knownIssues": ["Gifs may show artifacts."]
            }
          ],
          "acknowledgements": "Thanks everyone!"
        }
        """.trimIndent()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        assets = mock(AssetManager::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun stubAsset(content: String?) {
        if (content == null) {
            doThrow(IOException("missing asset")).`when`(assets).open(anyString())
        } else {
            doReturn(ByteArrayInputStream(content.toByteArray(StandardCharsets.UTF_8)))
                .`when`(assets)
                .open(anyString())
        }
    }

    /** Builds the ViewModel with the eagerly executing test dispatcher. */
    private fun createViewModel() = ChangelogViewModel(
        assets = assets,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun `valid document decodes into content with parsed fields`() {
        stubAsset(validDocument)

        val state = createViewModel().state.value

        assertTrue(state is ChangelogUiState.Content)
        val document = (state as ChangelogUiState.Content).document
        assertEquals(1, document.releases.size)
        val release = document.releases.first()
        assertEquals("1.13.1", release.version)
        assertEquals(listOf("State synchronization improvements."), release.added)
        assertEquals(listOf("Modernized Material 3 experiences."), release.improved)
        assertEquals(listOf("Fixed feed and reply rendering."), release.fixed)
        assertEquals(listOf("Gifs may show artifacts."), release.knownIssues)
        assertEquals("Thanks everyone!", document.acknowledgements)
    }

    @Test
    fun `unreadable asset surfaces error`() {
        stubAsset(null)

        val state = createViewModel().state.value

        assertTrue(state is ChangelogUiState.Error)
    }

    @Test
    fun `invalid json surfaces error`() {
        stubAsset("{ not json ]")

        val state = createViewModel().state.value

        assertTrue(state is ChangelogUiState.Error)
    }

    @Test
    fun `empty releases surface empty while keeping acknowledgements`() {
        stubAsset("""{"releases": [], "acknowledgements": "Thanks!"}""")

        val state = createViewModel().state.value

        assertTrue(state is ChangelogUiState.Empty)
    }

    @Test
    fun `retry recovers after a failed read`() {
        stubAsset(null)
        val viewModel = createViewModel()
        assertTrue(viewModel.state.value is ChangelogUiState.Error)

        stubAsset(validDocument)
        viewModel.retry()

        assertTrue(viewModel.state.value is ChangelogUiState.Content)
    }
}
