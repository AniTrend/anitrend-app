package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.mapper.toFeedRecord
import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.domain.feed.interactor.DeleteFeedInteractor
import com.mxt.anitrend.domain.like.interactor.ToggleLikeInteractor
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.FeedRepository
import com.mxt.anitrend.repository.MediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SecondaryFeedViewModelStoreObservationTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var feedRepository: FeedRepository
    private lateinit var mediaRepository: MediaRepository
    private lateinit var store: InMemoryFeedStore
    private lateinit var mutationRegistry: DefaultMutationRegistry
    private lateinit var toggleLikeInteractor: ToggleLikeInteractor
    private lateinit var deleteFeedInteractor: DeleteFeedInteractor

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        feedRepository = mock(FeedRepository::class.java)
        mediaRepository = mock(MediaRepository::class.java)
        store = InMemoryFeedStore()
        mutationRegistry = DefaultMutationRegistry()
        toggleLikeInteractor = mock(ToggleLikeInteractor::class.java)
        deleteFeedInteractor = mock(DeleteFeedInteractor::class.java)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `message feed view model observes inbox query from feed store`() = runTest {
        val queryKey = FeedQueryKey(
            scope = FeedScope.MESSAGE_INBOX,
            userId = 42L,
            mediaId = null,
            activityType = null,
            isFollowing = null,
            isMixed = null,
        )
        val viewModel = MessageFeedViewModel(
            feedRepository = feedRepository,
            feedStore = store,
            mutationRegistry = mutationRegistry,
            toggleLikeInteractor = toggleLikeInteractor,
            deleteFeedInteractor = deleteFeedInteractor,
            requestSequence = RequestSequence(),
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        stubMessagePage(queryKey = queryKey, page = 1, token = 1L, feeds = listOf(feed(1L), feed(2L)))
        stubMessagePage(queryKey = queryKey, page = 2, token = 1L, feeds = listOf(feed(3L), feed(4L)))

        viewModel.load(userId = 42L, page = 1, pageLimit = 20, messageType = 0)
        viewModel.load(userId = 42L, page = 2, pageLimit = 20, messageType = 0)
        advanceUntilIdle()

        val state = viewModel.state.value as MessageFeedViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L, 4L), state.items.map { it.id })
        assertEquals(setOf(1, 2), state.loadedPages)
        assertEquals("user-1", state.items.first().userName)
        assertEquals(101L, state.items.first().userId)
        assertEquals("https://avatar-101", state.items.first().userAvatarUrl)
        assertEquals("sent", state.items.first().headline.toString())
        assertEquals(2, state.items.first().likeCount)
        assertEquals(5, state.items.first().replyCount)
        // Recipient identity used by the message edit path is carried by the ui model.
        assertEquals(301L, state.items.first().recipientId)
        assertEquals("recipient-1", state.items.first().recipientName)
        collector.cancel()
    }

    @Test
    fun `media feed view model observes media query from feed store`() = runTest {
        val queryKey = FeedQueryKey(
            scope = FeedScope.MEDIA,
            userId = null,
            mediaId = 10L,
            activityType = null,
            isFollowing = true,
            isMixed = null,
        )
        val viewModel = MediaFeedViewModel(
            mediaRepository = mediaRepository,
            feedStore = store,
            mutationRegistry = mutationRegistry,
            toggleLikeInteractor = toggleLikeInteractor,
            deleteFeedInteractor = deleteFeedInteractor,
            requestSequence = RequestSequence(),
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        stubMediaPage(queryKey = queryKey, page = 1, token = 1L, feeds = listOf(feed(10L), feed(11L)))
        stubMediaPage(queryKey = queryKey, page = 2, token = 1L, feeds = listOf(feed(12L)))

        viewModel.load(mediaId = 10L, isFollowing = true, page = 1, pageLimit = 20)
        viewModel.load(mediaId = 10L, isFollowing = true, page = 2, pageLimit = 20)
        advanceUntilIdle()

        val state = viewModel.state.value as MediaFeedViewModel.UiState.Success
        assertEquals(listOf(10L, 11L, 12L), state.items.map { it.id })
        assertEquals(setOf(1, 2), state.loadedPages)
        assertEquals("user-10", state.items.first().userName)
        assertEquals(110L, state.items.first().userId)
        assertEquals("https://avatar-110", state.items.first().userAvatarUrl)
        assertEquals("sent", state.items.first().headline.toString())
        assertEquals(2, state.items.first().likeCount)
        assertEquals(5, state.items.first().replyCount)
        collector.cancel()
    }

    @Test
    fun `media feed reflects store like replacement in items and content`() = runTest {
        val queryKey = FeedQueryKey(
            scope = FeedScope.MEDIA,
            userId = null,
            mediaId = 10L,
            activityType = null,
            isFollowing = true,
            isMixed = null,
        )
        val viewModel = MediaFeedViewModel(
            mediaRepository = mediaRepository,
            feedStore = store,
            mutationRegistry = mutationRegistry,
            toggleLikeInteractor = toggleLikeInteractor,
            deleteFeedInteractor = deleteFeedInteractor,
            requestSequence = RequestSequence(),
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        stubMediaPage(queryKey = queryKey, page = 1, token = 1L, feeds = listOf(feed(10L)))
        viewModel.load(mediaId = 10L, isFollowing = true, page = 1, pageLimit = 20)
        advanceUntilIdle()

        store.apply(
            FeedStoreChange.FeedLikesReplaced(
                feedId = 10L,
                likes = listOf(
                    UserSummaryRecord(id = 9L, name = "liker", avatar = null, siteUrl = null),
                ),
                revision = 2L,
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as MediaFeedViewModel.UiState.Success
        assertEquals(1, state.items.single().likeCount)
        assertEquals(listOf(9L), state.items.single().likes.map { it.id })
        collector.cancel()
    }

    private fun stubMessagePage(
        queryKey: FeedQueryKey,
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
                .getFeedMessage(
                    page = page,
                    perPage = 20,
                    messengerId = null,
                    userId = 42L,
                    asHtml = false,
                    commitToStore = true,
                    queryKey = queryKey,
                    readToken = token,
                )
        }
    }

    private fun stubMediaPage(
        queryKey: FeedQueryKey,
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
                .`when`(mediaRepository)
                .getMediaSocial(
                    mediaId = 10L,
                    isFollowing = true,
                    page = page,
                    perPage = 20,
                    commitToStore = true,
                    queryKey = queryKey,
                    readToken = token,
                )
        }
    }

    private fun feed(id: Long): FeedList = FeedList(
        id = id,
        replyCount = 5,
        type = "MESSAGE",
        status = "sent",
        text = "feed-$id",
        createdAt = id * 1000L,
        user = user(id + 100, "user-$id"),
        messenger = user(id + 200, "messenger-$id"),
        recipient = user(id + 300, "recipient-$id"),
        likes = listOf(user(1L, "liker-a"), user(2L, "liker-b")),
    )

    private fun user(id: Long, name: String): UserBase = UserBase(name = name).also {
        it.id = id
        it.avatar = ImageBase(extraLarge = null, large = "https://avatar-$id", medium = null)
    }
}
