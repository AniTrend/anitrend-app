package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StudioBase
import com.mxt.anitrend.graphql.generated.StudioBaseData
import com.mxt.anitrend.graphql.generated.StudioMedia
import com.mxt.anitrend.graphql.generated.StudioMediaData
import com.mxt.anitrend.model.api.retro.anilist.StudioService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import com.mxt.anitrend.util.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Call
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class StudioRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(StudioService::class.java)
    private val settings = mock(Settings::class.java)
    private val repository = StudioRepository(
        studioService = service,
        settings = settings,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getStudioBase success maps GraphContainer data to StudioRecord`() = runTest {
        val call = studioBaseCall()
        val request = StudioBase.request(id = 5)
        `when`(service.getStudioBase(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = StudioBaseData(
                        studio = StudioBaseData.Studio(
                            id = 5,
                            name = "Kyoto Animation",
                            isAnimationStudio = true,
                            isFavourite = true,
                            siteUrl = "https://anilist.co/studio/5",
                        ),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getStudioBase(id = 5L)

        assertTrue(result.isSuccess)
        val studio = result.getOrThrow()
        assertEquals(5L, studio.id)
        assertEquals("Kyoto Animation", studio.name)
        assertEquals("https://anilist.co/studio/5", studio.siteUrl)
        assertTrue(studio.isFavourite)
    }

    @Test
    fun `getStudioBase GraphQL error returns failed Result with message`() = runTest {
        val call = studioBaseCall()
        val request = StudioBase.request(id = 5)
        `when`(service.getStudioBase(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<StudioBaseData>(
                    data = null,
                    errors = listOf(GraphError(message = "Studio failed")),
                ),
            ),
        )

        val result = repository.getStudioBase(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Studio failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStudioBase null body returns failed Result`() = runTest {
        val call = studioBaseCall()
        val request = StudioBase.request(id = 5)
        `when`(service.getStudioBase(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.getStudioBase(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStudioBase null root returns failed Result`() = runTest {
        val call = studioBaseCall()
        val request = StudioBase.request(id = 5)
        `when`(service.getStudioBase(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = StudioBaseData(studio = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getStudioBase(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStudioMedia success maps GraphContainer data to connection page`() = runTest {
        val call = studioMediaCall()
        val request = StudioMedia.request(
            id = 5,
            page = 1,
            perPage = 20,
            sort = listOf(MediaSort.POPULARITY_DESC),
        )
        `when`(service.getStudioMedia(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = StudioMediaData(
                        studio = StudioMediaData.Studio(
                            media = StudioMediaData.StudioMedia(
                                nodes = listOf(studioMediaNode()),
                                pageInfo = StudioMediaData.StudioMediaPageInfo(
                                    total = 1,
                                    perPage = 20,
                                    currentPage = 1,
                                    lastPage = 1,
                                    hasNextPage = false,
                                ),
                            ),
                        ),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getStudioMedia(
            id = 5L,
            page = 1,
            perPage = 20,
            sort = listOf(MediaSort.POPULARITY_DESC),
        )

        assertTrue(result.isSuccess)
        val page = result.getOrThrow().connection
        val media = page.pageData.single()
        assertEquals(1, page.pageInfo.total)
        assertEquals(20, page.pageInfo.perPage)
        assertEquals(1, page.pageInfo.currentPage)
        assertFalse(page.pageInfo.hasNextPage())
        assertEquals(10L, media.id)
        assertEquals("Violet Evergarden", media.title?.userPreferred)
        assertEquals("TV", media.format)
        assertEquals("FINISHED", media.status)
        assertEquals(13, media.episodes)
        assertNotNull(media.mediaListEntry)
    }

    @Test
    fun `getStudioMedia GraphQL error returns failed Result with message`() = runTest {
        val call = studioMediaCall()
        val request = StudioMedia.request(id = 5, page = null, perPage = null, sort = null)
        `when`(service.getStudioMedia(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<StudioMediaData>(
                    data = null,
                    errors = listOf(GraphError(message = "Studio media failed")),
                ),
            ),
        )

        val result = repository.getStudioMedia(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Studio media failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStudioMedia null body returns failed Result`() = runTest {
        val call = studioMediaCall()
        val request = StudioMedia.request(id = 5, page = null, perPage = null, sort = null)
        `when`(service.getStudioMedia(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.getStudioMedia(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStudioMedia null root returns failed Result`() = runTest {
        val call = studioMediaCall()
        val request = StudioMedia.request(id = 5, page = null, perPage = null, sort = null)
        `when`(service.getStudioMedia(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = StudioMediaData(studio = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getStudioMedia(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Suppress("UNCHECKED_CAST")
    private fun studioBaseCall(): Call<GraphContainer<StudioBaseData>> = mock(Call::class.java) as Call<GraphContainer<StudioBaseData>>

    @Suppress("UNCHECKED_CAST")
    private fun studioMediaCall(): Call<GraphContainer<StudioMediaData>> = mock(Call::class.java) as Call<GraphContainer<StudioMediaData>>

    private fun studioMediaNode(): StudioMediaData.StudioMediaNodes = StudioMediaData.StudioMediaNodes(
        id = 10,
        title = StudioMediaData.StudioMediaNodesTitle(
            romaji = "Violet Evergarden",
            english = "Violet Evergarden",
            native = "ヴァイオレット・エヴァーガーデン",
            userPreferred = "Violet Evergarden",
        ),
        coverImage = StudioMediaData.StudioMediaNodesCoverImage(
            extraLarge = "extra.jpg",
            large = "large.jpg",
            medium = "medium.jpg",
            color = "#fff",
        ),
        bannerImage = "banner.jpg",
        type = MediaType.ANIME,
        format = MediaFormat.TV,
        season = MediaSeason.WINTER,
        status = MediaStatus.FINISHED,
        siteUrl = "https://anilist.co/anime/10",
        meanScore = 84,
        averageScore = 85,
        startDate = StudioMediaData.StudioMediaNodesStartDate(day = 11, month = 1, year = 2018),
        endDate = StudioMediaData.StudioMediaNodesEndDate(day = 5, month = 4, year = 2018),
        episodes = 13,
        chapters = null,
        volumes = null,
        isAdult = false,
        isFavourite = true,
        nextAiringEpisode = StudioMediaData.StudioMediaNodesNextAiringEpisode(
            id = 100,
            mediaId = 10,
            airingAt = 123456789,
            timeUntilAiring = 3600,
            episode = 14,
        ),
        mediaListEntry = StudioMediaData.StudioMediaNodesMediaListEntry(
            id = 200,
            status = MediaListStatus.COMPLETED,
        ),
        updatedAt = 123,
    )
}
