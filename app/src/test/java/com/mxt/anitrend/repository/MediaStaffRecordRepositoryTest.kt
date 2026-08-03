package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.domain.mediadetail.model.MediaStaffRecord
import com.mxt.anitrend.graphql.generated.MediaStaff
import com.mxt.anitrend.graphql.generated.MediaStaffData
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StaffLanguage
import com.mxt.anitrend.graphql.generated.StaffSort
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
 * Focused tests for the MediaRepository media-staff record boundary (Lane C).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaStaffRecordRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(MediaService::class.java)
    private val repository = MediaRepository(
        mediaService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getMediaStaffRecord success maps GraphContainer data to MediaStaffRecord`() = runTest {
        val call = mediaStaffCall()
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaStaffData(
                        edges = listOf(
                            MediaStaffData.MediaStaffEdges(
                                role = "MAIN",
                                node = MediaStaffData.MediaStaffEdgesNode(
                                    id = 123,
                                    image = MediaStaffData.MediaStaffEdgesNodeImage(
                                        large = "https://cdn.example.com/large.jpg",
                                        medium = "https://cdn.example.com/medium.jpg",
                                    ),
                                    isFavourite = true,
                                    language = StaffLanguage.JAPANESE,
                                    name = MediaStaffData.MediaStaffEdgesNodeName(
                                        alternative = null,
                                        first = "Shinichiro",
                                        full = "Shinichiro Watanabe",
                                        last = "Watanabe",
                                        native = null,
                                    ),
                                    siteUrl = "https://anilist.co/staff/123",
                                ),
                            ),
                        ),
                        pageInfo = MediaStaffData.MediaStaffPageInfo(
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

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaStaffRecord = result.getOrThrow()
        assertEquals(1, record.edges?.size)
        assertEquals("MAIN", record.edges?.first()?.role)
        assertEquals(123L, record.edges?.first()?.node?.id)
        assertEquals("Shinichiro Watanabe", record.edges?.first()?.node?.name)
        assertEquals("https://anilist.co/staff/123", record.edges?.first()?.node?.siteUrl)
        assertTrue(record.edges?.first()?.node?.isFavourite == true)
        assertEquals(1, record.pageInfo?.currentPage)
        assertEquals(1, record.pageInfo?.lastPage)
        assertEquals(25, record.pageInfo?.perPage)
        assertEquals(2, record.pageInfo?.total)
        assertTrue(record.pageInfo?.hasNextPage == true)
    }

    @Test
    fun `getMediaStaffRecord forwards pagination and sort request inputs`() = runTest {
        val call = mediaStaffCall()
        val sort = listOf(StaffSort.ROLE, StaffSort.RELEVANCE, StaffSort.ID)
        val request = MediaStaff.request(
            id = 21,
            type = MediaType.ANIME,
            sort = sort,
            isAdult = false,
            page = 3,
            perPage = 50,
        )
        `when`(service.getMediaStaffRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaStaffData(
                        edges = listOf(
                            MediaStaffData.MediaStaffEdges(
                                role = "DIRECTOR",
                                node = MediaStaffData.MediaStaffEdgesNode(
                                    id = 1,
                                    image = null,
                                    isFavourite = false,
                                    language = null,
                                    name = null,
                                    siteUrl = null,
                                ),
                            ),
                        ),
                        pageInfo = MediaStaffData.MediaStaffPageInfo(
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

        val result = repository.getMediaStaffRecord(
            id = 21L,
            type = MediaType.ANIME,
            isAdult = false,
            page = 3,
            perPage = 50,
            sort = sort,
        )

        assertTrue(result.isSuccess)
        val record: MediaStaffRecord = result.getOrThrow()
        assertEquals(3, record.pageInfo?.currentPage)
        assertEquals(50, record.pageInfo?.perPage)
        assertEquals(150, record.pageInfo?.total)
        assertEquals("DIRECTOR", record.edges?.first()?.role)
    }

    @Test
    fun `getMediaStaffRecord preserves nullable optional blocks`() = runTest {
        val call = mediaStaffCall()
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = mediaStaffData(),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaStaffRecord = result.getOrThrow()
        assertNull(record.edges)
        assertNull(record.pageInfo)
    }

    @Test
    fun `getMediaStaffRecord GraphQL error returns failed Result with message`() = runTest {
        val call = mediaStaffCall()
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaStaffData>(
                    data = null,
                    errors = listOf(GraphError(message = "Media staff failed")),
                ),
            ),
        )

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Media staff failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStaffRecord null body returns failed Result`() = runTest {
        val call = mediaStaffCall()
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStaffRecord null data returns failed Result`() = runTest {
        val call = mediaStaffCall()
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaStaffData>(
                    data = null,
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStaffRecord null root media returns failed Result`() = runTest {
        val call = mediaStaffCall()
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = MediaStaffData(media = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStaffRecord HTTP error returns failed Result with server message`() = runTest {
        val call = mediaStaffCall()
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(call)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(call.execute()).thenReturn(Response.error(500, errorBody))

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    @Suppress("UNCHECKED_CAST")
    private fun mediaStaffCall(): Call<GraphContainer<MediaStaffData>> = mock(Call::class.java) as Call<GraphContainer<MediaStaffData>>

    private fun mediaStaffData(
        edges: List<MediaStaffData.MediaStaffEdges?>? = null,
        pageInfo: MediaStaffData.MediaStaffPageInfo? = null,
    ): MediaStaffData = MediaStaffData(
        media = MediaStaffData.Media(
            staff = MediaStaffData.MediaStaff(
                edges = edges,
                pageInfo = pageInfo,
            ),
        ),
    )
}
