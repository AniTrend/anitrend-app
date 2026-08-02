package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.domain.medialist.model.MediaListCollectionPageResult
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.fixture.MediaListFixtures.aMediaList
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.api.retro.anilist.BrowseService
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.MediaBase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class MediaListViewModelStoreObservationTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var browseRepository: BrowseRepository
    private lateinit var mediaListStore: InMemoryMediaListStore
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
    fun `observe store query emits rendered items from canonical store`() = runTest(testDispatcher) {
        val entry = mediaListEntity(id = 1L, mediaId = 100L, progress = 5)
        doReturn(Result.success(collectionResult(entry))).`when`(browseRepository).getMediaListCollection(
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

        val viewModel = MediaListViewModel(
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            mutationRegistry = DefaultMutationRegistry(),
            userRepository = userRepository,
            settings = settings,
            requestSequence = RequestSequence(),
            ioDispatcher = testDispatcher,
        )
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
        assertEquals(1, state.renderedItems.size)
        assertEquals(5, state.renderedItems.single().progress)
        assertEquals(5, state.entries.single().progress)
        verify(browseRepository).getMediaListCollection(
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
        collector.cancel()
    }

    @Test
    fun `success state exposes store pageInfo as immutable PageInfoRecord`() = runTest(testDispatcher) {
        val entry = aMediaList(id = 1L, mediaId = 100L, progress = 5)
        val pageInfo = PageInfoRecord(
            currentPage = 1,
            lastPage = 2,
            perPage = 25,
            total = 42,
            hasNextPage = true,
            hasPreviousPage = false,
        )
        doReturn(Result.success(collectionResult(entry))).`when`(browseRepository).getMediaListCollection(
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

        val viewModel = MediaListViewModel(
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            mutationRegistry = DefaultMutationRegistry(),
            userRepository = userRepository,
            settings = settings,
            requestSequence = RequestSequence(),
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        viewModel.load(userId = 42L, userName = null, mediaType = KeyUtil.ANIME, statusIn = KeyUtil.CURRENT)
        mediaListStore.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = queryKey,
                token = 1L,
                entries = listOf(entry.toMediaListRecord(revision = 1L, ownerUserId = 42L, ownerUserName = "max")),
                pageInfo = pageInfo,
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as MediaListViewModel.UiState.Success
        assertTrue(state.pageInfo is PageInfoRecord)
        assertEquals(pageInfo, state.pageInfo)
        assertEquals(2, state.pageInfo?.lastPage)
        assertEquals(42, state.pageInfo?.total)
        assertEquals(true, state.pageInfo?.hasNextPage)
        collector.cancel()
    }

    @Test
    fun `entry upsert updates rendered state without repository mutation events`() = runTest(testDispatcher) {
        val entry = aMediaList(id = 1L, mediaId = 100L, progress = 5)
        val updatedEntry = aMediaList(id = 1L, mediaId = 100L, progress = 9)
        doReturn(Result.success(collectionResult(entry))).`when`(browseRepository).getMediaListCollection(
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

        val viewModel = MediaListViewModel(
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            mutationRegistry = DefaultMutationRegistry(),
            userRepository = userRepository,
            settings = settings,
            requestSequence = RequestSequence(),
            ioDispatcher = testDispatcher,
        )
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
        mediaListStore.apply(
            MediaListStoreChange.EntryUpserted(
                updatedEntry.toMediaListRecord(revision = 2L, ownerUserId = 42L, ownerUserName = "max"),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as MediaListViewModel.UiState.Success
        assertEquals(9, state.renderedItems.single().progress)
        assertEquals(9, state.entries.single().progress)
        collector.cancel()
    }

    @Test
    fun `entry delete and status changes update query membership from store`() = runTest(testDispatcher) {
        val entry = aMediaList(id = 1L, mediaId = 100L, progress = 5)
        doReturn(Result.success(collectionResult(entry))).`when`(browseRepository).getMediaListCollection(
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

        val viewModel = MediaListViewModel(
            browseRepository = browseRepository,
            mediaListStore = mediaListStore,
            mutationRegistry = DefaultMutationRegistry(),
            userRepository = userRepository,
            settings = settings,
            requestSequence = RequestSequence(),
            ioDispatcher = testDispatcher,
        )
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
        mediaListStore.apply(
            MediaListStoreChange.EntryUpserted(
                aMediaList(id = 1L, mediaId = 100L, status = KeyUtil.COMPLETED, progress = 5).toMediaListRecord(
                    revision = 2L,
                    ownerUserId = 42L,
                    ownerUserName = "max",
                ),
            ),
        )
        advanceUntilIdle()

        var state = viewModel.state.value as MediaListViewModel.UiState.Success
        assertTrue(state.renderedItems.isEmpty())

        mediaListStore.apply(
            MediaListStoreChange.EntryUpserted(
                aMediaList(id = 1L, mediaId = 100L, status = KeyUtil.CURRENT, progress = 5).toMediaListRecord(
                    revision = 3L,
                    ownerUserId = 42L,
                    ownerUserName = "max",
                ),
            ),
        )
        advanceUntilIdle()

        state = viewModel.state.value as MediaListViewModel.UiState.Success
        assertEquals(1, state.renderedItems.size)

        mediaListStore.apply(
            MediaListStoreChange.EntryDeleted(
                entryId = 1L,
                mediaId = 100L,
                revision = 4L,
            ),
        )
        advanceUntilIdle()

        state = viewModel.state.value as MediaListViewModel.UiState.Success
        assertTrue(state.renderedItems.isEmpty())
        assertTrue(state.isEmpty)
        collector.cancel()
    }

    @Test
    fun `stale failure does not overwrite newer successful load`() = runTest(testDispatcher) {
        val localBrowseRepository = mock(BrowseRepository::class.java)
        val firstStarted = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val secondStarted = CountDownLatch(1)
        val entry = aMediaList(id = 1L, mediaId = 100L, progress = 5)

        doReturn(KeyUtil.PROGRESS).`when`(settings).mediaListSort
        doReturn(KeyUtil.DESC).`when`(settings).sortOrder

        org.mockito.Mockito.doAnswer {
            firstStarted.countDown()
            releaseFirst.await(5, TimeUnit.SECONDS)
            Result.failure<MediaListCollectionPageResult>(RuntimeException("stale failure"))
        }
            .`when`(localBrowseRepository)
            .getMediaListCollection(
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
        org.mockito.Mockito.doAnswer {
            secondStarted.countDown()
            Result.success(collectionResult(entry))
        }
            .`when`(localBrowseRepository)
            .getMediaListCollection(
                userId = 42L,
                userName = null,
                type = MediaType.ANIME,
                forceSingleCompletedList = true,
                sort = listOf(MediaListSort.PROGRESS_DESC),
                statusIn = listOf(MediaListStatus.CURRENT),
                scoreFormat = ScoreFormat.POINT_100,
                commitToStore = true,
                queryKey = queryKey,
                readToken = 2L,
            )

        val viewModel = MediaListViewModel(
            browseRepository = localBrowseRepository,
            mediaListStore = mediaListStore,
            mutationRegistry = DefaultMutationRegistry(),
            userRepository = userRepository,
            settings = settings,
            requestSequence = RequestSequence(),
            ioDispatcher = Dispatchers.IO,
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        viewModel.load(userId = 42L, userName = null, mediaType = KeyUtil.ANIME, statusIn = KeyUtil.CURRENT)
        viewModel.load(userId = 42L, userName = null, mediaType = KeyUtil.ANIME, statusIn = KeyUtil.CURRENT)
        assertTrue(secondStarted.await(5, TimeUnit.SECONDS))

        mediaListStore.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = queryKey,
                token = 2L,
                entries = listOf(entry.toMediaListRecord(revision = 2L, ownerUserId = 42L, ownerUserName = "max")),
                pageInfo = null,
            ),
        )
        advanceUntilIdle()

        releaseFirst.countDown()
        advanceUntilIdle()

        // The first load's failure continuation resumes from the real IO dispatcher, so
        // wait for the store-backed Success state to settle instead of reading it once.
        val state = awaitSuccess(viewModel)
        assertEquals(1, state.renderedItems.size)
        assertEquals(5, state.renderedItems.single().progress)
        collector.cancel()
    }

    private fun awaitSuccess(
        viewModel: MediaListViewModel,
        timeoutMillis: Long = 5_000,
    ): MediaListViewModel.UiState.Success {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            when (val current = viewModel.state.value) {
                is MediaListViewModel.UiState.Success -> return current
                is MediaListViewModel.UiState.Error ->
                    throw AssertionError("Expected Success, got Error: ${current.message}")
                else -> Thread.sleep(10)
            }
        }
        throw AssertionError("Timed out waiting for Success state")
    }

    private fun collectionResult(vararg entries: MediaList): MediaListCollectionPageResult = MediaListCollectionPageResult(
        entries = entries.map { it.toMediaListRecord(revision = 1L, ownerUserId = 42L, ownerUserName = "max") },
    )

    private fun mediaListEntity(
        id: Long,
        mediaId: Long,
        progress: Int,
    ): MediaList = MediaList().apply {
        this.id = id
        this.mediaId = mediaId
        this.status = KeyUtil.CURRENT
        this.progress = progress
        this.score = 8f
        this.media = MediaBase().apply {
            this.id = mediaId
            this.type = KeyUtil.ANIME
            this.episodes = 12
            this.status = KeyUtil.RELEASING
        }
    }
}
