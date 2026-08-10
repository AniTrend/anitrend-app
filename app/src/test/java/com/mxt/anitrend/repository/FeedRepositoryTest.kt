package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.graphql.generated.DeleteActivity
import com.mxt.anitrend.graphql.generated.DeleteActivityData
import com.mxt.anitrend.graphql.generated.FeedList
import com.mxt.anitrend.graphql.generated.FeedListData
import com.mxt.anitrend.graphql.generated.FeedListReply
import com.mxt.anitrend.graphql.generated.FeedListReplyData
import com.mxt.anitrend.graphql.generated.FeedMessage
import com.mxt.anitrend.graphql.generated.FeedMessageData
import com.mxt.anitrend.graphql.generated.SaveActivityReply
import com.mxt.anitrend.graphql.generated.SaveActivityReplyData
import com.mxt.anitrend.graphql.generated.SaveMessageActivity
import com.mxt.anitrend.graphql.generated.SaveMessageActivityData
import com.mxt.anitrend.graphql.generated.SaveTextActivity
import com.mxt.anitrend.graphql.generated.SaveTextActivityData
import com.mxt.anitrend.model.api.retro.anilist.FeedService
import com.mxt.anitrend.model.entity.container.body.PageContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Response
import com.mxt.anitrend.model.entity.anilist.FeedList as FeedListEntity

