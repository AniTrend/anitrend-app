package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.domain.model.MediaListItemUiModel
import com.mxt.anitrend.fixture.MediaListFixtures.aMediaList
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.api.retro.anilist.BrowseService
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy

@OptIn(ExperimentalCoroutinesApi::class)
class MediaListViewModelImmutableStateTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mediaListStore: InMemoryMediaListStore
    private lateinit var browseRepository: BrowseRepository
    private lateinit var userRepository: UserRepository
    private lateinit var settings: Settings

    private val queryKey = MediaListQueryKey(
        userId = 42L,
        userName = null,
        mediaType = MediaType.ANIME,
        statuses = setOf(MediaListStatus.CURRENT),
        sort = MediaListSort.PROGRESS_DESC,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mediaListStore = InMemoryMediaListStore()
        browseRepository = spy(BrowseRepository(mock(BrowseService::class.java), testDispatcher, mediaListStore))
        userRepository = mock(UserRepository::class.java)
        settings = mock(Settings::class.java)

        doReturn(KeyUtil.PROGRESS).`when`(settings).mediaListSort
        doReturn(KeyUtil.DESC).`when`(settings).sortOrder
        doReturn(true).`when`(settings).isAuthenticated
        doReturn(
            User().apply {
                id = 42L
                name = "max"
                mediaListOptions.scoreFormat = ScoreFormat.POINT_100.name
            },
        ).`when`(userRepository).cachedCurrentUser
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `success state exposes immutable domain snapshots only`() = runTest(testDispatcher) {
        val entry = aMediaList(id = 1L, mediaId = 100L, progress = 5)
        doReturn(Result.success(pageContainer(entry))).`when`(browseRepository).getMediaListCollection(
            userId = 42L,
            userName = null,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.PROGRESS_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_100,
            commitToStore = true,
            queryKey = queryKey,
            readToken = 1L,
        )

        val viewModel = createViewModel()
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        viewModel.load(userId = 42L, userName = null, mediaType = KeyUtil.ANIME, statusIn = KeyUtil.CURRENT)
        mediaListStore.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = queryKey,
                token = 1L,
                entries = listOf(entry.toMediaListRecord(revision = 1L, ownerUserId = 42L, ownerUserName = "max")),
                pageInfo = null,
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as MediaListViewModel.UiState.Success
        assertTrue(state.entries.isNotEmpty())
        assertTrue(state.entries.all { it is MediaListRecord })
        assertTrue(state.renderedItems.all { it is MediaListItemUiModel })
        assertEquals(1, state.renderedItems.size)
        assertEquals(5, state.renderedItems.single().progress)
        assertNull(state.pageInfo)
        collector.cancel()
    }

    @Test
    fun `previously emitted snapshot is not mutated by later store upserts`() = runTest(testDispatcher) {
        val entry = aMediaList(id = 1L, mediaId = 100L, progress = 5)
        doReturn(Result.success(pageContainer(entry))).`when`(browseRepository).getMediaListCollection(
            userId = 42L,
            userName = null,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.PROGRESS_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_100,
            commitToStore = true,
            queryKey = queryKey,
            readToken = 1L,
        )

        val viewModel = createViewModel()
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        viewModel.load(userId = 42L, userName = null, mediaType = KeyUtil.ANIME, statusIn = KeyUtil.CURRENT)
        mediaListStore.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = queryKey,
                token = 1L,
                entries = listOf(entry.toMediaListRecord(revision = 1L, ownerUserId = 42L, ownerUserName = "max")),
                pageInfo = null,
            ),
        )
        advanceUntilIdle()

        val firstState = viewModel.state.value as MediaListViewModel.UiState.Success
        val firstSnapshot = firstState.renderedItems.single()
        val firstEntry = firstState.entries.single()

        mediaListStore.apply(
            MediaListStoreChange.EntryUpserted(
                aMediaList(id = 1L, mediaId = 100L, progress = 9).toMediaListRecord(
                    revision = 2L,
                    ownerUserId = 42L,
                    ownerUserName = "max",
                ),
            ),
        )
        advanceUntilIdle()

        val secondState = viewModel.state.value as MediaListViewModel.UiState.Success
        assertEquals(9, secondState.renderedItems.single().progress)
        assertEquals(9, secondState.entries.single().progress)

        // Previously emitted snapshots are distinct, immutable objects and are never mutated in place.
        assertEquals(5, firstSnapshot.progress)
        assertEquals(5, firstEntry.progress)
        collector.cancel()
    }

    private fun createViewModel(): MediaListViewModel = MediaListViewModel(
        browseRepository = browseRepository,
        mediaListStore = mediaListStore,
        mutationRegistry = DefaultMutationRegistry(),
        userRepository = userRepository,
        settings = settings,
        requestSequence = RequestSequence(),
        ioDispatcher = testDispatcher,
    )

    private fun pageContainer(vararg entries: MediaList): PageContainer<MediaListCollection> = PageContainer<MediaListCollection>().apply {
        pageData = listOf(
            mock(MediaListCollection::class.java).apply {
                status = KeyUtil.CURRENT
                doReturn(entries.toList()).`when`(this).entries
            },
        )
    }
}
