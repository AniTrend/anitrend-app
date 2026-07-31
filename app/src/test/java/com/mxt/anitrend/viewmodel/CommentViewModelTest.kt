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
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.base.UserBase
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
    fun `delete reply commits store update`() = runTest {
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
        collector.cancel()
    }

    @Test
    fun `like reply commits store update`() = runTest {
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        val feed = feed(1L, replyCount = 1)
        val reply = reply(10L, activityId = 1L, text = "reply")
        stubDetailLoad(feed, listOf(reply))
        val likes = listOf(user(77L, "liked"))
        doReturn(Result.success(likes))
            .`when`(baseRepository)
            .toggleLike(10L, LikeableType.ACTIVITY_REPLY, false, 1L, 1L)

        viewModel.load(1L)
        advanceUntilIdle()

        val result = viewModel.toggleReplyLike(10L)
        advanceUntilIdle()

        assertEquals(MutationResult.Success, result)
        assertEquals(listOf(77L), store.state.value.repliesById.getValue(10L).likes.map { it.id })
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

    private fun stubDetailLoad(feed: FeedList, replies: List<FeedReply>) {
        runBlocking {
            store.apply(
                FeedStoreChange.FeedDetailLoaded(
                    feed = feed.toFeedRecord(revision = 0L),
                    replies = replies.map { it.toFeedReplyRecord(activityId = feed.id, revision = 0L) },
                ),
            )

            doReturn(Result.success(feed.apply { this.replies = replies }))
                .`when`(feedRepository)
                .getFeedListReply(1L, false, true, 0L)
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
}
