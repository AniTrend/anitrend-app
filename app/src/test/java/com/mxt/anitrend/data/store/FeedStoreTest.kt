package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedStoreTest {
    private val queryKey = FeedQueryKey(
        scope = FeedScope.GLOBAL,
        userId = null,
        mediaId = null,
        activityType = null,
        isFollowing = true,
        isMixed = false,
    )

    @Test
    fun `store upsert is deterministic`() = runTest {
        val store = InMemoryFeedStore()
        val feed = createFeed(id = 1L, revision = 1L)

        store.apply(FeedStoreChange.FeedUpserted(feed))
        val firstState = store.state.value

        store.apply(FeedStoreChange.FeedUpserted(feed))
        val secondState = store.state.value

        assertEquals(firstState, secondState)
    }

    @Test
    fun `store delete removes all references`() = runTest {
        val store = InMemoryFeedStore()
        val feed = createFeed(id = 1L, revision = 1L)
        val reply = createReply(id = 20L, activityId = 1L, revision = 1L)

        store.apply(FeedStoreChange.PageLoaded(queryKey, page = 1, generation = 1, feeds = listOf(feed), pageInfo = createPageInfo(1)))
        store.apply(FeedStoreChange.ReplyUpserted(feedId = 1L, reply = reply))
        store.apply(FeedStoreChange.FeedDeleted(feedId = 1L, revision = 2L))

        val state = store.state.value
        assertFalse(state.feedsById.containsKey(1L))
        assertFalse(state.repliesById.containsKey(20L))
        assertFalse(state.replyIdsByFeedId.containsKey(1L))
        assertFalse(state.queries.getValue(queryKey).orderedFeedIds.contains(1L))
    }

    @Test
    fun `reply upsert atomically changes reply count`() = runTest {
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeed(id = 1L, revision = 1L)))

        store.apply(FeedStoreChange.ReplyUpserted(feedId = 1L, reply = createReply(id = 10L, activityId = 1L, revision = 2L)))

        val state = store.state.value
        assertEquals(1, state.feedsById.getValue(1L).replyCount)
        assertEquals(listOf(10L), state.replyIdsByFeedId.getValue(1L))
        assertTrue(state.repliesById.containsKey(10L))
    }

    @Test
    fun `reply delete atomically changes reply count`() = runTest {
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeed(id = 1L, revision = 1L)))
        store.apply(FeedStoreChange.ReplyUpserted(feedId = 1L, reply = createReply(id = 10L, activityId = 1L, revision = 2L)))

        store.apply(FeedStoreChange.ReplyDeleted(feedId = 1L, replyId = 10L, revision = 3L))

        val state = store.state.value
        assertEquals(0, state.feedsById.getValue(1L).replyCount)
        assertFalse(state.replyIdsByFeedId.containsKey(1L))
        assertFalse(state.repliesById.containsKey(10L))
    }

    @Test
    fun `query page merge preserves order`() = runTest {
        val store = InMemoryFeedStore()
        store.apply(
            FeedStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                generation = 1,
                feeds = listOf(createFeed(1L, 1L), createFeed(2L, 1L)),
                pageInfo = createPageInfo(1),
            ),
        )

        store.apply(
            FeedStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 2,
                generation = 1,
                feeds = listOf(createFeed(3L, 1L), createFeed(4L, 1L)),
                pageInfo = createPageInfo(2),
            ),
        )

        assertEquals(listOf(1L, 2L, 3L, 4L), store.state.value.queries.getValue(queryKey).orderedFeedIds)
    }

    @Test
    fun `duplicate page ids are removed`() = runTest {
        val store = InMemoryFeedStore()
        store.apply(
            FeedStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                generation = 1,
                feeds = listOf(createFeed(1L, 1L), createFeed(2L, 1L)),
                pageInfo = createPageInfo(1),
            ),
        )

        store.apply(
            FeedStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 2,
                generation = 1,
                feeds = listOf(createFeed(2L, 2L), createFeed(3L, 1L)),
                pageInfo = createPageInfo(2),
            ),
        )

        assertEquals(listOf(1L, 2L, 3L), store.state.value.queries.getValue(queryKey).orderedFeedIds)
    }

    @Test
    fun `stale revisions are rejected`() = runTest {
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeed(id = 1L, revision = 5L, text = "new")))

        store.apply(FeedStoreChange.FeedUpserted(createFeed(id = 1L, revision = 4L, text = "old")))
        store.apply(FeedStoreChange.FeedLikesReplaced(feedId = 1L, likes = listOf(createUser(1L)), revision = 3L))

        val state = store.state.value
        assertEquals("new", state.feedsById.getValue(1L).text)
        assertTrue(state.feedsById.getValue(1L).likes.isEmpty())
    }

    @Test
    fun `reply likes replaced updates likes and rejects stale revision`() = runTest {
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeed(id = 1L, revision = 1L)))
        store.apply(FeedStoreChange.ReplyUpserted(feedId = 1L, reply = createReply(id = 10L, activityId = 1L, revision = 2L)))

        val newLikes = listOf(UserSummaryRecord(id = 99L, name = "user99", avatar = null, siteUrl = null))
        store.apply(FeedStoreChange.ReplyLikesReplaced(feedId = 1L, replyId = 10L, likes = newLikes, revision = 3L))

        val state = store.state.value
        assertEquals(newLikes, state.repliesById.getValue(10L).likes)
        assertEquals(3L, state.repliesById.getValue(10L).revision)

        val staleLikes = listOf(UserSummaryRecord(id = 88L, name = "user88", avatar = null, siteUrl = null))
        store.apply(FeedStoreChange.ReplyLikesReplaced(feedId = 1L, replyId = 10L, likes = staleLikes, revision = 2L))

        assertEquals(newLikes, store.state.value.repliesById.getValue(10L).likes)
        assertEquals(3L, store.state.value.repliesById.getValue(10L).revision)
    }

    @Test
    fun `two concurrent updates cannot produce an invalid store`() = runTest(StandardTestDispatcher()) {
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeed(id = 1L, revision = 1L)))

        val jobs = listOf(
            async { store.apply(FeedStoreChange.ReplyUpserted(feedId = 1L, reply = createReply(id = 10L, activityId = 1L, revision = 2L))) },
            async { store.apply(FeedStoreChange.ReplyUpserted(feedId = 1L, reply = createReply(id = 11L, activityId = 1L, revision = 3L))) },
            async { store.apply(FeedStoreChange.FeedLikesReplaced(feedId = 1L, likes = listOf(createUser(1L), createUser(2L)), revision = 4L)) },
        )
        jobs.awaitAll()

        val state = store.state.value
        val replyIds = state.replyIdsByFeedId.getValue(1L)
        assertEquals(replyIds.size, replyIds.distinct().size)
        assertEquals(2, state.feedsById.getValue(1L).replyCount)
        assertEquals(2, state.repliesById.size)
        replyIds.forEach { replyId ->
            assertTrue(state.repliesById.containsKey(replyId))
        }
    }

    private fun createFeed(
        id: Long,
        revision: Long,
        text: String = "feed-$id",
    ): FeedRecord = FeedRecord(
        id = id,
        type = "TEXT",
        status = "watched",
        text = text,
        createdAt = id * 100,
        user = createUser(id),
        messenger = null,
        recipient = null,
        media = null,
        likes = emptyList(),
        replyCount = 0,
        siteUrl = "https://feed/$id",
        revision = revision,
    )

    private fun createReply(
        id: Long,
        activityId: Long,
        revision: Long,
    ): FeedReplyRecord = FeedReplyRecord(
        id = id,
        activityId = activityId,
        reply = "reply-$id",
        createdAt = id * 10,
        user = createUser(id),
        likes = emptyList(),
        revision = revision,
    )

    private fun createUser(id: Long): UserSummaryRecord = UserSummaryRecord(
        id = id,
        name = "user-$id",
        avatar = null,
        siteUrl = null,
    )

    private fun createPageInfo(currentPage: Int): PageInfoRecord = PageInfoRecord(
        currentPage = currentPage,
        lastPage = 3,
        perPage = 10,
        total = 30,
        hasNextPage = currentPage < 3,
        hasPreviousPage = currentPage > 1,
    )
}
