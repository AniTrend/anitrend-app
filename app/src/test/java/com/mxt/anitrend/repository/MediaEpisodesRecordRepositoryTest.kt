package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
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
import retrofit2.Call
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
    fun `getMediaEpisodesRecord success maps GraphContainer data to MediaEpisodesRecord`() = runTest {
        val call = mediaEpisodesCall()
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaEpisodesData(
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
                    errors = null,
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
        val call = mediaEpisodesCall()
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaEpisodesData(),
                    errors = null,
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
        val call = mediaEpisodesCall()
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaEpisodesData>(
                    data = null,
                    errors = listOf(GraphError(message = "Media episodes failed")),
                ),
            ),
        )

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Media episodes failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaEpisodesRecord null body returns failed Result`() = runTest {
        val call = mediaEpisodesCall()
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaEpisodesRecord null data returns failed Result`() = runTest {
        val call = mediaEpisodesCall()
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaEpisodesData>(
                    data = null,
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaEpisodesRecord null root media returns failed Result`() = runTest {
        val call = mediaEpisodesCall()
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = MediaEpisodesData(media = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaEpisodesRecord HTTP error returns failed Result with server message`() = runTest {
        val call = mediaEpisodesCall()
        val request = MediaEpisodes.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaEpisodesRecord(request)).thenReturn(call)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(call.execute()).thenReturn(Response.error(500, errorBody))

        val result = repository.getMediaEpisodesRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    @Suppress("UNCHECKED_CAST")
    private fun mediaEpisodesCall(): Call<GraphContainer<MediaEpisodesData>> = mock(Call::class.java) as Call<GraphContainer<MediaEpisodesData>>

    private fun mediaEpisodesData(
        externalLinks: List<MediaEpisodesData.MediaExternalLinks?>? = null,
    ): MediaEpisodesData = MediaEpisodesData(
        media = MediaEpisodesData.Media(
            externalLinks = externalLinks,
        ),
    )
}
