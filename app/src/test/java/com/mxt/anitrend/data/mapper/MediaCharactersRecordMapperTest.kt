package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.CharacterRecord
import com.mxt.anitrend.graphql.generated.CharacterRole
import com.mxt.anitrend.graphql.generated.MediaCharactersData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaCharactersRecordMapperTest {

    @Test
    fun `maps generated Media to MediaCharactersRecord preserving all values`() {
        val record = media(
            characters = MediaCharactersData.MediaCharacters(
                edges = listOf(
                    MediaCharactersData.MediaCharactersEdges(
                        role = CharacterRole.MAIN,
                        node = node(
                            id = 123,
                            image = MediaCharactersData.MediaCharactersEdgesNodeImage(
                                large = "https://cdn.example.com/large.jpg",
                                medium = "https://cdn.example.com/medium.jpg",
                            ),
                            isFavourite = true,
                            name = MediaCharactersData.MediaCharactersEdgesNodeName(
                                alternative = listOf("Alt Name"),
                                first = "Light",
                                last = "Yagami",
                                native = "夜神月",
                            ),
                            siteUrl = "https://anilist.co/character/123",
                        ),
                    ),
                ),
                pageInfo = MediaCharactersData.MediaCharactersPageInfo(
                    currentPage = 1,
                    hasNextPage = true,
                    lastPage = 3,
                    perPage = 25,
                    total = 61,
                ),
            ),
        ).toMediaCharactersRecord()

        assertEquals(1, record.edges?.size)
        val edge = record.edges?.first()
        assertEquals("MAIN", edge?.role)
        val node = edge?.node
        assertEquals(123L, node?.id)
        assertEquals("Light Yagami", node?.name)
        assertEquals("https://anilist.co/character/123", node?.siteUrl)
        assertTrue(node?.isFavourite == true)
        assertEquals(1, record.pageInfo?.currentPage)
        assertEquals(3, record.pageInfo?.lastPage)
        assertEquals(25, record.pageInfo?.perPage)
        assertEquals(61, record.pageInfo?.total)
        assertTrue(record.pageInfo?.hasNextPage == true)
        assertFalse(record.pageInfo?.hasPreviousPage == true)
    }

    @Test
    fun `maps CharacterRole enums to their serialized names`() {
        val record = media(
            characters = MediaCharactersData.MediaCharacters(
                edges = listOf(
                    MediaCharactersData.MediaCharactersEdges(
                        role = CharacterRole.MAIN,
                        node = node(id = 1),
                    ),
                    MediaCharactersData.MediaCharactersEdges(
                        role = CharacterRole.SUPPORTING,
                        node = node(id = 2),
                    ),
                    MediaCharactersData.MediaCharactersEdges(
                        role = CharacterRole.BACKGROUND,
                        node = node(id = 3),
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaCharactersRecord()

        assertEquals("MAIN", record.edges?.get(0)?.role)
        assertEquals("SUPPORTING", record.edges?.get(1)?.role)
        assertEquals("BACKGROUND", record.edges?.get(2)?.role)
    }

    @Test
    fun `converts generated Int character ids to domain Longs`() {
        val record = media(
            characters = MediaCharactersData.MediaCharacters(
                edges = listOf(
                    MediaCharactersData.MediaCharactersEdges(
                        role = null,
                        node = node(id = Int.MAX_VALUE),
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaCharactersRecord()

        assertEquals(Int.MAX_VALUE.toLong(), record.edges?.first()?.node?.id)
    }

    @Test
    fun `maps CharacterCoreFragment node preserving name image site and favourite semantics`() {
        val record = media(
            characters = MediaCharactersData.MediaCharacters(
                edges = listOf(
                    MediaCharactersData.MediaCharactersEdges(
                        role = CharacterRole.SUPPORTING,
                        node = node(
                            id = 42,
                            image = MediaCharactersData.MediaCharactersEdgesNodeImage(
                                large = "https://cdn.example.com/large.jpg",
                                medium = "https://cdn.example.com/medium.jpg",
                            ),
                            isFavourite = false,
                            name = MediaCharactersData.MediaCharactersEdgesNodeName(
                                alternative = null,
                                first = "Maka",
                                last = "Albarn",
                                native = null,
                            ),
                            siteUrl = "https://anilist.co/character/42",
                        ),
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaCharactersRecord()

        val node: CharacterRecord? = record.edges?.first()?.node
        assertEquals(42L, node?.id)
        assertEquals("Maka Albarn", node?.name)
        assertEquals("https://anilist.co/character/42", node?.siteUrl)
        assertFalse(node?.isFavourite == true)
    }

    @Test
    fun `derives full name like legacy TitleBase fullName`() {
        val firstOnly = node(name = name(first = "Light", last = null))
        val lastOnly = node(name = name(first = null, last = "Yagami"))
        val both = node(name = name(first = "Light", last = "Yagami"))
        val none = node(name = name(first = null, last = null))

        val record = media(
            characters = MediaCharactersData.MediaCharacters(
                edges = listOf(
                    MediaCharactersData.MediaCharactersEdges(role = null, node = firstOnly),
                    MediaCharactersData.MediaCharactersEdges(role = null, node = lastOnly),
                    MediaCharactersData.MediaCharactersEdges(role = null, node = both),
                    MediaCharactersData.MediaCharactersEdges(role = null, node = none),
                ),
                pageInfo = null,
            ),
        ).toMediaCharactersRecord()

        assertEquals("Light", record.edges?.get(0)?.node?.name)
        assertEquals("Yagami", record.edges?.get(1)?.node?.name)
        assertEquals("Light Yagami", record.edges?.get(2)?.node?.name)
        assertNull(record.edges?.get(3)?.node?.name)
    }

    @Test
    fun `maps generated PageInfo to PageInfoRecord preserving page metadata`() {
        val record = media(
            characters = MediaCharactersData.MediaCharacters(
                edges = null,
                pageInfo = MediaCharactersData.MediaCharactersPageInfo(
                    currentPage = 2,
                    hasNextPage = false,
                    lastPage = 4,
                    perPage = 50,
                    total = 200,
                ),
            ),
        ).toMediaCharactersRecord()

        assertEquals(2, record.pageInfo?.currentPage)
        assertEquals(4, record.pageInfo?.lastPage)
        assertEquals(50, record.pageInfo?.perPage)
        assertEquals(200, record.pageInfo?.total)
        assertFalse(record.pageInfo?.hasNextPage == true)
        assertTrue(record.pageInfo?.hasPreviousPage == true)
    }

    @Test
    fun `preserves nullable semantics for optional blocks and lists`() {
        val record = media(
            characters = MediaCharactersData.MediaCharacters(
                edges = null,
                pageInfo = null,
            ),
        ).toMediaCharactersRecord()

        assertNull(record.edges)
        assertNull(record.pageInfo)
    }

    @Test
    fun `collapses null characters block into null edges and page info`() {
        val record = media(characters = null).toMediaCharactersRecord()

        assertNull(record.edges)
        assertNull(record.pageInfo)
    }

    @Test
    fun `maps empty edges list to empty list`() {
        val record = media(
            characters = MediaCharactersData.MediaCharacters(
                edges = emptyList(),
                pageInfo = null,
            ),
        ).toMediaCharactersRecord()

        assertEquals(0, record.edges?.size)
    }

    @Test
    fun `drops null list elements in edges`() {
        val record = media(
            characters = MediaCharactersData.MediaCharacters(
                edges = listOf(
                    null,
                    MediaCharactersData.MediaCharactersEdges(
                        role = CharacterRole.MAIN,
                        node = node(id = 1),
                    ),
                    null,
                ),
                pageInfo = null,
            ),
        ).toMediaCharactersRecord()

        assertEquals(1, record.edges?.size)
        assertEquals("MAIN", record.edges?.first()?.role)
        assertEquals(1L, record.edges?.first()?.node?.id)
    }

    @Test
    fun `preserves null node blocks within edges`() {
        val record = media(
            characters = MediaCharactersData.MediaCharacters(
                edges = listOf(
                    MediaCharactersData.MediaCharactersEdges(
                        role = CharacterRole.SUPPORTING,
                        node = null,
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaCharactersRecord()

        assertEquals(1, record.edges?.size)
        assertEquals("SUPPORTING", record.edges?.first()?.role)
        assertNull(record.edges?.first()?.node)
    }

    @Test
    fun `preserves null role and nullable node fields`() {
        val record = media(
            characters = MediaCharactersData.MediaCharacters(
                edges = listOf(
                    MediaCharactersData.MediaCharactersEdges(
                        role = null,
                        node = node(
                            id = 1,
                            image = null,
                            isFavourite = false,
                            name = null,
                            siteUrl = null,
                        ),
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaCharactersRecord()

        assertNull(record.edges?.first()?.role)
        assertNull(record.edges?.first()?.node?.name)
        assertNull(record.edges?.first()?.node?.siteUrl)
    }

    private fun media(
        characters: MediaCharactersData.MediaCharacters? = null,
    ): MediaCharactersData.Media = MediaCharactersData.Media(
        characters = characters,
    )

    private fun node(
        id: Int = 1,
        image: MediaCharactersData.MediaCharactersEdgesNodeImage? = null,
        isFavourite: Boolean = false,
        name: MediaCharactersData.MediaCharactersEdgesNodeName? = null,
        siteUrl: String? = null,
    ): MediaCharactersData.MediaCharactersEdgesNode = MediaCharactersData.MediaCharactersEdgesNode(
        id = id,
        image = image,
        isFavourite = isFavourite,
        name = name,
        siteUrl = siteUrl,
    )

    private fun name(
        first: String? = null,
        last: String? = null,
    ): MediaCharactersData.MediaCharactersEdgesNodeName = MediaCharactersData.MediaCharactersEdgesNodeName(
        alternative = null,
        first = first,
        last = last,
        native = null,
    )
}
