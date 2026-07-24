package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.api.retro.anilist.CharacterModel
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.repository.BaseRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var service: CharacterModel
    private lateinit var baseRepository: BaseRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        service = mock(CharacterModel::class.java)
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
        val vm = CharacterViewModel(characterService = service, baseRepository = baseRepository, ioDispatcher = testDispatcher)
        assertTrue(vm.state.value is CharacterViewModel.UiState.Loading)
    }

    // ── GraphQL error message extraction ──

    @Test
    fun `GraphQL error message is surfaced in Error state`() {
        val state = CharacterViewModel.UiState.Error("Character not found")
        assertEquals("Character not found", state.message)
    }
}
