package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
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
import retrofit2.Call
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
    fun `getMediaRelationsRecord success maps GraphContainer data to MediaRelationsRecord`() = runTest {
        val call = mediaRelationsCall()
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaRelationsData(
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
                    errors = null,
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
        val call = mediaRelationsCall()
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaRelationsData(),
                    errors = null,
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
        val call = mediaRelationsCall()
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaRelationsData>(
                    data = null,
                    errors = listOf(GraphError(message = "Media relations failed")),
                ),
            ),
        )

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Media relations failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRelationsRecord null body returns failed Result`() = runTest {
        val call = mediaRelationsCall()
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRelationsRecord null data returns failed Result`() = runTest {
        val call = mediaRelationsCall()
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaRelationsData>(
                    data = null,
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRelationsRecord null root media returns failed Result`() = runTest {
        val call = mediaRelationsCall()
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = MediaRelationsData(media = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaRelationsRecord HTTP error returns failed Result with server message`() = runTest {
        val call = mediaRelationsCall()
        val request = MediaRelations.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaRelationsRecord(request)).thenReturn(call)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(call.execute()).thenReturn(Response.error(500, errorBody))

        val result = repository.getMediaRelationsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    @Suppress("UNCHECKED_CAST")
    private fun mediaRelationsCall(): Call<GraphContainer<MediaRelationsData>> = mock(Call::class.java) as Call<GraphContainer<MediaRelationsData>>

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
