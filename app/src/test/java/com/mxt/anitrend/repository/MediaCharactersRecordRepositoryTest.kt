package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.domain.mediadetail.model.MediaCharactersRecord
import com.mxt.anitrend.graphql.generated.CharacterRole
import com.mxt.anitrend.graphql.generated.CharacterSort
import com.mxt.anitrend.graphql.generated.MediaCharacters
import com.mxt.anitrend.graphql.generated.MediaCharactersData
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
 * Focused tests for the MediaRepository media-characters record boundary (Lane C).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaCharactersRecordRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(MediaService::class.java)
    private val repository = MediaRepository(
        mediaService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getMediaCharactersRecord success maps GraphContainer data to MediaCharactersRecord`() = runTest {
        val call = mediaCharactersCall()
        val request = MediaCharacters.request(id = 21, type = MediaType.ANIME, isAdult = false, sort = null)
        `when`(service.getMediaCharactersRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaCharactersData(
                        edges = listOf(
                            MediaCharactersData.MediaCharactersEdges(
                                role = CharacterRole.MAIN,
                                node = MediaCharactersData.MediaCharactersEdgesNode(
                                    id = 123,
                                    image = MediaCharactersData.MediaCharactersEdgesNodeImage(
                                        large = "https://cdn.example.com/large.jpg",
                                        medium = "https://cdn.example.com/medium.jpg",
                                    ),
                                    isFavourite = true,
                                    name = MediaCharactersData.MediaCharactersEdgesNodeName(
                                        alternative = null,
                                        first = "Light",
                                        last = "Yagami",
                                        native = null,
                                    ),
                                    siteUrl = "https://anilist.co/character/123",
                                ),
                            ),
                        ),
                        pageInfo = MediaCharactersData.MediaCharactersPageInfo(
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

        val result = repository.getMediaCharactersRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaCharactersRecord = result.getOrThrow()
        assertEquals(1, record.edges?.size)
        assertEquals("MAIN", record.edges?.first()?.role)
        assertEquals(123L, record.edges?.first()?.node?.id)
        assertEquals("Light Yagami", record.edges?.first()?.node?.name)
        assertEquals("https://anilist.co/character/123", record.edges?.first()?.node?.siteUrl)
        assertTrue(record.edges?.first()?.node?.isFavourite == true)
        assertEquals(1, record.pageInfo?.currentPage)
        assertEquals(1, record.pageInfo?.lastPage)
        assertEquals(25, record.pageInfo?.perPage)
        assertEquals(2, record.pageInfo?.total)
        assertTrue(record.pageInfo?.hasNextPage == true)
    }

    @Test
    fun `getMediaCharactersRecord forwards pagination and sort request inputs`() = runTest {
        val call = mediaCharactersCall()
        val sort = listOf(CharacterSort.ROLE, CharacterSort.RELEVANCE, CharacterSort.ID)
        val request = MediaCharacters.request(
            id = 21,
            type = MediaType.ANIME,
            isAdult = false,
            page = 3,
            perPage = 50,
            sort = sort,
        )
        `when`(service.getMediaCharactersRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaCharactersData(
                        edges = listOf(
                            MediaCharactersData.MediaCharactersEdges(
                                role = CharacterRole.SUPPORTING,
                                node = MediaCharactersData.MediaCharactersEdgesNode(
                                    id = 1,
                                    image = null,
                                    isFavourite = false,
                                    name = null,
                                    siteUrl = null,
                                ),
                            ),
                        ),
                        pageInfo = MediaCharactersData.MediaCharactersPageInfo(
                            currentPage = 3,
                            hasNextPage = false,
                            lastPage = 3,
                            perPage = 50,
                            total = 150,
                        ),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaCharactersRecord(
            id = 21L,
            type = MediaType.ANIME,
            isAdult = false,
            page = 3,
            perPage = 50,
            sort = sort,
        )

        assertTrue(result.isSuccess)
        val record: MediaCharactersRecord = result.getOrThrow()
        assertEquals(3, record.pageInfo?.currentPage)
        assertEquals(50, record.pageInfo?.perPage)
        assertEquals(150, record.pageInfo?.total)
        assertEquals("SUPPORTING", record.edges?.first()?.role)
    }

    @Test
    fun `getMediaCharactersRecord preserves nullable optional blocks`() = runTest {
        val call = mediaCharactersCall()
        val request = MediaCharacters.request(id = 21, type = MediaType.ANIME, isAdult = false, sort = null)
        `when`(service.getMediaCharactersRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaCharactersData(),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaCharactersRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaCharactersRecord = result.getOrThrow()
        assertNull(record.edges)
        assertNull(record.pageInfo)
    }

    @Test
    fun `getMediaCharactersRecord GraphQL error returns failed Result with message`() = runTest {
        val call = mediaCharactersCall()
        val request = MediaCharacters.request(id = 21, type = MediaType.ANIME, isAdult = false, sort = null)
        `when`(service.getMediaCharactersRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaCharactersData>(
                    data = null,
                    errors = listOf(GraphError(message = "Media characters failed")),
                ),
            ),
        )

        val result = repository.getMediaCharactersRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Media characters failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaCharactersRecord null body returns failed Result`() = runTest {
        val call = mediaCharactersCall()
        val request = MediaCharacters.request(id = 21, type = MediaType.ANIME, isAdult = false, sort = null)
        `when`(service.getMediaCharactersRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.getMediaCharactersRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaCharactersRecord null data returns failed Result`() = runTest {
        val call = mediaCharactersCall()
        val request = MediaCharacters.request(id = 21, type = MediaType.ANIME, isAdult = false, sort = null)
        `when`(service.getMediaCharactersRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaCharactersData>(
                    data = null,
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaCharactersRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaCharactersRecord null root media returns failed Result`() = runTest {
        val call = mediaCharactersCall()
        val request = MediaCharacters.request(id = 21, type = MediaType.ANIME, isAdult = false, sort = null)
        `when`(service.getMediaCharactersRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = MediaCharactersData(media = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaCharactersRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaCharactersRecord HTTP error returns failed Result with server message`() = runTest {
        val call = mediaCharactersCall()
        val request = MediaCharacters.request(id = 21, type = MediaType.ANIME, isAdult = false, sort = null)
        `when`(service.getMediaCharactersRecord(request)).thenReturn(call)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(call.execute()).thenReturn(Response.error(500, errorBody))

        val result = repository.getMediaCharactersRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    @Suppress("UNCHECKED_CAST")
    private fun mediaCharactersCall(): Call<GraphContainer<MediaCharactersData>> =
        mock(Call::class.java) as Call<GraphContainer<MediaCharactersData>>

    private fun mediaCharactersData(
        edges: List<MediaCharactersData.MediaCharactersEdges?>? = null,
        pageInfo: MediaCharactersData.MediaCharactersPageInfo? = null,
    ): MediaCharactersData = MediaCharactersData(
        media = MediaCharactersData.Media(
            characters = MediaCharactersData.MediaCharacters(
                edges = edges,
                pageInfo = pageInfo,
            ),
        ),
    )
}
