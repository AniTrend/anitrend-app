package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.feed.FeedStoreState
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedStoreClearTest {
    private val queryKey = FeedQueryKey(
        scope = FeedScope.GLOBAL,
        userId = null,
        mediaId = null,
        activityType = null,
        isFollowing = true,
        isMixed = false,
    )

    @Test
    fun `given populated store when clear called then state and deletion revisions reset`() = runTest {
        val store = InMemoryFeedStore()
        val feed = createFeed(id = 1L, revision = 1L)
        val reply = createReply(id = 10L, activityId = 1L, revision = 1L)

        store.apply(
            FeedStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 1L,
                feeds = listOf(feed),
                pageInfo = createPageInfo(1),
            ),
        )
        store.apply(FeedStoreChange.ReplyUpserted(feedId = 1L, reply = reply))
        store.apply(FeedStoreChange.FeedDeleted(feedId = 1L, revision = 5L))

        store.clear()

        assertEquals(FeedStoreState(), store.state.value)

        store.apply(FeedStoreChange.FeedUpserted(feed))

        assertTrue(store.state.value.feedsById.containsKey(1L))
    }

    private fun createFeed(
        id: Long,
        revision: Long,
    ): FeedRecord = FeedRecord(
        id = id,
        type = "TEXT",
        status = "watched",
        text = "feed-$id",
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
        lastPage = 1,
        perPage = 10,
        total = 10,
        hasNextPage = false,
        hasPreviousPage = false,
    )
}
