package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaRelation
import com.mxt.anitrend.graphql.generated.MediaRelationsData
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaRelationsRecordMapperTest {

    @Test
    fun `maps generated Media to MediaRelationsRecord preserving all values`() {
        val record = media(
            relations = MediaRelationsData.MediaRelations(
                edges = listOf(
                    MediaRelationsData.MediaRelationsEdges(
                        relationType = MediaRelation.SEQUEL,
                        node = node(
                            averageScore = 80,
                            bannerImage = "https://cdn.example.com/banner.jpg",
                            chapters = 12,
                            coverImage = MediaRelationsData.MediaRelationsEdgesNodeCoverImage(
                                color = "#123456",
                                extraLarge = "https://cdn.example.com/extra-large.jpg",
                                large = "https://cdn.example.com/large.jpg",
                                medium = "https://cdn.example.com/medium.jpg",
                            ),
                            endDate = MediaRelationsData.MediaRelationsEdgesNodeEndDate(day = 31, month = 12, year = 2020),
                            episodes = 24,
                            format = MediaFormat.TV,
                            id = 123,
                            isAdult = false,
                            isFavourite = true,
                            meanScore = 85,
                            mediaListEntry = MediaRelationsData.MediaRelationsEdgesNodeMediaListEntry(
                                id = 456,
                                status = MediaListStatus.COMPLETED,
                            ),
                            nextAiringEpisode = MediaRelationsData.MediaRelationsEdgesNodeNextAiringEpisode(
                                airingAt = 1_600_000_000,
                                episode = 5,
                                id = 7,
                                mediaId = 123,
                                timeUntilAiring = 86_400,
                            ),
                            season = MediaSeason.WINTER,
                            siteUrl = "https://anilist.co/anime/123",
                            startDate = MediaRelationsData.MediaRelationsEdgesNodeStartDate(day = 1, month = 1, year = 2020),
                            status = MediaStatus.RELEASING,
                            title = MediaRelationsData.MediaRelationsEdgesNodeTitle(
                                english = "English Title",
                                native = "Original Title",
                                romaji = "Romaji Title",
                                userPreferred = "Preferred Title",
                            ),
                            type = MediaType.ANIME,
                            updatedAt = 1_600_000_000,
                            volumes = 6,
                        ),
                    ),
                ),
                pageInfo = MediaRelationsData.MediaRelationsPageInfo(
                    currentPage = 1,
                    hasNextPage = true,
                    lastPage = 3,
                    perPage = 25,
                    total = 61,
                ),
            ),
        ).toMediaRelationsRecord()

        assertEquals(1, record.edges?.size)
        val edge = record.edges?.first()
        assertEquals("SEQUEL", edge?.relationType)
        val node = edge?.node
        assertEquals(123L, node?.id)
        assertEquals("Preferred Title", node?.titleUserPreferred)
        assertEquals("Romaji Title", node?.titleRomaji)
        assertEquals("English Title", node?.titleEnglish)
        assertEquals("Original Title", node?.titleOriginal)
        assertEquals("https://cdn.example.com/banner.jpg", node?.bannerImage)
        assertEquals("#123456", node?.coverImage?.color)
        assertEquals("https://cdn.example.com/extra-large.jpg", node?.coverImage?.extraLarge)
        assertEquals("https://cdn.example.com/large.jpg", node?.coverImage?.large)
        assertEquals("https://cdn.example.com/medium.jpg", node?.coverImage?.medium)
        assertEquals("ANIME", node?.type)
        assertEquals("TV", node?.format)
        assertEquals("WINTER", node?.season)
        assertEquals("RELEASING", node?.status)
        assertEquals(85, node?.meanScore)
        assertEquals(80, node?.averageScore)
        assertEquals(2020, node?.startDate?.year)
        assertEquals(1, node?.startDate?.month)
        assertEquals(1, node?.startDate?.day)
        assertEquals(2020, node?.endDate?.year)
        assertEquals(12, node?.endDate?.month)
        assertEquals(31, node?.endDate?.day)
        assertEquals(24, node?.episodes)
        assertEquals(12, node?.chapters)
        assertEquals(6, node?.volumes)
        assertFalse(node?.isAdult == true)
        assertTrue(node?.isFavourite == true)
        assertEquals(1_600_000_000L, node?.nextAiringEpisode?.airingAt)
        assertEquals(86_400L, node?.nextAiringEpisode?.timeUntilAiring)
        assertEquals(5, node?.nextAiringEpisode?.episode)
        assertEquals(456L, node?.mediaListEntry?.id)
        assertEquals("COMPLETED", node?.mediaListEntry?.status)
        assertEquals("https://anilist.co/anime/123", node?.siteUrl)
        assertEquals(1_600_000_000L, node?.updatedAt)
        assertEquals(1, record.pageInfo?.currentPage)
        assertEquals(3, record.pageInfo?.lastPage)
        assertEquals(25, record.pageInfo?.perPage)
        assertEquals(61, record.pageInfo?.total)
        assertTrue(record.pageInfo?.hasNextPage == true)
        assertFalse(record.pageInfo?.hasPreviousPage == true)
    }

    @Test
    fun `maps generated relation and media enums to their serialized names`() {
        val record = media(
            relations = MediaRelationsData.MediaRelations(
                edges = listOf(
                    MediaRelationsData.MediaRelationsEdges(
                        relationType = MediaRelation.PREQUEL,
                        node = node(
                            format = MediaFormat.MOVIE,
                            mediaListEntry = MediaRelationsData.MediaRelationsEdgesNodeMediaListEntry(
                                id = 1,
                                status = MediaListStatus.PLANNING,
                            ),
                            season = MediaSeason.SPRING,
                            status = MediaStatus.NOT_YET_RELEASED,
                            type = MediaType.MANGA,
                        ),
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaRelationsRecord()

        assertEquals("PREQUEL", record.edges?.first()?.relationType)
        assertEquals("MANGA", record.edges?.first()?.node?.type)
        assertEquals("MOVIE", record.edges?.first()?.node?.format)
        assertEquals("SPRING", record.edges?.first()?.node?.season)
        assertEquals("NOT_YET_RELEASED", record.edges?.first()?.node?.status)
        assertEquals("PLANNING", record.edges?.first()?.node?.mediaListEntry?.status)
    }

    @Test
    fun `converts generated Int ids and timestamps to domain Longs`() {
        val record = media(
            relations = MediaRelationsData.MediaRelations(
                edges = listOf(
                    MediaRelationsData.MediaRelationsEdges(
                        relationType = MediaRelation.OTHER,
                        node = node(
                            id = Int.MAX_VALUE,
                            mediaListEntry = MediaRelationsData.MediaRelationsEdgesNodeMediaListEntry(
                                id = Int.MAX_VALUE,
                                status = null,
                            ),
                            nextAiringEpisode = MediaRelationsData.MediaRelationsEdgesNodeNextAiringEpisode(
                                airingAt = Int.MAX_VALUE,
                                episode = 1,
                                id = Int.MAX_VALUE,
                                mediaId = Int.MAX_VALUE,
                                timeUntilAiring = Int.MAX_VALUE,
                            ),
                            updatedAt = Int.MAX_VALUE,
                        ),
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaRelationsRecord()

        assertEquals(Int.MAX_VALUE.toLong(), record.edges?.first()?.node?.id)
        assertEquals(Int.MAX_VALUE.toLong(), record.edges?.first()?.node?.mediaListEntry?.id)
        assertEquals(Int.MAX_VALUE.toLong(), record.edges?.first()?.node?.nextAiringEpisode?.airingAt)
        assertEquals(Int.MAX_VALUE.toLong(), record.edges?.first()?.node?.nextAiringEpisode?.timeUntilAiring)
        assertEquals(Int.MAX_VALUE.toLong(), record.edges?.first()?.node?.updatedAt)
    }

    @Test
    fun `maps generated PageInfo to PageInfoRecord preserving page metadata`() {
        val record = media(
            relations = MediaRelationsData.MediaRelations(
                edges = null,
                pageInfo = MediaRelationsData.MediaRelationsPageInfo(
                    currentPage = 2,
                    hasNextPage = false,
                    lastPage = 4,
                    perPage = 50,
                    total = 200,
                ),
            ),
        ).toMediaRelationsRecord()

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
            relations = MediaRelationsData.MediaRelations(
                edges = null,
                pageInfo = null,
            ),
        ).toMediaRelationsRecord()

        assertNull(record.edges)
        assertNull(record.pageInfo)
    }

    @Test
    fun `collapses null relations block into null edges and page info`() {
        val record = media(relations = null).toMediaRelationsRecord()

        assertNull(record.edges)
        assertNull(record.pageInfo)
    }

    @Test
    fun `maps empty edges list to empty list`() {
        val record = media(
            relations = MediaRelationsData.MediaRelations(
                edges = emptyList(),
                pageInfo = null,
            ),
        ).toMediaRelationsRecord()

        assertEquals(0, record.edges?.size)
    }

    @Test
    fun `drops null list elements in edges`() {
        val record = media(
            relations = MediaRelationsData.MediaRelations(
                edges = listOf(
                    null,
                    MediaRelationsData.MediaRelationsEdges(
                        relationType = MediaRelation.SIDE_STORY,
                        node = node(id = 1),
                    ),
                    null,
                ),
                pageInfo = null,
            ),
        ).toMediaRelationsRecord()

        assertEquals(1, record.edges?.size)
        assertEquals("SIDE_STORY", record.edges?.first()?.relationType)
        assertEquals(1L, record.edges?.first()?.node?.id)
    }

    @Test
    fun `preserves null node blocks within edges`() {
        val record = media(
            relations = MediaRelationsData.MediaRelations(
                edges = listOf(
                    MediaRelationsData.MediaRelationsEdges(
                        relationType = MediaRelation.PARENT,
                        node = null,
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaRelationsRecord()

        assertEquals(1, record.edges?.size)
        assertEquals("PARENT", record.edges?.first()?.relationType)
        assertNull(record.edges?.first()?.node)
    }

    @Test
    fun `preserves null relation type and nullable node fields`() {
        val record = media(
            relations = MediaRelationsData.MediaRelations(
                edges = listOf(
                    MediaRelationsData.MediaRelationsEdges(
                        relationType = null,
                        node = node(
                            coverImage = null,
                            endDate = null,
                            isAdult = null,
                            mediaListEntry = null,
                            nextAiringEpisode = null,
                            startDate = null,
                            title = null,
                            updatedAt = null,
                        ),
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaRelationsRecord()

        assertNull(record.edges?.first()?.relationType)
        assertNull(record.edges?.first()?.node?.titleUserPreferred)
        assertNull(record.edges?.first()?.node?.bannerImage)
        assertNull(record.edges?.first()?.node?.coverImage)
        assertNull(record.edges?.first()?.node?.startDate)
        assertNull(record.edges?.first()?.node?.endDate)
        assertNull(record.edges?.first()?.node?.isAdult)
        assertNull(record.edges?.first()?.node?.nextAiringEpisode)
        assertNull(record.edges?.first()?.node?.mediaListEntry)
        assertNull(record.edges?.first()?.node?.siteUrl)
        assertNull(record.edges?.first()?.node?.updatedAt)
    }

    private fun media(
        relations: MediaRelationsData.MediaRelations? = null,
    ): MediaRelationsData.Media = MediaRelationsData.Media(
        relations = relations,
    )

    private fun node(
        averageScore: Int? = null,
        bannerImage: String? = null,
        chapters: Int? = null,
        coverImage: MediaRelationsData.MediaRelationsEdgesNodeCoverImage? = null,
        endDate: MediaRelationsData.MediaRelationsEdgesNodeEndDate? = null,
        episodes: Int? = null,
        format: MediaFormat? = null,
        id: Int = 1,
        isAdult: Boolean? = null,
        isFavourite: Boolean = false,
        meanScore: Int? = null,
        mediaListEntry: MediaRelationsData.MediaRelationsEdgesNodeMediaListEntry? = null,
        nextAiringEpisode: MediaRelationsData.MediaRelationsEdgesNodeNextAiringEpisode? = null,
        season: MediaSeason? = null,
        siteUrl: String? = null,
        startDate: MediaRelationsData.MediaRelationsEdgesNodeStartDate? = null,
        status: MediaStatus? = null,
        title: MediaRelationsData.MediaRelationsEdgesNodeTitle? = null,
        type: MediaType? = null,
        updatedAt: Int? = null,
        volumes: Int? = null,
    ): MediaRelationsData.MediaRelationsEdgesNode = MediaRelationsData.MediaRelationsEdgesNode(
        averageScore = averageScore,
        bannerImage = bannerImage,
        chapters = chapters,
        coverImage = coverImage,
        endDate = endDate,
        episodes = episodes,
        format = format,
        id = id,
        isAdult = isAdult,
        isFavourite = isFavourite,
        meanScore = meanScore,
        mediaListEntry = mediaListEntry,
        nextAiringEpisode = nextAiringEpisode,
        season = season,
        siteUrl = siteUrl,
        startDate = startDate,
        status = status,
        title = title,
        type = type,
        updatedAt = updatedAt,
        volumes = volumes,
    )
}
