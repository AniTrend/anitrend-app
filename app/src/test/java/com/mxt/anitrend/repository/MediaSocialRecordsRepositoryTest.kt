package com.mxt.anitrend.repository

import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.graphql.generated.MediaSocial
import com.mxt.anitrend.model.api.retro.anilist.MediaService
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.DataContainer
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
 * Focused tests for the MediaRepository media-social record boundary (Lane C).
 *
 * Covers the record store commit and the legacy entity-typed return surface.
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
        val response = pageCall(page = 1, feeds = listOf(feedEntity(1L), feedEntity(2L)))
        `when`(service.getMediaSocial(request)).thenReturn(response)
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
        val response = pageCall(page = 1, feeds = listOf(feedEntity(1L), feedEntity(2L)))
        `when`(service.getMediaSocial(request)).thenReturn(response)

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
        val response = success(AniListContainer<PageContainer<FeedList>>(data = null, errors = null))
        `when`(service.getMediaSocial(request)).thenReturn(response)

        val result = repository.getMediaSocialRecords(mediaId = 7L, isFollowing = true, page = 1, perPage = 10)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
        assertTrue(store.state.value.queries.isEmpty())
    }

    private fun feedEntity(id: Long): FeedList = FeedList(
        id = id,
        type = "TEXT",
        status = "watched",
        text = "feed-$id",
        replyCount = 0,
    )

    private fun pageCall(
        page: Int,
        feeds: List<FeedList>,
    ): Response<AniListContainer<PageContainer<FeedList>>> {
        val content = PageContainer<FeedList>().apply {
            pageData = feeds
            pageInfo = PageInfo(total = feeds.size, perPage = 10, currentPage = page).apply { setHasNextPage(false) }
        }
        return success(AniListContainer(DataContainer(content), errors = null))
    }

    private fun <R> success(
        body: AniListContainer<R>,
    ): Response<AniListContainer<R>> = Response.success(body)
}
