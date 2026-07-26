package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.graphql.generated.CharacterActors
import com.mxt.anitrend.graphql.generated.CharacterBase
import com.mxt.anitrend.graphql.generated.CharacterBaseData
import com.mxt.anitrend.graphql.generated.CharacterMedia
import com.mxt.anitrend.graphql.generated.CharacterOverview
import com.mxt.anitrend.graphql.generated.CharacterOverviewData
import com.mxt.anitrend.model.api.retro.anilist.CharacterModel
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.DataContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Call
import retrofit2.Response
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(CharacterModel::class.java)
    private val repository = CharacterRepository(
        characterService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getCharacterBase success maps GraphContainer data to CharacterEntity`() = runTest {
        val call = characterBaseCall()
        val request = CharacterBase.request(id = 1)
        `when`(service.getCharacterBase(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = characterBaseData(),
                    errors = null,
                ),
            ),
        )

        val result = repository.getCharacterBase(id = 1L)

        assertTrue(result.isSuccess)
        val character = result.getOrThrow()
        assertEquals(1L, character.id)
        assertEquals("Spike", character.name?.first)
        assertEquals("large.jpg", character.image?.large)
        assertTrue(character.isFavourite)
    }

    @Test
    fun `getCharacterBase GraphQL error returns failed Result with message`() = runTest {
        val call = characterBaseCall()
        val request = CharacterBase.request(id = 1)
        `when`(service.getCharacterBase(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<CharacterBaseData>(
                    data = null,
                    errors = listOf(GraphError(message = "Character failed")),
                ),
            ),
        )

        val result = repository.getCharacterBase(id = 1L)

        assertTrue(result.isFailure)
        assertEquals("Character failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterBase null body returns failed Result`() = runTest {
        val call = characterBaseCall()
        val request = CharacterBase.request(id = 1)
        `when`(service.getCharacterBase(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.getCharacterBase(id = 1L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterBase null data returns failed Result`() = runTest {
        val call = characterBaseCall()
        val request = CharacterBase.request(id = 1)
        `when`(service.getCharacterBase(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<CharacterBaseData>(
                    data = null,
                    errors = null,
                ),
            ),
        )

        val result = repository.getCharacterBase(id = 1L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterBase null root returns failed Result`() = runTest {
        val call = characterBaseCall()
        val request = CharacterBase.request(id = 1)
        `when`(service.getCharacterBase(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = CharacterBaseData(character = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getCharacterBase(id = 1L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterOverview success maps GraphContainer data to MediaCharacter`() = runTest {
        val call = characterOverviewCall()
        val request = CharacterOverview.request(id = 2, asHtml = true)
        `when`(service.getCharacterOverview(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = characterOverviewData(),
                    errors = null,
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
        val call = characterOverviewCall()
        val request = CharacterOverview.request(id = 2, asHtml = false)
        `when`(service.getCharacterOverview(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<CharacterOverviewData>(
                    data = null,
                    errors = listOf(GraphError(message = "Character overview failed")),
                ),
            ),
        )

        val result = repository.getCharacterOverview(id = 2L)

        assertTrue(result.isFailure)
        assertEquals("Character overview failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterOverview null body returns failed Result`() = runTest {
        val call = characterOverviewCall()
        val request = CharacterOverview.request(id = 2, asHtml = false)
        `when`(service.getCharacterOverview(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(Response.success(null))

        val result = repository.getCharacterOverview(id = 2L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterOverview null data returns failed Result`() = runTest {
        val call = characterOverviewCall()
        val request = CharacterOverview.request(id = 2, asHtml = false)
        `when`(service.getCharacterOverview(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer<CharacterOverviewData>(
                    data = null,
                    errors = null,
                ),
            ),
        )

        val result = repository.getCharacterOverview(id = 2L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterOverview null root returns failed Result`() = runTest {
        val call = characterOverviewCall()
        val request = CharacterOverview.request(id = 2, asHtml = false)
        `when`(service.getCharacterOverview(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                GraphContainer(
                    data = CharacterOverviewData(character = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getCharacterOverview(id = 2L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCharacterMedia keeps legacy AniListContainer response handling`() = runTest {
        val call = characterMediaCall()
        val request = CharacterMedia.request(id = 3, page = null, perPage = null, sort = null, type = null)
        val expected = ConnectionContainer<PageContainer<MediaEntity>>().also { connection ->
            connection.connection = PageContainer<MediaEntity>().also { page ->
                page.pageData = listOf(
                    MediaEntity().apply {
                        id = 30L
                    },
                )
            }
        }
        `when`(service.getCharacterMedia(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                AniListContainer(
                    data = DataContainer(result = expected),
                    errors = null,
                ),
            ),
        )

        val result = repository.getCharacterMedia(id = 3L)

        assertTrue(result.isSuccess)
        assertSame(expected, result.getOrThrow())
        assertEquals(30L, result.getOrThrow().connection.pageData.single().id)
    }

    @Test
    fun `getCharacterActors keeps legacy AniListContainer response handling`() = runTest {
        val call = characterActorsCall()
        val request = CharacterActors.request(id = 4, page = null, perPage = null, sort = null)
        val expected = ConnectionContainer<EdgeContainer<MediaEdge>>().also { connection ->
            connection.connection = EdgeContainer<MediaEdge>().also { edgeContainer ->
                edgeContainer.edges = listOf(
                    MediaEdge().apply {
                        characterRole = "MAIN"
                    },
                )
            }
        }
        `when`(service.getCharacterActors(request)).thenReturn(call)
        `when`(call.execute()).thenReturn(
            Response.success(
                AniListContainer(
                    data = DataContainer(result = expected),
                    errors = null,
                ),
            ),
        )

        val result = repository.getCharacterActors(id = 4L)

        assertTrue(result.isSuccess)
        assertSame(expected, result.getOrThrow())
        assertEquals("MAIN", result.getOrThrow().connection.edges.single().characterRole)
    }

    @Suppress("UNCHECKED_CAST")
    private fun characterBaseCall(): Call<GraphContainer<CharacterBaseData>> =
        mock(Call::class.java) as Call<GraphContainer<CharacterBaseData>>

    @Suppress("UNCHECKED_CAST")
    private fun characterOverviewCall(): Call<GraphContainer<CharacterOverviewData>> =
        mock(Call::class.java) as Call<GraphContainer<CharacterOverviewData>>

    @Suppress("UNCHECKED_CAST")
    private fun characterMediaCall(): Call<AniListContainer<ConnectionContainer<PageContainer<MediaEntity>>>> =
        mock(Call::class.java) as Call<AniListContainer<ConnectionContainer<PageContainer<MediaEntity>>>>

    @Suppress("UNCHECKED_CAST")
    private fun characterActorsCall(): Call<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>> =
        mock(Call::class.java) as Call<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>>

    private fun characterBaseData(): CharacterBaseData =
        CharacterBaseData(
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

    private fun characterOverviewData(): CharacterOverviewData =
        CharacterOverviewData(
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
}
