package com.mxt.anitrend.repository

import com.mxt.anitrend.model.api.retro.crunchy.EpisodeService
import com.mxt.anitrend.model.entity.crunchy.Channel
import com.mxt.anitrend.model.entity.crunchy.Rss
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Response

/**
 * Focused tests for the CrunchyrollRepository suspend response boundary.
 *
 * Transport exceptions thrown by the service must surface as Result.failure
 * instead of escaping the withContext/runCatching boundary, while successful,
 * HTTP-error, and empty-body responses keep their existing behavior.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CrunchyrollRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val feedService = mock(EpisodeService::class.java)
    private val crunchyrollService = mock(EpisodeService::class.java)
    private val repository = CrunchyrollRepository(
        feedService = feedService,
        crunchyrollService = crunchyrollService,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getLatestFeed returns failure when service throws transport exception`() = runTest {
        val transportError = IOException("network unreachable")
        `when`(feedService.getLatestFeed()).thenAnswer { throw transportError }

        val result = repository.getLatestFeed()

        assertTrue(result.isFailure)
        assertEquals(transportError, result.exceptionOrNull())
    }

    @Test
    fun `getPopularFeed returns failure when service throws transport exception`() = runTest {
        val transportError = IOException("network unreachable")
        `when`(feedService.getPopularFeed()).thenAnswer { throw transportError }

        val result = repository.getPopularFeed()

        assertTrue(result.isFailure)
        assertEquals(transportError, result.exceptionOrNull())
    }

    @Test
    fun `getRss returns failure when service throws transport exception`() = runTest {
        val transportError = IOException("network unreachable")
        `when`(crunchyrollService.getRssByUrl("https://example.com/rss")).thenAnswer { throw transportError }

        val result = repository.getRss("https://example.com/rss")

        assertTrue(result.isFailure)
        assertEquals(transportError, result.exceptionOrNull())
    }

    @Test
    fun `getLatestFeed returns body for successful response`() = runTest {
        val rss = Rss(Channel())
        `when`(feedService.getLatestFeed()).thenReturn(Response.success(rss))

        val result = repository.getLatestFeed()

        assertTrue(result.isSuccess)
        assertEquals(rss, result.getOrThrow())
    }

    @Test
    fun `getPopularFeed returns failure with apiError message for HTTP error`() = runTest {
        val errorBody = """{"errors":[{"message":"Server exploded"}]}"""
            .toResponseBody("application/json".toMediaType())
        `when`(feedService.getPopularFeed()).thenReturn(Response.error(500, errorBody))

        val result = repository.getPopularFeed()

        assertTrue(result.isFailure)
        assertEquals("Server exploded", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getRss returns failure with empty body message when body is null`() = runTest {
        `when`(crunchyrollService.getRssByUrl(null)).thenReturn(Response.success(null))

        val result = repository.getRss(null)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }
}
