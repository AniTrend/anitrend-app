package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.GraphQLResponseError
import com.mxt.anitrend.graphql.generated.CharacterActors
import com.mxt.anitrend.graphql.generated.CharacterActorsData
import com.mxt.anitrend.graphql.generated.CharacterBase
import com.mxt.anitrend.graphql.generated.CharacterBaseData
import com.mxt.anitrend.graphql.generated.CharacterMedia
import com.mxt.anitrend.graphql.generated.CharacterMediaData
import com.mxt.anitrend.graphql.generated.CharacterOverview
import com.mxt.anitrend.graphql.generated.CharacterOverviewData
import com.mxt.anitrend.graphql.generated.CharacterRole
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StaffLanguage
import com.mxt.anitrend.model.api.retro.anilist.CharacterService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(CharacterService::class.java)
    private val repository = CharacterRepository(
        characterService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getCharacterBase success maps GraphQLResponse data to CharacterRecord`() = runTest {
        val request = CharacterBase.request(id = 1)
        `when`(service.getCharacterBase(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(characterBaseData()),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterBase(id = 1L)

        assertTrue(result.isSuccess)
        val character = result.getOrThrow()
        assertEquals(1L, character.id)
        assertEquals("Spike Spiegel", character.name)
        assertEquals("https://anilist.co/character/1", character.siteUrl)
        assertTrue(character.isFavourite)
    }

    @Test
    fun `getCharacterBase GraphQL error returns failed Result with message`() = runTest {
        val request = CharacterBase.request(id = 1)
        `when`(service.getCharacterBase(request)).thenReturn(
            Response.success(
                GraphQLResponse<CharacterBaseData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Character failed")),
                ),
            ),
        )

        val result = repository.getCharacterBase(id = 1L)

        assertTrue(result.isFailure)
        assertEquals("Character failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterBase null body returns failed Result`() = runTest {
        val request = CharacterBase.request(id = 1)
        `when`(service.getCharacterBase(request)).thenReturn(Response.success(null))

        val result = repository.getCharacterBase(id = 1L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterBase null data returns failed Result`() = runTest {
        val request = CharacterBase.request(id = 1)
        `when`(service.getCharacterBase(request)).thenReturn(
            Response.success(
                GraphQLResponse<CharacterBaseData>(
                    data = GraphQLData.Absent,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterBase(id = 1L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterBase null root returns failed Result`() = runTest {
        val request = CharacterBase.request(id = 1)
        `when`(service.getCharacterBase(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(CharacterBaseData(character = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterBase(id = 1L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterOverview success maps GraphQLResponse data to MediaCharacter`() = runTest {
        val request = CharacterOverview.request(id = 2, asHtml = true)
        `when`(service.getCharacterOverview(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(characterOverviewData()),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterOverview(id = 2L, asHtml = true)

        assertTrue(result.isSuccess)
        val character = result.getOrThrow()
        assertEquals(2L, character.id)
        assertEquals("Faye", character.name?.first)
        assertEquals("medium-faye.jpg", character.image?.medium)
        assertEquals("Space cowboy", character.description)
        assertFalse(character.isFavourite)
    }

    @Test
    fun `getCharacterOverview GraphQL error returns failed Result with message`() = runTest {
        val request = CharacterOverview.request(id = 2, asHtml = false)
        `when`(service.getCharacterOverview(request)).thenReturn(
            Response.success(
                GraphQLResponse<CharacterOverviewData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Character overview failed")),
                ),
            ),
        )

        val result = repository.getCharacterOverview(id = 2L)

        assertTrue(result.isFailure)
        assertEquals("Character overview failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterOverview null body returns failed Result`() = runTest {
        val request = CharacterOverview.request(id = 2, asHtml = false)
        `when`(service.getCharacterOverview(request)).thenReturn(Response.success(null))

        val result = repository.getCharacterOverview(id = 2L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterOverview null data returns failed Result`() = runTest {
        val request = CharacterOverview.request(id = 2, asHtml = false)
        `when`(service.getCharacterOverview(request)).thenReturn(
            Response.success(
                GraphQLResponse<CharacterOverviewData>(
                    data = GraphQLData.Absent,
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterOverview(id = 2L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterOverview null root returns failed Result`() = runTest {
        val request = CharacterOverview.request(id = 2, asHtml = false)
        `when`(service.getCharacterOverview(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(CharacterOverviewData(character = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterOverview(id = 2L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterMedia success maps GraphQLResponse data to connection page`() = runTest {
        val request = CharacterMedia.request(id = 3, page = 1, perPage = 20, sort = null, type = null)
        `when`(service.getCharacterMedia(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(characterMediaData()),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterMedia(id = 3L, page = 1, perPage = 20)

        assertTrue(result.isSuccess)
        val page = result.getOrThrow().connection
        val media = page.pageData.single()
        assertEquals(1, page.pageInfo.total)
        assertEquals(20, page.pageInfo.perPage)
        assertEquals(1, page.pageInfo.currentPage)
        assertFalse(page.pageInfo.hasNextPage())
        assertEquals(30L, media.id)
        assertEquals("Cowboy Bebop", media.title?.userPreferred)
        assertEquals("TV", media.format)
        assertEquals("FINISHED", media.status)
        assertEquals(26, media.episodes)
        assertNotNull(media.mediaListEntry)
    }

    @Test
    fun `getCharacterMedia GraphQL error returns failed Result with message`() = runTest {
        val request = CharacterMedia.request(id = 3, page = null, perPage = null, sort = null, type = null)
        `when`(service.getCharacterMedia(request)).thenReturn(
            Response.success(
                GraphQLResponse<CharacterMediaData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Character media failed")),
                ),
            ),
        )

        val result = repository.getCharacterMedia(id = 3L)

        assertTrue(result.isFailure)
        assertEquals("Character media failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterMedia null body returns failed Result`() = runTest {
        val request = CharacterMedia.request(id = 3, page = null, perPage = null, sort = null, type = null)
        `when`(service.getCharacterMedia(request)).thenReturn(Response.success(null))

        val result = repository.getCharacterMedia(id = 3L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterMedia null root returns failed Result`() = runTest {
        val request = CharacterMedia.request(id = 3, page = null, perPage = null, sort = null, type = null)
        `when`(service.getCharacterMedia(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(CharacterMediaData(character = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterMedia(id = 3L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterMedia null media connection returns failed Result`() = runTest {
        val request = CharacterMedia.request(id = 3, page = null, perPage = null, sort = null, type = null)
        `when`(service.getCharacterMedia(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        CharacterMediaData(
                            character = CharacterMediaData.Character(media = null),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterMedia(id = 3L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterActors success maps GraphQLResponse data to connection edges`() = runTest {
        val request = CharacterActors.request(id = 4, page = 1, perPage = 20, sort = null)
        `when`(service.getCharacterActors(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(characterActorsData()),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterActors(id = 4L, page = 1, perPage = 20)

        assertTrue(result.isSuccess)
        val connection = result.getOrThrow()
        val edge = connection.connection.edges.single()
        assertEquals("MAIN", edge.characterRole)
        assertEquals(40L, edge.node.id)
        assertEquals("Cowboy Bebop", edge.node.title?.userPreferred)
        assertEquals(1, connection.connection.pageInfo.total)
        assertFalse(connection.connection.pageInfo.hasNextPage())
        assertEquals(1, edge.voiceActors?.size)
        assertEquals("Kôichi Yamadera", edge.voiceActors?.single()?.name?.fullName)
        assertEquals("JAPANESE", edge.voiceActors?.single()?.language)
    }

    @Test
    fun `getCharacterActors GraphQL error returns failed Result with message`() = runTest {
        val request = CharacterActors.request(id = 4, page = null, perPage = null, sort = null)
        `when`(service.getCharacterActors(request)).thenReturn(
            Response.success(
                GraphQLResponse<CharacterActorsData>(
                    data = GraphQLData.Absent,
                    errors = listOf(GraphQLResponseError(message = "Character actors failed")),
                ),
            ),
        )

        val result = repository.getCharacterActors(id = 4L)

        assertTrue(result.isFailure)
        assertEquals("Character actors failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterActors null body returns failed Result`() = runTest {
        val request = CharacterActors.request(id = 4, page = null, perPage = null, sort = null)
        `when`(service.getCharacterActors(request)).thenReturn(Response.success(null))

        val result = repository.getCharacterActors(id = 4L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterActors null root returns failed Result`() = runTest {
        val request = CharacterActors.request(id = 4, page = null, perPage = null, sort = null)
        `when`(service.getCharacterActors(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(CharacterActorsData(character = null)),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterActors(id = 4L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterActors null media connection returns failed Result`() = runTest {
        val request = CharacterActors.request(id = 4, page = null, perPage = null, sort = null)
        `when`(service.getCharacterActors(request)).thenReturn(
            Response.success(
                GraphQLResponse(
                    data = GraphQLData.Present(
                        CharacterActorsData(
                            character = CharacterActorsData.Character(media = null),
                        ),
                    ),
                    errors = emptyList(),
                ),
            ),
        )

        val result = repository.getCharacterActors(id = 4L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    private fun characterBaseData(): CharacterBaseData = CharacterBaseData(
        character = CharacterBaseData.Character(
            id = 1,
            name = CharacterBaseData.CharacterName(
                first = "Spike",
                last = "Spiegel",
                native = "スパイク・スピーゲル",
                alternative = listOf("Spikey"),
            ),
            image = CharacterBaseData.CharacterImage(
                large = "large.jpg",
                medium = "medium.jpg",
            ),
            isFavourite = true,
            siteUrl = "https://anilist.co/character/1",
        ),
    )

    private fun characterOverviewData(): CharacterOverviewData = CharacterOverviewData(
        character = CharacterOverviewData.Character(
            id = 2,
            description = "Space cowboy",
            name = CharacterOverviewData.CharacterName(
                first = "Faye",
                last = "Valentine",
                native = "フェイ・ヴァレンタイン",
                alternative = listOf("Poker Alice"),
            ),
            image = CharacterOverviewData.CharacterImage(
                large = "large-faye.jpg",
                medium = "medium-faye.jpg",
            ),
            isFavourite = false,
            siteUrl = "https://anilist.co/character/2",
        ),
    )

    private fun characterMediaData(): CharacterMediaData = CharacterMediaData(
        character = CharacterMediaData.Character(
            media = CharacterMediaData.CharacterMedia(
                nodes = listOf(characterMediaNode()),
                pageInfo = CharacterMediaData.CharacterMediaPageInfo(
                    total = 1,
                    perPage = 20,
                    currentPage = 1,
                    lastPage = 1,
                    hasNextPage = false,
                ),
            ),
        ),
    )

    private fun characterMediaNode(): CharacterMediaData.CharacterMediaNodes = CharacterMediaData.CharacterMediaNodes(
        id = 30,
        title = CharacterMediaData.CharacterMediaNodesTitle(
            romaji = "Cowboy Bebop",
            english = "Cowboy Bebop",
            native = "カウボーイビバップ",
            userPreferred = "Cowboy Bebop",
        ),
        coverImage = CharacterMediaData.CharacterMediaNodesCoverImage(
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
        siteUrl = "https://anilist.co/anime/30",
        meanScore = 86,
        averageScore = 87,
        startDate = CharacterMediaData.CharacterMediaNodesStartDate(day = 4, month = 4, year = 1998),
        endDate = CharacterMediaData.CharacterMediaNodesEndDate(day = 25, month = 4, year = 1999),
        episodes = 26,
        chapters = null,
        volumes = null,
        isAdult = false,
        isFavourite = true,
        nextAiringEpisode = null,
        mediaListEntry = CharacterMediaData.CharacterMediaNodesMediaListEntry(
            id = 300,
            status = MediaListStatus.COMPLETED,
        ),
        updatedAt = 123,
    )

    private fun characterActorsData(): CharacterActorsData = CharacterActorsData(
        character = CharacterActorsData.Character(
            media = CharacterActorsData.CharacterMedia(
                edges = listOf(characterActorEdge()),
                pageInfo = CharacterActorsData.CharacterMediaPageInfo(
                    total = 1,
                    perPage = 20,
                    currentPage = 1,
                    lastPage = 1,
                    hasNextPage = false,
                ),
            ),
        ),
    )

    private fun characterActorEdge(): CharacterActorsData.CharacterMediaEdges = CharacterActorsData.CharacterMediaEdges(
        characterRole = CharacterRole.MAIN,
        node = CharacterActorsData.CharacterMediaEdgesNode(
            id = 40,
            title = CharacterActorsData.CharacterMediaEdgesNodeTitle(
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
        voiceActors = listOf(
            CharacterActorsData.CharacterMediaEdgesVoiceActors(
                id = 50,
                image = CharacterActorsData.CharacterMediaEdgesVoiceActorsImage(
                    large = "large-koichi.jpg",
                    medium = "medium-koichi.jpg",
                ),
                isFavourite = false,
                language = StaffLanguage.JAPANESE,
                name = CharacterActorsData.CharacterMediaEdgesVoiceActorsName(
                    alternative = null,
                    first = "Kôichi",
                    full = "Kôichi Yamadera",
                    last = "Yamadera",
                    native = "山寺宏一",
                ),
                siteUrl = "https://anilist.co/staff/50",
            ),
        ),
    )
}
