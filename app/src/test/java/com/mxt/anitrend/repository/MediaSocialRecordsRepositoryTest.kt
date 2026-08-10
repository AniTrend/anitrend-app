package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.graphql.generated.MediaSocial
import com.mxt.anitrend.graphql.generated.MediaSocialData
import com.mxt.anitrend.model.api.retro.anilist.MediaService
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Response

/**
 * Focused tests for the MediaRepository media-social boundary.
 *
 * Covers the record store commit and the legacy entity-typed return surface,
 * both sourcing from the generated MediaSocialData transport.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaSocialRecordsRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(MediaService::class.java)
    private val store = InMemoryFeedStore()
    private val repository = MediaRepository(
        mediaService = service,
        ioDispatcher = testDispatcher,
        feedStore = store,
    )

    @Test
    fun `getMediaSocialRecords commits FeedRecord page to store under MEDIA scope`() = runTest {
        val request = MediaSocial.request(mediaId = 7, isFollowing = true, page = 1, perPage = 10)
        val response = pageCall(page = 1, feeds = listOf(1L to "feed-1", 2L to "feed-2"))
        `when`(service.getMediaSocialRecord(request)).thenReturn(response)
        val mediaKey = FeedQueryKey(
            scope = FeedScope.MEDIA,
            userId = null,
            mediaId = 7,
            activityType = null,
            isFollowing = true,
            isMixed = null,
        )

        val result = repository.getMediaSocialRecords(
            mediaId = 7L,
            isFollowing = true,
            page = 1,
            perPage = 10,
            queryKey = mediaKey,
            readToken = 4L,
        )

        assertTrue(result.isSuccess)
        val page: FeedRecordPage = result.getOrThrow()
        assertEquals(listOf(1L, 2L), page.feeds.map { it.id })
        assertEquals(4L, page.feeds.first().revision)
        assertEquals(1, page.pageInfo?.currentPage)

        val state = store.state.value
        assertEquals(listOf(1L, 2L), state.queries.getValue(mediaKey).orderedFeedIds)
        assertEquals(4L, state.feedsById.getValue(1L).revision)
    }

    @Test
    fun `legacy getMediaSocial still returns entity typed page`() = runTest {
        val request = MediaSocial.request(mediaId = 7, isFollowing = true, page = 1, perPage = 10)
        val response = pageCall(page = 1, feeds = listOf(1L to "feed-1", 2L to "feed-2"))
        `when`(service.getMediaSocialRecord(request)).thenReturn(response)

        val result = repository.getMediaSocial(
            mediaId = 7L,
            isFollowing = true,
            page = 1,
            perPage = 10,
        )

        assertTrue(result.isSuccess)
        val page: PageContainer<FeedList> = result.getOrThrow()
        assertEquals(listOf(1L, 2L), page.pageData.map { it.id })
    }

    @Test
    fun `getMediaSocialRecords failure returns failed Result and does not commit`() = runTest {
        val request = MediaSocial.request(mediaId = 7, isFollowing = true, page = 1, perPage = 10)
        val response = Response.success(
            GraphQLResponse<MediaSocialData>(
                data = GraphQLData.Absent,
                errors = emptyList(),
            ),
        )
        `when`(service.getMediaSocialRecord(request)).thenReturn(response)

        val result = repository.getMediaSocialRecords(mediaId = 7L, isFollowing = true, page = 1, perPage = 10)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
        assertTrue(store.state.value.queries.isEmpty())
    }

    private fun pageCall(
        page: Int,
        feeds: List<Pair<Long, String>>,
    ): Response<GraphQLResponse<MediaSocialData>> = Response.success(
        GraphQLResponse(
            data = GraphQLData.Present(socialData(page = page, feeds = feeds)),
            errors = emptyList(),
        ),
    )

    private fun socialData(
        page: Int,
        feeds: List<Pair<Long, String>>,
    ): MediaSocialData = MediaSocialData(
        page = MediaSocialData.Page(
            activities = feeds.map { (id, text) ->
                MediaSocialData.PageActivities.ListActivity(
                    createdAt = 1_700_000_000,
                    id = id.toInt(),
                    isLocked = false,
                    likes = null,
                    media = null,
                    progress = text,
                    replies = null,
                    replyCount = 1,
                    siteUrl = null,
                    status = "watched",
                    type = ActivityType.MEDIA_LIST,
                    user = MediaSocialData.ListActivityPageActivitiesUser(
                        avatar = null,
                        bannerImage = null,
                        id = 55,
                        isFollowing = null,
                        name = "user-$id",
                        updatedAt = null,
                    ),
                )
            },
            pageInfo = MediaSocialData.PagePageInfo(
                currentPage = page,
                hasNextPage = false,
                lastPage = 1,
                perPage = 10,
                total = feeds.size,
            ),
        ),
    )
}
