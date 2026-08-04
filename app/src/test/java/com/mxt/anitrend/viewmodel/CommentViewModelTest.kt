package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.mapper.toFeedRecord
import com.mxt.anitrend.data.mapper.toFeedReplyRecord
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.domain.feed.interactor.DeleteFeedInteractor
import com.mxt.anitrend.domain.feed.interactor.DeleteReplyInteractor
import com.mxt.anitrend.domain.feed.interactor.SaveFeedInteractor
import com.mxt.anitrend.domain.feed.interactor.SaveReplyInteractor
import com.mxt.anitrend.domain.like.interactor.ToggleLikeInteractor
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.toFeedItemUiModel
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.FeedDetailResult
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
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class CommentViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var feedRepository: FeedRepository
    private lateinit var baseRepository: BaseRepository
    private lateinit var store: InMemoryFeedStore
    private lateinit var registry: DefaultMutationRegistry
    private lateinit var viewModel: CommentViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        feedRepository = mock(FeedRepository::class.java)
        baseRepository = mock(BaseRepository::class.java)
        store = InMemoryFeedStore()
        registry = DefaultMutationRegistry()

        val applicationScope = CoroutineScope(SupervisorJob() + dispatcher)
        val mutationExecutor = DefaultMutationExecutor(applicationScope = applicationScope, keyedMutex = KeyedMutex(applicationScope), mutationRegistry = registry, operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch())
        viewModel = CommentViewModel(
            feedStore = store,
            feedRepository = feedRepository,
            mutationRegistry = registry,
            toggleLikeInteractor = ToggleLikeInteractor(baseRepository, mutationExecutor, store, RequestSequence()),
            saveReplyInteractor = SaveReplyInteractor(feedRepository, mutationExecutor, store, RequestSequence()),
            deleteReplyInteractor = DeleteReplyInteractor(feedRepository, mutationExecutor, store, RequestSequence()),
            deleteFeedInteractor = DeleteFeedInteractor(feedRepository, mutationExecutor, store, RequestSequence()),
            saveFeedInteractor = SaveFeedInteractor(feedRepository, mutationExecutor, store, RequestSequence()),
            requestSequence = RequestSequence(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load feed detail from store backed repository hydration`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 1)
        val reply = reply(10L, activityId = 1L, text = "first")
        stubDetailLoad(feed, listOf(reply))

        viewModel.load(1L)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1L, state.feed?.id)
        assertEquals(listOf(10L), state.replies.map { it.id })
        collector.cancel()
    }

    @Test
    fun `state feed is a FeedRecord and feedItem is the canonical projection`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 1)
        val reply = reply(10L, activityId = 1L, text = "first")
        stubDetailLoad(feed, listOf(reply))

        viewModel.load(1L)
        advanceUntilIdle()

        val state = viewModel.state.value
        val record = requireNotNull(state.feed)
        assertEquals("feed-1", record.text)
        // The header projection is precomputed from the store record.
        val expected =
            record.toFeedItemUiModel(
                isLikePending = false,
                isDeletePending = false,
                currentUserId = null,
            )
        assertEquals(expected, state.feedItem)
        assertTrue(state.feedItem is FeedItemUiModel)
        collector.cancel()
    }

    @Test
    fun `active state carries no legacy entity reverse mapping`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 1)
        val reply = reply(10L, activityId = 1L, text = "first")
        stubDetailLoad(feed, listOf(reply))

        viewModel.load(1L)
        advanceUntilIdle()

        val state = viewModel.state.value
        // State surface is record-backed: the feed field is the exact store
        // record, not a reverse-mapped legacy FeedList entity.
        assertEquals(store.state.value.feedsById.getValue(1L), state.feed)
        assertFalse(state.replies.isEmpty())
        assertEquals(listOf(10L), state.replies.map { it.id })
        assertEquals("first", state.replies.first().reply)
        collector.cancel()
    }

    @Test
    fun `canonical feed item precomputes like state from current user id`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 0).apply {
            likes = listOf(userWithAvatar(99L, "max"), userWithAvatar(5L, "other"))
        }
        stubDetailLoad(feed, emptyList())

        viewModel.load(1L, currentUserId = 99L)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.feedItem?.isLikedByCurrentUser == true)
        assertEquals(2, state.feedItem?.likeCount)
        assertEquals(listOf(99L, 5L), state.feedItem?.likes?.map { it.id })
        collector.cancel()
    }

    @Test
    fun `reply like state is precomputed from current user id`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 1)
        val reply = reply(10L, activityId = 1L, text = "first").apply {
            likes = listOf(userWithAvatar(99L, "max"), userWithAvatar(5L, "other"))
        }
        stubDetailLoad(feed, listOf(reply))

        viewModel.load(1L, currentUserId = 99L)
        advanceUntilIdle()

        val replyModel = viewModel.state.value.replies.first()
        assertTrue(replyModel.isLikedByCurrentUser)
        assertEquals(2, replyModel.likeCount)

        // A different current user renders the same row as not liked.
        doReturn(
            Result.success(
                FeedDetailResult(
                    feed = feed.toFeedRecord(revision = 2L),
                    replies = listOf(reply.toFeedReplyRecord(activityId = 1L, revision = 2L)),
                ),
            ),
        )
            .`when`(feedRepository)
            .getFeedListReplyRecords(1L, false, true, 0L, 2L)
        viewModel.load(1L, currentUserId = 7L)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.replies.first().isLikedByCurrentUser)
        collector.cancel()
    }

    @Test
    fun `reply ordering follows store order and new replies append`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 2)
        val first = reply(10L, activityId = 1L, text = "first")
        val second = reply(11L, activityId = 1L, text = "second")
        stubDetailLoad(feed, listOf(first, second))

        viewModel.load(1L)
        advanceUntilIdle()
        assertEquals(listOf(10L, 11L), viewModel.state.value.replies.map { it.id })

        doReturn(Result.success(reply(12L, activityId = 1L, text = "new reply")))
            .`when`(feedRepository)
            .saveActivityReply(null, 1L, "new reply", false, false, 1L)

        val result = viewModel.submitReply(feedId = 1L, text = "new reply")
        advanceUntilIdle()

        assertEquals(MutationResult.Success, result)
        assertEquals(listOf(10L, 11L, 12L), viewModel.state.value.replies.map { it.id })
        collector.cancel()
    }

    @Test
    fun `submit reply commits store update`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 0)
        stubDetailLoad(feed, emptyList())
        doReturn(Result.success(reply(11L, activityId = 1L, text = "new reply")))
            .`when`(feedRepository)
            .saveActivityReply(null, 1L, "new reply", false, false, 1L)

        viewModel.load(1L)
        advanceUntilIdle()

        val result = viewModel.submitReply(feedId = 1L, text = "new reply")
        advanceUntilIdle()

        assertEquals(MutationResult.Success, result)
        assertEquals(listOf(11L), store.state.value.replyIdsByFeedId.getValue(1L))
        assertEquals(1, store.state.value.feedsById.getValue(1L).replyCount)
        collector.cancel()
    }

    @Test
    fun `delete reply commits store update and converges state`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 1)
        val reply = reply(10L, activityId = 1L, text = "reply")
        stubDetailLoad(feed, listOf(reply))
        doReturn(Result.success(DeleteState(isDeleted = true)))
            .`when`(feedRepository)
            .deleteActivityReply(10L, 1L, false, 1L)

        viewModel.load(1L)
        advanceUntilIdle()

        val result = viewModel.deleteReply(10L)
        advanceUntilIdle()

        assertEquals(MutationResult.Success, result)
        assertFalse(store.state.value.repliesById.containsKey(10L))
        assertEquals(0, store.state.value.feedsById.getValue(1L).replyCount)
        assertTrue(viewModel.state.value.replies.isEmpty())
        collector.cancel()
    }

    @Test
    fun `like reply commits store update and converges like state`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 1)
        val reply = reply(10L, activityId = 1L, text = "reply")
        stubDetailLoad(feed, listOf(reply))
        val likes = listOf(user(77L, "liked"))
        doReturn(Result.success(likes))
            .`when`(baseRepository)
            .toggleLike(10L, LikeableType.ACTIVITY_REPLY, false, 1L, 1L)

        viewModel.load(1L, currentUserId = 77L)
        advanceUntilIdle()

        val result = viewModel.toggleReplyLike(10L)
        advanceUntilIdle()

        assertEquals(MutationResult.Success, result)
        assertEquals(listOf(77L), store.state.value.repliesById.getValue(10L).likes.map { it.id })
        val replyModel = viewModel.state.value.replies.first()
        assertEquals(1, replyModel.likeCount)
        assertTrue(replyModel.isLikedByCurrentUser)
        collector.cancel()
    }

    @Test
    fun `delete feed commits store update`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 0)
        stubDetailLoad(feed, emptyList())
        doReturn(Result.success(DeleteState(isDeleted = true)))
            .`when`(feedRepository)
            .deleteActivity(1L, false, 1L)

        viewModel.load(1L)
        advanceUntilIdle()

        val result = viewModel.deleteFeed(1L)
        advanceUntilIdle()

        assertEquals(MutationResult.Success, result)
        assertTrue(viewModel.state.value.isDeleted)
        assertFalse(store.state.value.feedsById.containsKey(1L))
        collector.cancel()
    }

    @Test
    fun `detail response with fresh request token is not rejected as stale after revision 1 feed list commit`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 1)
        val reply = reply(10L, activityId = 1L, text = "first")

        // A feed list query previously committed this feed at revision 1 (the
        // FeedListViewModel RequestSequence token). A detail response stamped
        // with the default token 0 is rejected by the store staleness guard,
        // which is the reply loading bug under test.
        runBlocking {
            store.apply(FeedStoreChange.FeedUpserted(feed = feed.toFeedRecord(revision = 1L)))
        }

        // The comment detail commit carries a fresh RequestSequence token (1L)
        // and must not be rejected as stale over the existing revision 1 feed.
        runBlocking {
            store.apply(
                FeedStoreChange.FeedDetailLoaded(
                    feed = feed.toFeedRecord(revision = 1L),
                    replies = listOf(reply.toFeedReplyRecord(activityId = feed.id, revision = 1L)),
                ),
            )
        }
        assertEquals(1L, store.state.value.feedsById.getValue(1L).revision)
        assertEquals(listOf(10L), store.state.value.replyIdsByFeedId.getValue(1L))

        // The ViewModel must request the detail with that same fresh token so
        // a real repository commit carries revision >= 1 and clears the guard.
        doReturn(
            Result.success(
                FeedDetailResult(
                    feed = feed.toFeedRecord(revision = 1L),
                    replies = listOf(reply.toFeedReplyRecord(activityId = feed.id, revision = 1L)),
                ),
            ),
        )
            .`when`(feedRepository)
            .getFeedListReplyRecords(1L, false, true, 0L, 1L)

        viewModel.load(1L)
        advanceUntilIdle()

        verify(feedRepository).getFeedListReplyRecords(1L, false, true, 0L, 1L)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(listOf(10L), viewModel.state.value.replies.map { it.id })
        collector.cancel()
    }

    @Test
    fun `refresh issues a fresh request token instead of being suppressed`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 1)
        stubDetailLoad(feed, listOf(reply(10L, activityId = 1L, text = "first")))

        viewModel.load(1L)
        advanceUntilIdle()

        doReturn(
            Result.success(
                FeedDetailResult(
                    feed = feed.toFeedRecord(revision = 2L),
                    replies = listOf(reply(11L, activityId = 1L, text = "second").toFeedReplyRecord(activityId = 1L, revision = 2L)),
                ),
            ),
        )
            .`when`(feedRepository)
            .getFeedListReplyRecords(1L, false, true, 0L, 2L)

        viewModel.load(1L)
        advanceUntilIdle()

        // Pull-to-refresh is not gated behind isLoading: a second load issues
        // a new request token, and the newer token is used for the request.
        verify(feedRepository).getFeedListReplyRecords(1L, false, true, 0L, 1L)
        verify(feedRepository).getFeedListReplyRecords(1L, false, true, 0L, 2L)
        assertFalse(viewModel.state.value.isLoading)
        collector.cancel()
    }

    private fun stubDetailLoad(feed: FeedList, replies: List<FeedReply>) {
        runBlocking {
            store.apply(
                FeedStoreChange.FeedDetailLoaded(
                    feed = feed.toFeedRecord(revision = 1L),
                    replies = replies.map { it.toFeedReplyRecord(activityId = feed.id, revision = 1L) },
                ),
            )

            doReturn(
                Result.success(
                    FeedDetailResult(
                        feed = feed.toFeedRecord(revision = 1L),
                        replies = replies.map { it.toFeedReplyRecord(activityId = feed.id, revision = 1L) },
                    ),
                ),
            )
                .`when`(feedRepository)
                .getFeedListReplyRecords(1L, false, true, 0L, 1L)
        }
    }

    private fun feed(id: Long, replyCount: Int): FeedList = FeedList(
        id = id,
        type = "TEXT",
        status = "watched",
        text = "feed-$id",
        replyCount = replyCount,
    )

    private fun reply(id: Long, activityId: Long, text: String): FeedReply = FeedReply(
        id = id,
        text = text,
    ).apply {
        user = user(id, "user-$id")
    }

    private fun user(id: Long, name: String): UserBase = UserBase(name = name).apply {
        this.id = id
    }

    private fun userWithAvatar(id: Long, name: String): UserBase = UserBase(name = name).also {
        it.id = id
        it.avatar = ImageBase(extraLarge = null, large = "https://avatar-$id", medium = null)
    }
}
