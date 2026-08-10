package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
import com.mxt.anitrend.domain.mediadetail.model.MediaStatsRecord
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaRankType
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaStats
import com.mxt.anitrend.graphql.generated.MediaStatsData
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
 * Focused tests for the MediaRepository media-stats record boundary (Lane C).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaStatsRecordRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(MediaService::class.java)
    private val repository = MediaRepository(
        mediaService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `legacy getMediaStats maps generated data back to legacy Media entity`() = runTest {
        val request = MediaStats.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaStatsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        mediaStatsData(
                            type = MediaType.ANIME,
                            externalLinks = listOf(
                                MediaStatsData.MediaExternalLinks(
                                    id = 301,
                                    site = "Crunchyroll",
                                    url = "https://www.crunchyroll.com/series/123",
                                ),
                            ),
                            scoreDistribution = listOf(
                                MediaStatsData.MediaStatsScoreDistribution(amount = 452, score = 90),
                            ),
                            statusDistribution = listOf(
                                MediaStatsData.MediaStatsStatusDistribution(amount = 1200, status = MediaListStatus.COMPLETED),
                            ),
                            rankings = listOf(
                                MediaStatsData.MediaRankings(
                                    allTime = true,
                                    context = "highest rated",
                                    format = MediaFormat.TV,
                                    id = 501,
                                    rank = 7,
                                    season = MediaSeason.WINTER,
                                    type = MediaRankType.RATED,
                                    year = 2012,
                                ),
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaStats(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val entity: Media = result.getOrThrow()
        assertEquals("ANIME", entity.type)
        assertEquals(1, entity.externalLinks?.size)
        assertEquals(301, entity.externalLinks?.first()?.id)
        assertEquals("Crunchyroll", entity.externalLinks?.first()?.site)
        assertEquals(90, entity.stats?.scoreDistribution?.first()?.score)
        assertEquals(452, entity.stats?.scoreDistribution?.first()?.amount)
        assertEquals("COMPLETED", entity.stats?.statusDistribution?.first()?.status)
        assertEquals(1200, entity.stats?.statusDistribution?.first()?.amount)
        assertEquals(1, entity.rankings?.size)
        assertEquals(501, entity.rankings?.first()?.id)
        assertEquals("RATED", entity.rankings?.first()?.type)
        assertEquals("WINTER", entity.rankings?.first()?.season)
        assertTrue(entity.rankings?.first()?.isAllTime == true)
    }

    @Test
    fun `getMediaStatsRecord success maps GraphQLResponse data to MediaStatsRecord`() = runTest {
        val request = MediaStats.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaStatsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        mediaStatsData(
                            type = MediaType.ANIME,
                            externalLinks = listOf(
                                MediaStatsData.MediaExternalLinks(
                                    id = 301,
                                    site = "Crunchyroll",
                                    url = "https://www.crunchyroll.com/series/123",
                                ),
                            ),
                            scoreDistribution = listOf(
                                MediaStatsData.MediaStatsScoreDistribution(amount = 452, score = 90),
                            ),
                            statusDistribution = listOf(
                                MediaStatsData.MediaStatsStatusDistribution(amount = 1200, status = MediaListStatus.COMPLETED),
                            ),
                            rankings = listOf(
                                MediaStatsData.MediaRankings(
                                    allTime = true,
                                    context = "highest rated",
                                    format = MediaFormat.TV,
                                    id = 501,
                                    rank = 7,
                                    season = MediaSeason.WINTER,
                                    type = MediaRankType.RATED,
                                    year = 2012,
                                ),
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaStatsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaStatsRecord = result.getOrThrow()
        assertEquals("ANIME", record.type)
        assertEquals(1, record.externalLinks?.size)
        assertEquals(301L, record.externalLinks?.first()?.id)
        assertEquals("Crunchyroll", record.externalLinks?.first()?.site)
        assertEquals(90, record.scoreDistribution?.first()?.score)
        assertEquals(452, record.scoreDistribution?.first()?.amount)
        assertEquals("COMPLETED", record.statusDistribution?.first()?.status)
        assertEquals(1200, record.statusDistribution?.first()?.amount)
        assertEquals(1, record.rankings?.size)
        assertEquals(501L, record.rankings?.first()?.id)
        assertEquals("RATED", record.rankings?.first()?.type)
        assertEquals("WINTER", record.rankings?.first()?.season)
        assertTrue(record.rankings?.first()?.allTime == true)
    }

    @Test
    fun `getMediaStatsRecord preserves nullable optional blocks`() = runTest {
        val request = MediaStats.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaStatsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(mediaStatsData()),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaStatsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isSuccess)
        val record: MediaStatsRecord = result.getOrThrow()
        assertNull(record.type)
        assertNull(record.externalLinks)
        assertNull(record.scoreDistribution)
        assertNull(record.statusDistribution)
        assertNull(record.rankings)
    }

    @Test
    fun `getMediaStatsRecord GraphQL error returns failed Result with message`() = runTest {
        val request = MediaStats.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaStatsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse<MediaStatsData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Media stats failed")),
                ),
            ),
        )

        val result = repository.getMediaStatsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Media stats failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStatsRecord null body returns failed Result`() = runTest {
        val request = MediaStats.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaStatsRecord(request)).thenReturn(Response.success(null))

        val result = repository.getMediaStatsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStatsRecord null data returns failed Result`() = runTest {
        val request = MediaStats.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaStatsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse<MediaStatsData>(
                    data = GraphQLData.Absent,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaStatsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStatsRecord null root media returns failed Result`() = runTest {
        val request = MediaStats.request(id = 21, type = MediaType.ANIME, isAdult = false)
        `when`(service.getMediaStatsRecord(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(MediaStatsData(media = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getMediaStatsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getMediaStatsRecord HTTP error returns failed Result with server message`() = runTest {
        val request = MediaStats.request(id = 21, type = MediaType.ANIME, isAdult = false)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(service.getMediaStatsRecord(request)).thenReturn(Response.error(500, errorBody))

        val result = repository.getMediaStatsRecord(id = 21L, type = MediaType.ANIME, isAdult = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    private fun mediaStatsData(
        type: MediaType? = null,
        externalLinks: List<MediaStatsData.MediaExternalLinks?>? = null,
        scoreDistribution: List<MediaStatsData.MediaStatsScoreDistribution?>? = null,
        statusDistribution: List<MediaStatsData.MediaStatsStatusDistribution?>? = null,
        rankings: List<MediaStatsData.MediaRankings?>? = null,
    ): MediaStatsData = MediaStatsData(
        media = MediaStatsData.Media(
            externalLinks = externalLinks,
            rankings = rankings,
            stats = MediaStatsData.MediaStats(
                scoreDistribution = scoreDistribution,
                statusDistribution = statusDistribution,
            ),
            type = type,
        ),
    )
}
