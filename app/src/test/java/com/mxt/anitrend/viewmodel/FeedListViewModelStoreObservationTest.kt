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
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.graphql.generated.LikeableType
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
import org.junit.Assert.assertTrue
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
        assertEquals(listOf(1L, 2L, 3L, 4L), state.items.map { it.id })
        assertEquals(setOf(1, 2), state.loadedPages)
        collector.cancel()
    }

    @Test
    fun `active ui items are derived from canonical feed records`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        stubPage(page = 1, token = 1L, feeds = listOf(richFeed(1L)))

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        val item = state.items.single()
        assertEquals(1L, item.id)
        assertEquals("TEXT", item.type)
        assertEquals("watched feed-1 of: Romaji-1", item.headline.toString())
        assertEquals("feed-1", item.feedText)
        assertEquals("feed-1", item.body.toString())
        assertEquals(1000L, item.createdAt)
        assertEquals("user-1", item.userName)
        assertEquals(101L, item.userId)
        assertEquals("https://avatar-101", item.userAvatarUrl)
        assertEquals(2, item.likeCount)
        assertEquals(listOf(1L, 2L), item.likes.map { it.id })
        assertTrue(item.hasLikes)
        assertEquals(5, item.replyCount)
        assertEquals(1L, item.mediaId)
        assertEquals("ANIME", item.mediaType)
        assertEquals("English-1", item.mediaTitleEnglish)
        assertEquals("Original-1", item.mediaTitleOriginal)
        assertEquals("https://cover-1", item.mediaCoverImageUrl)
        collector.cancel()
    }

    @Test
    fun `upserted feed record propagates to items and compatibility content`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        stubPage(page = 1, token = 1L, feeds = listOf(feed(1L)))

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        store.apply(
            FeedStoreChange.FeedUpserted(
                feed = feed(1L).toFeedRecord(revision = 2L).copy(text = "edited"),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals("edited", state.items.single().feedText)
        collector.cancel()
    }

    @Test
    fun `store like replacement propagates to items and compatibility content`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        stubPage(page = 1, token = 1L, feeds = listOf(richFeed(1L)))

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        store.apply(
            FeedStoreChange.FeedLikesReplaced(
                feedId = 1L,
                likes = listOf(
                    UserSummaryRecord(id = 9L, name = "liker", avatar = null, siteUrl = null),
                ),
                revision = 2L,
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(1, state.items.single().likeCount)
        assertEquals(listOf(9L), state.items.single().likes.map { it.id })
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
        assertEquals(listOf(1L), state.items.map { it.id })
        assertFalse(store.state.value.feedsById.containsKey(2L))
        collector.cancel()
    }

    @Test
    fun `current user id flows into ownership and liked state projections`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        stubPage(page = 1, token = 1L, feeds = listOf(richFeed(1L)))

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null, currentUserId = 101L)
        advanceUntilIdle()

        val owned = viewModel.state.value as FeedListViewModel.UiState.Success
        assertTrue(owned.items.single().canEdit)
        assertTrue(owned.items.single().canDelete)
        assertFalse(owned.items.single().isLikedByCurrentUser)

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null, currentUserId = 1L)
        advanceUntilIdle()

        val liked = viewModel.state.value as FeedListViewModel.UiState.Success
        assertTrue(liked.items.single().isLikedByCurrentUser)
        assertFalse(liked.items.single().canEdit)
        collector.cancel()
    }

    @Test
    fun `success carries page info record for pagination termination`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val pageInfo = PageInfoRecord(
            currentPage = 1,
            lastPage = 3,
            perPage = 20,
            total = 60,
            hasNextPage = true,
            hasPreviousPage = false,
        )
        stubPage(page = 1, token = 1L, feeds = listOf(feed(1L)), pageInfo = pageInfo)

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(pageInfo, state.pageInfo)
        collector.cancel()
    }

    private fun stubPage(
        page: Int,
        token: Long,
        feeds: List<FeedList>,
        pageInfo: PageInfoRecord? = null,
    ) {
        val content = PageContainer<FeedList>().apply { pageData = feeds }
        runBlocking {
            store.apply(
                FeedStoreChange.PageLoaded(
                    queryKey = queryKey,
                    page = page,
                    token = token,
                    feeds = feeds.map { it.toFeedRecord(revision = token) },
                    pageInfo = pageInfo,
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

    private fun richFeed(id: Long): FeedList = FeedList(
        id = id,
        replyCount = 5,
        type = "TEXT",
        status = "watched",
        text = "feed-$id",
        createdAt = id * 1000L,
        user = userWithAvatar(id + 100, "user-$id"),
        media = media(id),
        likes = listOf(userWithAvatar(1L, "liker-a"), userWithAvatar(2L, "liker-b")),
        siteUrl = "https://anilist.co/activity/$id",
    )

    private fun media(id: Long): MediaBase = MediaBase().also {
        it.id = id
        it.title = MediaTitle("Romaji-$id", "English-$id", "Original-$id", "Preferred-$id")
        it.coverImage = ImageBase(extraLarge = "https://cover-$id", large = null, medium = null)
        it.type = "ANIME"
    }

    private fun user(id: Long, name: String): UserBase = UserBase(name = name).apply {
        this.id = id
    }

    private fun userWithAvatar(id: Long, name: String): UserBase = UserBase(name = name).also {
        it.id = id
        it.avatar = ImageBase(extraLarge = null, large = "https://avatar-$id", medium = null)
    }
}
