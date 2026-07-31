package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.mapper.toFeedRecord
import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.domain.feed.interactor.DeleteFeedInteractor
import com.mxt.anitrend.domain.like.interactor.ToggleLikeInteractor
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.FeedRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class FeedListViewModelStoreObservationTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var feedRepository: FeedRepository
    private lateinit var baseRepository: BaseRepository
    private lateinit var store: InMemoryFeedStore
    private lateinit var registry: DefaultMutationRegistry
    private lateinit var viewModel: FeedListViewModel

    private val queryKey = FeedQueryKey(
        scope = FeedScope.GLOBAL,
        userId = null,
        mediaId = null,
        activityType = null,
        isFollowing = null,
        isMixed = null,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        feedRepository = mock(FeedRepository::class.java)
        baseRepository = mock(BaseRepository::class.java)
        store = InMemoryFeedStore()
        registry = DefaultMutationRegistry()

        val applicationScope = CoroutineScope(SupervisorJob() + dispatcher)
        val mutationExecutor = DefaultMutationExecutor(applicationScope = applicationScope, keyedMutex = KeyedMutex(applicationScope), mutationRegistry = registry, operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch())
        viewModel = FeedListViewModel(
            feedRepository = feedRepository,
            feedStore = store,
            mutationRegistry = registry,
            toggleLikeInteractor = ToggleLikeInteractor(baseRepository, mutationExecutor, store, RequestSequence()),
            deleteFeedInteractor = DeleteFeedInteractor(feedRepository, mutationExecutor, store, RequestSequence()),
            requestSequence = RequestSequence(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observe store query and derive accumulated items from store state`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        stubPage(page = 1, token = 1L, feeds = listOf(feed(1L), feed(2L)))
        stubPage(page = 2, token = 1L, feeds = listOf(feed(3L), feed(4L)))

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        viewModel.load(page = 2, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L, 4L), state.content.pageData.map { it.id })
        assertEquals(listOf(1L, 2L, 3L, 4L), state.items.map { it.id })
        assertEquals(setOf(1, 2), state.loadedPages)
        collector.cancel()
    }

    @Test
    fun `like from list updates store and view model state`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        stubPage(page = 1, token = 1L, feeds = listOf(feed(1L)))
        val likes = listOf(user(99L, "max"))
        doReturn(Result.success(likes))
            .`when`(baseRepository)
            .toggleLike(1L, LikeableType.ACTIVITY, false, null, 1L)

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        viewModel.toggleLike(1L)
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(1, state.items.first().likeCount)
        assertEquals(listOf(99L), store.state.value.feedsById.getValue(1L).likes.map { it.id })
        collector.cancel()
    }

    @Test
    fun `delete from list updates store and view model state`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        stubPage(page = 1, token = 1L, feeds = listOf(feed(1L), feed(2L)))
        doReturn(Result.success(DeleteState(isDeleted = true)))
            .`when`(feedRepository)
            .deleteActivity(2L, false, 1L)

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        viewModel.deleteFeed(2L)
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(listOf(1L), state.content.pageData.map { it.id })
        assertFalse(store.state.value.feedsById.containsKey(2L))
        collector.cancel()
    }

    private fun stubPage(
        page: Int,
        token: Long,
        feeds: List<FeedList>,
    ) {
        val content = PageContainer<FeedList>().apply { pageData = feeds }
        runBlocking {
            store.apply(
                FeedStoreChange.PageLoaded(
                    queryKey = queryKey,
                    page = page,
                    token = token,
                    feeds = feeds.map { it.toFeedRecord(revision = token) },
                    pageInfo = null,
                ),
            )

            doReturn(Result.success(content))
                .`when`(feedRepository)
                .getFeedList(
                    page = page,
                    perPage = 20,
                    id = null,
                    isFollowing = null,
                    userId = null,
                    type = null,
                    isMixed = null,
                    asHtml = false,
                    commitToStore = true,
                    queryKey = queryKey,
                    readToken = token,
                )
        }
    }

    private fun feed(id: Long): FeedList = FeedList(
        id = id,
        type = "TEXT",
        status = "watched",
        text = "feed-$id",
    )

    private fun user(id: Long, name: String): UserBase = UserBase(name = name).apply {
        this.id = id
    }
}
