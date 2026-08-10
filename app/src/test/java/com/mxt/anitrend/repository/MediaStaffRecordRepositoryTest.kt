package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
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
    fun `legacy getMediaStaff maps generated data back to legacy connection edges`() = runTest {
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        mediaStaffData(
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
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaStaff(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val connection = result.getOrThrow()
        val edge = connection.connection.edges.first()
        assertEquals("MAIN", edge.role)
        assertEquals(123L, edge.node.id)
        assertEquals("Shinichiro Watanabe", edge.node.name?.fullName)
        assertEquals("https://cdn.example.com/large.jpg", edge.node.image?.large)
        assertEquals(true, edge.node.isFavourite)
        assertEquals("JAPANESE", edge.node.language)
        assertEquals("https://anilist.co/staff/123", edge.node.siteUrl)
        assertEquals(1, connection.connection.pageInfo.currentPage)
        assertTrue(connection.connection.pageInfo.hasNextPage())
    }

    @Test
    fun `getMediaStaffRecord success maps GraphQLResponse data to MediaStaffRecord`() = runTest {
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        mediaStaffData(
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
                    ),
                    errors = emptyList(),
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
        val sort = listOf(StaffSort.ROLE, StaffSort.RELEVANCE, StaffSort.ID)
        val request = MediaStaff.request(
            id = 21,
            type = MediaType.ANIME,
            sort = sort,
            isAdult = false,
            page = 3,
            perPage = 50,
        )
        `when`(service.getMediaStaffRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        mediaStaffData(
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
                    ),
                    errors = emptyList(),
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
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(mediaStaffData()),
                    errors = emptyList(),
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
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse<MediaStaffData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Media staff failed")),
                ),
            ),
        )

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Media staff failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStaffRecord null body returns failed Result`() = runTest {
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(Response.success(null))

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStaffRecord null data returns failed Result`() = runTest {
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse<MediaStaffData>(
                    data = GraphQLData.Absent,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStaffRecord null root media returns failed Result`() = runTest {
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        `when`(service.getMediaStaffRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(MediaStaffData(media = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStaffRecord HTTP error returns failed Result with server message`() = runTest {
        val request = MediaStaff.request(id = 21, type = MediaType.ANIME, sort = null, isAdult = false)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(service.getMediaStaffRecord(request)).thenReturn(Response.error(500, errorBody))

        val result = repository.getMediaStaffRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

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
