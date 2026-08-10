package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StaffBase
import com.mxt.anitrend.graphql.generated.StaffBaseData
import com.mxt.anitrend.graphql.generated.StaffCharacters
import com.mxt.anitrend.graphql.generated.StaffCharactersData
import com.mxt.anitrend.graphql.generated.StaffLanguage
import com.mxt.anitrend.graphql.generated.StaffMedia
import com.mxt.anitrend.graphql.generated.StaffMediaData
import com.mxt.anitrend.graphql.generated.StaffOverview
import com.mxt.anitrend.graphql.generated.StaffOverviewData
import com.mxt.anitrend.graphql.generated.StaffRoles
import com.mxt.anitrend.graphql.generated.StaffRolesData
import com.mxt.anitrend.model.api.retro.anilist.StaffService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class StaffRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(StaffService::class.java)
    private val repository = StaffRepository(
        staffService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getStaffBase success maps GraphQLResponse data to StaffRecord`() = runTest {
        val request = StaffBase.request(id = 5)
        `when`(service.getStaffBase(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(staffBaseData()),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffBase(id = 5L)

        assertTrue(result.isSuccess)
        val staff = result.getOrThrow()
        assertEquals(5L, staff.id)
        assertEquals("Shinichiro Watanabe", staff.name)
        assertEquals("https://anilist.co/staff/5", staff.siteUrl)
        assertTrue(staff.isFavourite)
    }

    @Test
    fun `getStaffBase GraphQL error returns failed Result with message`() = runTest {
        val request = StaffBase.request(id = 5)
        `when`(service.getStaffBase(request)).thenReturn(
            Response.success(
                GraphQLResponse<StaffBaseData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Staff failed")),
                ),
            ),
        )

        val result = repository.getStaffBase(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Staff failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffBase GraphQL errors take precedence over present data`() = runTest {
        val request = StaffBase.request(id = 5)
        `when`(service.getStaffBase(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(staffBaseData()),
                    errors = listOf(GraphQLResponseError(message = "Staff failed")),
                ),
            ),
        )

        val result = repository.getStaffBase(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Staff failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffBase null body returns failed Result`() = runTest {
        val request = StaffBase.request(id = 5)
        `when`(service.getStaffBase(request)).thenReturn(Response.success(null))

        val result = repository.getStaffBase(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffBase null root returns failed Result`() = runTest {
        val request = StaffBase.request(id = 5)
        `when`(service.getStaffBase(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(StaffBaseData(staff = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffBase(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    /**
     * The transport codec decodes a literal `"data": null` into `Present(null)`; the
     * envelope value is null at runtime even though the Kotlin type is non-null.
     */
    @Suppress("UNCHECKED_CAST")
    @Test
    fun `getStaffBase Present null data returns failed Result`() = runTest {
        val request = StaffBase.request(id = 5)
        val nullData = GraphQLData.Present<StaffBaseData?>(null) as GraphQLData<StaffBaseData>
        `when`(service.getStaffBase(request)).thenReturn(
            Response.success(
                GraphQLResponse<StaffBaseData>(
                    data = nullData,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffBase(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffBase unmapped staff name falls back correctly`() = runTest {
        val request = StaffBase.request(id = 6)
        `when`(service.getStaffBase(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        StaffBaseData(
                            staff = StaffBaseData.Staff(
                                id = 6,
                                image = null,
                                isFavourite = false,
                                language = null,
                                name = null,
                                siteUrl = null,
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffBase(id = 6L)

        assertTrue(result.isSuccess)
        val staff = result.getOrThrow()
        assertEquals(6L, staff.id)
        assertNull(staff.name)
        assertNull(staff.siteUrl)
        assertFalse(staff.isFavourite)
    }

    @Test
    fun `getStaffOverview success maps GraphQLResponse data to StaffEntity`() = runTest {
        val request = StaffOverview.request(id = 5, asHtml = false)
        `when`(service.getStaffOverview(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        StaffOverviewData(
                            staff = StaffOverviewData.Staff(
                                id = 5,
                                description = "Legendary director",
                                image = StaffOverviewData.StaffImage(
                                    large = "large.jpg",
                                    medium = "medium.jpg",
                                ),
                                isFavourite = true,
                                language = StaffLanguage.JAPANESE,
                                name = StaffOverviewData.StaffName(
                                    alternative = listOf("Watanabe Shinichiro"),
                                    first = "Shinichiro",
                                    full = "Shinichiro Watanabe",
                                    last = "Watanabe",
                                    native = "渡辺信一郎",
                                ),
                                siteUrl = "https://anilist.co/staff/5",
                            ),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffOverview(id = 5L)

        assertTrue(result.isSuccess)
        val staff = result.getOrThrow()
        assertEquals(5L, staff.id)
        assertEquals("Legendary director", staff.description)
        assertEquals("JAPANESE", staff.language)
        assertEquals("Shinichiro", staff.name?.first)
        assertEquals("Watanabe", staff.name?.last)
        assertEquals("渡辺信一郎", staff.name?.original)
        assertEquals("medium.jpg", staff.image?.medium)
        assertTrue(staff.isFavourite)
    }

    @Test
    fun `getStaffOverview GraphQL error returns failed Result with message`() = runTest {
        val request = StaffOverview.request(id = 5, asHtml = false)
        `when`(service.getStaffOverview(request)).thenReturn(
            Response.success(
                GraphQLResponse<StaffOverviewData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Staff overview failed")),
                ),
            ),
        )

        val result = repository.getStaffOverview(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Staff overview failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffOverview null body returns failed Result`() = runTest {
        val request = StaffOverview.request(id = 5, asHtml = false)
        `when`(service.getStaffOverview(request)).thenReturn(Response.success(null))

        val result = repository.getStaffOverview(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffOverview null root returns failed Result`() = runTest {
        val request = StaffOverview.request(id = 5, asHtml = false)
        `when`(service.getStaffOverview(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(StaffOverviewData(staff = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffOverview(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffCharacters success maps GraphQLResponse data to connection edges`() = runTest {
        val request = StaffCharacters.request(id = 5, onList = null, page = 1, sort = null)
        `when`(service.getStaffCharacters(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(staffCharactersData()),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffCharacters(id = 5L, page = 1)

        assertTrue(result.isSuccess)
        val edge = result.getOrThrow().connection.edges.single()
        assertEquals(10L, edge.node.id)
        assertEquals("Cowboy Bebop", edge.node.title?.userPreferred)
        assertEquals(1, edge.characters?.size)
        assertEquals("Spike Spiegel", edge.characters?.single()?.name?.fullName)
        assertEquals(1, result.getOrThrow().connection.pageInfo.total)
        assertFalse(result.getOrThrow().connection.pageInfo.hasNextPage())
    }

    @Test
    fun `getStaffCharacters GraphQL error returns failed Result with message`() = runTest {
        val request = StaffCharacters.request(id = 5, onList = null, page = null, sort = null)
        `when`(service.getStaffCharacters(request)).thenReturn(
            Response.success(
                GraphQLResponse<StaffCharactersData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Staff characters failed")),
                ),
            ),
        )

        val result = repository.getStaffCharacters(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Staff characters failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffCharacters null body returns failed Result`() = runTest {
        val request = StaffCharacters.request(id = 5, onList = null, page = null, sort = null)
        `when`(service.getStaffCharacters(request)).thenReturn(Response.success(null))

        val result = repository.getStaffCharacters(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffCharacters null root returns failed Result`() = runTest {
        val request = StaffCharacters.request(id = 5, onList = null, page = null, sort = null)
        `when`(service.getStaffCharacters(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(StaffCharactersData(staff = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffCharacters(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffCharacters null connection returns failed Result`() = runTest {
        val request = StaffCharacters.request(id = 5, onList = null, page = null, sort = null)
        `when`(service.getStaffCharacters(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        StaffCharactersData(
                            staff = StaffCharactersData.Staff(characterMedia = null),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffCharacters(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffMedia success maps GraphQLResponse data to connection page`() = runTest {
        val request = StaffMedia.request(id = 5, onList = null, page = 1, perPage = 20, sort = null, type = null)
        `when`(service.getStaffMedia(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(staffMediaData()),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffMedia(id = 5L, page = 1, perPage = 20)

        assertTrue(result.isSuccess)
        val page = result.getOrThrow().connection
        val media = page.pageData.single()
        assertEquals(20L, media.id)
        assertEquals("Cowboy Bebop", media.title?.userPreferred)
        assertEquals("TV", media.format)
        assertEquals("FINISHED", media.status)
        assertEquals(26, media.episodes)
        assertNotNull(media.mediaListEntry)
        assertEquals(1, page.pageInfo.total)
        assertFalse(page.pageInfo.hasNextPage())
    }

    @Test
    fun `getStaffMedia GraphQL error returns failed Result with message`() = runTest {
        val request = StaffMedia.request(id = 5, onList = null, page = null, perPage = null, sort = null, type = null)
        `when`(service.getStaffMedia(request)).thenReturn(
            Response.success(
                GraphQLResponse<StaffMediaData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Staff media failed")),
                ),
            ),
        )

        val result = repository.getStaffMedia(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Staff media failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffMedia null body returns failed Result`() = runTest {
        val request = StaffMedia.request(id = 5, onList = null, page = null, perPage = null, sort = null, type = null)
        `when`(service.getStaffMedia(request)).thenReturn(Response.success(null))

        val result = repository.getStaffMedia(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffMedia null root returns failed Result`() = runTest {
        val request = StaffMedia.request(id = 5, onList = null, page = null, perPage = null, sort = null, type = null)
        `when`(service.getStaffMedia(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(StaffMediaData(staff = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffMedia(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffMedia null connection returns failed Result`() = runTest {
        val request = StaffMedia.request(id = 5, onList = null, page = null, perPage = null, sort = null, type = null)
        `when`(service.getStaffMedia(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        StaffMediaData(
                            staff = StaffMediaData.Staff(staffMedia = null),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffMedia(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffRoles success maps GraphQLResponse data to connection edges`() = runTest {
        val request = StaffRoles.request(id = 5, onList = null, page = 1, perPage = 20, sort = null, type = null)
        `when`(service.getStaffRoles(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(staffRolesData()),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffRoles(id = 5L, page = 1, perPage = 20)

        assertTrue(result.isSuccess)
        val edge = result.getOrThrow().connection.edges.single()
        assertEquals("Director", edge.staffRole)
        assertEquals(30L, edge.node.id)
        assertEquals("Cowboy Bebop", edge.node.title?.userPreferred)
        assertEquals(1, result.getOrThrow().connection.pageInfo.total)
        assertFalse(result.getOrThrow().connection.pageInfo.hasNextPage())
    }

    @Test
    fun `getStaffRoles GraphQL error returns failed Result with message`() = runTest {
        val request = StaffRoles.request(id = 5, onList = null, page = null, perPage = null, sort = null, type = null)
        `when`(service.getStaffRoles(request)).thenReturn(
            Response.success(
                GraphQLResponse<StaffRolesData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Staff roles failed")),
                ),
            ),
        )

        val result = repository.getStaffRoles(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Staff roles failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffRoles null body returns failed Result`() = runTest {
        val request = StaffRoles.request(id = 5, onList = null, page = null, perPage = null, sort = null, type = null)
        `when`(service.getStaffRoles(request)).thenReturn(Response.success(null))

        val result = repository.getStaffRoles(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffRoles null root returns failed Result`() = runTest {
        val request = StaffRoles.request(id = 5, onList = null, page = null, perPage = null, sort = null, type = null)
        `when`(service.getStaffRoles(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(StaffRolesData(staff = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffRoles(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffRoles null connection returns failed Result`() = runTest {
        val request = StaffRoles.request(id = 5, onList = null, page = null, perPage = null, sort = null, type = null)
        `when`(service.getStaffRoles(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        StaffRolesData(
                            staff = StaffRolesData.Staff(staffMedia = null),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getStaffRoles(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    private fun staffCharactersData(): StaffCharactersData = StaffCharactersData(
        staff = StaffCharactersData.Staff(
            characterMedia = StaffCharactersData.StaffCharacterMedia(
                edges = listOf(
                    StaffCharactersData.StaffCharacterMediaEdges(
                        characters = listOf(
                            StaffCharactersData.StaffCharacterMediaEdgesCharacters(
                                id = 60,
                                image = StaffCharactersData.StaffCharacterMediaEdgesCharactersImage(
                                    large = "large-spike.jpg",
                                    medium = "medium-spike.jpg",
                                ),
                                isFavourite = false,
                                name = StaffCharactersData.StaffCharacterMediaEdgesCharactersName(
                                    alternative = listOf("Spikey"),
                                    first = "Spike",
                                    last = "Spiegel",
                                    native = "スパイク・スピーゲル",
                                ),
                                siteUrl = "https://anilist.co/character/60",
                            ),
                        ),
                        node = staffCharactersNode(),
                    ),
                ),
                pageInfo = StaffCharactersData.StaffCharacterMediaPageInfo(
                    total = 1,
                    perPage = null,
                    currentPage = 1,
                    lastPage = 1,
                    hasNextPage = false,
                ),
            ),
        ),
    )

    private fun staffCharactersNode(): StaffCharactersData.StaffCharacterMediaEdgesNode = StaffCharactersData.StaffCharacterMediaEdgesNode(
        id = 10,
        title = StaffCharactersData.StaffCharacterMediaEdgesNodeTitle(
            romaji = "Cowboy Bebop",
            english = "Cowboy Bebop",
            native = "カウボーイビバップ",
            userPreferred = "Cowboy Bebop",
        ),
        coverImage = null,
        bannerImage = null,
        type = MediaType.ANIME,
        format = MediaFormat.TV,
        season = MediaSeason.WINTER,
        status = MediaStatus.FINISHED,
        siteUrl = null,
        meanScore = null,
        averageScore = null,
        startDate = null,
        endDate = null,
        episodes = 26,
        chapters = null,
        volumes = null,
        isAdult = null,
        isFavourite = false,
        nextAiringEpisode = null,
        mediaListEntry = null,
        updatedAt = null,
    )

    private fun staffMediaData(): StaffMediaData = StaffMediaData(
        staff = StaffMediaData.Staff(
            staffMedia = StaffMediaData.StaffStaffMedia(
                nodes = listOf(staffMediaNode()),
                pageInfo = StaffMediaData.StaffStaffMediaPageInfo(
                    total = 1,
                    perPage = 20,
                    currentPage = 1,
                    lastPage = 1,
                    hasNextPage = false,
                ),
            ),
        ),
    )

    private fun staffMediaNode(): StaffMediaData.StaffStaffMediaNodes = StaffMediaData.StaffStaffMediaNodes(
        id = 20,
        title = StaffMediaData.StaffStaffMediaNodesTitle(
            romaji = "Cowboy Bebop",
            english = "Cowboy Bebop",
            native = "カウボーイビバップ",
            userPreferred = "Cowboy Bebop",
        ),
        coverImage = StaffMediaData.StaffStaffMediaNodesCoverImage(
            extraLarge = "extra.jpg",
            large = "large.jpg",
            medium = "medium.jpg",
            color = "#fff",
        ),
        bannerImage = "banner.jpg",
        type = MediaType.ANIME,
        format = MediaFormat.TV,
        season = MediaSeason.WINTER,
        status = MediaStatus.FINISHED,
        siteUrl = "https://anilist.co/anime/20",
        meanScore = 86,
        averageScore = 87,
        startDate = StaffMediaData.StaffStaffMediaNodesStartDate(day = 4, month = 4, year = 1998),
        endDate = StaffMediaData.StaffStaffMediaNodesEndDate(day = 25, month = 4, year = 1999),
        episodes = 26,
        chapters = null,
        volumes = null,
        isAdult = false,
        isFavourite = true,
        nextAiringEpisode = null,
        mediaListEntry = StaffMediaData.StaffStaffMediaNodesMediaListEntry(
            id = 200,
            status = MediaListStatus.COMPLETED,
        ),
        updatedAt = 123,
    )

    private fun staffRolesData(): StaffRolesData = StaffRolesData(
        staff = StaffRolesData.Staff(
            staffMedia = StaffRolesData.StaffStaffMedia(
                edges = listOf(
                    StaffRolesData.StaffStaffMediaEdges(
                        node = StaffRolesData.StaffStaffMediaEdgesNode(
                            id = 30,
                            title = StaffRolesData.StaffStaffMediaEdgesNodeTitle(
                                romaji = "Cowboy Bebop",
                                english = "Cowboy Bebop",
                                native = "カウボーイビバップ",
                                userPreferred = "Cowboy Bebop",
                            ),
                            coverImage = null,
                            bannerImage = null,
                            type = MediaType.ANIME,
                            format = MediaFormat.TV,
                            season = null,
                            status = MediaStatus.FINISHED,
                            siteUrl = null,
                            meanScore = null,
                            averageScore = null,
                            startDate = null,
                            endDate = null,
                            episodes = 26,
                            chapters = null,
                            volumes = null,
                            isAdult = null,
                            isFavourite = false,
                            nextAiringEpisode = null,
                            mediaListEntry = null,
                            updatedAt = null,
                        ),
                        staffRole = "Director",
                    ),
                ),
                pageInfo = StaffRolesData.StaffStaffMediaPageInfo(
                    total = 1,
                    perPage = 20,
                    currentPage = 1,
                    lastPage = 1,
                    hasNextPage = false,
                ),
            ),
        ),
    )

    private fun staffBaseData(): StaffBaseData = StaffBaseData(
        staff = StaffBaseData.Staff(
            id = 5,
            name = StaffBaseData.StaffName(
                first = "Shinichiro",
                last = "Watanabe",
                full = "Shinichiro Watanabe",
                native = "渡辺信一郎",
                alternative = null,
            ),
            image = null,
            isFavourite = true,
            language = null,
            siteUrl = "https://anilist.co/staff/5",
        ),
    )
}
