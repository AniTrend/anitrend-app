package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.MediaEpisodesData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaEpisodesRecordMapperTest {

    @Test
    fun `maps generated Media to MediaEpisodesRecord preserving all values`() {
        val record = media(
            externalLinks = listOf(
                MediaEpisodesData.MediaExternalLinks(
                    id = 301,
                    site = "Crunchyroll",
                    url = "https://www.crunchyroll.com/series/123",
                ),
            ),
        ).toMediaEpisodesRecord()

        assertEquals(1, record.externalLinks?.size)
        assertEquals(301L, record.externalLinks?.first()?.id)
        assertEquals("Crunchyroll", record.externalLinks?.first()?.site)
        assertEquals("https://www.crunchyroll.com/series/123", record.externalLinks?.first()?.url)
    }

    @Test
    fun `converts generated Int ids to domain Longs`() {
        val record = media(
            externalLinks = listOf(
                MediaEpisodesData.MediaExternalLinks(
                    id = Int.MAX_VALUE,
                    site = "Site",
                    url = null,
                ),
            ),
        ).toMediaEpisodesRecord()

        assertEquals(Int.MAX_VALUE.toLong(), record.externalLinks?.first()?.id)
    }

    @Test
    fun `preserves nullable semantics for the optional external links block`() {
        val record = media(externalLinks = null).toMediaEpisodesRecord()

        assertNull(record.externalLinks)
    }

    @Test
    fun `maps empty external links list to empty list`() {
        val record = media(externalLinks = emptyList()).toMediaEpisodesRecord()

        assertEquals(0, record.externalLinks?.size)
    }

    @Test
    fun `drops null list elements in external links`() {
        val record = media(
            externalLinks = listOf(
                null,
                MediaEpisodesData.MediaExternalLinks(id = 1, site = "Site A", url = null),
                null,
            ),
        ).toMediaEpisodesRecord()

        assertEquals(1, record.externalLinks?.size)
        assertEquals(1L, record.externalLinks?.first()?.id)
        assertEquals("Site A", record.externalLinks?.first()?.site)
    }

    @Test
    fun `carries nullable url values`() {
        val record = media(
            externalLinks = listOf(
                MediaEpisodesData.MediaExternalLinks(id = 1, site = "Site A", url = null),
            ),
        ).toMediaEpisodesRecord()

        assertNull(record.externalLinks?.first()?.url)
    }

    private fun media(
        externalLinks: List<MediaEpisodesData.MediaExternalLinks?>? = null,
    ): MediaEpisodesData.Media = MediaEpisodesData.Media(
        externalLinks = externalLinks,
    )
}
