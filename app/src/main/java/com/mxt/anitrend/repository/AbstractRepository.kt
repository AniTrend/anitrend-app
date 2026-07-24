package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Abstract base for all GraphQL-backed repositories.
 *
 * Provides shared infrastructure for:
 * - GraphQL response unwrapping ([handleGraphResponse])
 * - Mutation event flow ([mutationEvents] / [emitMutationEvent])
 *
 * Concrete repositories extend this class and specify their own mutation
 * sealed class as the type parameter [T].
 */
abstract class AbstractRepository<T : Any>(
    protected val ioDispatcher: CoroutineDispatcher,
) {
    protected val _mutationEvents = MutableSharedFlow<T>(replay = 1, extraBufferCapacity = 64)
    val mutationEvents: SharedFlow<T> = _mutationEvents.asSharedFlow()

    fun emitMutationEvent(event: T) {
        _mutationEvents.tryEmit(event)
    }

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
}
