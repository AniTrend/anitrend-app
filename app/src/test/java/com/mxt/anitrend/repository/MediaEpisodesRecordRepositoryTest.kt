package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
import com.mxt.anitrend.domain.mediadetail.model.MediaEpisodesRecord
import com.mxt.anitrend.graphql.generated.MediaEpisodes
import com.mxt.anitrend.graphql.generated.MediaEpisodesData
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
 * Focused tests for the MediaRepository media-episodes record boundary (Lane C).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaEpisodesRecordRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(MediaService::class.java)
    private val repository = MediaRepository(
        mediaService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `legacy getMediaEpisodes maps generated data back to legacy connection links`() = runTest {
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        mediaEpisodesData(
                            externalLinks = listOf(
                                MediaEpisodesData.MediaExternalLinks(
                                    id = 301,
                                    site = "Crunchyroll",
                                    url = "https://www.crunchyroll.com/series/123",
                                ),
                                MediaEpisodesData.MediaExternalLinks(
                                    id = 302,
                                    site = "AniList",
                                    url = null,
                                ),
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaEpisodes(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val links = result.getOrThrow().connection
        assertEquals(2, links.size)
        assertEquals(301, links.first().id)
        assertEquals("Crunchyroll", links.first().site)
        assertEquals("https://www.crunchyroll.com/series/123", links.first().url)
        assertEquals(302, links.last().id)
        assertEquals("AniList", links.last().site)
        assertNull(links.last().url)
    }

    @Test
    fun `getMediaEpisodesRecord success maps GraphQLResponse data to MediaEpisodesRecord`() = runTest {
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        mediaEpisodesData(
                            externalLinks = listOf(
                                MediaEpisodesData.MediaExternalLinks(
                                    id = 301,
                                    site = "Crunchyroll",
                                    url = "https://www.crunchyroll.com/series/123",
                                ),
                                MediaEpisodesData.MediaExternalLinks(
                                    id = 302,
                                    site = "AniList",
                                    url = null,
                                ),
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaEpisodesRecord = result.getOrThrow()
        assertEquals(2, record.externalLinks?.size)
        assertEquals(301L, record.externalLinks?.first()?.id)
        assertEquals("Crunchyroll", record.externalLinks?.first()?.site)
        assertEquals("https://www.crunchyroll.com/series/123", record.externalLinks?.first()?.url)
        assertEquals(302L, record.externalLinks?.last()?.id)
        assertEquals("AniList", record.externalLinks?.last()?.site)
        assertNull(record.externalLinks?.last()?.url)
    }

    @Test
    fun `getMediaEpisodesRecord preserves nullable optional blocks`() = runTest {
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(mediaEpisodesData()),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaEpisodesRecord = result.getOrThrow()
        assertNull(record.externalLinks)
    }

    @Test
    fun `getMediaEpisodesRecord GraphQL error returns failed Result with message`() = runTest {
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse<MediaEpisodesData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Media episodes failed")),
                ),
            ),
        )

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Media episodes failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaEpisodesRecord null body returns failed Result`() = runTest {
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(Response.success(null))

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaEpisodesRecord null data returns failed Result`() = runTest {
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse<MediaEpisodesData>(
                    data = GraphQLData.Absent,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaEpisodesRecord null root media returns failed Result`() = runTest {
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(MediaEpisodesData(media = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaEpisodesRecord HTTP error returns failed Result with server message`() = runTest {
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(Response.error(500, errorBody))

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    private fun mediaEpisodesData(
        externalLinks: List<MediaEpisodesData.MediaExternalLinks?>? = null,
    ): MediaEpisodesData = MediaEpisodesData(
        media = MediaEpisodesData.Media(
            externalLinks = externalLinks,
        ),
    )
}
