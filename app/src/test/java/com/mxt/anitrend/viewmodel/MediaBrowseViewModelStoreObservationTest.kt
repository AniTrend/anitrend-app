package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.fixture.MediaListFixtures.aMediaList
import com.mxt.anitrend.fixture.MediaListFixtures.anAnimeMediaBase
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Pins the complete-snapshot contract that [MediaBrowseFragment] renders.
 *
 * Regression scope: MediaBrowseFragment.handleSuccess no longer appends; every
 * [MediaBrowseViewModel.UiState.Success] replaces the adapter contents with
 * `content.pageData` via `mAdapter.onItemsInserted(...)` followed by `updateUI()`.
 * That replacement is only correct if every Success carries the full accumulated
 * deduplicated snapshot, so these tests pin that ViewModel contract for page one,
 * page two, and store recombination.
 *
 * Remaining UI seam: the replacement itself lives in the fragment and is not
 * exercised by the local JVM suite. A fragment-level test would require a live
 * MediaAdapter, scroll listener, Koin Settings, and view lifecycle (no
 * Robolectric/Espresso local runner exists), so it is deliberately out of scope
 * rather than introducing a new test framework. If an append path is ever
 * reintroduced, page-two snapshots will duplicate all previously rendered items.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaBrowseViewModelStoreObservationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var baseRepository: BaseRepository
    private lateinit var browseRepository: BrowseRepository
    private lateinit var mediaListStore: InMemoryMediaListStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        baseRepository = mock(BaseRepository::class.java)
        browseRepository = mock(BrowseRepository::class.java)
        mediaListStore = InMemoryMediaListStore()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loaded media uses media list entry from store`() = runTest(testDispatcher) {
        val media = anAnimeMediaBase(id = 100L)
        val entry = aMediaList(id = 1L, mediaId = 100L, progress = 7, media = media)
        doReturn(Result.success(pageContainer(media))).`when`(browseRepository).getMediaBrowse(
            id = null,
            page = 1,
            perPage = 50,
            seasonYear = null,
            type = MediaType.ANIME,
            format = null,
            startDateLike = null,
            endDateLike = null,
            season = null,
            genres = null,
            genresExclude = null,
            isAdult = false,
            sort = null,
            onList = null,
            status = null,
            tags = null,
            tagsExclude = null,
        )
        mediaListStore.apply(
            MediaListStoreChange.EntryUpserted(
                entry.toMediaListRecord(revision = 1L, ownerUserId = 42L, ownerUserName = "max"),
            ),
        )

        val viewModel = MediaBrowseViewModel(
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        viewModel.load(
            type = MediaType.ANIME,
            page = 1,
            pageLimit = 50,
            season = null,
            sort = null,
            isAdult = false,
            format = null,
            seasonYear = null,
            startDateLike = null,
            status = null,
            genres = emptyList(),
            tags = emptyList(),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as MediaBrowseViewModel.UiState.Success
        assertEquals(7, state.content.pageData.single().mediaListEntry?.progress)
        verify(browseRepository).getMediaBrowse(
            id = null,
            page = 1,
            perPage = 50,
            seasonYear = null,
            type = MediaType.ANIME,
            format = null,
            startDateLike = null,
            endDateLike = null,
            season = null,
            genres = null,
            genresExclude = null,
            isAdult = false,
            sort = null,
            onList = null,
            status = null,
            tags = null,
            tagsExclude = null,
        )
        collector.cancel()
    }

    @Test
    fun `store entry updates are reflected in browse state`() = runTest(testDispatcher) {
        val media = anAnimeMediaBase(id = 100L)
        doReturn(Result.success(pageContainer(media))).`when`(browseRepository).getMediaBrowse(
            id = null,
            page = 1,
            perPage = 50,
            seasonYear = null,
            type = MediaType.ANIME,
            format = null,
            startDateLike = null,
            endDateLike = null,
            season = null,
            genres = null,
            genresExclude = null,
            isAdult = false,
            sort = null,
            onList = null,
            status = null,
            tags = null,
            tagsExclude = null,
        )

        val viewModel = MediaBrowseViewModel(
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        viewModel.load(
            type = MediaType.ANIME,
            page = 1,
            pageLimit = 50,
            season = null,
            sort = null,
            isAdult = false,
            format = null,
            seasonYear = null,
            startDateLike = null,
            status = null,
            genres = emptyList(),
            tags = emptyList(),
        )
        advanceUntilIdle()

        var state = viewModel.state.value as MediaBrowseViewModel.UiState.Success
        assertNull(state.content.pageData.single().mediaListEntry)

        mediaListStore.apply(
            MediaListStoreChange.EntryUpserted(
                aMediaList(id = 1L, mediaId = 100L, progress = 11, media = media).toMediaListRecord(
                    revision = 2L,
                    ownerUserId = 42L,
                    ownerUserName = "max",
                ),
            ),
        )
        advanceUntilIdle()

        state = viewModel.state.value as MediaBrowseViewModel.UiState.Success
        assertEquals(11, state.content.pageData.single().mediaListEntry?.progress)
        collector.cancel()
    }

    @Test
    fun `never loaded idle state emits Loading instead of an empty Success`() = runTest(testDispatcher) {
        val viewModel = MediaBrowseViewModel(
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        assertTrue(
            "Idle never-loaded state must stay Loading, not Success(empty)",
            viewModel.state.value is MediaBrowseViewModel.UiState.Loading,
        )
        collector.cancel()
    }

    @Test
    fun `genuine empty successful response emits Success with empty content`() = runTest(testDispatcher) {
        doReturn(Result.success(pageContainer())).`when`(browseRepository).getMediaBrowse(
            id = null,
            page = 1,
            perPage = 50,
            seasonYear = null,
            type = MediaType.ANIME,
            format = null,
            startDateLike = null,
            endDateLike = null,
            season = null,
            genres = null,
            genresExclude = null,
            isAdult = false,
            sort = null,
            onList = null,
            status = null,
            tags = null,
            tagsExclude = null,
        )

        val viewModel = MediaBrowseViewModel(
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        viewModel.load(
            type = MediaType.ANIME,
            page = 1,
            pageLimit = 50,
            season = null,
            sort = null,
            isAdult = false,
            format = null,
            seasonYear = null,
            startDateLike = null,
            status = null,
            genres = emptyList(),
            tags = emptyList(),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as MediaBrowseViewModel.UiState.Success
        assertTrue("First successful empty response must surface as an empty Success", state.content.pageData.isEmpty())
        collector.cancel()
    }

    @Test
    fun `success content is the complete deduplicated snapshot after page two`() = runTest(testDispatcher) {
        val pageOneMedia = anAnimeMediaBase(id = 100L)
        val pageTwoMedia = anAnimeMediaBase(id = 200L)
        stubBrowsePage(page = 1, pageOneMedia)
        // Page two overlaps page one: the backend may re-return previously seen items.
        stubBrowsePage(page = 2, pageOneMedia, pageTwoMedia)

        val viewModel = MediaBrowseViewModel(
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        loadBrowsePage(viewModel, page = 1)
        advanceUntilIdle()
        var state = viewModel.state.value as MediaBrowseViewModel.UiState.Success
        assertEquals(listOf(100L), state.content.pageData.map { it.id })

        loadBrowsePage(viewModel, page = 2)
        advanceUntilIdle()
        state = viewModel.state.value as MediaBrowseViewModel.UiState.Success
        assertEquals(
            "Success must carry the complete page one plus page two snapshot, deduplicated",
            listOf(100L, 200L),
            state.content.pageData.map { it.id },
        )
        collector.cancel()
    }

    @Test
    fun `store recombination keeps the complete multi page snapshot`() = runTest(testDispatcher) {
        val pageOneMedia = anAnimeMediaBase(id = 100L)
        val pageTwoMedia = anAnimeMediaBase(id = 200L)
        stubBrowsePage(page = 1, pageOneMedia)
        stubBrowsePage(page = 2, pageTwoMedia)

        val viewModel = MediaBrowseViewModel(
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        loadBrowsePage(viewModel, page = 1)
        advanceUntilIdle()
        loadBrowsePage(viewModel, page = 2)
        advanceUntilIdle()

        mediaListStore.apply(
            MediaListStoreChange.EntryUpserted(
                aMediaList(id = 1L, mediaId = 100L, progress = 11, media = pageOneMedia)
                    .toMediaListRecord(revision = 2L, ownerUserId = 42L, ownerUserName = "max"),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as MediaBrowseViewModel.UiState.Success
        assertEquals(
            "Recombination must re-emit the full snapshot, not a single page",
            listOf(100L, 200L),
            state.content.pageData.map { it.id },
        )
        assertEquals(11, state.content.pageData.first { it.id == 100L }.mediaListEntry?.progress)
        collector.cancel()
    }

    private suspend fun stubBrowsePage(
        page: Int,
        vararg media: MediaBase,
    ) {
        doReturn(Result.success(pageContainer(*media))).`when`(browseRepository).getMediaBrowse(
            id = null,
            page = page,
            perPage = 50,
            seasonYear = null,
            type = MediaType.ANIME,
            format = null,
            startDateLike = null,
            endDateLike = null,
            season = null,
            genres = null,
            genresExclude = null,
            isAdult = false,
            sort = null,
            onList = null,
            status = null,
            tags = null,
            tagsExclude = null,
        )
    }

    private fun loadBrowsePage(
        viewModel: MediaBrowseViewModel,
        page: Int,
    ) {
        viewModel.load(
            type = MediaType.ANIME,
            page = page,
            pageLimit = 50,
            season = null,
            sort = null,
            isAdult = false,
            format = null,
            seasonYear = null,
            startDateLike = null,
            status = null,
            genres = emptyList(),
            tags = emptyList(),
        )
    }

    private fun pageContainer(vararg media: MediaBase): PageContainer<MediaBase> = PageContainer<MediaBase>().apply {
        pageData = media.toList()
    }
}
