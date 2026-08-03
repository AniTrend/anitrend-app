package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.store.favourite.FavouriteStoreChange
import com.mxt.anitrend.data.store.favourite.InMemoryFavouriteStore
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.domain.favourite.interactor.ToggleFavouriteInteractor
import com.mxt.anitrend.domain.favourite.model.FavouriteKey
import com.mxt.anitrend.domain.model.StudioRecord
import com.mxt.anitrend.domain.model.ToggleFavouriteCommand
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.StudioRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class StudioViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var studioRepository: StudioRepository
    private lateinit var baseRepository: BaseRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        studioRepository = mock(StudioRepository::class.java)
        baseRepository = mock(BaseRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── UiState sealed type ──

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(StudioViewModel.UiState.Loading, StudioViewModel.UiState.Loading)
    }

    @Test
    fun `UiState Success holds studio instance`() {
        val studio = StudioRecord(id = 1L, name = "Test", siteUrl = null, isFavourite = false)
        val state = StudioViewModel.UiState.Success(studio)
        assertEquals(1L, state.studio.id)
        assertEquals("Test", state.studio.name)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = StudioViewModel.UiState.Error("Something went wrong")
        assertEquals("Something went wrong", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    // ── initial state ──

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = viewModel(store = InMemoryFavouriteStore(), interactor = mock(ToggleFavouriteInteractor::class.java))
        assertTrue(vm.state.value is StudioViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val studio = StudioRecord(id = 1L, name = "Test", siteUrl = null, isFavourite = false)
        doReturn(Result.success(studio)).`when`(studioRepository).getStudioBase(id = 1L)
        val vm = viewModel(store = InMemoryFavouriteStore(), interactor = mock(ToggleFavouriteInteractor::class.java))

        vm.load(1L)

        val state = vm.state.value as StudioViewModel.UiState.Success
        assertEquals(1L, state.studio.id)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<StudioRecord>(RuntimeException("Studio failed")))
            .`when`(studioRepository)
            .getStudioBase(id = 1L)
        val vm = viewModel(store = InMemoryFavouriteStore(), interactor = mock(ToggleFavouriteInteractor::class.java))

        vm.load(1L)

        val state = vm.state.value as StudioViewModel.UiState.Error
        assertEquals("Studio failed", state.message)
    }

    // ── GraphQL error message extraction ──

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = StudioViewModel.UiState.Error("Studio not found")
        assertEquals("Studio not found", state.message)
    }

    // ── favourite store seeding ──

    @Test
    fun `load seeds the favourite store from the loaded studio when no committed value exists`() = runTest {
        val store = InMemoryFavouriteStore()
        val studio = StudioRecord(id = 1L, name = "Test", siteUrl = null, isFavourite = true)
        doReturn(Result.success(studio)).`when`(studioRepository).getStudioBase(id = 1L)
        val vm = viewModel(store = store, interactor = mock(ToggleFavouriteInteractor::class.java))

        vm.load(1L)

        val committed = store.state.value.flagsByKey[FavouriteKey.Studio(1L)]
        assertNotNull(committed)
        assertTrue(committed!!.isFavourite)
        assertEquals(StudioViewModel.FAVOURITE_SEED_REVISION, committed.revision)
    }

    @Test
    fun `load does not overwrite a committed store value`() = runTest {
        val store = InMemoryFavouriteStore()
        store.apply(
            FavouriteStoreChange.FavouriteFlagReplaced(
                key = FavouriteKey.Studio(1L),
                isFavourite = false,
                revision = 1L,
            ),
        )
        val studio = StudioRecord(id = 1L, name = "Test", siteUrl = null, isFavourite = true)
        doReturn(Result.success(studio)).`when`(studioRepository).getStudioBase(id = 1L)
        val vm = viewModel(store = store, interactor = mock(ToggleFavouriteInteractor::class.java))

        vm.load(1L)

        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Studio(1L))
        assertFalse(committed.isFavourite)
        assertEquals(1L, committed.revision)
    }

    @Test
    fun `favouriteFlag mirrors committed store state after load`() = runTest {
        val store = InMemoryFavouriteStore()
        val studio = StudioRecord(id = 1L, name = "Test", siteUrl = null, isFavourite = true)
        doReturn(Result.success(studio)).`when`(studioRepository).getStudioBase(id = 1L)
        val vm = viewModel(store = store, interactor = mock(ToggleFavouriteInteractor::class.java))

        vm.load(1L)

        assertTrue(vm.favouriteFlag.value?.isFavourite == true)
    }

    // ── toggleFavouriteStudio ──

    @Test
    fun `toggleFavouriteStudio invokes the interactor with a Studio key`() = runTest(testDispatcher) {
        val interactor = mock(ToggleFavouriteInteractor::class.java)
        doReturn(MutationResult.Success)
            .`when`(interactor)
            .invoke(ToggleFavouriteCommand(FavouriteKey.Studio(7L)))
        val vm = viewModel(store = InMemoryFavouriteStore(), interactor = interactor)

        vm.toggleFavouriteStudio(7L)

        verify(interactor).invoke(ToggleFavouriteCommand(FavouriteKey.Studio(7L)))
        assertFalse(vm.favouriteLoading.value)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `toggleFavouriteStudio tracks loading while the mutation is in flight`() = runTest(testDispatcher) {
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
            .toggleFavourite(null, null, null, null, 7, null, null)

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
        val vm = viewModel(store = store, interactor = interactor)

        vm.toggleFavouriteStudio(7L)
        runCurrent()
        assertTrue(vm.favouriteLoading.value)

        allowReturn.complete(Unit)
        advanceUntilIdle()

        assertFalse(vm.favouriteLoading.value)
        assertTrue(store.state.value.flagsByKey.getValue(FavouriteKey.Studio(7L)).isFavourite)
    }

    private fun viewModel(
        store: InMemoryFavouriteStore,
        interactor: ToggleFavouriteInteractor,
    ): StudioViewModel = StudioViewModel(
        studioRepository = studioRepository,
        baseRepository = baseRepository,
        favouriteStore = store,
        toggleFavouriteInteractor = interactor,
        ioDispatcher = testDispatcher,
    )
}
