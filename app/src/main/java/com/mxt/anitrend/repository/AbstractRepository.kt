package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Abstract base for all GraphQL-backed repositories.
 *
 * Provides shared infrastructure for:
 * - GraphQL response unwrapping ([handleGraphResponse])
 * - Neutral retrofit-graphql response unwrapping ([handleGraphQLResponse])
 *
 * Concrete repositories extend this class for common GraphQL handling.
 */
abstract class AbstractRepository(
    protected val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Unwraps an AniList GraphQL response, throwing on graph errors or empty data.
     */
    protected fun <R> handleGraphResponse(body: AniListContainer<R>): R {
        val graphErrors: List<GraphError>? = body.errors
        if (!graphErrors.isNullOrEmpty()) {
            throw RuntimeException(graphErrors.first().message ?: "GraphQL error")
        }
        return body.data?.result ?: throw IllegalStateException("Empty response body")
    }

    /**
     * Unwraps a neutral retrofit-graphql response envelope, throwing on graph errors
     * or absent/null data.
     *
     * @param body The neutral response envelope produced by the transport codec
     */
    protected fun <R> handleGraphQLResponse(body: GraphQLResponse<R>): R {
        val graphErrors = body.errors
        if (!graphErrors.isNullOrEmpty()) {
            throw RuntimeException(graphErrors.first().message)
        }
        val data = body.data
        val value = when (data) {
            is GraphQLData.Absent -> null
            is GraphQLData.Present -> data.value
        }
        return value ?: throw IllegalStateException("Empty response body")
    }
}
