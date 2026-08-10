package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
import com.mxt.anitrend.domain.model.RecommendationPageResult
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.RecommendationMedia
import com.mxt.anitrend.graphql.generated.RecommendationMediaData
import com.mxt.anitrend.graphql.generated.RecommendationRating
import com.mxt.anitrend.model.api.retro.anilist.MediaService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MediaRecommendationsRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(MediaService::class.java)
    private val repository = MediaRepository(
        mediaService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getMediaRecommendations success maps GraphQLResponse nodes preserving order and page info`() = runTest {
        val request = RecommendationMedia.request(id = 7, type = MediaType.MANGA, isAdult = false, page = null, perPage = null, sort = null)
        `when`(service.getMediaRecommendations(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(recommendationMediaData(nodes = listOf(firstNode(), secondNode()))),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaRecommendations(id = 7L, type = MediaType.MANGA, isAdult = false)

        assertTrue(result.isSuccess)
        val page: RecommendationPageResult = result.getOrThrow()
        assertEquals(listOf(1L, 4L), page.recommendations.map { it.id })
        assertEquals(2L, page.recommendations.first().mediaRecommendation?.id)
        assertEquals(88, page.recommendations.first().rating)
        assertEquals("RATE_UP", page.recommendations.first().userRating)
        assertEquals("Recommender", page.recommendations.first().user?.name)
        assertNotNull(page.pageInfo)
        assertEquals(1, page.pageInfo?.currentPage)
        assertTrue(page.pageInfo?.hasNextPage == true)
        assertEquals(2, page.pageInfo?.total)
    }

    @Test
    fun `getMediaRecommendations success returns empty result for empty nodes`() = runTest {
        val request = RecommendationMedia.request(id = 7, type = MediaType.MANGA, isAdult = false, page = null, perPage = null, sort = null)
        `when`(service.getMediaRecommendations(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(recommendationMediaData(nodes = emptyList())),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaRecommendations(id = 7L, type = MediaType.MANGA, isAdult = false)

        assertTrue(result.isSuccess)
        val page: RecommendationPageResult = result.getOrThrow()
        assertTrue(page.recommendations.isEmpty())
        assertNotNull(page.pageInfo)
    }

    @Test
    fun `getMediaRecommendations GraphQL error returns failed Result with message`() = runTest {
        val request = RecommendationMedia.request(id = 7, type = MediaType.MANGA, isAdult = false, page = null, perPage = null, sort = null)
        `when`(service.getMediaRecommendations(request)).thenReturn(
            Response.success(
                GraphQLResponse<RecommendationMediaData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Recommendations failed")),
                ),
            ),
        )

        val result = repository.getMediaRecommendations(id = 7L, type = MediaType.MANGA, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Recommendations failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRecommendations null body returns failed Result`() = runTest {
        val request = RecommendationMedia.request(id = 7, type = MediaType.MANGA, isAdult = false, page = null, perPage = null, sort = null)
        `when`(service.getMediaRecommendations(request)).thenReturn(Response.success(null))

        val result = repository.getMediaRecommendations(id = 7L, type = MediaType.MANGA, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRecommendations null data returns failed Result`() = runTest {
        val request = RecommendationMedia.request(id = 7, type = MediaType.MANGA, isAdult = false, page = null, perPage = null, sort = null)
        `when`(service.getMediaRecommendations(request)).thenReturn(
            Response.success(
                GraphQLResponse<RecommendationMediaData>(
                    data = GraphQLData.Absent,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaRecommendations(id = 7L, type = MediaType.MANGA, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRecommendations null root media returns failed Result`() = runTest {
        val request = RecommendationMedia.request(id = 7, type = MediaType.MANGA, isAdult = false, page = null, perPage = null, sort = null)
        `when`(service.getMediaRecommendations(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(RecommendationMediaData(media = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaRecommendations(id = 7L, type = MediaType.MANGA, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRecommendations null recommendations block returns failed Result`() = runTest {
        val request = RecommendationMedia.request(id = 7, type = MediaType.MANGA, isAdult = false, page = null, perPage = null, sort = null)
        `when`(service.getMediaRecommendations(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        RecommendationMediaData(
                            media = RecommendationMediaData.Media(recommendations = null),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaRecommendations(id = 7L, type = MediaType.MANGA, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRecommendations drops null nodes while preserving order`() = runTest {
        val request = RecommendationMedia.request(id = 7, type = MediaType.MANGA, isAdult = false, page = null, perPage = null, sort = null)
        `when`(service.getMediaRecommendations(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(recommendationMediaData(nodes = listOf(firstNode(), null, secondNode()))),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaRecommendations(id = 7L, type = MediaType.MANGA, isAdult = false)

        assertTrue(result.isSuccess)
        val page: RecommendationPageResult = result.getOrThrow()
        assertEquals(listOf(1L, 4L), page.recommendations.map { it.id })
    }

    private fun recommendationMediaData(
        nodes: List<RecommendationMediaData.MediaRecommendationsNodes?>,
    ): RecommendationMediaData = RecommendationMediaData(
        media = RecommendationMediaData.Media(
            recommendations = RecommendationMediaData.MediaRecommendations(
                nodes = nodes,
                pageInfo = RecommendationMediaData.MediaRecommendationsPageInfo(
                    currentPage = 1,
                    hasNextPage = true,
                    lastPage = 1,
                    perPage = 10,
                    total = nodes.size,
                ),
            ),
        ),
    )

    private fun firstNode(): RecommendationMediaData.MediaRecommendationsNodes = RecommendationMediaData.MediaRecommendationsNodes(
        id = 1,
        mediaRecommendation = RecommendationMediaData.MediaRecommendationsNodesMediaRecommendation(
            averageScore = null,
            bannerImage = null,
            chapters = null,
            coverImage = null,
            endDate = null,
            episodes = 12,
            format = null,
            id = 2,
            isAdult = null,
            isFavourite = false,
            meanScore = null,
            mediaListEntry = null,
            nextAiringEpisode = null,
            season = null,
            siteUrl = "https://anilist.co/anime/2",
            startDate = null,
            status = null,
            title = null,
            type = MediaType.ANIME,
            updatedAt = null,
            volumes = null,
        ),
        rating = 88,
        user = RecommendationMediaData.MediaRecommendationsNodesUser(
            avatar = null,
            bannerImage = null,
            id = 3,
            isFollowing = false,
            name = "Recommender",
            updatedAt = null,
        ),
        userRating = RecommendationRating.RATE_UP,
    )

    private fun secondNode(): RecommendationMediaData.MediaRecommendationsNodes = RecommendationMediaData.MediaRecommendationsNodes(
        id = 4,
        mediaRecommendation = null,
        rating = null,
        user = null,
        userRating = null,
    )
}
