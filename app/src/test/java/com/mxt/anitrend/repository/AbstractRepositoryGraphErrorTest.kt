package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Focused tests for the shared AbstractRepository GraphQL unwrapping helpers.
 *
 * The legacy lane (AniListContainer/GraphError) keeps the "GraphQL error" fallback
 * for null error messages, matching the pre-migration decode behavior. The neutral
 * lane (GraphQLResponse/GraphQLResponseError) carries a non-null message in the
 * resolved retrofit-graphql API, so no fallback applies there and the message is
 * surfaced as-is.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AbstractRepositoryGraphErrorTest {

    private class ExposedRepository(
        ioDispatcher: CoroutineDispatcher,
    ) : AbstractRepository(ioDispatcher) {
        fun unwrapLegacy(body: AniListContainer<String>): String = handleGraphResponse(body)
        fun unwrapNeutral(body: GraphQLResponse<String>): String = handleGraphQLResponse(body)
    }

    private val repository = ExposedRepository(UnconfinedTestDispatcher())

    @Test
    fun `legacy helper falls back to GraphQL error when the error message is null`() {
        val body = AniListContainer<String>(
            data = null,
            errors = listOf(GraphError(message = null)),
        )

        val thrown = runCatching { repository.unwrapLegacy(body) }.exceptionOrNull()

        assertTrue(thrown is RuntimeException)
        assertEquals("GraphQL error", thrown?.message)
    }

    @Test
    fun `legacy helper surfaces the first error message when present`() {
        val body = AniListContainer<String>(
            data = null,
            errors = listOf(GraphError(message = "Legacy graph error")),
        )

        val thrown = runCatching { repository.unwrapLegacy(body) }.exceptionOrNull()

        assertTrue(thrown is RuntimeException)
        assertEquals("Legacy graph error", thrown?.message)
    }

    @Test
    fun `neutral helper surfaces the first error message`() {
        val body = GraphQLResponse<String>(
            data = GraphQLData.Absent,
            errors = listOf(GraphQLResponseError(message = "Neutral graph error")),
        )

        val thrown = runCatching { repository.unwrapNeutral(body) }.exceptionOrNull()

        assertTrue(thrown is RuntimeException)
        assertEquals("Neutral graph error", thrown?.message)
    }
}
