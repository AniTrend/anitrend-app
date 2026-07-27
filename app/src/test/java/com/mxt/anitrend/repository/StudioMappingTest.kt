package com.mxt.anitrend.repository

import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StudioBaseData
import com.mxt.anitrend.graphql.generated.StudioMediaData
import com.mxt.anitrend.repository.mapper.toStudioEntity
import com.mxt.anitrend.repository.mapper.toStudioMediaConnection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudioMappingTest {

    @Test
    fun `maps studio base to entity`() {
        val studio = StudioBaseData(
            studio = StudioBaseData.Studio(
                id = 5,
                name = "Kyoto Animation",
                isAnimationStudio = true,
                isFavourite = true,
                siteUrl = "https://anilist.co/studio/5",
            ),
        ).toStudioEntity()

        assertEquals(5L, studio.id)
        assertEquals("Kyoto Animation", studio.name)
        assertEquals("https://anilist.co/studio/5", studio.siteUrl)
        assertTrue(studio.isFavourite)
    }

    @Test
    fun `maps studio media to connection page and media entity`() {
        val connection = StudioMediaData(
            studio = StudioMediaData.Studio(
                media = StudioMediaData.StudioMedia(
                    nodes = listOf(studioMediaNode()),
                    pageInfo = StudioMediaData.StudioMediaPageInfo(
                        total = 1,
                        perPage = 20,
                        currentPage = 1,
                        lastPage = 1,
                        hasNextPage = false,
                    ),
                ),
            ),
        ).toStudioMediaConnection()

        val page = connection.connection
        val media = page.pageData.single()
        assertEquals(1, page.pageInfo.total)
        assertEquals(20, page.pageInfo.perPage)
        assertEquals(1, page.pageInfo.currentPage)
        assertFalse(page.pageInfo.hasNextPage())
        assertEquals(10L, media.id)
        assertEquals("Violet Evergarden", media.title?.userPreferred)
        assertEquals("large.jpg", media.coverImage?.large)
        assertEquals("ANIME", media.type)
        assertEquals("TV", media.format)
        assertEquals("WINTER", media.season)
        assertEquals("FINISHED", media.status)
        assertEquals(85, media.averageScore)
        assertEquals(84, media.meanScore)
        assertEquals(13, media.episodes)
        assertEquals(0, media.chapters)
        assertEquals(0, media.volumes)
        assertTrue(media.isFavourite)
        assertFalse(media.isAdult)
        assertEquals(123456789L, media.nextAiringEpisode?.airingAt)
        assertEquals(200L, media.mediaListEntry?.id)
        assertEquals("COMPLETED", media.mediaListEntry?.status)
        assertNotNull(media.startDate)
        assertNotNull(media.endDate)
    }

    @Test
    fun `maps null media nodes to empty page data`() {
        val connection = StudioMediaData(
            studio = StudioMediaData.Studio(
                media = StudioMediaData.StudioMedia(
                    nodes = listOf(null),
                    pageInfo = null,
                ),
            ),
        ).toStudioMediaConnection()

        assertTrue(connection.connection.pageData.isEmpty())
        assertFalse(connection.connection.hasPageInfo())
    }

    @Test
    fun `throws on null studio roots`() {
        val baseError = runCatching {
            StudioBaseData(studio = null).toStudioEntity()
        }.exceptionOrNull()
        val mediaError = runCatching {
            StudioMediaData(studio = null).toStudioMediaConnection()
        }.exceptionOrNull()

        assertEquals(IllegalStateException::class.java, baseError!!::class.java)
        assertEquals(IllegalStateException::class.java, mediaError!!::class.java)
    }

    private fun studioMediaNode(): StudioMediaData.StudioMediaNodes = StudioMediaData.StudioMediaNodes(
        id = 10,
        title = StudioMediaData.StudioMediaNodesTitle(
            romaji = "Violet Evergarden",
            english = "Violet Evergarden",
            native = "ヴァイオレット・エヴァーガーデン",
            userPreferred = "Violet Evergarden",
        ),
        coverImage = StudioMediaData.StudioMediaNodesCoverImage(
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
        siteUrl = "https://anilist.co/anime/10",
        meanScore = 84,
        averageScore = 85,
        startDate = StudioMediaData.StudioMediaNodesStartDate(
            day = 11,
            month = 1,
            year = 2018,
        ),
        endDate = StudioMediaData.StudioMediaNodesEndDate(
            day = 5,
            month = 4,
            year = 2018,
        ),
        episodes = 13,
        chapters = null,
        volumes = null,
        isAdult = false,
        isFavourite = true,
        nextAiringEpisode = StudioMediaData.StudioMediaNodesNextAiringEpisode(
            id = 100,
            mediaId = 10,
            airingAt = 123456789,
            timeUntilAiring = 3600,
            episode = 14,
        ),
        mediaListEntry = StudioMediaData.StudioMediaNodesMediaListEntry(
            id = 200,
            status = MediaListStatus.COMPLETED,
        ),
        updatedAt = 123,
    )
}
