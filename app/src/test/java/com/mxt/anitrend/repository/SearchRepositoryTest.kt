package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import com.mxt.anitrend.graphql.generated.CharacterSearch
import com.mxt.anitrend.graphql.generated.CharacterSearchData
import com.mxt.anitrend.graphql.generated.MediaSearch
import com.mxt.anitrend.graphql.generated.MediaSearchData
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StaffSearch
import com.mxt.anitrend.graphql.generated.StaffSearchData
import com.mxt.anitrend.graphql.generated.StudioSearch
import com.mxt.anitrend.graphql.generated.StudioSearchData
import com.mxt.anitrend.graphql.generated.UserSearch
import com.mxt.anitrend.graphql.generated.UserSearchData
import com.mxt.anitrend.model.api.retro.anilist.SearchService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Response

/**
 * Focused tests for the SearchRepository page handlers.
 *
 * A present Page block maps into legacy entities, while a null generated Page
 * root must fail with "Empty response body" instead of returning a successful
 * empty page container, matching the previous AniListContainer semantics.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(SearchService::class.java)
    private val repository = SearchRepository(
        searchService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `searchMedia maps a present page into legacy media entities`() = runTest {
        val request = MediaSearch.request(id = null, page = 1, perPage = 20, search = "naruto", type = MediaType.ANIME, format = null, startDate = null, endDate = null, season = null, genres = null, genresExclude = null, isAdult = null, sort = null)
        `when`(service.getMediaSearch(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        MediaSearchData(
                            page = MediaSearchData.Page(
                                media = listOf(media(id = 42)),
                                pageInfo = MediaSearchData.PagePageInfo(
                                    currentPage = 1,
                                    hasNextPage = false,
                                    lastPage = 1,
                                    perPage = 20,
                                    total = 1,
                                ),
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.searchMedia(page = 1, perPage = 20, search = "naruto", type = MediaType.ANIME)

        assertTrue(result.isSuccess)
        val page = result.getOrThrow()
        assertEquals(42L, page.pageData.single().id)
        assertEquals(MediaType.ANIME.name, page.pageData.single().type)
        assertTrue(page.hasPageInfo())
        assertEquals(1, page.pageInfo.currentPage)
    }

    @Test
    fun `searchMedia with null page fails with empty response body`() = runTest {
        val request = MediaSearch.request(id = null, page = 1, perPage = 20, search = "naruto", type = MediaType.ANIME, format = null, startDate = null, endDate = null, season = null, genres = null, genresExclude = null, isAdult = null, sort = null)
        `when`(service.getMediaSearch(request)).thenReturn(
            Response.success(
                GraphQLResponse(data = GraphQLData.Present(MediaSearchData(page = null)), errors = emptyList()),
            ),
        )

        val result = repository.searchMedia(page = 1, perPage = 20, search = "naruto", type = MediaType.ANIME)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `searchStudio with null page fails with empty response body`() = runTest {
        val request = StudioSearch.request(id = null, page = 1, perPage = 20, search = "ufotable", sort = null)
        `when`(service.getStudioSearch(request)).thenReturn(
            Response.success(
                GraphQLResponse(data = GraphQLData.Present(StudioSearchData(page = null)), errors = emptyList()),
            ),
        )

        val result = repository.searchStudio(page = 1, perPage = 20, search = "ufotable")

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `searchStaff with null page fails with empty response body`() = runTest {
        val request = StaffSearch.request(id = null, page = 1, perPage = 20, search = "sawashiro", sort = null)
        `when`(service.getStaffSearch(request)).thenReturn(
            Response.success(
                GraphQLResponse(data = GraphQLData.Present(StaffSearchData(page = null)), errors = emptyList()),
            ),
        )

        val result = repository.searchStaff(page = 1, perPage = 20, search = "sawashiro")

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `searchCharacter with null page fails with empty response body`() = runTest {
        val request = CharacterSearch.request(id = null, page = 1, perPage = 20, search = "goku", sort = null)
        `when`(service.getCharacterSearch(request)).thenReturn(
            Response.success(
                GraphQLResponse(data = GraphQLData.Present(CharacterSearchData(page = null)), errors = emptyList()),
            ),
        )

        val result = repository.searchCharacter(page = 1, perPage = 20, search = "goku")

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `searchUser with null page fails with empty response body`() = runTest {
        val request = UserSearch.request(id = null, page = 1, perPage = 20, search = "max", sort = null)
        `when`(service.getUserSearch(request)).thenReturn(
            Response.success(
                GraphQLResponse(data = GraphQLData.Present(UserSearchData(page = null)), errors = emptyList()),
            ),
        )

        val result = repository.searchUser(page = 1, perPage = 20, search = "max")

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    private fun media(id: Int): MediaSearchData.PageMedia = MediaSearchData.PageMedia(
        averageScore = null,
        bannerImage = null,
        chapters = null,
        coverImage = null,
        endDate = null,
        episodes = null,
        format = null,
        id = id,
        isAdult = null,
        isFavourite = false,
        meanScore = null,
        mediaListEntry = null,
        nextAiringEpisode = null,
        season = null,
        siteUrl = null,
        startDate = null,
        status = null,
        title = null,
        type = MediaType.ANIME,
        updatedAt = null,
        volumes = null,
    )
}
