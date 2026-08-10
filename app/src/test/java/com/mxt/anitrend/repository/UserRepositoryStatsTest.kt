package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.graphql.generated.UserStats
import com.mxt.anitrend.graphql.generated.UserStatsData
import com.mxt.anitrend.model.api.retro.anilist.UserService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryStatsTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(UserService::class.java)
    private val boxQuery = mock(BoxQuery::class.java)
    private val repository = UserRepository(
        userService = service,
        boxQuery = boxQuery,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getUserStats success maps transport stats to record`() = runTest {
        val request = UserStats.request(id = null, userName = "profile")
        `when`(service.getUserStats(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(statsData(animeCount = 6)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getUserStats(userName = "profile")

        assertTrue(result.isSuccess)
        val record = result.getOrThrow()
        assertEquals(6, record.anime.count)
        assertEquals("Action", record.anime.genres?.single()?.genre)
        assertEquals(0, record.manga.count)
    }

    @Test
    fun `getUserStats GraphQL error returns failed Result with message`() = runTest {
        val request = UserStats.request(id = null, userName = "profile")
        `when`(service.getUserStats(request)).thenReturn(
            Response.success(
                GraphQLResponse<UserStatsData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "User stats failed")),
                ),
            ),
        )

        val result = repository.getUserStats(userName = "profile")

        assertTrue(result.isFailure)
        assertEquals("User stats failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserStats null body returns failed Result`() = runTest {
        val request = UserStats.request(id = null, userName = "profile")
        `when`(service.getUserStats(request)).thenReturn(Response.success(null))

        val result = repository.getUserStats(userName = "profile")

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserStats null data returns failed Result`() = runTest {
        val request = UserStats.request(id = null, userName = "profile")
        `when`(service.getUserStats(request)).thenReturn(
            Response.success(
                GraphQLResponse<UserStatsData>(
                    data = GraphQLData.Absent,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getUserStats(userName = "profile")

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    private fun statsData(animeCount: Int): UserStatsData = UserStatsData(
        user = UserStatsData.User(
            statistics = UserStatsData.UserStatistics(
                anime = UserStatsData.UserStatisticsAnime(
                    chaptersRead = 0,
                    count = animeCount,
                    countries = null,
                    episodesWatched = 0,
                    formats = null,
                    genres = listOf(
                        UserStatsData.UserStatisticsAnimeGenres(
                            chaptersRead = 0,
                            count = animeCount,
                            genre = "Action",
                            meanScore = 76.5,
                            mediaIds = listOf(1, 2),
                            minutesWatched = 900,
                        ),
                    ),
                    lengths = null,
                    meanScore = 76.5,
                    minutesWatched = 900,
                    releaseYears = null,
                    scores = null,
                    staff = null,
                    standardDeviation = 0.0,
                    startYears = null,
                    statuses = null,
                    studios = null,
                    tags = null,
                    voiceActors = null,
                    volumesRead = 0,
                ),
                manga = null,
            ),
        ),
    )
}
