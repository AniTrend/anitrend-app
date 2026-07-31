package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.fixture.MediaListFixtures.aMediaList
import com.mxt.anitrend.fixture.MediaListFixtures.anAiringMediaBase
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.api.retro.anilist.BrowseService
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class AiringListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var browseRepository: BrowseRepository
    private lateinit var mediaListStore: InMemoryMediaListStore

    private val queryKey = MediaListQueryKey(
        userId = 10L,
        userName = null,
        mediaType = MediaType.ANIME,
        statuses = setOf(MediaListStatus.CURRENT),
        sort = MediaListSort.UPDATED_TIME_DESC,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mediaListStore = InMemoryMediaListStore()
        browseRepository = spy(BrowseRepository(mock(BrowseService::class.java), testDispatcher, mediaListStore))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        val vm = AiringListViewModel(
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            mutationRegistry = DefaultMutationRegistry(),
            requestSequence = RequestSequence(),
        )

        assertTrue(vm.state.value is AiringListViewModel.UiState.Loading)
    }

    @Test
    fun `load emits store-backed airing items`() = runTest(testDispatcher) {
        val entry = aMediaList(
            id = 1,
            mediaId = 100,
            progress = 7,
            media = anAiringMediaBase(id = 100L),
        )
        doReturn(Result.success(pageContainer(entry)))
            .`when`(browseRepository)
            .getMediaListCollection(
                userId = 10L,
                type = MediaType.ANIME,
                forceSingleCompletedList = true,
                sort = listOf(MediaListSort.UPDATED_TIME_DESC),
                statusIn = listOf(MediaListStatus.CURRENT),
                scoreFormat = ScoreFormat.POINT_10,
                commitToStore = true,
                queryKey = queryKey,
                readToken = 1L,
            )

        val vm = AiringListViewModel(
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            mutationRegistry = DefaultMutationRegistry(),
            requestSequence = RequestSequence(),
        )
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(
            type = MediaType.ANIME,
            userId = 10,
            sort = MediaListSort.UPDATED_TIME_DESC.name,
            statusIn = MediaListStatus.CURRENT.name,
            scoreFormat = ScoreFormat.POINT_10,
        )
        mediaListStore.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = queryKey,
                token = 1L,
                entries = listOf(entry.toMediaListRecord(revision = 1L, ownerUserId = 10L)),
                pageInfo = null,
            ),
        )
        advanceUntilIdle()

        val state = vm.state.value as AiringListViewModel.UiState.Success
        assertEquals(1, state.items.size)
        assertEquals(7, state.items.single().progress)
        assertEquals(7, state.renderedItems.single().progress)
        verify(browseRepository).getMediaListCollection(
            userId = 10L,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.UPDATED_TIME_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_10,
            commitToStore = true,
            queryKey = queryKey,
            readToken = 1L,
        )
        collector.cancel()
    }

    @Test
    fun `store upsert and delete update airing state without repository mutation events`() = runTest(testDispatcher) {
        val entry = aMediaList(
            id = 1,
            mediaId = 100,
            progress = 4,
            media = anAiringMediaBase(id = 100L),
        )
        doReturn(Result.success(pageContainer(entry)))
            .`when`(browseRepository)
            .getMediaListCollection(
                userId = 10L,
                type = MediaType.ANIME,
                forceSingleCompletedList = true,
                sort = listOf(MediaListSort.UPDATED_TIME_DESC),
                statusIn = listOf(MediaListStatus.CURRENT),
                scoreFormat = ScoreFormat.POINT_100,
                commitToStore = true,
                queryKey = queryKey,
                readToken = 1L,
            )

        val vm = AiringListViewModel(
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            mutationRegistry = DefaultMutationRegistry(),
            requestSequence = RequestSequence(),
        )
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(
            type = MediaType.ANIME,
            userId = 10,
            sort = MediaListSort.UPDATED_TIME_DESC.name,
            statusIn = MediaListStatus.CURRENT.name,
            scoreFormat = null,
        )
        mediaListStore.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = queryKey,
                token = 1L,
                entries = listOf(entry.toMediaListRecord(revision = 1L, ownerUserId = 10L)),
                pageInfo = null,
            ),
        )
        mediaListStore.apply(
            MediaListStoreChange.EntryUpserted(
                aMediaList(
                    id = 1,
                    mediaId = 100,
                    progress = 8,
                    media = anAiringMediaBase(id = 100L),
                ).toMediaListRecord(revision = 2L, ownerUserId = 10L),
            ),
        )
        advanceUntilIdle()

        var state = vm.state.value as AiringListViewModel.UiState.Success
        assertEquals(8, state.items.single().progress)

        mediaListStore.apply(
            MediaListStoreChange.EntryDeleted(
                entryId = 1L,
                mediaId = 100L,
                revision = 3L,
            ),
        )
        advanceUntilIdle()

        state = vm.state.value as AiringListViewModel.UiState.Success
        assertTrue(state.items.isEmpty())
        assertTrue(state.isEmpty)
        collector.cancel()
    }

    @Test
    fun `load emits Error from repository failure`() = runTest(testDispatcher) {
        doReturn(Result.failure<PageContainer<MediaListCollection>>(RuntimeException("Airing failed")))
            .`when`(browseRepository)
            .getMediaListCollection(
                userId = 10L,
                type = MediaType.ANIME,
                forceSingleCompletedList = true,
                sort = null,
                statusIn = null,
                scoreFormat = ScoreFormat.POINT_100,
                commitToStore = true,
                queryKey = MediaListQueryKey(
                    userId = 10L,
                    userName = null,
                    mediaType = MediaType.ANIME,
                    statuses = emptySet(),
                    sort = null,
                ),
                readToken = 1L,
            )

        val vm = AiringListViewModel(
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            mutationRegistry = DefaultMutationRegistry(),
            requestSequence = RequestSequence(),
        )
        val collector = backgroundScope.launch { vm.state.collect {} }

        vm.load(type = MediaType.ANIME, userId = 10, sort = null, statusIn = null, scoreFormat = null)
        advanceUntilIdle()

        val state = vm.state.value as AiringListViewModel.UiState.Error
        assertEquals("Airing failed", state.message)
        collector.cancel()
    }

    private fun pageContainer(vararg entries: MediaList): PageContainer<MediaListCollection> = PageContainer<MediaListCollection>().apply {
        pageData = listOf(
            mock(MediaListCollection::class.java).apply {
                status = KeyUtil.CURRENT
                doReturn(entries.toList()).`when`(this).entries
            },
        )
    }
}
