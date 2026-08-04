package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.domain.user.model.UserSettingsRecord
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.graphql.generated.UpdateUser
import com.mxt.anitrend.graphql.generated.UpdateUserData
import com.mxt.anitrend.graphql.generated.UserTitleLanguage
import com.mxt.anitrend.model.api.retro.anilist.UserService
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
    fun `updateUser success maps GraphContainer data to UserSettingsRecord`() = runTest {
        val call = updateUserCall()
        val request = UpdateUser.request(
            about = "New bio",
            displayAdultContent = true,
            scoreFormat = ScoreFormat.POINT_5,
        )
        `when`(service.updateUser(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = UpdateUserData(
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
                    errors = null,
                ),
            ),
        )

        val result = repository.updateUser(
            about = "New bio",
            displayAdultContent = true,
            scoreFormat = ScoreFormat.POINT_5,
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
        val call = updateUserCall()
        val request = UpdateUser.request(titleLanguage = UserTitleLanguage.NATIVE)
        `when`(service.updateUser(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = UpdateUserData(
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
                    errors = null,
                ),
            ),
        )

        val result = repository.updateUser(titleLanguage = UserTitleLanguage.NATIVE)

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
        val call = updateUserCall()
        val request = UpdateUser.request(profileColor = "red")
        `when`(service.updateUser(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<UpdateUserData>(
                    data = null,
                    errors = listOf(GraphError(message = "Update user failed")),
                ),
            ),
        )

        val result = repository.updateUser(profileColor = "red")

        assertTrue(result.isFailure)
        assertEquals("Update user failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateUser null body returns failed Result`() = runTest {
        val call = updateUserCall()
        val request = UpdateUser.request()
        `when`(service.updateUser(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.updateUser()

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateUser null data returns failed Result`() = runTest {
        val call = updateUserCall()
        val request = UpdateUser.request()
        `when`(service.updateUser(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<UpdateUserData>(
                    data = null,
                    errors = null,
                ),
            ),
        )

        val result = repository.updateUser()

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateUser null root user returns failed Result`() = runTest {
        val call = updateUserCall()
        val request = UpdateUser.request()
        `when`(service.updateUser(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = UpdateUserData(updateUser = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.updateUser()

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateUser HTTP error returns failed Result with server message`() = runTest {
        val call = updateUserCall()
        val request = UpdateUser.request(airingNotifications = false)
        `when`(service.updateUser(request)).thenReturn(call)
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(call.execute()).thenReturn(Response.error(500, errorBody))

        val result = repository.updateUser(airingNotifications = false)

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateUserCall(): Call<GraphContainer<UpdateUserData>> = mock(Call::class.java) as Call<GraphContainer<UpdateUserData>>
}
