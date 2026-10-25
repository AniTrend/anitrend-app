package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.api.retro.crunchy.EpisodeService
import com.mxt.anitrend.model.entity.crunchy.Channel
import com.mxt.anitrend.model.entity.crunchy.Episode
import com.mxt.anitrend.model.entity.crunchy.Rss
import com.mxt.anitrend.repository.CrunchyrollRepository
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.Settings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class WatchListViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mediaRepository: MediaRepository
    private lateinit var settings: Settings

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mediaRepository = mock(MediaRepository::class.java)
        settings = mock(Settings::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search filters titles case insensitively and preserves original row identity`() = runTest(testDispatcher) {
        val service = StaticEpisodeService(
            latest = rss(
                Episode(title = "First episode", link = "https://example.test/first"),
                Episode(title = "The Target episode", link = "https://example.test/target"),
            ),
        )
        val viewModel = createViewModel(service)
        viewModel.configure(
            mediaId = 0L,
            mediaType = null,
            isPopular = false,
            isFeedRoute = true,
            externalLinks = null,
        )

        viewModel.refresh()
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("TARGET")
        val filtered = viewModel.state.value as WatchListViewModel.UiState.Content
        assertEquals(listOf("episode:1"), filtered.rows.map { it.id })
        assertEquals("https://example.test/target", viewModel.episodeActionAt(0)?.link)

        viewModel.onSearchQueryChanged("no matching title")
        val noMatches = viewModel.state.value as WatchListViewModel.UiState.Content
        assertTrue(noMatches.rows.isEmpty())
        assertEquals(2, noMatches.sourceCount)

        viewModel.onSearchQueryChanged(null)
        val restored = viewModel.state.value as WatchListViewModel.UiState.Content
        assertEquals(listOf("episode:0", "episode:1"), restored.rows.map { it.id })
    }

    @Test
    fun `response for prior selection cannot overwrite the newly configured feed`() = runTest(testDispatcher) {
        val staleResponse = CompletableDeferred<Response<Rss>>()
        val latestRequestStarted = CompletableDeferred<Unit>()
        val service = StaticEpisodeService(
            latestResponse = staleResponse,
            latestRequestStarted = latestRequestStarted,
            popular = rss(Episode(title = "Current selection")),
        )
        val viewModel = createViewModel(service)
        viewModel.configure(
            mediaId = 0L,
            mediaType = null,
            isPopular = false,
            isFeedRoute = true,
            externalLinks = null,
        )

        viewModel.refresh()
        runCurrent()
        assertTrue(latestRequestStarted.isCompleted)
        viewModel.configure(
            mediaId = 0L,
            mediaType = null,
            isPopular = true,
            isFeedRoute = true,
            externalLinks = null,
        )
        staleResponse.complete(Response.success(rss(Episode(title = "Stale selection"))))
        advanceUntilIdle()

        assertTrue(viewModel.state.value is WatchListViewModel.UiState.Loading)
        viewModel.refresh()
        advanceUntilIdle()

        val current = viewModel.state.value as WatchListViewModel.UiState.Content
        assertEquals(listOf("Current selection"), current.rows.map { it.title })
    }

    private fun createViewModel(service: EpisodeService) = WatchListViewModel(
        mediaRepository = mediaRepository,
        crunchyrollRepository = CrunchyrollRepository(
            feedService = service,
            crunchyrollService = service,
            ioDispatcher = testDispatcher,
        ),
        settings = settings,
        ioDispatcher = testDispatcher,
    )

    private fun rss(vararg episodes: Episode) = Rss(
        channel = Channel(
            copyright = "Copyright",
            episode = episodes.toList(),
        ),
    )

    private class StaticEpisodeService(
        private val latest: Rss = Rss(),
        private val latestResponse: CompletableDeferred<Response<Rss>>? = null,
        private val latestRequestStarted: CompletableDeferred<Unit>? = null,
        private val popular: Rss = latest,
    ) : EpisodeService {
        override suspend fun getPopularFeed(): Response<Rss> = Response.success(popular)

        override suspend fun getLatestFeed(): Response<Rss> {
            latestRequestStarted?.complete(Unit)
            return latestResponse?.await() ?: Response.success(latest)
        }

        override suspend fun getRssByUrl(link: String?): Response<Rss> = Response.success(latest)
    }
}
