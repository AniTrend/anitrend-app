package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.store.favourite.FavouriteStoreChange
import com.mxt.anitrend.data.store.favourite.InMemoryFavouriteStore
import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.domain.favourite.interactor.ToggleFavouriteInteractor
import com.mxt.anitrend.domain.favourite.model.FavouriteKey
import com.mxt.anitrend.domain.mediadetail.model.MediaDetailRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaListEntryRecord
import com.mxt.anitrend.domain.model.ToggleFavouriteCommand
import com.mxt.anitrend.fixture.MediaListFixtures.aMangaMediaDetailRecord
import com.mxt.anitrend.fixture.MediaListFixtures.aMediaListRecord
import com.mxt.anitrend.fixture.MediaListFixtures.anAnimeMediaDetailRecord
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.KeyUtil
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doAnswer
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
        val media = MediaDetailRecord(
            id = 1L,
            idMal = null,
            titleUserPreferred = null,
            type = null,
            bannerImage = null,
            isFavourite = false,
            siteUrl = "https://anilist.co/anime/1",
            mediaListEntry = null,
        )
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
        val vm = viewModel()
        assertTrue(vm.state.value is MediaViewModel.UiState.Loading)
    }

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = MediaViewModel.UiState.Error("Media not found")
        assertEquals("Media not found", state.message)
    }

    @Test
    fun `loaded media projects media list entry from store`() = runTest(testDispatcher) {
        val media = anAnimeMediaDetailRecord(id = 100L)
        doReturn(Result.success(media))
            .`when`(mediaRepository)
            .getMediaBaseRecord(100L, null, false)

        val canonical = aMediaListRecord(
            id = 5L,
            mediaId = 100L,
            status = KeyUtil.CURRENT,
            progress = 7,
        )
        mediaListStore.apply(MediaListStoreChange.EntryUpserted(canonical))

        val vm = viewModel()
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()

        val state = vm.state.value as MediaViewModel.UiState.Success
        assertEquals(5L, state.media.mediaListEntry?.id)
        assertEquals(KeyUtil.CURRENT, state.media.mediaListEntry?.status)
        assertEquals(7, mediaListStore.state.value.entriesById.getValue(5L).progress)
        verify(mediaRepository).getMediaBaseRecord(100L, null, false)
        collector.cancel()
    }

    @Test
    fun `loaded media preserves the initial media list entry when the store has none`() = runTest(testDispatcher) {
        val media = anAnimeMediaDetailRecord(
            id = 100L,
            mediaListEntry = MediaListEntryRecord(id = 5L, status = KeyUtil.CURRENT),
        )
        doReturn(Result.success(media))
            .`when`(mediaRepository)
            .getMediaBaseRecord(100L, null, false)

        val vm = viewModel()
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()

        val state = vm.state.value as MediaViewModel.UiState.Success
        assertEquals(5L, state.media.mediaListEntry?.id)
        assertEquals(KeyUtil.CURRENT, state.media.mediaListEntry?.status)
        assertTrue(mediaListStore.state.value.entriesById.isEmpty())
        verify(mediaRepository).getMediaBaseRecord(100L, null, false)
        collector.cancel()
    }

    @Test
    fun `load skips repeated fetches after first success`() = runTest(testDispatcher) {
        val media = anAnimeMediaDetailRecord(
            id = 100L,
            mediaListEntry = MediaListEntryRecord(id = 1L, status = KeyUtil.CURRENT),
        )
        doReturn(Result.success(media))
            .`when`(mediaRepository)
            .getMediaBaseRecord(100L, null, false)

        val vm = viewModel()
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()

        var state = vm.state.value as MediaViewModel.UiState.Success
        assertEquals(KeyUtil.CURRENT, state.media.mediaListEntry?.status)

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()

        state = vm.state.value as MediaViewModel.UiState.Success
        assertEquals(KeyUtil.CURRENT, state.media.mediaListEntry?.status)
        verify(mediaRepository).getMediaBaseRecord(100L, null, false)
        collector.cancel()
    }

    @Test
    fun `load failure emits Error state`() = runTest(testDispatcher) {
        doReturn(Result.failure<MediaDetailRecord>(IllegalStateException("Media failed")))
            .`when`(mediaRepository)
            .getMediaBaseRecord(100L, null, false)

        val vm = viewModel()
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()

        val state = vm.state.value as MediaViewModel.UiState.Error
        assertEquals("Media failed", state.message)
        collector.cancel()
    }

    // ── favourite store seeding ──

    @Test
    fun `load seeds the favourite store with an Anime key from an anime media`() = runTest {
        val store = InMemoryFavouriteStore()
        val media = anAnimeMediaDetailRecord(id = 100L).copy(isFavourite = true)
        doReturn(Result.success(media)).`when`(mediaRepository).getMediaBaseRecord(100L, null, false)
        val vm = viewModel(favouriteStore = store)

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)

        val committed = store.state.value.flagsByKey[FavouriteKey.Anime(100L)]
        assertNotNull(committed)
        assertTrue(committed!!.isFavourite)
        assertEquals(MediaViewModel.FAVOURITE_SEED_REVISION, committed.revision)
        assertNull(store.state.value.flagsByKey[FavouriteKey.Manga(100L)])
    }

    @Test
    fun `load seeds the favourite store with a Manga key from a manga media`() = runTest {
        val store = InMemoryFavouriteStore()
        val media = aMangaMediaDetailRecord(id = 100L).copy(isFavourite = true)
        doReturn(Result.success(media)).`when`(mediaRepository).getMediaBaseRecord(100L, null, false)
        val vm = viewModel(favouriteStore = store)

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)

        val committed = store.state.value.flagsByKey[FavouriteKey.Manga(100L)]
        assertNotNull(committed)
        assertTrue(committed!!.isFavourite)
        assertEquals(MediaViewModel.FAVOURITE_SEED_REVISION, committed.revision)
        assertNull(store.state.value.flagsByKey[FavouriteKey.Anime(100L)])
    }

    @Test
    fun `load does not overwrite a committed store value`() = runTest {
        val store = InMemoryFavouriteStore()
        store.apply(
            FavouriteStoreChange.FavouriteFlagReplaced(
                key = FavouriteKey.Anime(100L),
                isFavourite = false,
                revision = 1L,
            ),
        )
        val media = anAnimeMediaDetailRecord(id = 100L).copy(isFavourite = true)
        doReturn(Result.success(media)).`when`(mediaRepository).getMediaBaseRecord(100L, null, false)
        val vm = viewModel(favouriteStore = store)

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)

        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Anime(100L))
        assertFalse(committed.isFavourite)
        assertEquals(1L, committed.revision)
    }

    @Test
    fun `favouriteFlag mirrors committed store state after load`() = runTest {
        val store = InMemoryFavouriteStore()
        val media = anAnimeMediaDetailRecord(id = 100L).copy(isFavourite = true)
        doReturn(Result.success(media)).`when`(mediaRepository).getMediaBaseRecord(100L, null, false)
        val vm = viewModel(favouriteStore = store)

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)

        assertTrue(vm.favouriteFlag.value?.isFavourite == true)
    }

    // ── toggleFavouriteMedia command routing ──

    @Test
    fun `toggleFavouriteMedia routes an Anime command for anime media`() = runTest(testDispatcher) {
        val interactor = mock(ToggleFavouriteInteractor::class.java)
        doReturn(MutationResult.Success)
            .`when`(interactor)
            .invoke(ToggleFavouriteCommand(FavouriteKey.Anime(100L)))
        val media = anAnimeMediaDetailRecord(id = 100L)
        doReturn(Result.success(media)).`when`(mediaRepository).getMediaBaseRecord(100L, null, false)
        val vm = viewModel(toggleFavouriteInteractor = interactor)

        vm.load(mediaId = 100L, mediaType = null, showAdult = false)
        advanceUntilIdle()
        vm.toggleFavouriteMedia(100L, mediaType = null)

        verify(interactor).invoke(ToggleFavouriteCommand(FavouriteKey.Anime(100L)))
        assertFalse(vm.favouriteLoading.value)
    }

    @Test
    fun `toggleFavouriteMedia routes a Manga command for manga media`() = runTest(testDispatcher) {
        val interactor = mock(ToggleFavouriteInteractor::class.java)
        doReturn(MutationResult.Success)
            .`when`(interactor)
            .invoke(ToggleFavouriteCommand(FavouriteKey.Manga(101L)))
        val media = aMangaMediaDetailRecord(id = 101L)
        doReturn(Result.success(media)).`when`(mediaRepository).getMediaBaseRecord(101L, null, false)
        val vm = viewModel(toggleFavouriteInteractor = interactor)

        vm.load(mediaId = 101L, mediaType = null, showAdult = false)
        advanceUntilIdle()
        vm.toggleFavouriteMedia(101L, mediaType = null)

        verify(interactor).invoke(ToggleFavouriteCommand(FavouriteKey.Manga(101L)))
    }

    @Test
    fun `toggleFavouriteMedia falls back to the mediaType argument before the entity is loaded`() = runTest(testDispatcher) {
        val interactor = mock(ToggleFavouriteInteractor::class.java)
        doReturn(MutationResult.Success)
            .`when`(interactor)
            .invoke(ToggleFavouriteCommand(FavouriteKey.Anime(7L)))
        doReturn(MutationResult.Success)
            .`when`(interactor)
            .invoke(ToggleFavouriteCommand(FavouriteKey.Manga(7L)))
        val vm = viewModel(toggleFavouriteInteractor = interactor)

        vm.toggleFavouriteMedia(7L, KeyUtil.ANIME)
        verify(interactor).invoke(ToggleFavouriteCommand(FavouriteKey.Anime(7L)))

        vm.toggleFavouriteMedia(7L, KeyUtil.MANGA)
        verify(interactor).invoke(ToggleFavouriteCommand(FavouriteKey.Manga(7L)))
    }

    // ── toggleFavouriteMedia loading and convergence ──

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `toggleFavouriteMedia tracks loading and converges the store on success`() = runTest(testDispatcher) {
        val store = InMemoryFavouriteStore()
        val repository = mock(BaseRepository::class.java)
        val allowReturn = CompletableDeferred<Unit>()
        doAnswer { invocation ->
            val continuation = invocation.rawArguments.last() as Continuation<Result<Unit>>
            backgroundScope.launch {
                allowReturn.await()
                continuation.resume(Result.success(Unit))
            }
            COROUTINE_SUSPENDED
        }.`when`(repository)
            .toggleFavourite(7, null, null, null, null, null, null)

        val interactor = ToggleFavouriteInteractor(
            baseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(
                applicationScope = backgroundScope,
                keyedMutex = KeyedMutex(backgroundScope),
                mutationRegistry = DefaultMutationRegistry(),
                operationIdGenerator = DefaultOperationIdGenerator(),
                sessionEpoch = SessionEpoch(),
            ),
            favouriteStore = store,
            requestSequence = RequestSequence(),
        )
        val media = anAnimeMediaDetailRecord(id = 7L)
        doReturn(Result.success(media)).`when`(mediaRepository).getMediaBaseRecord(7L, null, false)
        val vm = viewModel(favouriteStore = store, toggleFavouriteInteractor = interactor)

        vm.load(mediaId = 7L, mediaType = null, showAdult = false)
        advanceUntilIdle()
        vm.toggleFavouriteMedia(7L, mediaType = null)
        runCurrent()
        assertTrue(vm.favouriteLoading.value)

        allowReturn.complete(Unit)
        advanceUntilIdle()

        assertFalse(vm.favouriteLoading.value)
        assertTrue(store.state.value.flagsByKey.getValue(FavouriteKey.Anime(7L)).isFavourite)
        assertTrue(vm.favouriteFlag.value?.isFavourite == true)
    }

    @Test
    fun `toggleFavouriteMedia leaves the store unchanged on failure`() = runTest(testDispatcher) {
        val store = InMemoryFavouriteStore()
        val interactor = mock(ToggleFavouriteInteractor::class.java)
        doReturn(MutationResult.Failure(message = "Unable to toggle favourite"))
            .`when`(interactor)
            .invoke(ToggleFavouriteCommand(FavouriteKey.Anime(7L)))
        val media = anAnimeMediaDetailRecord(id = 7L).copy(isFavourite = true)
        doReturn(Result.success(media)).`when`(mediaRepository).getMediaBaseRecord(7L, null, false)
        val vm = viewModel(favouriteStore = store, toggleFavouriteInteractor = interactor)

        vm.load(mediaId = 7L, mediaType = null, showAdult = false)
        advanceUntilIdle()
        assertTrue(store.state.value.flagsByKey.getValue(FavouriteKey.Anime(7L)).isFavourite)

        vm.toggleFavouriteMedia(7L, mediaType = null)
        advanceUntilIdle()

        assertFalse(vm.favouriteLoading.value)
        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Anime(7L))
        assertTrue(committed.isFavourite)
        assertEquals(MediaViewModel.FAVOURITE_SEED_REVISION, committed.revision)
        assertTrue(vm.favouriteFlag.value?.isFavourite == true)
    }

    private fun viewModel(
        favouriteStore: InMemoryFavouriteStore = InMemoryFavouriteStore(),
        toggleFavouriteInteractor: ToggleFavouriteInteractor = mock(ToggleFavouriteInteractor::class.java),
    ): MediaViewModel = MediaViewModel(
        mediaRepository = mediaRepository,
        baseRepository = baseRepository,
        mediaListStore = mediaListStore,
        favouriteStore = favouriteStore,
        toggleFavouriteInteractor = toggleFavouriteInteractor,
        ioDispatcher = testDispatcher,
    )
}
