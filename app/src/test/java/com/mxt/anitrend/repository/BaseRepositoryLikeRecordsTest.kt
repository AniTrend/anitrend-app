package com.mxt.anitrend.repository

import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.graphql.generated.ToggleLike
import com.mxt.anitrend.model.api.retro.anilist.BaseService
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.DataContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Call
import retrofit2.Response

/**
 * Focused tests for the BaseRepository like record surface (Lane C).
 *
 * Covers like record store commits, legacy entity-typed return compatibility,
 * reply like convergence, and stale revision rejection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BaseRepositoryLikeRecordsTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(BaseService::class.java)
    private val boxQuery = mock(BoxQuery::class.java)
    private val store = InMemoryFeedStore()
    private val repository = BaseRepository(
        baseService = service,
        boxQuery = boxQuery,
        ioDispatcher = testDispatcher,
        feedStore = store,
    )

    @Test
    fun `toggleLikeRecords for activity commits FeedLikesReplaced records`() = runTest {
        store.apply(FeedStoreChange.FeedUpserted(createFeedRecord(id = 1L, revision = 1L)))
        val request = ToggleLike.request(id = 1, type = LikeableType.ACTIVITY)
        stubLikeResponse(request, listOf(userEntity(99L, "max"), userEntity(98L, "jane")))

        val result = repository.toggleLikeRecords(
            id = 1L,
            type = LikeableType.ACTIVITY,
            revision = 2L,
        )

        assertTrue(result.isSuccess)
        val likes: List<UserSummaryRecord> = result.getOrThrow()
        assertEquals(listOf(99L, 98L), likes.map { it.id })

        val state = store.state.value
        assertEquals(2L, state.feedsById.getValue(1L).revision)
        assertEquals(listOf(99L, 98L), state.feedsById.getValue(1L).likes.map { it.id })
    }

    @Test
    fun `toggleLikeRecords for reply commits ReplyLikesReplaced records`() = runTest {
        store.apply(FeedStoreChange.FeedUpserted(createFeedRecord(id = 1L, revision = 1L)))
        store.apply(
            FeedStoreChange.ReplyUpserted(
                feedId = 1L,
                reply = createReplyRecord(id = 10L, activityId = 1L, revision = 1L),
            ),
        )

        val request = ToggleLike.request(id = 10, type = LikeableType.ACTIVITY_REPLY)
        stubLikeResponse(request, listOf(userEntity(99L, "max")))

        val result = repository.toggleLikeRecords(
            id = 10L,
            type = LikeableType.ACTIVITY_REPLY,
            revision = 3L,
        )

        assertTrue(result.isSuccess)
        val state = store.state.value
        assertEquals(listOf(99L), state.repliesById.getValue(10L).likes.map { it.id })
        assertEquals(3L, state.repliesById.getValue(10L).revision)
        assertEquals(1L, state.repliesById.getValue(10L).activityId)
    }

    @Test
    fun `legacy toggleLike still returns entity typed user list`() = runTest {
        val request = ToggleLike.request(id = 1, type = LikeableType.ACTIVITY)
        stubLikeResponse(request, listOf(userEntity(99L, "max")))

        val result = repository.toggleLike(id = 1L, type = LikeableType.ACTIVITY)

        assertTrue(result.isSuccess)
        val likes: List<UserBase> = result.getOrThrow()
        assertEquals(listOf(99L), likes.map { it.id })
    }

    @Test
    fun `stale revision like replacement is rejected by store`() = runTest {
        store.apply(FeedStoreChange.FeedUpserted(createFeedRecord(id = 1L, revision = 5L, likes = emptyList())))

        val request = ToggleLike.request(id = 1, type = LikeableType.ACTIVITY)
        stubLikeResponse(request, listOf(userEntity(99L, "max")))

        repository.toggleLikeRecords(id = 1L, type = LikeableType.ACTIVITY, revision = 4L)

        val state = store.state.value
        assertEquals(5L, state.feedsById.getValue(1L).revision)
        assertTrue(state.feedsById.getValue(1L).likes.isEmpty())
    }

    @Test
    fun `failed like is server authoritative and leaves store unchanged`() = runTest {
        store.apply(FeedStoreChange.FeedUpserted(createFeedRecord(id = 1L, revision = 1L)))
        val request = ToggleLike.request(id = 1, type = LikeableType.ACTIVITY)
        val call = responseCall(AniListContainer<List<UserBase>>(data = null, errors = null))
        `when`(service.toggleLike(request)).thenReturn(call)

        val result = repository.toggleLikeRecords(id = 1L, type = LikeableType.ACTIVITY, revision = 2L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
        assertTrue(store.state.value.feedsById.getValue(1L).likes.isEmpty())
    }

    private fun stubLikeResponse(
        request: co.anitrend.retrofit.graphql.model.GraphQLRequest<com.mxt.anitrend.graphql.generated.ToggleLikeVariables>,
        likes: List<UserBase>,
    ) {
        val call = responseCall(AniListContainer(DataContainer(likes), errors = null))
        `when`(service.toggleLike(request)).thenReturn(call)
    }

    private fun userEntity(id: Long, name: String): UserBase = UserBase(name = name).apply {
        this.id = id
    }

    private fun createFeedRecord(
        id: Long,
        revision: Long,
        likes: List<UserSummaryRecord> = emptyList(),
    ): FeedRecord = FeedRecord(
        id = id,
        type = "TEXT",
        status = "watched",
        text = "feed-$id",
        createdAt = id * 100,
        user = null,
        messenger = null,
        recipient = null,
        media = null,
        likes = likes,
        replyCount = 0,
        siteUrl = "https://feed/$id",
        revision = revision,
    )

    private fun createReplyRecord(
        id: Long,
        activityId: Long,
        revision: Long,
    ): FeedReplyRecord = FeedReplyRecord(
        id = id,
        activityId = activityId,
        reply = "reply-$id",
        createdAt = id * 10,
        user = null,
        likes = emptyList(),
        revision = revision,
    )

    @Suppress("UNCHECKED_CAST")
    private fun <R> responseCall(
        body: AniListContainer<R>,
    ): Call<AniListContainer<R>> {
        val call = mock(Call::class.java) as Call<AniListContainer<R>>
        `when`(call.execute()).thenReturn(Response.success(body))
        return call
    }
}
