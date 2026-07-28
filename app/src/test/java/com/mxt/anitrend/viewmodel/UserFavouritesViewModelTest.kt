package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.repository.UserRepository
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
class UserFavouritesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mock(UserRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `CharacterFavouritesViewModel starts Loading and routes success`() = runTest {
        val content = ConnectionContainer<Favourite>()
        doReturn(Result.success(content))
            .`when`(userRepository)
            .getCharacterFavourites(id = 1L, page = 2, perPage = KeyUtil.PAGING_LIMIT)
        val vm = CharacterFavouritesViewModel(userRepository = userRepository)
        assertTrue(vm.state.value is CharacterFavouritesViewModel.UiState.Loading)

        vm.load(userId = 1L, page = 2)

        val state = vm.state.value as CharacterFavouritesViewModel.UiState.Success
        assertSame(content, state.content)
        verify(userRepository).getCharacterFavourites(id = 1L, page = 2, perPage = KeyUtil.PAGING_LIMIT)
    }

    @Test
    fun `StaffFavouritesViewModel routes success through repository`() = runTest {
        val content = ConnectionContainer<Favourite>()
        doReturn(Result.success(content))
            .`when`(userRepository)
            .getStaffFavourites(id = 3L, page = 4, perPage = KeyUtil.PAGING_LIMIT)
        val vm = StaffFavouritesViewModel(userRepository = userRepository)

        vm.load(userId = 3L, page = 4)

        val state = vm.state.value as StaffFavouritesViewModel.UiState.Success
        assertSame(content, state.content)
        verify(userRepository).getStaffFavourites(id = 3L, page = 4, perPage = KeyUtil.PAGING_LIMIT)
    }

    @Test
    fun `StudioFavouritesViewModel routes failure through repository`() = runTest {
        doReturn(Result.failure<ConnectionContainer<Favourite>>(RuntimeException("Studio favourites failed")))
            .`when`(userRepository)
            .getStudioFavourites(id = 5L, page = 6, perPage = KeyUtil.PAGING_LIMIT)
        val vm = StudioFavouritesViewModel(userRepository = userRepository)

        vm.load(userId = 5L, page = 6)

        val state = vm.state.value as StudioFavouritesViewModel.UiState.Error
        assertEquals("Studio favourites failed", state.message)
    }

    @Test
    fun `MediaFavouritesViewModel routes anime branch through repository`() = runTest {
        val content = ConnectionContainer<Favourite>()
        doReturn(Result.success(content))
            .`when`(userRepository)
            .getAnimeFavourites(id = 7L, page = 8, perPage = KeyUtil.PAGING_LIMIT)
        val vm = MediaFavouritesViewModel(userRepository = userRepository)

        vm.load(userId = 7L, page = 8, mediaType = KeyUtil.ANIME)

        val state = vm.state.value as MediaFavouritesViewModel.UiState.Success
        assertSame(content, state.content)
        verify(userRepository).getAnimeFavourites(id = 7L, page = 8, perPage = KeyUtil.PAGING_LIMIT)
    }

    @Test
    fun `MediaFavouritesViewModel routes manga branch through repository`() = runTest {
        val content = ConnectionContainer<Favourite>()
        doReturn(Result.success(content))
            .`when`(userRepository)
            .getMangaFavourites(id = 9L, page = 10, perPage = KeyUtil.PAGING_LIMIT)
        val vm = MediaFavouritesViewModel(userRepository = userRepository)

        vm.load(userId = 9L, page = 10, mediaType = KeyUtil.MANGA)

        val state = vm.state.value as MediaFavouritesViewModel.UiState.Success
        assertSame(content, state.content)
        verify(userRepository).getMangaFavourites(id = 9L, page = 10, perPage = KeyUtil.PAGING_LIMIT)
    }
}
