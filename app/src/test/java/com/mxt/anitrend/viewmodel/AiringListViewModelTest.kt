package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.api.retro.anilist.BrowseService
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.anilist.meta.CustomList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
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
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class AiringListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var browseRepository: BrowseRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        browseRepository = spy(BrowseRepository(mock(BrowseService::class.java), testDispatcher))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = AiringListViewModel(browseRepository = browseRepository)
        assertTrue(vm.state.value is AiringListViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success from repository result`() = runTest {
        val content = PageContainer<MediaListCollection>()
        doReturn(Result.success(content))
            .`when`(browseRepository)
            .getMediaListCollection(
                userId = 10L,
                type = MediaType.ANIME,
                forceSingleCompletedList = true,
                sort = listOf(MediaListSort.UPDATED_TIME_DESC),
                statusIn = listOf(MediaListStatus.CURRENT),
                scoreFormat = ScoreFormat.POINT_10,
            )
        val vm = AiringListViewModel(browseRepository = browseRepository)

        vm.load(
            type = MediaType.ANIME,
            userId = 10,
            sort = "UPDATED_TIME_DESC",
            statusIn = "CURRENT",
            scoreFormat = ScoreFormat.POINT_10,
        )

        val state = vm.state.value as AiringListViewModel.UiState.Success
        assertSame(content, state.content)
        verify(browseRepository).getMediaListCollection(
            userId = 10L,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.UPDATED_TIME_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_10,
        )
    }

    @Test
    fun `load falls back for invalid enums and null score format`() = runTest {
        val content = PageContainer<MediaListCollection>()
        doReturn(Result.success(content))
            .`when`(browseRepository)
            .getMediaListCollection(
                userId = 11L,
                type = MediaType.MANGA,
                forceSingleCompletedList = true,
                sort = null,
                statusIn = null,
                scoreFormat = ScoreFormat.POINT_100,
            )
        val vm = AiringListViewModel(browseRepository = browseRepository)

        vm.load(
            type = MediaType.MANGA,
            userId = 11,
            sort = "INVALID_SORT",
            statusIn = "INVALID_STATUS",
            scoreFormat = null,
        )

        val state = vm.state.value as AiringListViewModel.UiState.Success
        assertSame(content, state.content)
        verify(browseRepository).getMediaListCollection(
            userId = 11L,
            type = MediaType.MANGA,
            forceSingleCompletedList = true,
            sort = null,
            statusIn = null,
            scoreFormat = ScoreFormat.POINT_100,
        )
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<PageContainer<MediaListCollection>>(RuntimeException("Airing failed")))
            .`when`(browseRepository)
            .getMediaListCollection(
                userId = 10L,
                type = MediaType.ANIME,
                forceSingleCompletedList = true,
                sort = null,
                statusIn = null,
                scoreFormat = ScoreFormat.POINT_100,
            )
        val vm = AiringListViewModel(browseRepository = browseRepository)

        vm.load(type = MediaType.ANIME, userId = 10, sort = null, statusIn = null, scoreFormat = null)

        val state = vm.state.value as AiringListViewModel.UiState.Error
        assertEquals("Airing failed", state.message)
    }

    @Test
    fun `saved media list event patches airing content without reload`() = runTest {
        val initialEntry = MediaList().apply {
            id = 1L
            mediaId = 100L
            status = KeyUtil.CURRENT
            media.id = 100L
            media.type = KeyUtil.ANIME
            media.status = KeyUtil.RELEASING
        }
        val savedEntry = MediaList().apply {
            id = 1L
            mediaId = 100L
            status = KeyUtil.CURRENT
            progress = 7
            customLists = listOf(CustomList(name = "Watch Party", isEnabled = true))
            media.id = 100L
            media.type = KeyUtil.ANIME
            media.status = KeyUtil.RELEASING
        }
        doReturn(Result.success(pageContainer(initialEntry)))
            .`when`(browseRepository)
            .getMediaListCollection(
                userId = 10L,
                type = MediaType.ANIME,
                forceSingleCompletedList = true,
                sort = listOf(MediaListSort.UPDATED_TIME_DESC),
                statusIn = listOf(MediaListStatus.CURRENT),
                scoreFormat = ScoreFormat.POINT_10,
            )

        val vm = AiringListViewModel(browseRepository = browseRepository)

        vm.load(
            type = MediaType.ANIME,
            userId = 10,
            sort = "UPDATED_TIME_DESC",
            statusIn = "CURRENT",
            scoreFormat = ScoreFormat.POINT_10,
        )
        advanceUntilIdle()
        vm.onMediaListSaved(savedEntry)

        val state = vm.state.value as AiringListViewModel.UiState.Success
        val updatedEntry = state.content.pageData.single().entries.orEmpty().single()
        assertEquals(7, updatedEntry.progress)
        assertEquals("Watch Party", updatedEntry.customLists?.single()?.name)
        verify(browseRepository).getMediaListCollection(
            userId = 10L,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.UPDATED_TIME_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_10,
        )
    }

    private fun pageContainer(entry: MediaList): PageContainer<MediaListCollection> =
        PageContainer<MediaListCollection>().apply {
            pageData = listOf(
                newMediaListCollection(
                    status = KeyUtil.CURRENT,
                    entries = listOf(entry),
                ),
            )
        }

    private fun newMediaListCollection(
        status: String,
        entries: List<MediaList>,
    ): MediaListCollection =
        mock(MediaListCollection::class.java).apply {
            this.status = status
            doReturn(entries).`when`(this).entries
        }
}
