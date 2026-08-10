package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
import com.mxt.anitrend.domain.mediadetail.model.MediaRelationsRecord
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaRelation
import com.mxt.anitrend.graphql.generated.MediaRelations
import com.mxt.anitrend.graphql.generated.MediaRelationsData
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.api.retro.anilist.MediaService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Response

/**
 * Focused tests for the MediaRepository media-relations record boundary (Lane C).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaRelationsRecordRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(MediaService::class.java)
    private val repository = MediaRepository(
        mediaService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `legacy getMediaRelations maps generated data back to legacy connection edges`() = runTest {
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        mediaRelationsData(
                            edges = listOf(
                                MediaRelationsData.MediaRelationsEdges(
                                    relationType = MediaRelation.SEQUEL,
                                    node = MediaRelationsData.MediaRelationsEdgesNode(
                                        averageScore = 80,
                                        bannerImage = null,
                                        chapters = null,
                                        coverImage = null,
                                        endDate = null,
                                        episodes = 24,
                                        format = MediaFormat.TV,
                                        id = 123,
                                        isAdult = false,
                                        isFavourite = true,
                                        meanScore = 85,
                                        mediaListEntry = null,
                                        nextAiringEpisode = null,
                                        season = MediaSeason.WINTER,
                                        siteUrl = "https://anilist.co/anime/123",
                                        startDate = null,
                                        status = MediaStatus.RELEASING,
                                        title = MediaRelationsData.MediaRelationsEdgesNodeTitle(
                                            english = null,
                                            native = null,
                                            romaji = null,
                                            userPreferred = "Preferred Title",
                                        ),
                                        type = MediaType.ANIME,
                                        updatedAt = 1_600_000_000,
                                        volumes = null,
                                    ),
                                ),
                            ),
                            pageInfo = MediaRelationsData.MediaRelationsPageInfo(
                                currentPage = 1,
                                hasNextPage = true,
                                lastPage = 1,
                                perPage = 25,
                                total = 2,
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaRelations(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val connection = result.getOrThrow()
        val edge = connection.connection.edges.first()
        assertEquals("SEQUEL", edge.relationType)
        assertEquals(123L, edge.node.id)
        assertEquals("Preferred Title", edge.node.title?.userPreferred)
        assertEquals("ANIME", edge.node.type)
        assertEquals("TV", edge.node.format)
        assertEquals("RELEASING", edge.node.status)
        assertEquals(85, edge.node.meanScore)
        assertEquals(24, edge.node.episodes)
        assertEquals(true, edge.node.isFavourite)
        assertEquals(1, connection.connection.pageInfo.currentPage)
        assertTrue(connection.connection.pageInfo.hasNextPage())
    }

    @Test
    fun `getMediaRelationsRecord success maps GraphQLResponse data to MediaRelationsRecord`() = runTest {
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        mediaRelationsData(
                            edges = listOf(
                                MediaRelationsData.MediaRelationsEdges(
                                    relationType = MediaRelation.SEQUEL,
                                    node = MediaRelationsData.MediaRelationsEdgesNode(
                                        averageScore = 80,
                                        bannerImage = null,
                                        chapters = null,
                                        coverImage = null,
                                        endDate = null,
                                        episodes = 24,
                                        format = MediaFormat.TV,
                                        id = 123,
                                        isAdult = false,
                                        isFavourite = true,
                                        meanScore = 85,
                                        mediaListEntry = MediaRelationsData.MediaRelationsEdgesNodeMediaListEntry(
                                            id = 456,
                                            status = MediaListStatus.COMPLETED,
                                        ),
                                        nextAiringEpisode = null,
                                        season = MediaSeason.WINTER,
                                        siteUrl = "https://anilist.co/anime/123",
                                        startDate = null,
                                        status = MediaStatus.RELEASING,
                                        title = MediaRelationsData.MediaRelationsEdgesNodeTitle(
                                            english = null,
                                            native = null,
                                            romaji = null,
                                            userPreferred = "Preferred Title",
                                        ),
                                        type = MediaType.ANIME,
                                        updatedAt = 1_600_000_000,
                                        volumes = null,
                                    ),
                                ),
                            ),
                            pageInfo = MediaRelationsData.MediaRelationsPageInfo(
                                currentPage = 1,
                                hasNextPage = true,
                                lastPage = 1,
                                perPage = 25,
                                total = 2,
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaRelationsRecord = result.getOrThrow()
        assertEquals(1, record.edges?.size)
        assertEquals("SEQUEL", record.edges?.first()?.relationType)
        assertEquals(123L, record.edges?.first()?.node?.id)
        assertEquals("Preferred Title", record.edges?.first()?.node?.titleUserPreferred)
        assertEquals("ANIME", record.edges?.first()?.node?.type)
        assertEquals("TV", record.edges?.first()?.node?.format)
        assertEquals("RELEASING", record.edges?.first()?.node?.status)
        assertEquals(85, record.edges?.first()?.node?.meanScore)
        assertEquals(24, record.edges?.first()?.node?.episodes)
        assertEquals("COMPLETED", record.edges?.first()?.node?.mediaListEntry?.status)
        assertEquals(1_600_000_000L, record.edges?.first()?.node?.updatedAt)
        assertEquals(1, record.pageInfo?.currentPage)
        assertEquals(1, record.pageInfo?.lastPage)
        assertEquals(25, record.pageInfo?.perPage)
        assertEquals(2, record.pageInfo?.total)
        assertTrue(record.pageInfo?.hasNextPage == true)
    }

    @Test
    fun `getMediaRelationsRecord preserves nullable optional blocks`() = runTest {
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(mediaRelationsData()),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaRelationsRecord = result.getOrThrow()
        assertNull(record.edges)
        assertNull(record.pageInfo)
    }

    @Test
    fun `getMediaRelationsRecord GraphQL error returns failed Result with message`() = runTest {
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse<MediaRelationsData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Media relations failed")),
                ),
            ),
        )

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Media relations failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRelationsRecord null body returns failed Result`() = runTest {
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(Response.success(null))

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRelationsRecord null data returns failed Result`() = runTest {
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse<MediaRelationsData>(
                    data = GraphQLData.Absent,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRelationsRecord null root media returns failed Result`() = runTest {
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(MediaRelationsData(media = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRelationsRecord HTTP error returns failed Result with server message`() = runTest {
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(service.getMediaRelationsRecord(request)).thenReturn(Response.error(500, errorBody))

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    private fun mediaRelationsData(
        edges: List<MediaRelationsData.MediaRelationsEdges?>? = null,
        pageInfo: MediaRelationsData.MediaRelationsPageInfo? = null,
    ): MediaRelationsData = MediaRelationsData(
        media = MediaRelationsData.Media(
            relations = MediaRelationsData.MediaRelations(
                edges = edges,
                pageInfo = pageInfo,
            ),
        ),
    )
}
