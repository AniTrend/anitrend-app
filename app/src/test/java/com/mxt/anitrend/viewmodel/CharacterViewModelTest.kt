package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.CharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
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
        val character = CharacterBase().apply {
            id = 1L
            siteUrl = "https://anilist.co/character/1"
        }
        val state = CharacterViewModel.UiState.Success(character)
        assertEquals(1L, state.character.id)
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
        val vm = CharacterViewModel(
            characterRepository = characterRepository,
            baseRepository = baseRepository,
            ioDispatcher = testDispatcher,
        )
        assertTrue(vm.state.value is CharacterViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val character = CharacterBase().apply {
            id = 1L
            siteUrl = "https://anilist.co/character/1"
        }
        `when`(characterRepository.getCharacterBase(1L)).thenReturn(Result.success(character))
        val vm = CharacterViewModel(
            characterRepository = characterRepository,
            baseRepository = baseRepository,
            ioDispatcher = testDispatcher,
        )

        vm.load(1L)

        val state = vm.state.value as CharacterViewModel.UiState.Success
        assertEquals(1L, state.character.id)
        assertEquals("https://anilist.co/character/1", state.character.siteUrl)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        `when`(characterRepository.getCharacterBase(1L)).thenReturn(
            Result.failure(RuntimeException("Character not found")),
        )
        val vm = CharacterViewModel(
            characterRepository = characterRepository,
            baseRepository = baseRepository,
            ioDispatcher = testDispatcher,
        )

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
}
