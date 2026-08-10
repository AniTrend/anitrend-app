package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewRecord
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaOverview
import com.mxt.anitrend.graphql.generated.MediaOverviewData
import com.mxt.anitrend.graphql.generated.MediaSource
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.api.retro.anilist.MediaService
import com.mxt.anitrend.model.entity.anilist.Media
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
 * Focused tests for the MediaRepository media-overview record boundary (Lane C).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaOverviewRecordRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(MediaService::class.java)
    private val repository = MediaRepository(
        mediaService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `legacy getMediaOverview maps generated data back to legacy Media entity`() = runTest {
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        MediaOverviewData(
                            media = media(
                                id = 21,
                                type = MediaType.ANIME,
                                isFavourite = true,
                                siteUrl = "https://anilist.co/anime/21",
                                description = "Overview description",
                                genres = listOf("Action", "Adventure"),
                                studios = MediaOverviewData.MediaStudios(
                                    nodes = listOf(
                                        MediaOverviewData.MediaStudiosNodes(
                                            id = 11,
                                            isAnimationStudio = true,
                                            isFavourite = true,
                                            name = "A-1 Pictures",
                                            siteUrl = "https://anilist.co/studio/11",
                                        ),
                                    ),
                                ),
                                tags = listOf(
                                    MediaOverviewData.MediaTags(
                                        category = "Themes",
                                        description = "desc",
                                        id = 7,
                                        isAdult = false,
                                        isGeneralSpoiler = true,
                                        name = "Video Games",
                                        rank = 78,
                                    ),
                                ),
                                trailer = MediaOverviewData.MediaTrailer(
                                    id = "abc123",
                                    site = "youtube",
                                    thumbnail = "thumb.jpg",
                                ),
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaOverview(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val entity: Media = result.getOrThrow()
        assertEquals(21L, entity.id)
        assertEquals("Sword Art Online", entity.title?.userPreferred)
        assertEquals("ANIME", entity.type)
        assertEquals("banner.jpg", entity.bannerImage)
        assertEquals(true, entity.isFavourite)
        assertEquals("https://anilist.co/anime/21", entity.siteUrl)
        assertEquals(listOf("Action", "Adventure"), entity.genres)
        assertEquals("Video Games", entity.tags?.first()?.name)
        assertEquals("A-1 Pictures", entity.studios?.connection?.first()?.name)
        assertEquals("youtube", entity.trailer?.site)
        assertEquals("Overview description", entity.description)
    }

    @Test
    fun `getMediaOverviewRecord success maps GraphQLResponse data to MediaOverviewRecord`() = runTest {
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        overviewData(
                            id = 21,
                            type = MediaType.ANIME,
                            isFavourite = true,
                            siteUrl = "https://anilist.co/anime/21",
                            mediaListEntry = MediaOverviewData.MediaMediaListEntry(id = 99, status = MediaListStatus.CURRENT),
                            source = MediaSource.ORIGINAL,
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaOverviewRecord = result.getOrThrow()
        assertEquals(21L, record.id)
        assertEquals("Sword Art Online", record.titleUserPreferred)
        assertEquals("ANIME", record.type)
        assertEquals("banner.jpg", record.bannerImage)
        assertEquals(true, record.isFavourite)
        assertEquals("https://anilist.co/anime/21", record.siteUrl)
        assertEquals(99L, record.mediaListEntry?.id)
        assertEquals("CURRENT", record.mediaListEntry?.status)
        assertEquals("ORIGINAL", record.source)
    }

    @Test
    fun `getMediaOverviewRecord maps optional trailer studios and tags blocks`() = runTest {
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        MediaOverviewData(
                            media = media(
                                id = 21,
                                description = "Overview description",
                                genres = listOf("Action", "Adventure"),
                                studios = MediaOverviewData.MediaStudios(
                                    nodes = listOf(
                                        MediaOverviewData.MediaStudiosNodes(
                                            id = 11,
                                            isAnimationStudio = true,
                                            isFavourite = true,
                                            name = "A-1 Pictures",
                                            siteUrl = "https://anilist.co/studio/11",
                                        ),
                                    ),
                                ),
                                tags = listOf(
                                    MediaOverviewData.MediaTags(
                                        category = "Themes",
                                        description = "desc",
                                        id = 7,
                                        isAdult = false,
                                        isGeneralSpoiler = true,
                                        name = "Video Games",
                                        rank = 78,
                                    ),
                                ),
                                trailer = MediaOverviewData.MediaTrailer(
                                    id = "abc123",
                                    site = "youtube",
                                    thumbnail = "thumb.jpg",
                                ),
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaOverviewRecord = result.getOrThrow()
        assertEquals(listOf("Action", "Adventure"), record.genres)
        assertEquals(1, record.tags?.size)
        assertEquals("Video Games", record.tags?.first()?.name)
        assertEquals("A-1 Pictures", record.studios?.first()?.name)
        assertTrue(record.studios?.first()?.isAnimationStudio == true)
        assertEquals("youtube", record.trailer?.site)
        assertEquals("Overview description", record.description)
    }

    @Test
    fun `getMediaOverviewRecord preserves nullable optional blocks`() = runTest {
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        MediaOverviewData(
                            media = media(id = 21, isFavourite = true),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaOverviewRecord = result.getOrThrow()
        assertNull(record.trailer)
        assertNull(record.studios)
        assertNull(record.tags)
        assertNull(record.genres)
        assertNull(record.description)
        assertNull(record.mediaListEntry)
    }

    @Test
    fun `getMediaOverviewRecord forwards asHtml to the generated request`() = runTest {
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = true)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        MediaOverviewData(
                            media = media(id = 21, description = "<p>html</p>"),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false, asHtml = true)

        assertTrue(result.isSuccess)
        assertEquals("<p>html</p>", result.getOrThrow().description)
    }

    @Test
    fun `getMediaOverviewRecord GraphQL error returns failed Result with message`() = runTest {
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse<MediaOverviewData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Media overview failed")),
                ),
            ),
        )

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Media overview failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaOverviewRecord null body returns failed Result`() = runTest {
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(Response.success(null))

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaOverviewRecord null data returns failed Result`() = runTest {
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse<MediaOverviewData>(
                    data = GraphQLData.Absent,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaOverviewRecord null root media returns failed Result`() = runTest {
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(MediaOverviewData(media = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaOverviewRecord HTTP error returns failed Result with server message`() = runTest {
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(service.getMediaOverviewRecord(request)).thenReturn(Response.error(500, errorBody))

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    private fun overviewData(
        id: Int,
        type: MediaType? = null,
        isFavourite: Boolean = false,
        siteUrl: String? = null,
        mediaListEntry: MediaOverviewData.MediaMediaListEntry? = null,
        source: MediaSource? = null,
    ): MediaOverviewData = MediaOverviewData(
        media = media(
            id = id,
            type = type,
            isFavourite = isFavourite,
            siteUrl = siteUrl,
            mediaListEntry = mediaListEntry,
            source = source,
        ),
    )

    private fun media(
        id: Int,
        bannerImage: String? = "banner.jpg",
        description: String? = null,
        genres: List<String?>? = null,
        isFavourite: Boolean = false,
        mediaListEntry: MediaOverviewData.MediaMediaListEntry? = null,
        siteUrl: String? = null,
        source: MediaSource? = null,
        studios: MediaOverviewData.MediaStudios? = null,
        tags: List<MediaOverviewData.MediaTags?>? = null,
        title: MediaOverviewData.MediaTitle? = MediaOverviewData.MediaTitle(
            english = "Sword Art Online",
            native = "ソードアート・オンライン",
            romaji = "Sword Art Online",
            userPreferred = "Sword Art Online",
        ),
        trailer: MediaOverviewData.MediaTrailer? = null,
        type: MediaType? = null,
    ): MediaOverviewData.Media = MediaOverviewData.Media(
        averageScore = null,
        bannerImage = bannerImage,
        chapters = null,
        coverImage = null,
        description = description,
        duration = null,
        endDate = null,
        episodes = null,
        format = null,
        genres = genres,
        hashtag = null,
        id = id,
        isAdult = null,
        isFavourite = isFavourite,
        meanScore = null,
        mediaListEntry = mediaListEntry,
        nextAiringEpisode = null,
        season = null,
        siteUrl = siteUrl,
        source = source,
        startDate = null,
        status = null,
        studios = studios,
        tags = tags,
        title = title,
        trailer = trailer,
        type = type,
        updatedAt = null,
        volumes = null,
    )
}
