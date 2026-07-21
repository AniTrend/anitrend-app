package com.mxt.anitrend.viewmodel

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.StudioMedia
import com.mxt.anitrend.model.api.retro.anilist.StudioModel
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.DataContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class StudioMediaViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var service: StudioModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        service = mock(StudioModel::class.java)
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
            studioService = service,
            ioDispatcher = testDispatcher,
        )
        assertTrue(vm.state.value is StudioMediaViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success on successful response`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>
        val pageContainer = PageContainer<MediaBase>().apply { pageData = emptyList() }
        val result = ConnectionContainer<PageContainer<MediaBase>>().apply { connection = pageContainer }
        val container = AniListContainer(data = DataContainer(result = result), errors = null)
        val request = StudioMedia.request(id = 1, page = 1, perPage = 50, sort = listOf(MediaSort.POPULARITY_DESC))

        `when`(service.getStudioMedia(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(container))

        val vm = StudioMediaViewModel(
            studioService = service,
            ioDispatcher = testDispatcher,
        )

        vm.load(studioId = 1L, page = 1, perPage = 50, sort = "POPULARITY_DESC")

        val state = vm.state.value as StudioMediaViewModel.UiState.Success
        assertEquals(result, state.container)
    }

    @Test
    fun `load emits Error on request failure`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>
        val request = StudioMedia.request(id = 1, page = 1, perPage = 50, sort = listOf(MediaSort.POPULARITY))

        `when`(service.getStudioMedia(request)).thenReturn(call)
        `when`(call.execute()).thenThrow(IOException("Network failed"))

        val vm = StudioMediaViewModel(
            studioService = service,
            ioDispatcher = testDispatcher,
        )

        vm.load(studioId = 1L, page = 1, perPage = 50, sort = null)

        val state = vm.state.value as StudioMediaViewModel.UiState.Error
        assertEquals("Network failed", state.message)
    }

    @Test
    fun `load emits Error on GraphQL errors`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>
        val error = mock(GraphError::class.java)
        val container = AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>(data = null, errors = listOf(error))
        val request = StudioMedia.request(id = 1, page = 1, perPage = 50, sort = listOf(MediaSort.POPULARITY))

        `when`(error.message).thenReturn("Studio media failed")
        `when`(service.getStudioMedia(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(container))

        val vm = StudioMediaViewModel(
            studioService = service,
            ioDispatcher = testDispatcher,
        )

        vm.load(studioId = 1L, page = 1, perPage = 50, sort = null)

        val state = vm.state.value as StudioMediaViewModel.UiState.Error
        assertEquals("Studio media failed", state.message)
    }

    @Test
    fun `resolveMediaSort falls back for null and invalid values`() = runTest {
        val vm = StudioMediaViewModel(
            studioService = service,
            ioDispatcher = testDispatcher,
        )

        assertEquals(listOf(MediaSort.POPULARITY), vm.resolveMediaSort(null))
        assertEquals(listOf(MediaSort.POPULARITY_DESC), vm.resolveMediaSort("POPULARITY_DESC"))
        assertEquals(listOf(MediaSort.POPULARITY), vm.resolveMediaSort("INVALID_SORT"))
    }
}
