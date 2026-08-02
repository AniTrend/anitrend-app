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
import com.mxt.anitrend.domain.model.CharacterRecord
import com.mxt.anitrend.domain.model.ToggleFavouriteCommand
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.CharacterRepository
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
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var characterRepository: CharacterRepository
    private lateinit var baseRepository: BaseRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        characterRepository = mock(CharacterRepository::class.java)
        baseRepository = mock(BaseRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── UiState sealed type ──

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(CharacterViewModel.UiState.Loading, CharacterViewModel.UiState.Loading)
    }

    @Test
    fun `UiState Success holds character instance`() {
        val character = CharacterRecord(
            id = 1L,
            name = "Spike Spiegel",
            siteUrl = "https://anilist.co/character/1",
            isFavourite = false,
        )
        val state = CharacterViewModel.UiState.Success(character)
        assertEquals(1L, state.character.id)
        assertEquals("Spike Spiegel", state.character.name)
        assertEquals("https://anilist.co/character/1", state.character.siteUrl)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = CharacterViewModel.UiState.Error("Something went wrong")
        assertEquals("Something went wrong", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    // ── initial state ──

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = viewModel()
        assertTrue(vm.state.value is CharacterViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val character = CharacterRecord(
            id = 1L,
            name = "Spike Spiegel",
            siteUrl = "https://anilist.co/character/1",
            isFavourite = false,
        )
        `when`(characterRepository.getCharacterBase(1L)).thenReturn(Result.success(character))
        val vm = viewModel()

        vm.load(1L)

        val state = vm.state.value as CharacterViewModel.UiState.Success
        assertEquals(1L, state.character.id)
        assertEquals("Spike Spiegel", state.character.name)
        assertEquals("https://anilist.co/character/1", state.character.siteUrl)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        `when`(characterRepository.getCharacterBase(1L)).thenReturn(
            Result.failure(RuntimeException("Character not found")),
        )
        val vm = viewModel()

        vm.load(1L)

        val state = vm.state.value as CharacterViewModel.UiState.Error
        assertEquals("Character not found", state.message)
    }

    // GraphQL error message extraction

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = CharacterViewModel.UiState.Error("Character not found")
        assertEquals("Character not found", state.message)
    }

    // ── favourite store seeding ──

    @Test
    fun `load seeds the favourite store with a Character key`() = runTest {
        val store = InMemoryFavouriteStore()
        val character = CharacterRecord(
            id = 1L,
            name = null,
            siteUrl = null,
            isFavourite = true,
        )
        `when`(characterRepository.getCharacterBase(1L)).thenReturn(Result.success(character))
        val vm = viewModel(favouriteStore = store)

        vm.load(1L)

        val committed = store.state.value.flagsByKey[FavouriteKey.Character(1L)]
        assertNotNull(committed)
        assertTrue(committed!!.isFavourite)
        assertEquals(CharacterViewModel.FAVOURITE_SEED_REVISION, committed.revision)
    }

    @Test
    fun `load does not overwrite a committed store value`() = runTest {
        val store = InMemoryFavouriteStore()
        store.apply(
            FavouriteStoreChange.FavouriteFlagReplaced(
                key = FavouriteKey.Character(1L),
                isFavourite = false,
                revision = 1L,
            ),
        )
        val character = CharacterRecord(
            id = 1L,
            name = null,
            siteUrl = null,
            isFavourite = true,
        )
        `when`(characterRepository.getCharacterBase(1L)).thenReturn(Result.success(character))
        val vm = viewModel(favouriteStore = store)

        vm.load(1L)

        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Character(1L))
        assertFalse(committed.isFavourite)
        assertEquals(1L, committed.revision)
    }

    @Test
    fun `favouriteFlag mirrors committed store state after load`() = runTest {
        val store = InMemoryFavouriteStore()
        val character = CharacterRecord(
            id = 1L,
            name = null,
            siteUrl = null,
            isFavourite = true,
        )
        `when`(characterRepository.getCharacterBase(1L)).thenReturn(Result.success(character))
        val vm = viewModel(favouriteStore = store)

        vm.load(1L)

        assertTrue(vm.favouriteFlag.value?.isFavourite == true)
    }

    // ── toggleFavouriteCharacter command routing ──

    @Test
    fun `toggleFavouriteCharacter invokes the interactor with a Character key`() = runTest(testDispatcher) {
        val interactor = mock(ToggleFavouriteInteractor::class.java)
        doReturn(MutationResult.Success)
            .`when`(interactor)
            .invoke(ToggleFavouriteCommand(FavouriteKey.Character(7L)))
        val vm = viewModel(toggleFavouriteInteractor = interactor)

        vm.toggleFavouriteCharacter(7L)

        verify(interactor).invoke(ToggleFavouriteCommand(FavouriteKey.Character(7L)))
        assertFalse(vm.favouriteLoading.value)
    }

    // ── toggleFavouriteCharacter loading and convergence ──

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `toggleFavouriteCharacter tracks loading and converges the store on success`() = runTest(testDispatcher) {
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
            .toggleFavourite(null, null, 7, null, null, null, null)

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
        val character = CharacterRecord(
            id = 7L,
            name = null,
            siteUrl = null,
            isFavourite = false,
        )
        `when`(characterRepository.getCharacterBase(7L)).thenReturn(Result.success(character))
        val vm = viewModel(favouriteStore = store, toggleFavouriteInteractor = interactor)

        vm.load(7L)
        advanceUntilIdle()
        vm.toggleFavouriteCharacter(7L)
        runCurrent()
        assertTrue(vm.favouriteLoading.value)

        allowReturn.complete(Unit)
        advanceUntilIdle()

        assertFalse(vm.favouriteLoading.value)
        assertTrue(store.state.value.flagsByKey.getValue(FavouriteKey.Character(7L)).isFavourite)
        assertTrue(vm.favouriteFlag.value?.isFavourite == true)
    }

    @Test
    fun `toggleFavouriteCharacter leaves the store unchanged on failure`() = runTest(testDispatcher) {
        val store = InMemoryFavouriteStore()
        val interactor = mock(ToggleFavouriteInteractor::class.java)
        doReturn(MutationResult.Failure(message = "Unable to toggle favourite"))
            .`when`(interactor)
            .invoke(ToggleFavouriteCommand(FavouriteKey.Character(7L)))
        val character = CharacterRecord(
            id = 7L,
            name = null,
            siteUrl = null,
            isFavourite = true,
        )
        `when`(characterRepository.getCharacterBase(7L)).thenReturn(Result.success(character))
        val vm = viewModel(favouriteStore = store, toggleFavouriteInteractor = interactor)

        vm.load(7L)
        advanceUntilIdle()
        assertTrue(store.state.value.flagsByKey.getValue(FavouriteKey.Character(7L)).isFavourite)

        vm.toggleFavouriteCharacter(7L)
        advanceUntilIdle()

        assertFalse(vm.favouriteLoading.value)
        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Character(7L))
        assertTrue(committed.isFavourite)
        assertEquals(CharacterViewModel.FAVOURITE_SEED_REVISION, committed.revision)
        assertTrue(vm.favouriteFlag.value?.isFavourite == true)
    }

    private fun viewModel(
        favouriteStore: InMemoryFavouriteStore = InMemoryFavouriteStore(),
        toggleFavouriteInteractor: ToggleFavouriteInteractor = mock(ToggleFavouriteInteractor::class.java),
    ): CharacterViewModel = CharacterViewModel(
        characterRepository = characterRepository,
        baseRepository = baseRepository,
        favouriteStore = favouriteStore,
        toggleFavouriteInteractor = toggleFavouriteInteractor,
        ioDispatcher = testDispatcher,
    )
}
