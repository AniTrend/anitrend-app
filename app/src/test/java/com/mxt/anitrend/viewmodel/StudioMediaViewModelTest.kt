package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.StudioRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class StudioMediaViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var studioRepository: StudioRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        studioRepository = mock(StudioRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(
            StudioMediaViewModel.UiState.Loading,
            StudioMediaViewModel.UiState.Loading,
        )
    }

    @Test
    fun `UiState Success wraps a container`() {
        val pageContainer = PageContainer<MediaBase>()
        pageContainer.pageData = emptyList()
        val container = ConnectionContainer<PageContainer<MediaBase>>().apply {
            connection = pageContainer
        }
        val state = StudioMediaViewModel.UiState.Success(container)
        assertEquals(container, state.container)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = StudioMediaViewModel.UiState.Error("Failed")
        assertEquals("Failed", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = StudioMediaViewModel(
            studioRepository = studioRepository,
        )
        assertTrue(vm.state.value is StudioMediaViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val pageContainer = PageContainer<MediaBase>()
        val container = ConnectionContainer<PageContainer<MediaBase>>().apply {
            connection = pageContainer
        }
        doReturn(Result.success(container))
            .`when`(studioRepository)
            .getStudioMedia(id = 1L, page = 1, perPage = 50, sort = listOf(MediaSort.POPULARITY_DESC))

        val vm = StudioMediaViewModel(
            studioRepository = studioRepository,
        )

        vm.load(studioId = 1L, page = 1, perPage = 50, sort = "POPULARITY_DESC")

        val state = vm.state.value as StudioMediaViewModel.UiState.Success
        assertSame(container, state.container)
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<ConnectionContainer<PageContainer<MediaBase>>>(RuntimeException("Network failed")))
            .`when`(studioRepository)
            .getStudioMedia(id = 1L, page = 1, perPage = 50, sort = listOf(MediaSort.POPULARITY))

        val vm = StudioMediaViewModel(
            studioRepository = studioRepository,
        )

        vm.load(studioId = 1L, page = 1, perPage = 50, sort = null)

        val state = vm.state.value as StudioMediaViewModel.UiState.Error
        assertEquals("Network failed", state.message)
    }

    @Test
    fun `resolveMediaSort falls back for null and invalid values`() = runTest {
        val vm = StudioMediaViewModel(
            studioRepository = studioRepository,
        )

        assertEquals(listOf(MediaSort.POPULARITY), vm.resolveMediaSort(null))
        assertEquals(listOf(MediaSort.POPULARITY_DESC), vm.resolveMediaSort("POPULARITY_DESC"))
        assertEquals(listOf(MediaSort.POPULARITY), vm.resolveMediaSort("INVALID_SORT"))
    }
}
