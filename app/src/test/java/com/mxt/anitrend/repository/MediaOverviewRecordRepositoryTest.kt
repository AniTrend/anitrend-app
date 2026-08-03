package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewRecord
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaOverview
import com.mxt.anitrend.graphql.generated.MediaOverviewData
import com.mxt.anitrend.graphql.generated.MediaSource
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
    fun `getMediaOverviewRecord success maps GraphContainer data to MediaOverviewRecord`() = runTest {
        val call = overviewCall()
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = overviewData(
                        id = 21,
                        type = MediaType.ANIME,
                        isFavourite = true,
                        siteUrl = "https://anilist.co/anime/21",
                        mediaListEntry = MediaOverviewData.MediaMediaListEntry(id = 99, status = MediaListStatus.CURRENT),
                        source = MediaSource.ORIGINAL,
                    ),
                    errors = null,
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
        val call = overviewCall()
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = MediaOverviewData(
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
                    errors = null,
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
        val call = overviewCall()
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = MediaOverviewData(
                        media = media(id = 21, isFavourite = true),
                    ),
                    errors = null,
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
        val call = overviewCall()
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = true)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = MediaOverviewData(
                        media = media(id = 21, description = "<p>html</p>"),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false, asHtml = true)

        assertTrue(result.isSuccess)
        assertEquals("<p>html</p>", result.getOrThrow().description)
    }

    @Test
    fun `getMediaOverviewRecord GraphQL error returns failed Result with message`() = runTest {
        val call = overviewCall()
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaOverviewData>(
                    data = null,
                    errors = listOf(GraphError(message = "Media overview failed")),
                ),
            ),
        )

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Media overview failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaOverviewRecord null body returns failed Result`() = runTest {
        val call = overviewCall()
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaOverviewRecord null data returns failed Result`() = runTest {
        val call = overviewCall()
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<MediaOverviewData>(
                    data = null,
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaOverviewRecord null root media returns failed Result`() = runTest {
        val call = overviewCall()
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = MediaOverviewData(media = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaOverviewRecord HTTP error returns failed Result with server message`() = runTest {
        val call = overviewCall()
        val request = MediaOverview.request(id = 21, type = MediaType.ANIME, isAdult = false, asHtml = false)
        `when`(service.getMediaOverviewRecord(request)).thenReturn(call)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(call.execute()).thenReturn(Response.error(500, errorBody))

        val result = repository.getMediaOverviewRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    @Suppress("UNCHECKED_CAST")
    private fun overviewCall(): Call<GraphContainer<MediaOverviewData>> = mock(Call::class.java) as Call<GraphContainer<MediaOverviewData>>

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
