package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.data.store.favourite.FavouriteStoreChange
import com.mxt.anitrend.data.store.favourite.InMemoryFavouriteStore
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.domain.favourite.interactor.ToggleFavouriteInteractor
import com.mxt.anitrend.domain.favourite.model.FavouriteKey
import com.mxt.anitrend.domain.model.ToggleFavouriteCommand
import com.mxt.anitrend.graphql.generated.ToggleFavourite
import com.mxt.anitrend.graphql.generated.ToggleFavouriteData
import com.mxt.anitrend.model.api.retro.anilist.BaseService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Response

/**
 * Focused tests for the BaseRepository toggleFavourite surface.
 *
 * The mutation must unwrap and validate the `GraphQLResponse<ToggleFavouriteData>`
 * body through the shared handleGraphQLResponse before reporting success, so
 * GraphQL errors and absent/null data fail instead of silently succeeding. The
 * favourite store is owned by the interactor lane and must stay unchanged when the
 * repository reports failure.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BaseRepositoryToggleFavouriteTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(BaseService::class.java)
    private val boxQuery = mock(BoxQuery::class.java)
    private val repository = BaseRepository(
        baseService = service,
        boxQuery = boxQuery,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `toggleFavourite succeeds when the response unwraps to a non null root`() = runTest {
        val request = ToggleFavourite.request(animeId = null, mangaId = null, characterId = null, staffId = null, studioId = 7, page = null, perPage = null)
        `when`(service.toggleFavourite(request)).thenReturn(
            success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        ToggleFavouriteData(
                            toggleFavourite = ToggleFavouriteData.ToggleFavourite(
                                anime = null,
                                characters = null,
                                manga = null,
                                staff = null,
                                studios = null,
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.toggleFavourite(studioId = 7)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `toggleFavourite GraphQL error returns failed result with message`() = runTest {
        val request = ToggleFavourite.request(animeId = null, mangaId = null, characterId = null, staffId = null, studioId = 7, page = null, perPage = null)
        `when`(service.toggleFavourite(request)).thenReturn(
            success(
                GraphQLResponse<ToggleFavouriteData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Toggle favourite failed")),
                ),
            ),
        )

        val result = repository.toggleFavourite(studioId = 7)

        assertTrue(result.isFailure)
        assertEquals("Toggle favourite failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `toggleFavourite absent data returns failed result with empty response body`() = runTest {
        val request = ToggleFavourite.request(animeId = null, mangaId = null, characterId = null, staffId = null, studioId = 7, page = null, perPage = null)
        `when`(service.toggleFavourite(request)).thenReturn(
            success(
                GraphQLResponse<ToggleFavouriteData>(
                    data = GraphQLData.Absent,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.toggleFavourite(studioId = 7)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `toggleFavourite null root returns failed result with empty response body`() = runTest {
        val request = ToggleFavourite.request(animeId = null, mangaId = null, characterId = null, staffId = null, studioId = 7, page = null, perPage = null)
        `when`(service.toggleFavourite(request)).thenReturn(
            success(
                GraphQLResponse<ToggleFavouriteData>(
                    data = GraphQLData.Present(ToggleFavouriteData(toggleFavourite = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.toggleFavourite(studioId = 7)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `toggleFavourite http failure is preserved as a failed result`() = runTest {
        val request = ToggleFavourite.request(animeId = null, mangaId = null, characterId = null, staffId = null, studioId = 7, page = null, perPage = null)
        `when`(service.toggleFavourite(request)).thenReturn(
            Response.error(500, """{"errors":[{"message":"HTTP boom"}]}""".toResponseBody("application/json".toMediaType())),
        )

        val result = repository.toggleFavourite(studioId = 7)

        assertTrue(result.isFailure)
    }

    @Test
    fun `failed toggle through the interactor leaves the favourite store unchanged`() = runTest {
        val store = InMemoryFavouriteStore()
        store.apply(
            FavouriteStoreChange.FavouriteFlagReplaced(
                key = FavouriteKey.Studio(7L),
                isFavourite = false,
                revision = 1L,
            ),
        )
        val request = ToggleFavourite.request(animeId = null, mangaId = null, characterId = null, staffId = null, studioId = 7, page = null, perPage = null)
        `when`(service.toggleFavourite(request)).thenReturn(
            success(
                GraphQLResponse<ToggleFavouriteData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Toggle favourite failed")),
                ),
            ),
        )
        val interactor = ToggleFavouriteInteractor(
            baseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(
                applicationScope = backgroundScope,
                keyedMutex = KeyedMutex(backgroundScope),
                mutationRegistry = DefaultMutationRegistry(),
                operationIdGenerator = DefaultOperationIdGenerator(),
                sessionEpoch = SessionEpoch(),
            ),
            favouriteStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(ToggleFavouriteCommand(FavouriteKey.Studio(7L)))

        assertTrue(result is MutationResult.Failure)
        val committed = store.state.value.flagsByKey.getValue(FavouriteKey.Studio(7L))
        assertFalse(committed.isFavourite)
        assertEquals(1L, committed.revision)
    }

    private fun <R> success(body: GraphQLResponse<R>): Response<GraphQLResponse<R>> = Response.success(body)
}
