package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import com.mxt.anitrend.graphql.generated.MediaBrowse
import com.mxt.anitrend.graphql.generated.MediaBrowseData
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListBrowse
import com.mxt.anitrend.graphql.generated.MediaListBrowseData
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.api.retro.anilist.BrowseService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Response

/**
 * Focused tests for the BrowseRepository page handlers.
 *
 * A present Page block maps into legacy entities, while a null generated Page
 * root must fail with "Empty response body" instead of returning a successful
 * empty page container, matching the previous AniListContainer semantics.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BrowseMediaBrowseRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(BrowseService::class.java)
    private val repository = BrowseRepository(
        browseService = service,
        ioDispatcher = testDispatcher,
        mediaListStore = null,
        reviewStore = null,
    )

    @Test
    fun `getMediaBrowse maps a present page into legacy media entities`() = runTest {
        val request = MediaBrowse.request(id = null, page = 1, perPage = 20, seasonYear = null, type = MediaType.ANIME, format = MediaFormat.TV, startDateLike = null, endDateLike = null, season = null, genres = null, genresExclude = null, isAdult = null, sort = null, onList = null, status = null, tags = null, tagsExclude = null)
        `when`(service.getMediaBrowse(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        MediaBrowseData(
                            page = MediaBrowseData.Page(
                                media = listOf(media(id = 21)),
                                pageInfo = MediaBrowseData.PagePageInfo(
                                    currentPage = 1,
                                    hasNextPage = true,
                                    lastPage = null,
                                    perPage = 20,
                                    total = 1,
                                ),
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaBrowse(id = null, page = 1, perPage = 20, type = MediaType.ANIME, format = MediaFormat.TV)

        assertTrue(result.isSuccess)
        val page = result.getOrThrow()
        assertEquals(21L, page.pageData.single().id)
        assertEquals(MediaType.ANIME.name, page.pageData.single().type)
        assertEquals(MediaFormat.TV.name, page.pageData.single().format)
        assertTrue(page.hasPageInfo())
        assertEquals(1, page.pageInfo.currentPage)
    }

    @Test
    fun `getMediaBrowse with null page fails with empty response body`() = runTest {
        val request = MediaBrowse.request(id = null, page = 1, perPage = 20, seasonYear = null, type = MediaType.ANIME, format = MediaFormat.TV, startDateLike = null, endDateLike = null, season = null, genres = null, genresExclude = null, isAdult = null, sort = null, onList = null, status = null, tags = null, tagsExclude = null)
        `when`(service.getMediaBrowse(request)).thenReturn(
            Response.success(
                GraphQLResponse(data = GraphQLData.Present(MediaBrowseData(page = null)), errors = emptyList()),
            ),
        )

        val result = repository.getMediaBrowse(id = null, page = 1, perPage = 20, type = MediaType.ANIME, format = MediaFormat.TV)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaListBrowse with null page fails with empty response body`() = runTest {
        val request = MediaListBrowse.request(id = null, userId = null, userName = null, page = 1, perPage = 20, type = null, status = null, sort = null, scoreFormat = ScoreFormat.POINT_100)
        `when`(service.getMediaListBrowse(request)).thenReturn(
            Response.success(
                GraphQLResponse(data = GraphQLData.Present(MediaListBrowseData(page = null)), errors = emptyList()),
            ),
        )

        val result = repository.getMediaListBrowse(page = 1, perPage = 20)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    private fun media(id: Int): MediaBrowseData.PageMedia = MediaBrowseData.PageMedia(
        averageScore = null,
        bannerImage = null,
        chapters = null,
        coverImage = null,
        endDate = null,
        episodes = null,
        format = MediaFormat.TV,
        id = id,
        isAdult = null,
        isFavourite = false,
        meanScore = null,
        mediaListEntry = null,
        nextAiringEpisode = null,
        season = null,
        siteUrl = null,
        startDate = null,
        status = null,
        title = null,
        type = MediaType.ANIME,
        updatedAt = null,
        volumes = null,
    )
}
