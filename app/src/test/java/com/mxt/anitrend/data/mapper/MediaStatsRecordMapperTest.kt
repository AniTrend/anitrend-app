package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaRankType
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaStatsData
import com.mxt.anitrend.graphql.generated.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaStatsRecordMapperTest {

    @Test
    fun `maps generated Media to MediaStatsRecord preserving all values`() {
        val record = media(
            type = MediaType.ANIME,
            externalLinks = listOf(
                MediaStatsData.MediaExternalLinks(
                    id = 301,
                    site = "Crunchyroll",
                    url = "https://www.crunchyroll.com/series/123",
                ),
            ),
            stats = MediaStatsData.MediaStats(
                scoreDistribution = listOf(
                    MediaStatsData.MediaStatsScoreDistribution(amount = 452, score = 90),
                ),
                statusDistribution = listOf(
                    MediaStatsData.MediaStatsStatusDistribution(amount = 1200, status = MediaListStatus.COMPLETED),
                ),
            ),
            rankings = listOf(
                MediaStatsData.MediaRankings(
                    allTime = true,
                    context = "highest rated",
                    format = MediaFormat.TV,
                    id = 501,
                    rank = 7,
                    season = MediaSeason.WINTER,
                    type = MediaRankType.RATED,
                    year = 2012,
                ),
            ),
        ).toMediaStatsRecord()

        assertEquals("ANIME", record.type)
        assertEquals(1, record.externalLinks?.size)
        assertEquals(301L, record.externalLinks?.first()?.id)
        assertEquals("Crunchyroll", record.externalLinks?.first()?.site)
        assertEquals("https://www.crunchyroll.com/series/123", record.externalLinks?.first()?.url)
        assertEquals(1, record.scoreDistribution?.size)
        assertEquals(90, record.scoreDistribution?.first()?.score)
        assertEquals(452, record.scoreDistribution?.first()?.amount)
        assertEquals(1, record.statusDistribution?.size)
        assertEquals("COMPLETED", record.statusDistribution?.first()?.status)
        assertEquals(1200, record.statusDistribution?.first()?.amount)
        assertEquals(1, record.rankings?.size)
        assertEquals(501L, record.rankings?.first()?.id)
        assertEquals(7, record.rankings?.first()?.rank)
        assertEquals("RATED", record.rankings?.first()?.type)
        assertEquals("TV", record.rankings?.first()?.format)
        assertEquals(2012, record.rankings?.first()?.year)
        assertEquals("WINTER", record.rankings?.first()?.season)
        assertTrue(record.rankings?.first()?.allTime == true)
        assertEquals("highest rated", record.rankings?.first()?.context)
    }

    @Test
    fun `converts generated Int ids to domain Longs`() {
        val record = media(
            type = MediaType.MANGA,
            externalLinks = listOf(
                MediaStatsData.MediaExternalLinks(
                    id = Int.MAX_VALUE,
                    site = "Site",
                    url = null,
                ),
            ),
            rankings = listOf(
                MediaStatsData.MediaRankings(
                    allTime = null,
                    context = "context",
                    format = MediaFormat.MOVIE,
                    id = Int.MAX_VALUE,
                    rank = Int.MAX_VALUE,
                    season = null,
                    type = MediaRankType.POPULAR,
                    year = null,
                ),
            ),
        ).toMediaStatsRecord()

        assertEquals(Int.MAX_VALUE.toLong(), record.externalLinks?.first()?.id)
        assertEquals(Int.MAX_VALUE.toLong(), record.rankings?.first()?.id)
        assertEquals(Int.MAX_VALUE, record.rankings?.first()?.rank)
    }

    @Test
    fun `maps generated enums to their serialized names`() {
        val record = media(
            type = MediaType.MANGA,
            stats = MediaStatsData.MediaStats(
                scoreDistribution = null,
                statusDistribution = listOf(
                    MediaStatsData.MediaStatsStatusDistribution(amount = 5, status = MediaListStatus.PLANNING),
                ),
            ),
            rankings = listOf(
                MediaStatsData.MediaRankings(
                    allTime = false,
                    context = "context",
                    format = MediaFormat.ONA,
                    id = 1,
                    rank = 2,
                    season = MediaSeason.FALL,
                    type = MediaRankType.POPULAR,
                    year = 2020,
                ),
            ),
        ).toMediaStatsRecord()

        assertEquals("MANGA", record.type)
        assertEquals("PLANNING", record.statusDistribution?.first()?.status)
        assertEquals("POPULAR", record.rankings?.first()?.type)
        assertEquals("ONA", record.rankings?.first()?.format)
        assertEquals("FALL", record.rankings?.first()?.season)
    }

    @Test
    fun `preserves nullable semantics for optional blocks and lists`() {
        val record = media(
            type = null,
            externalLinks = null,
            stats = null,
            rankings = null,
        ).toMediaStatsRecord()
        assertNull(record.type)
        assertNull(record.externalLinks)
        assertNull(record.scoreDistribution)
        assertNull(record.statusDistribution)
        assertNull(record.rankings)
    }

    @Test
    fun `collapses null stats block into null distribution lists`() {
        val record = media(
            type = MediaType.ANIME,
            stats = MediaStatsData.MediaStats(
                scoreDistribution = null,
                statusDistribution = null,
            ),
        ).toMediaStatsRecord()

        assertEquals("ANIME", record.type)
        assertNull(record.scoreDistribution)
        assertNull(record.statusDistribution)
    }

    @Test
    fun `drops null list elements in distributions rankings and external links`() {
        val record = media(
            type = MediaType.ANIME,
            externalLinks = listOf(
                MediaStatsData.MediaExternalLinks(id = 1, site = "Site A", url = null),
                null,
            ),
            stats = MediaStatsData.MediaStats(
                scoreDistribution = listOf(
                    MediaStatsData.MediaStatsScoreDistribution(amount = 1, score = 80),
                    null,
                ),
                statusDistribution = listOf(
                    null,
                    MediaStatsData.MediaStatsStatusDistribution(amount = 2, status = MediaListStatus.CURRENT),
                ),
            ),
            rankings = listOf(
                MediaStatsData.MediaRankings(
                    allTime = false,
                    context = "context",
                    format = MediaFormat.TV,
                    id = 3,
                    rank = 4,
                    season = null,
                    type = MediaRankType.RATED,
                    year = null,
                ),
                null,
            ),
        ).toMediaStatsRecord()

        assertEquals(1, record.externalLinks?.size)
        assertEquals("Site A", record.externalLinks?.first()?.site)
        assertEquals(1, record.scoreDistribution?.size)
        assertEquals(80, record.scoreDistribution?.first()?.score)
        assertEquals(1, record.statusDistribution?.size)
        assertEquals("CURRENT", record.statusDistribution?.first()?.status)
        assertEquals(1, record.rankings?.size)
        assertEquals(4, record.rankings?.first()?.rank)
    }

    @Test
    fun `carries nullable ranking and distribution values`() {
        val record = media(
            type = MediaType.ANIME,
            stats = MediaStatsData.MediaStats(
                scoreDistribution = listOf(
                    MediaStatsData.MediaStatsScoreDistribution(amount = null, score = null),
                ),
                statusDistribution = listOf(
                    MediaStatsData.MediaStatsStatusDistribution(amount = null, status = null),
                ),
            ),
            rankings = listOf(
                MediaStatsData.MediaRankings(
                    allTime = null,
                    context = "context",
                    format = MediaFormat.TV,
                    id = 1,
                    rank = 1,
                    season = null,
                    type = MediaRankType.RATED,
                    year = null,
                ),
            ),
        ).toMediaStatsRecord()

        assertNull(record.scoreDistribution?.first()?.score)
        assertNull(record.scoreDistribution?.first()?.amount)
        assertNull(record.statusDistribution?.first()?.status)
        assertNull(record.statusDistribution?.first()?.amount)
        assertNull(record.rankings?.first()?.year)
        assertNull(record.rankings?.first()?.season)
        assertNull(record.rankings?.first()?.allTime)
        assertEquals("context", record.rankings?.first()?.context)
    }

    private fun media(
        type: MediaType? = null,
        externalLinks: List<MediaStatsData.MediaExternalLinks?>? = null,
        stats: MediaStatsData.MediaStats? = null,
        rankings: List<MediaStatsData.MediaRankings?>? = null,
    ): MediaStatsData.Media = MediaStatsData.Media(
        externalLinks = externalLinks,
        rankings = rankings,
        stats = stats,
        type = type,
    )
}
