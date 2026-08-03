package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.domain.mediadetail.model.MediaDetailRecord
import com.mxt.anitrend.graphql.generated.MediaBase
import com.mxt.anitrend.graphql.generated.MediaBaseData
import com.mxt.anitrend.graphql.generated.MediaListStatus
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
 * Focused tests for the MediaRepository media-base record boundary (Lane C).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaBaseRecordRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(MediaService::class.java)
    private val repository = MediaRepository(
        mediaService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getMediaBaseRecord success maps GraphContainer data to MediaDetailRecord`() = runTest {
        val call = mediaBaseCall()
        val request = MediaBase.request(id = 7, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaBaseRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaBaseData(
                        id = 7,
                        idMal = 12345,
                        type = MediaType.ANIME,
                        isFavourite = true,
                        siteUrl = "https://anilist.co/anime/7",
                        mediaListEntry = MediaBaseData.MediaMediaListEntry(id = 99, status = MediaListStatus.CURRENT),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaBaseRecord(id = 7L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaDetailRecord = result.getOrThrow()
        assertEquals(7L, record.id)
        assertEquals(12345L, record.idMal)
        assertEquals("Sword Art Online", record.titleUserPreferred)
        assertEquals("ANIME", record.type)
        assertEquals("banner.jpg", record.bannerImage)
        assertEquals(true, record.isFavourite)
        assertEquals("https://anilist.co/anime/7", record.siteUrl)
        assertEquals(99L, record.mediaListEntry?.id)
        assertEquals("CURRENT", record.mediaListEntry?.status)
    }

    @Test
    fun `getMediaBaseRecord success maps nullable mediaListEntry`() = runTest {
        val call = mediaBaseCall()
        val request = MediaBase.request(id = 7, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaBaseRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaBaseData(
                        id = 7,
                        mediaListEntry = null,
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaBaseRecord(id = 7L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaDetailRecord = result.getOrThrow()
        assertEquals(7L, record.id)
        assertNull(record.mediaListEntry)
    }

    @Test
    fun `getMediaBaseRecord GraphQL error returns failed Result with message`() = runTest {
        val call = mediaBaseCall()
        val request = MediaBase.request(id = 7, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaBaseRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaBaseData>(
                    data = null,
                    errors = listOf(GraphError(message = "Media base failed")),
                ),
            ),
        )

        val result = repository.getMediaBaseRecord(id = 7L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Media base failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaBaseRecord null body returns failed Result`() = runTest {
        val call = mediaBaseCall()
        val request = MediaBase.request(id = 7, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaBaseRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.getMediaBaseRecord(id = 7L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaBaseRecord null data returns failed Result`() = runTest {
        val call = mediaBaseCall()
        val request = MediaBase.request(id = 7, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaBaseRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaBaseData>(
                    data = null,
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaBaseRecord(id = 7L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaBaseRecord null root media returns failed Result`() = runTest {
        val call = mediaBaseCall()
        val request = MediaBase.request(id = 7, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaBaseRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = MediaBaseData(media = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaBaseRecord(id = 7L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaBaseRecord HTTP error returns failed Result with server message`() = runTest {
        val call = mediaBaseCall()
        val request = MediaBase.request(id = 7, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaBaseRecord(request)).thenReturn(call)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(call.execute()).thenReturn(Response.error(500, errorBody))

        val result = repository.getMediaBaseRecord(id = 7L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    @Suppress("UNCHECKED_CAST")
    private fun mediaBaseCall(): Call<GraphContainer<MediaBaseData>> = mock(Call::class.java) as Call<GraphContainer<MediaBaseData>>

    private fun mediaBaseData(
        id: Int,
        idMal: Int? = null,
        type: MediaType? = null,
        isFavourite: Boolean = false,
        siteUrl: String? = null,
        mediaListEntry: MediaBaseData.MediaMediaListEntry? = null,
    ): MediaBaseData = MediaBaseData(
        media = MediaBaseData.Media(
            bannerImage = "banner.jpg",
            id = id,
            idMal = idMal,
            isFavourite = isFavourite,
            mediaListEntry = mediaListEntry,
            siteUrl = siteUrl,
            title = MediaBaseData.MediaTitle(userPreferred = "Sword Art Online"),
            type = type,
        ),
    )
}
