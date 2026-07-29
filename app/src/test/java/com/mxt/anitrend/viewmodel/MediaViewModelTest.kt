package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.api.retro.anilist.BrowseService
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mediaRepository: MediaRepository
    private lateinit var baseRepository: BaseRepository
    private lateinit var browseRepository: BrowseRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mediaRepository = mock(MediaRepository::class.java)
        baseRepository = mock(BaseRepository::class.java)
        browseRepository = spy(BrowseRepository(mock(BrowseService::class.java), testDispatcher))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── UiState sealed type ──

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

    // ── initial state ──

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = MediaViewModel(
            mediaRepository = mediaRepository,
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            ioDispatcher = testDispatcher,
        )
        assertTrue(vm.state.value is MediaViewModel.UiState.Loading)
    }

    // ── GraphQL error message extraction ──

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = MediaViewModel.UiState.Error("Media not found")
        assertEquals("Media not found", state.message)
    }

    @Test
    fun `saved media list event updates loaded media entry`() = runTest {
        val media = MediaBase().apply { id = 100L }
        val savedEntry = com.mxt.anitrend.model.entity.anilist.MediaList().apply {
            id = 5L
            mediaId = 100L
        }
        doReturn(Result.success(media))
            .`when`(mediaRepository)
            .getMediaBase(100L, null, false)

        val vm = MediaViewModel(
            mediaRepository = mediaRepository,
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            ioDispatcher = testDispatcher,
        )

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()
        vm.onMediaListSaved(savedEntry)

        val state = vm.state.value as MediaViewModel.UiState.Success
        assertEquals(5L, state.media.mediaListEntry?.id)
        verify(mediaRepository).getMediaBase(100L, null, false)
    }

    @Test
    fun `deleted media list event clears loaded media entry`() = runTest {
        val entry = com.mxt.anitrend.model.entity.anilist.MediaList().apply {
            id = 9L
            mediaId = 100L
        }
        val media = MediaBase().apply {
            id = 100L
            mediaListEntry = entry
        }
        doReturn(Result.success(media))
            .`when`(mediaRepository)
            .getMediaBase(100L, null, false)

        val vm = MediaViewModel(
            mediaRepository = mediaRepository,
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            ioDispatcher = testDispatcher,
        )

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()
        vm.onMediaListDeleted(9L)

        val state = vm.state.value as MediaViewModel.UiState.Success
        assertNull(state.media.mediaListEntry)
    }
}
