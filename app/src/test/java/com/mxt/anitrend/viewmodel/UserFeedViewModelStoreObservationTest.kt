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
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
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
class UserFeedViewModelStoreObservationTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var feedRepository: FeedRepository
    private lateinit var baseRepository: BaseRepository
    private lateinit var store: InMemoryFeedStore
    private lateinit var registry: DefaultMutationRegistry
    private lateinit var viewModel: UserFeedViewModel

    private val queryKey = FeedQueryKey(
        scope = FeedScope.USER,
        userId = 42L,
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
        viewModel = UserFeedViewModel(
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
    fun `user feed observes user query and derives record-backed items across pages`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        stubPage(page = 1, token = 1L, feeds = listOf(feed(1L), feed(2L)))
        stubPage(page = 2, token = 1L, feeds = listOf(feed(3L)))

        viewModel.load(userId = 42, page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        viewModel.load(userId = 42, page = 2, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        val state = viewModel.state.value as UserFeedViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L), state.items.map { it.id })
        assertEquals(setOf(1, 2), state.loadedPages)
        assertEquals("user-1", state.items.first().userName)
        assertEquals(101L, state.items.first().userId)
        assertEquals("https://avatar-101", state.items.first().userAvatarUrl)
        assertEquals("watched feed-1 of: Romaji-1", state.items.first().headline.toString())
        assertEquals(2, state.items.first().likeCount)
        assertEquals(5, state.items.first().replyCount)
        collector.cancel()
    }

    @Test
    fun `upserted feed record propagates to user feed items and content`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        stubPage(page = 1, token = 1L, feeds = listOf(feed(1L)))

        viewModel.load(userId = 42, page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        store.apply(
            FeedStoreChange.FeedUpserted(
                feed = feed(1L).toFeedRecord(revision = 2L).copy(text = "edited"),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as UserFeedViewModel.UiState.Success
        assertEquals("edited", state.items.single().feedText)
        collector.cancel()
    }

    @Test
    fun `delete from user feed converges store and view model state`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        stubPage(page = 1, token = 1L, feeds = listOf(feed(1L), feed(2L)))
        doReturn(Result.success(DeleteState(isDeleted = true)))
            .`when`(feedRepository)
            .deleteActivity(2L, false, 1L)

        viewModel.load(userId = 42, page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        viewModel.deleteFeed(2L)
        advanceUntilIdle()

        val state = viewModel.state.value as UserFeedViewModel.UiState.Success
        assertEquals(listOf(1L), state.items.map { it.id })
        assertFalse(store.state.value.feedsById.containsKey(2L))
        collector.cancel()
    }

    @Test
    fun `load ignores invalid user ids and keeps loading state`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        viewModel.load(userId = null, page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        viewModel.load(userId = 0, page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        assertEquals(UserFeedViewModel.UiState.Loading, viewModel.state.value)
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
                    userId = 42L,
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
        replyCount = 5,
        type = "TEXT",
        status = "watched",
        text = "feed-$id",
        createdAt = id * 1000L,
        user = user(id + 100, "user-$id"),
        media = MediaBase().also {
            it.id = id
            it.title = MediaTitle("Romaji-$id", "English-$id", "Original-$id", "Preferred-$id")
        },
        likes = listOf(user(1L, "liker-a"), user(2L, "liker-b")),
    )

    private fun user(id: Long, name: String): UserBase = UserBase(name = name).also {
        it.id = id
        it.avatar = ImageBase(extraLarge = null, large = "https://avatar-$id", medium = null)
    }
}