/**
 * Focused tests for the FeedRepository record surface (Lane C).
 *
 * Covers record store commits, legacy entity-typed return compatibility,
 * page ordering, save/delete convergence, and stale revision rejection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(FeedService::class.java)
    private val store = InMemoryFeedStore()
    private val repository = FeedRepository(
        feedService = service,
        ioDispatcher = testDispatcher,
        feedStore = store,
    )

    private val globalKey = FeedQueryKey(
        scope = FeedScope.GLOBAL,
        userId = null,
        mediaId = null,
        activityType = null,
        isFollowing = null,
        isMixed = null,
    )

    @Test
    fun `getFeedListRecords success commits FeedRecord page to store`() = runTest {
        val request = FeedList.request(page = 1, perPage = 10, id = null, isFollowing = null, userId = null, type = null, isMixed = null, asHtml = false)
        val response = feedListPageCall(page = 1, feeds = listOf(textActivity(1L), textActivity(2L)))
        `when`(service.getFeedList(request)).thenReturn(response)

        val result = repository.getFeedListRecords(
            page = 1,
            perPage = 10,
            queryKey = globalKey,
            readToken = 5L,
        )

        assertTrue(result.isSuccess)
        val page: FeedRecordPage = result.getOrThrow()
        assertEquals(listOf(1L, 2L), page.feeds.map { it.id })
        assertEquals(5L, page.feeds.first().revision)
        assertEquals(1, page.pageInfo?.currentPage)
        assertTrue(page.pageInfo?.hasNextPage == false)

        val state = store.state.value
        assertEquals(listOf(1L, 2L), state.queries.getValue(globalKey).orderedFeedIds)
        assertEquals(5L, state.feedsById.getValue(1L).revision)
        assertEquals(5L, state.queries.getValue(globalKey).token)
    }

    @Test
    fun `legacy getFeedList still returns entity typed page`() = runTest {
        val request = FeedList.request(page = 1, perPage = 10, id = null, isFollowing = null, userId = null, type = null, isMixed = null, asHtml = false)
        val response = feedListPageCall(page = 1, feeds = listOf(textActivity(1L), textActivity(2L)))
        `when`(service.getFeedList(request)).thenReturn(response)

        val result = repository.getFeedList(page = 1, perPage = 10)

        assertTrue(result.isSuccess)
        val page: PageContainer<FeedListEntity> = result.getOrThrow()
        assertEquals(listOf(1L, 2L), page.pageData.map { it.id })
    }

    @Test
    fun `record page loads preserve server ordering across pages`() = runTest {
        val firstPage = feedListPageCall(page = 1, feeds = listOf(textActivity(1L), textActivity(2L)))
        val secondPage = feedListPageCall(page = 2, feeds = listOf(textActivity(3L), textActivity(4L)))
        `when`(service.getFeedList(FeedList.request(page = 1, perPage = 10))).thenReturn(firstPage)
        `when`(service.getFeedList(FeedList.request(page = 2, perPage = 10))).thenReturn(secondPage)

        repository.getFeedListRecords(page = 1, perPage = 10, queryKey = globalKey, readToken = 1L)
        repository.getFeedListRecords(page = 2, perPage = 10, queryKey = globalKey, readToken = 1L)

        val snapshot = store.state.value.queries.getValue(globalKey)
        assertEquals(listOf(1L, 2L, 3L, 4L), snapshot.orderedFeedIds)
        assertEquals(setOf(1, 2), snapshot.loadedPages)
    }

    @Test
    fun `getFeedMessageRecords commits FeedRecord page to store`() = runTest {
        val request = FeedMessage.request(page = 1, perPage = 10, messengerId = 7, userId = null, asHtml = false)
        val response = feedMessagePageCall(page = 1, feeds = listOf(messageActivity(1L)))
        `when`(service.getFeedMessage(request)).thenReturn(response)
        val outboxKey = FeedQueryKey(
            scope = FeedScope.MESSAGE_OUTBOX,
            userId = 7,
            mediaId = null,
            activityType = null,
            isFollowing = null,
            isMixed = null,
        )

        val result = repository.getFeedMessageRecords(
            page = 1,
            perPage = 10,
            messengerId = 7L,
            queryKey = outboxKey,
            readToken = 2L,
        )

        assertTrue(result.isSuccess)
        assertEquals(listOf(1L), result.getOrThrow().feeds.map { it.id })
        assertEquals(listOf(1L), store.state.value.queries.getValue(outboxKey).orderedFeedIds)
    }

    @Test
    fun `getFeedListReplyRecords commits feed and reply records to store`() = runTest {
        val feed = feedDetailData(
            id = 5L,
            text = "detail",
            replyCount = 2,
            replies = listOf(replyData(10L, text = "first"), replyData(11L, text = "second")),
        )
        val request = FeedListReply.request(id = 5, asHtml = false)
        val response = success(GraphQLResponse(data = GraphQLData.Present(feed), errors = emptyList()))
        `when`(service.getFeedListReply(request)).thenReturn(response)

        val result = repository.getFeedListReplyRecords(id = 5L, readToken = 3L)

        assertTrue(result.isSuccess)
        val detail: FeedDetailResult = result.getOrThrow()
        assertEquals(5L, detail.feed.id)
        assertEquals(listOf(10L, 11L), detail.replies.map { it.id })

        val state = store.state.value
        assertEquals(3L, state.feedsById.getValue(5L).revision)
        assertEquals(3L, state.repliesById.getValue(10L).revision)
        assertEquals(listOf(10L, 11L), state.replyIdsByFeedId.getValue(5L))
    }

    @Test
    fun `saveTextActivityRecord commits FeedUpserted with revision`() = runTest {
        val request = SaveTextActivity.request(id = null, text = "hello", asHtml = false)
        val response = success(
            GraphQLResponse(
                data = GraphQLData.Present(saveTextActivityData(id = 1L, text = "hello")),
                errors = emptyList(),
            ),
        )
        `when`(service.saveTextActivity(request)).thenReturn(response)

        val result = repository.saveTextActivityRecord(text = "hello", revision = 7L)

        assertTrue(result.isSuccess)
        val record: FeedRecord = result.getOrThrow()
        assertEquals(1L, record.id)
        assertEquals("hello", record.text)
        assertEquals(7L, record.revision)
        assertEquals(7L, store.state.value.feedsById.getValue(1L).revision)
    }

    @Test
    fun `saveMessageActivityRecord commits FeedUpserted`() = runTest {
        val request = SaveMessageActivity.request(id = null, message = "hi", recipientId = 9, asHtml = false)
        val response = success(
            GraphQLResponse(
                data = GraphQLData.Present(saveMessageActivityData(id = 1L, message = "hi")),
                errors = emptyList(),
            ),
        )
        `when`(service.saveMessageActivity(request)).thenReturn(response)

        val result = repository.saveMessageActivityRecord(message = "hi", recipientId = 9L, revision = 2L)

        assertTrue(result.isSuccess)
        assertEquals("hi", store.state.value.feedsById.getValue(1L).text)
    }

    @Test
    fun `saveActivityReplyRecord commits ReplyUpserted under parent feed`() = runTest {
        seedFeed(id = 5L)
        val request = SaveActivityReply.request(id = null, activityId = 5, text = "thanks", asHtml = false)
        val response = success(
            GraphQLResponse(
                data = GraphQLData.Present(saveActivityReplyData(id = 10L, text = "thanks")),
                errors = emptyList(),
            ),
        )
        `when`(service.saveActivityReply(request)).thenReturn(response)

        val result = repository.saveActivityReplyRecord(activityId = 5L, text = "thanks", revision = 4L)

        assertTrue(result.isSuccess)
        val reply: FeedReplyRecord = result.getOrThrow()
        assertEquals(10L, reply.id)
        assertEquals(4L, reply.revision)
        assertEquals(4L, store.state.value.repliesById.getValue(10L).revision)
        assertEquals(5L, store.state.value.repliesById.getValue(10L).activityId)
    }

    @Test
    fun `deleteActivity commits FeedDeleted and converges store`() = runTest {
        val saveRequest = SaveTextActivity.request(id = null, text = "to delete", asHtml = false)
        val saveCall = success(
            GraphQLResponse(
                data = GraphQLData.Present(saveTextActivityData(id = 1L, text = "to delete")),
                errors = emptyList(),
            ),
        )
        `when`(service.saveTextActivity(saveRequest)).thenReturn(saveCall)
        repository.saveTextActivityRecord(text = "to delete", revision = 1L)
        assertTrue(store.state.value.feedsById.containsKey(1L))

        val deleteRequest = DeleteActivity.request(id = 1)
        val deleteCall = success(
            GraphQLResponse(
                data = GraphQLData.Present(DeleteActivityData(deleteActivity = DeleteActivityData.DeleteActivity(deleted = true))),
                errors = emptyList(),
            ),
        )
        `when`(service.deleteActivity(deleteRequest)).thenReturn(deleteCall)

        val result = repository.deleteActivity(id = 1L, revision = 2L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isDeleted)
        assertFalse(store.state.value.feedsById.containsKey(1L))
    }

    @Test
    fun `failed save is server authoritative and leaves store unchanged`() = runTest {
        val request = SaveTextActivity.request(id = null, text = "boom", asHtml = false)
        val response = success(GraphQLResponse<SaveTextActivityData>(data = GraphQLData.Absent, errors = emptyList()))
        `when`(service.saveTextActivity(request)).thenReturn(response)

        val result = repository.saveTextActivityRecord(text = "boom", revision = 1L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
        assertTrue(store.state.value.feedsById.isEmpty())
    }

    @Test
    fun `stale revision save is rejected by store`() = runTest {
        val newerRequest = SaveTextActivity.request(id = null, text = "newer", asHtml = false)
        val newerCall = success(
            GraphQLResponse(
                data = GraphQLData.Present(saveTextActivityData(id = 1L, text = "newer")),
                errors = emptyList(),
            ),
        )
        `when`(service.saveTextActivity(newerRequest)).thenReturn(newerCall)
        repository.saveTextActivityRecord(text = "newer", revision = 10L)

        val olderRequest = SaveTextActivity.request(id = null, text = "older", asHtml = false)
        val olderCall = success(
            GraphQLResponse(
                data = GraphQLData.Present(saveTextActivityData(id = 1L, text = "older")),
                errors = emptyList(),
            ),
        )
        `when`(service.saveTextActivity(olderRequest)).thenReturn(olderCall)
        repository.saveTextActivityRecord(text = "older", revision = 9L)

        val state = store.state.value
        assertEquals("newer", state.feedsById.getValue(1L).text)
        assertEquals(10L, state.feedsById.getValue(1L).revision)
    }

    @Test
    fun `stale page token is rejected by store`() = runTest {
        val freshCall = feedListPageCall(page = 1, feeds = listOf(textActivity(1L)))
        `when`(service.getFeedList(FeedList.request(page = 1, perPage = 10))).thenReturn(freshCall)
        repository.getFeedListRecords(page = 1, perPage = 10, queryKey = globalKey, readToken = 10L)

        val staleCall = feedListPageCall(page = 1, feeds = listOf(textActivity(1L, text = "older page")))
        `when`(service.getFeedList(FeedList.request(page = 1, perPage = 10))).thenReturn(staleCall)
        repository.getFeedListRecords(page = 1, perPage = 10, queryKey = globalKey, readToken = 5L)

        assertEquals(10L, store.state.value.queries.getValue(globalKey).token)
    }

    private suspend fun seedFeed(id: Long) {
        val feed = FeedRecord(
            id = id,
            type = "TEXT",
            status = "watched",
            text = "seed-$id",
            createdAt = id * 100,
            user = null,
            messenger = null,
            recipient = null,
            media = null,
            likes = emptyList(),
            replyCount = 0,
            siteUrl = null,
            revision = 0L,
        )
        store.apply(FeedStoreChange.FeedUpserted(feed))
    }

    private fun textActivity(id: Long, text: String = "feed-$id", replyCount: Int = 0): FeedListData.PageActivities.TextActivity = FeedListData.PageActivities.TextActivity(
        createdAt = 0,
        id = id.toInt(),
        isLocked = null,
        likes = null,
        replies = null,
        replyCount = replyCount,
        siteUrl = null,
        text = text,
        type = ActivityType.TEXT,
        user = null,
    )

    private fun messageActivity(id: Long, text: String = "feed-$id", replyCount: Int = 0): FeedMessageData.PageActivities.MessageActivity = FeedMessageData.PageActivities.MessageActivity(
        createdAt = 0,
        id = id.toInt(),
        isLocked = null,
        likes = null,
        message = text,
        messenger = null,
        recipient = null,
        replies = null,
        replyCount = replyCount,
        siteUrl = null,
        type = ActivityType.MESSAGE,
    )

    private fun feedDetailData(
        id: Long,
        text: String,
        replyCount: Int,
        replies: List<FeedListReplyData.TextActivityActivityReplies>,
    ): FeedListReplyData = FeedListReplyData(
        activity = FeedListReplyData.Activity.TextActivity(
            createdAt = 0,
            id = id.toInt(),
            isLocked = null,
            likes = null,
            replies = replies,
            replyCount = replyCount,
            siteUrl = null,
            text = text,
            type = ActivityType.TEXT,
            user = null,
        ),
    )

    private fun replyData(id: Long, text: String = "reply-$id"): FeedListReplyData.TextActivityActivityReplies = FeedListReplyData.TextActivityActivityReplies(
        createdAt = 0,
        id = id.toInt(),
        likes = null,
        text = text,
        user = null,
    )

    private fun saveTextActivityData(id: Long, text: String): SaveTextActivityData = SaveTextActivityData(
        saveTextActivity = SaveTextActivityData.SaveTextActivity(
            createdAt = 0,
            id = id.toInt(),
            replyCount = 0,
            text = text,
            type = ActivityType.TEXT,
        ),
    )

    private fun saveMessageActivityData(id: Long, message: String): SaveMessageActivityData = SaveMessageActivityData(
        saveMessageActivity = SaveMessageActivityData.SaveMessageActivity(
            createdAt = 0,
            id = id.toInt(),
            message = message,
            replyCount = 0,
            type = ActivityType.TEXT,
        ),
    )

    private fun saveActivityReplyData(id: Long, text: String): SaveActivityReplyData = SaveActivityReplyData(
        saveActivityReply = SaveActivityReplyData.SaveActivityReply(
            createdAt = 0,
            id = id.toInt(),
            text = text,
        ),
    )

    private fun feedListPageCall(
        page: Int,
        feeds: List<FeedListData.PageActivities>,
    ): Response<GraphQLResponse<FeedListData>> = success(
        GraphQLResponse(
            data = GraphQLData.Present(
                FeedListData(
                    page = FeedListData.Page(
                        activities = feeds,
                        pageInfo = FeedListData.PagePageInfo(
                            currentPage = page,
                            hasNextPage = false,
                            lastPage = 1,
                            perPage = 10,
                            total = feeds.size,
                        ),
                    ),
                ),
            ),
            errors = emptyList(),
        ),
    )

    private fun feedMessagePageCall(
        page: Int,
        feeds: List<FeedMessageData.PageActivities>,
    ): Response<GraphQLResponse<FeedMessageData>> = success(
        GraphQLResponse(
            data = GraphQLData.Present(
                FeedMessageData(
                    page = FeedMessageData.Page(
                        activities = feeds,
                        pageInfo = FeedMessageData.PagePageInfo(
                            currentPage = page,
                            hasNextPage = false,
                            lastPage = 1,
                            perPage = 10,
                            total = feeds.size,
                        ),
                    ),
                ),
            ),
            errors = emptyList(),
        ),
    )

    private fun <R> success(
        body: GraphQLResponse<R>,
    ): Response<GraphQLResponse<R>> = Response.success(body)
}
