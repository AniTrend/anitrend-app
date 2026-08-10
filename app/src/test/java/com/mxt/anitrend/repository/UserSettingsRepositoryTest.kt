package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.domain.user.model.UserSettingsRecord
import com.mxt.anitrend.domain.user.model.UserSettingsUpdate
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.graphql.generated.UpdateUser
import com.mxt.anitrend.graphql.generated.UpdateUserData
import com.mxt.anitrend.graphql.generated.UserTitleLanguage
import com.mxt.anitrend.model.api.retro.anilist.UserService
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions
import com.mxt.anitrend.model.entity.anilist.meta.UserOptions
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import com.mxt.anitrend.model.entity.anilist.user.UserStatistics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import retrofit2.Response

/**
 * Focused tests for the [UserRepository.updateUser] server-backed settings slice.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserSettingsRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(UserService::class.java)
    private val repository = UserRepository(
        userService = service,
        boxQuery = mock(com.mxt.anitrend.base.interfaces.dao.BoxQuery::class.java),
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `updateUser success maps GraphQLResponse data to UserSettingsRecord`() = runTest {
        val request = UpdateUser.request(
            about = "New bio",
            displayAdultContent = true,
            scoreFormat = ScoreFormat.POINT_5,
        )
        `when`(service.updateUser(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        UpdateUserData(
                            updateUser = UpdateUserData.UpdateUser(
                                about = "New bio",
                                avatar = null,
                                bannerImage = null,
                                id = 7,
                                isFollowing = false,
                                mediaListOptions = UpdateUserData.UpdateUserMediaListOptions(
                                    rowOrder = "CUSTOM",
                                    scoreFormat = ScoreFormat.POINT_5,
                                ),
                                name = "mxt",
                                options = UpdateUserData.UpdateUserOptions(
                                    airingNotifications = false,
                                    displayAdultContent = true,
                                    profileColor = "purple",
                                    titleLanguage = UserTitleLanguage.ROMAJI,
                                ),
                                updatedAt = 1_700_000_000,
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.updateUser(
            UserSettingsUpdate(
                about = "New bio",
                displayAdultContent = true,
                scoreFormat = ScoreFormat.POINT_5.name,
            ),
        )

        assertTrue(result.isSuccess)
        val record: UserSettingsRecord = result.getOrThrow()
        assertEquals(7L, record.id)
        assertEquals("New bio", record.about)
        assertEquals("ROMAJI", record.titleLanguage)
        assertEquals(true, record.displayAdultContent)
        assertEquals(false, record.airingNotifications)
        assertEquals("purple", record.profileColor)
        assertEquals("POINT_5", record.scoreFormat)
        assertEquals("CUSTOM", record.rowOrder)
    }

    @Test
    fun `updateUser success preserves nullable settings blocks`() = runTest {
        val request = UpdateUser.request(titleLanguage = UserTitleLanguage.NATIVE)
        `when`(service.updateUser(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        UpdateUserData(
                            updateUser = UpdateUserData.UpdateUser(
                                about = null,
                                avatar = null,
                                bannerImage = null,
                                id = 7,
                                isFollowing = null,
                                mediaListOptions = null,
                                name = "mxt",
                                options = null,
                                updatedAt = null,
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.updateUser(UserSettingsUpdate(titleLanguage = UserTitleLanguage.NATIVE.name))

        assertTrue(result.isSuccess)
        val record: UserSettingsRecord = result.getOrThrow()
        assertNull(record.about)
        assertNull(record.titleLanguage)
        assertNull(record.displayAdultContent)
        assertNull(record.airingNotifications)
        assertNull(record.profileColor)
        assertNull(record.scoreFormat)
        assertNull(record.rowOrder)
    }

    @Test
    fun `updateUser GraphQL error returns failed Result with message`() = runTest {
        val request = UpdateUser.request(profileColor = "red")
        `when`(service.updateUser(request)).thenReturn(
            Response.success(
                GraphQLResponse<UpdateUserData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Update user failed")),
                ),
            ),
        )

        val result = repository.updateUser(UserSettingsUpdate(profileColor = "red"))

        assertTrue(result.isFailure)
        assertEquals("Update user failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateUser null body returns failed Result`() = runTest {
        val request = UpdateUser.request()
        `when`(service.updateUser(request)).thenReturn(Response.success(null))

        val result = repository.updateUser()

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateUser null data returns failed Result`() = runTest {
        val request = UpdateUser.request()
        `when`(service.updateUser(request)).thenReturn(
            Response.success(
                GraphQLResponse<UpdateUserData>(
                    data = GraphQLData.Absent,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.updateUser()

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateUser null root user returns failed Result`() = runTest {
        val request = UpdateUser.request()
        `when`(service.updateUser(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(UpdateUserData(updateUser = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.updateUser()

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateUser HTTP error returns failed Result with server message`() = runTest {
        val request = UpdateUser.request(airingNotifications = false)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(service.updateUser(request)).thenReturn(Response.error(500, errorBody))

        val result = repository.updateUser(UserSettingsUpdate(airingNotifications = false))

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    // ── cache merge on success ──

    @Test
    fun `updateUser success merges the returned slice into the cached current user`() = runTest {
        val boxQuery = mock(BoxQuery::class.java)
        val cached = createCachedUser()
        `when`(boxQuery.currentUser).thenReturn(cached)
        val repositoryWithCache = UserRepository(
            userService = service,
            boxQuery = boxQuery,
            ioDispatcher = testDispatcher,
        )
        val request = UpdateUser.request(about = "New bio")
        `when`(service.updateUser(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        UpdateUserData(
                            updateUser = UpdateUserData.UpdateUser(
                                about = "New bio",
                                avatar = UpdateUserData.UpdateUserAvatar(
                                    large = "https://avatar-large",
                                    medium = "https://avatar-medium",
                                ),
                                bannerImage = "https://banner-new",
                                id = 7,
                                isFollowing = false,
                                mediaListOptions = UpdateUserData.UpdateUserMediaListOptions(
                                    rowOrder = "CUSTOM",
                                    scoreFormat = ScoreFormat.POINT_5,
                                ),
                                name = "updated-name",
                                options = UpdateUserData.UpdateUserOptions(
                                    airingNotifications = false,
                                    displayAdultContent = true,
                                    profileColor = "purple",
                                    titleLanguage = UserTitleLanguage.NATIVE,
                                ),
                                updatedAt = 1_700_000_000,
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repositoryWithCache.updateUser(UserSettingsUpdate(about = "New bio"))

        assertTrue(result.isSuccess)
        // Fields returned by the mutation are merged into the cached entity.
        assertEquals("updated-name", cached.name)
        assertEquals("https://avatar-large", cached.avatar?.large)
        assertEquals("https://banner-new", cached.bannerImage)
        assertEquals("New bio", cached.about)
        assertEquals("NATIVE", cached.options?.titleLanguage)
        assertEquals(true, cached.options?.isDisplayAdultContent)
        assertEquals("purple", cached.options?.profileColor)
        assertEquals("POINT_5", cached.mediaListOptions.scoreFormat)
        assertEquals("CUSTOM", cached.mediaListOptions.rowOrder)
        // Fields not returned by the mutation are preserved.
        assertEquals(7L, cached.id)
        assertTrue(cached.isFollowing)
        assertEquals(42, cached.stats?.watchedTime)
        assertEquals(5, cached.unreadNotificationCount)
        assertTrue(cached.statistics?.anime?.count == 11)
        // The merged entity is persisted through the saveCurrentUser seam.
        verify(boxQuery).currentUser = cached
    }

    @Test
    fun `updateUser success without a cached user skips the cache merge`() = runTest {
        val boxQuery = mock(BoxQuery::class.java)
        val repositoryWithoutCache = UserRepository(
            userService = service,
            boxQuery = boxQuery,
            ioDispatcher = testDispatcher,
        )
        val request = UpdateUser.request(about = "New bio")
        `when`(service.updateUser(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        UpdateUserData(
                            updateUser = UpdateUserData.UpdateUser(
                                about = "New bio",
                                avatar = null,
                                bannerImage = null,
                                id = 7,
                                isFollowing = null,
                                mediaListOptions = null,
                                name = "mxt",
                                options = null,
                                updatedAt = null,
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repositoryWithoutCache.updateUser(UserSettingsUpdate(about = "New bio"))

        assertTrue(result.isSuccess)
        assertEquals("New bio", result.getOrThrow().about)
        verify(boxQuery, never()).currentUser = any(User::class.java)
    }

    private fun createCachedUser(): User = User().also {
        it.id = 7L
        it.name = "mxt"
        it.bannerImage = "https://banner-old"
        it.about = "Old bio"
        it.options = UserOptions(
            titleLanguage = "ROMAJI",
            isDisplayAdultContent = false,
            isAiringNotifications = true,
            profileColor = "blue",
        )
        it.mediaListOptions = MediaListOptions(scoreFormat = "POINT_10", rowOrder = null)
        it.isFollowing = true
        it.stats = com.mxt.anitrend.model.entity.anilist.UserStats(watchedTime = 42)
        it.statistics = UserStatisticTypes(
            anime = statistics(count = 11),
            manga = statistics(count = 22),
        )
        it.unreadNotificationCount = 5
    }

    private fun statistics(count: Int): UserStatistics = UserStatistics(
        chaptersRead = 0,
        count = count,
        countries = null,
        episodesWatched = 0,
        formats = null,
        genres = null,
        lengths = null,
        meanScore = 0f,
        minutesWatched = 0,
        releaseYears = null,
        scores = null,
        staff = null,
        standardDeviation = 0f,
        startYears = null,
        statuses = null,
        studios = null,
        tags = null,
        voiceActors = null,
        volumesRead = 0,
    )
}
