package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.repository.CharacterRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterActorsViewModelTest {

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
    fun `initial state is Loading`() = runTest {
        val vm = CharacterActorsViewModel(characterRepository = characterRepository)
        assertTrue(vm.state.value is CharacterActorsViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val content = ConnectionContainer<EdgeContainer<MediaEdge>>()
        doReturn(Result.success(content))
            .`when`(characterRepository)
            .getCharacterActors(id = 1L, page = 2, perPage = KeyUtil.PAGING_LIMIT)
        val vm = CharacterActorsViewModel(characterRepository = characterRepository)

        vm.load(id = 1L, page = 2)

        val state = vm.state.value as CharacterActorsViewModel.UiState.Success
        assertSame(content, state.content)
        verify(characterRepository).getCharacterActors(id = 1L, page = 2, perPage = KeyUtil.PAGING_LIMIT)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<ConnectionContainer<EdgeContainer<MediaEdge>>>(RuntimeException("Actors failed")))
            .`when`(characterRepository)
            .getCharacterActors(id = 1L, page = 3, perPage = KeyUtil.PAGING_LIMIT)
        val vm = CharacterActorsViewModel(characterRepository = characterRepository)

        vm.load(id = 1L, page = 3)

        val state = vm.state.value as CharacterActorsViewModel.UiState.Error
        assertEquals("Actors failed", state.message)
    }
}
