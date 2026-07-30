package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.fixture.MediaListFixtures.aMediaList
import com.mxt.anitrend.fixture.MediaListFixtures.anAnimeMediaBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.MediaRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mediaRepository: MediaRepository
    private lateinit var baseRepository: BaseRepository
    private lateinit var mediaListStore: InMemoryMediaListStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mediaRepository = mock(MediaRepository::class.java)
        baseRepository = mock(BaseRepository::class.java)
        mediaListStore = InMemoryMediaListStore()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(MediaViewModel.UiState.Loading, MediaViewModel.UiState.Loading)
    }

    @Test
    fun `UiState Success holds media instance`() {
        val media = MediaBase().apply {
            id = 1L
            siteUrl = "https://anilist.co/anime/1"
        }
        val state = MediaViewModel.UiState.Success(media)
        assertEquals(1L, state.media.id)
        assertEquals("https://anilist.co/anime/1", state.media.siteUrl)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = MediaViewModel.UiState.Error("Something went wrong")
        assertEquals("Something went wrong", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        val vm = MediaViewModel(
            mediaRepository = mediaRepository,
            baseRepository = baseRepository,
            mediaListStore = mediaListStore,
            ioDispatcher = testDispatcher,
        )
        assertTrue(vm.state.value is MediaViewModel.UiState.Loading)
    }

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = MediaViewModel.UiState.Error("Media not found")
        assertEquals("Media not found", state.message)
    }

    @Test
    fun `loaded media uses media list entry from store`() = runTest(testDispatcher) {
        val media = anAnimeMediaBase(id = 100L)
        media.mediaListEntry = aMediaList(id = 5, mediaId = 100, progress = 7, media = media)
        doReturn(Result.success(media))
            .`when`(mediaRepository)
            .getMediaBase(100L, null, false)

        val vm = MediaViewModel(
            mediaRepository = mediaRepository,
            baseRepository = baseRepository,
            mediaListStore = mediaListStore,
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()

        val state = vm.state.value as MediaViewModel.UiState.Success
        assertEquals(5L, state.media.mediaListEntry?.id)
        assertEquals(7, state.media.mediaListEntry?.progress)
        assertEquals(7, mediaListStore.state.value.entriesById.getValue(5L).progress)
        verify(mediaRepository).getMediaBase(100L, null, false)
        collector.cancel()
    }

    @Test
    fun `load skips repeated fetches after first success`() = runTest(testDispatcher) {
        val media = anAnimeMediaBase(id = 100L)
        media.mediaListEntry = aMediaList(id = 1, mediaId = 100, progress = 5, media = media)
        doReturn(Result.success(media))
            .`when`(mediaRepository)
            .getMediaBase(100L, null, false)

        val vm = MediaViewModel(
            mediaRepository = mediaRepository,
            baseRepository = baseRepository,
            mediaListStore = mediaListStore,
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()

        var state = vm.state.value as MediaViewModel.UiState.Success
        assertEquals(5, state.media.mediaListEntry?.progress)

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()

        state = vm.state.value as MediaViewModel.UiState.Success
        assertEquals(5, state.media.mediaListEntry?.progress)
        verify(mediaRepository).getMediaBase(100L, null, false)
        collector.cancel()
    }

    @Test
    fun `load failure emits Error state`() = runTest(testDispatcher) {
        doReturn(Result.failure<MediaBase>(IllegalStateException("Media failed")))
            .`when`(mediaRepository)
            .getMediaBase(100L, null, false)

        val vm = MediaViewModel(
            mediaRepository = mediaRepository,
            baseRepository = baseRepository,
            mediaListStore = mediaListStore,
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()

        val state = vm.state.value as MediaViewModel.UiState.Error
        assertEquals("Media failed", state.message)
        collector.cancel()
    }
}
