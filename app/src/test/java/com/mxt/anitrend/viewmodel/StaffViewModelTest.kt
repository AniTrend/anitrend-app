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
import com.mxt.anitrend.domain.model.StaffRecord
import com.mxt.anitrend.domain.model.ToggleFavouriteCommand
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.StaffRepository
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
class StaffViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var staffRepository: StaffRepository
    private lateinit var baseRepository: BaseRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        staffRepository = mock(StaffRepository::class.java)
        baseRepository = mock(BaseRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── UiState sealed type ──

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(StaffViewModel.UiState.Loading, StaffViewModel.UiState.Loading)
    }

    @Test
    fun `UiState Success holds staff instance`() {
        val staff =
            StaffRecord(
                id = 1L,
                name = "Shinichiro Watanabe",
                siteUrl = "https://anilist.co/staff/1",
                isFavourite = false,
            )
        val state = StaffViewModel.UiState.Success(staff)
        assertEquals(1L, state.staff.id)
        assertEquals("Shinichiro Watanabe", state.staff.name)
        assertEquals("https://anilist.co/staff/1", state.staff.siteUrl)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = StaffViewModel.UiState.Error("Something went wrong")
        assertEquals("Something went wrong", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    // ── initial state ──

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = viewModel()
        assertTrue(vm.state.value is StaffViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val staff = staffRecord(id = 1L)
        doReturn(Result.success(staff)).`when`(staffRepository).getStaffBase(id = 1L)
        val vm = viewModel()

        vm.load(1L)

        val state = vm.state.value as StaffViewModel.UiState.Success
        assertEquals(1L, state.staff.id)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<StaffRecord>(RuntimeException("Staff failed")))
            .`when`(staffRepository)
            .getStaffBase(id = 1L)
        val vm = viewModel()

        vm.load(1L)

        val state = vm.state.value as StaffViewModel.UiState.Error
        assertEquals("Staff failed", state.message)
    }

    // ── GraphQL error message extraction ──

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = StaffViewModel.UiState.Error("Staff not found")
        assertEquals("Staff not found", state.message)
    }

    // ── favourite store seeding ──

    @Test
    fun `load seeds the favourite store with a Staff key`() = runTest {
        val store = InMemoryFavouriteStore()
        val staff = staffRecord(id = 1L, isFavourite = true)
        doReturn(Result.success(staff)).`when`(staffRepository).getStaffBase(id = 1L)
        val vm = viewModel(favouriteStore = store)

        vm.load(1L)

        val committed = store.state.value.flagsByKey[FavouriteKey.Staff(1L)]
        assertNotNull(committed)
        assertTrue(committed!!.isFavourite)
        assertEquals(StaffViewModel.FAVOURITE_SEED_REVISION, committed.revision)
    }

    @Test
    fun `load does not overwrite a committed store value`() = runTest {
        val store = InMemoryFavouriteStore()
        store.apply(
            FavouriteStoreChange.FavouriteFlagReplaced(
                key = FavouriteKey.Staff(1L),
                isFavourite = false,
                revision = 1L,
            ),
        )
        val staff = staffRecord(id = 1L, isFavourite = true)
        doReturn(Result.success(staff)).`when`(staffRepository).getStaffBase(id = 1L)
        val vm = viewModel(favouriteStore = store)

        vm.load(1L)

        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Staff(1L))
        assertFalse(committed.isFavourite)
        assertEquals(1L, committed.revision)
    }

    @Test
    fun `favouriteFlag mirrors committed store state after load`() = runTest {
        val store = InMemoryFavouriteStore()
        val staff = staffRecord(id = 1L, isFavourite = true)
        doReturn(Result.success(staff)).`when`(staffRepository).getStaffBase(id = 1L)
        val vm = viewModel(favouriteStore = store)

        vm.load(1L)

        assertTrue(vm.favouriteFlag.value?.isFavourite == true)
    }

    // ── toggleFavouriteStaff command routing ──

    @Test
    fun `toggleFavouriteStaff invokes the interactor with a Staff key`() = runTest(testDispatcher) {
        val interactor = mock(ToggleFavouriteInteractor::class.java)
        doReturn(MutationResult.Success)
            .`when`(interactor)
            .invoke(ToggleFavouriteCommand(FavouriteKey.Staff(7L)))
        val vm = viewModel(toggleFavouriteInteractor = interactor)

        vm.toggleFavouriteStaff(7L)

        verify(interactor).invoke(ToggleFavouriteCommand(FavouriteKey.Staff(7L)))
        assertFalse(vm.favouriteLoading.value)
    }

    // ── toggleFavouriteStaff loading and convergence ──

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `toggleFavouriteStaff tracks loading and converges the store on success`() = runTest(testDispatcher) {
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
            .toggleFavourite(null, null, null, 7, null, null, null)

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
        val staff = staffRecord(id = 7L)
        doReturn(Result.success(staff)).`when`(staffRepository).getStaffBase(id = 7L)
        val vm = viewModel(favouriteStore = store, toggleFavouriteInteractor = interactor)

        vm.load(7L)
        advanceUntilIdle()
        vm.toggleFavouriteStaff(7L)
        runCurrent()
        assertTrue(vm.favouriteLoading.value)

        allowReturn.complete(Unit)
        advanceUntilIdle()

        assertFalse(vm.favouriteLoading.value)
        assertTrue(store.state.value.flagsByKey.getValue(FavouriteKey.Staff(7L)).isFavourite)
        assertTrue(vm.favouriteFlag.value?.isFavourite == true)
    }

    @Test
    fun `toggleFavouriteStaff leaves the store unchanged on failure`() = runTest(testDispatcher) {
        val store = InMemoryFavouriteStore()
        val interactor = mock(ToggleFavouriteInteractor::class.java)
        doReturn(MutationResult.Failure(message = "Unable to toggle favourite"))
            .`when`(interactor)
            .invoke(ToggleFavouriteCommand(FavouriteKey.Staff(7L)))
        val staff = staffRecord(id = 7L, isFavourite = true)
        doReturn(Result.success(staff)).`when`(staffRepository).getStaffBase(id = 7L)
        val vm = viewModel(favouriteStore = store, toggleFavouriteInteractor = interactor)

        vm.load(7L)
        advanceUntilIdle()
        assertTrue(store.state.value.flagsByKey.getValue(FavouriteKey.Staff(7L)).isFavourite)

        vm.toggleFavouriteStaff(7L)
        advanceUntilIdle()

        assertFalse(vm.favouriteLoading.value)
        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Staff(7L))
        assertTrue(committed.isFavourite)
        assertEquals(StaffViewModel.FAVOURITE_SEED_REVISION, committed.revision)
        assertTrue(vm.favouriteFlag.value?.isFavourite == true)
    }

    private fun viewModel(
        favouriteStore: InMemoryFavouriteStore = InMemoryFavouriteStore(),
        toggleFavouriteInteractor: ToggleFavouriteInteractor = mock(ToggleFavouriteInteractor::class.java),
    ): StaffViewModel = StaffViewModel(
        staffRepository = staffRepository,
        baseRepository = baseRepository,
        favouriteStore = favouriteStore,
        toggleFavouriteInteractor = toggleFavouriteInteractor,
        ioDispatcher = testDispatcher,
    )

    private fun staffRecord(
        id: Long,
        isFavourite: Boolean = false,
    ): StaffRecord = StaffRecord(
        id = id,
        name = null,
        siteUrl = "https://anilist.co/staff/$id",
        isFavourite = isFavourite,
    )
}
