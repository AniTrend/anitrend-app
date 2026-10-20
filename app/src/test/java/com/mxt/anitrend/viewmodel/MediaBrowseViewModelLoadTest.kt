package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.fixture.MediaListFixtures.anAnimeMediaBase
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Browse loader request mapping: wire sort strings are parsed before the
 * repository call, invalid values fall back to an unsorted query, and
 * repository failures surface as error state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaBrowseViewModelLoadTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var baseRepository: BaseRepository
    private lateinit var browseRepository: BrowseRepository
    private lateinit var mediaListStore: InMemoryMediaListStore
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        baseRepository = mock(BaseRepository::class.java)
        browseRepository = mock(BrowseRepository::class.java)
        mediaListStore = InMemoryMediaListStore()
        userRepository = mock(UserRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = MediaBrowseViewModel(
        baseRepository = baseRepository,
        browseRepository = browseRepository,
        mediaListStore = mediaListStore,
        userRepository = userRepository,
        ioDispatcher = testDispatcher,
    )

    private fun pageContainer(vararg media: MediaBase): PageContainer<MediaBase> = PageContainer<MediaBase>().apply { pageData = media.toList() }

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = buildViewModel()
        assertTrue(vm.state.value is MediaBrowseViewModel.UiState.Loading)
    }

    @Test
    fun `load maps parsed sort onto repository request`() = runTest {
        val content = PageContainer<MediaBase>()
        doReturn(Result.success(content))
            .`when`(browseRepository)
            .getMediaBrowse(
                page = 3,
                perPage = 25,
                type = MediaType.ANIME,
                sort = listOf(MediaSort.POPULARITY_DESC),
                isAdult = false,
                onList = false,
            )
        val vm = buildViewModel()
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(
            type = MediaType.ANIME,
            page = 3,
            pageLimit = 25,
            season = null,
            sort = "POPULARITY_DESC",
            isAdult = false,
            format = null,
            seasonYear = null,
            startDateLike = null,
            status = null,
            genres = emptyList(),
            tags = emptyList(),
            onList = false,
        )
        advanceUntilIdle()

        val state = vm.state.value as MediaBrowseViewModel.UiState.Success
        assertTrue(state.content.pageData.isEmpty())
        verify(browseRepository).getMediaBrowse(
            page = 3,
            perPage = 25,
            type = MediaType.ANIME,
            sort = listOf(MediaSort.POPULARITY_DESC),
            isAdult = false,
            onList = false,
        )
        collector.cancel()
    }

    @Test
    fun `load falls back to null sort for invalid enum`() = runTest {
        val content = PageContainer<MediaBase>()
        doReturn(Result.success(content))
            .`when`(browseRepository)
            .getMediaBrowse(
                page = 1,
                perPage = 10,
                type = MediaType.MANGA,
                sort = null,
                isAdult = null,
            )
        val vm = buildViewModel()
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(
            type = MediaType.MANGA,
            page = 1,
            pageLimit = 10,
            season = null,
            sort = "INVALID_SORT",
            isAdult = null,
            format = null,
            seasonYear = null,
            startDateLike = null,
            status = null,
            genres = null,
            tags = null,
            onList = null,
        )
        advanceUntilIdle()

        val state = vm.state.value as MediaBrowseViewModel.UiState.Success
        assertTrue(state.content.pageData.isEmpty())
        verify(browseRepository).getMediaBrowse(
            page = 1,
            perPage = 10,
            type = MediaType.MANGA,
            sort = null,
            isAdult = null,
        )
        collector.cancel()
    }

    @Test
    fun `stale response cannot overwrite active query`() = runTest(testDispatcher) {
        val animeMedia = anAnimeMediaBase(id = 100L)
        val mangaMedia = anAnimeMediaBase(id = 200L)
        doReturn(Result.success(pageContainer(animeMedia)))
            .`when`(browseRepository)
            .getMediaBrowse(page = 1, perPage = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        doReturn(Result.success(pageContainer(mangaMedia)))
            .`when`(browseRepository)
            .getMediaBrowse(page = 1, perPage = 21, type = MediaType.MANGA, sort = null, isAdult = false)
        val vm = buildViewModel()
        val collector = backgroundScope.launch { vm.state.collect {} }

        // The older Anime request is issued first and completes only after the
        // Manga request has claimed the active generation.
        vm.load(
            type = MediaType.ANIME,
            page = 1,
            pageLimit = 21,
            season = null,
            sort = null,
            isAdult = false,
            format = null,
            seasonYear = null,
            startDateLike = null,
            status = null,
            genres = null,
            tags = null,
            onList = null,
        )
        vm.load(
            type = MediaType.MANGA,
            page = 1,
            pageLimit = 21,
            season = null,
            sort = null,
            isAdult = false,
            format = null,
            seasonYear = null,
            startDateLike = null,
            status = null,
            genres = null,
            tags = null,
            onList = null,
        )
        advanceUntilIdle()

        val state = vm.state.value as MediaBrowseViewModel.UiState.Success
        assertEquals(listOf(200L), state.content.pageData.map { it.id })
        collector.cancel()
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<PageContainer<MediaBase>>(RuntimeException("Media failed")))
            .`when`(browseRepository)
            .getMediaBrowse(page = 1, perPage = 10, type = null, sort = null, isAdult = false)
        val vm = buildViewModel()
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(
            type = null,
            page = 1,
            pageLimit = 10,
            season = null,
            sort = null,
            isAdult = false,
            format = null,
            seasonYear = null,
            startDateLike = null,
            status = null,
            genres = null,
            tags = null,
            onList = null,
        )
        advanceUntilIdle()

        val state = vm.state.value as MediaBrowseViewModel.UiState.Error
        assertEquals("Media failed", state.message)
        collector.cancel()
    }
}
