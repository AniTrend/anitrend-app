package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.anilist.MediaCharacter
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
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
class CharacterOverviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var characterRepository: CharacterRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        characterRepository = mock(CharacterRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(
            CharacterOverviewViewModel.UiState.Loading,
            CharacterOverviewViewModel.UiState.Loading,
        )
    }

    @Test
    fun `UiState Success holds character instance`() {
        val character = MediaCharacter().apply {
            id = 1L
            name = TitleBase("Spike", "Spiegel", null, null)
        }
        val state = CharacterOverviewViewModel.UiState.Success(character)
        assertEquals(1L, state.character.id)
        assertEquals("Spike Spiegel", state.character.name?.fullName)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = CharacterOverviewViewModel.UiState.Error("Failed")
        assertEquals("Failed", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = CharacterOverviewViewModel(
            characterRepository = characterRepository,
            ioDispatcher = testDispatcher,
        )
        assertTrue(vm.state.value is CharacterOverviewViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val character = MediaCharacter().apply {
            id = 1L
            name = TitleBase("Spike", "Spiegel", null, null)
            applyDescription("Space cowboy")
        }
        `when`(characterRepository.getCharacterOverview(1L)).thenReturn(Result.success(character))
        val vm = CharacterOverviewViewModel(
            characterRepository = characterRepository,
            ioDispatcher = testDispatcher,
        )

        vm.load(1L)

        val state = vm.state.value as CharacterOverviewViewModel.UiState.Success
        assertEquals(1L, state.character.id)
        assertEquals("Spike Spiegel", state.character.name?.fullName)
        assertEquals("Space cowboy", state.character.description)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        `when`(characterRepository.getCharacterOverview(1L)).thenReturn(
            Result.failure(RuntimeException("Overview failed")),
        )
        val vm = CharacterOverviewViewModel(
            characterRepository = characterRepository,
            ioDispatcher = testDispatcher,
        )

        vm.load(1L)

        val state = vm.state.value as CharacterOverviewViewModel.UiState.Error
        assertEquals("Overview failed", state.message)
    }
}
