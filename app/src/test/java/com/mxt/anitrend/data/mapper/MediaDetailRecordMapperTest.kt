package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.MediaBaseData
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaDetailRecordMapperTest {

    @Test
    fun `maps generated Media to MediaDetailRecord preserving all values`() {
        val record = media(
            id = 7,
            idMal = 12345,
            title = MediaBaseData.MediaTitle(userPreferred = "Sword Art Online"),
            type = MediaType.ANIME,
            bannerImage = "banner.jpg",
            isFavourite = true,
            siteUrl = "https://anilist.co/anime/7",
            mediaListEntry = MediaBaseData.MediaMediaListEntry(id = 99, status = MediaListStatus.CURRENT),
        ).toMediaDetailRecord()

        assertEquals(7L, record.id)
        assertEquals(12345L, record.idMal)
        assertEquals("Sword Art Online", record.titleUserPreferred)
        assertEquals("ANIME", record.type)
        assertEquals("banner.jpg", record.bannerImage)
        assertTrue(record.isFavourite)
        assertEquals("https://anilist.co/anime/7", record.siteUrl)
        assertEquals(99L, record.mediaListEntry?.id)
        assertEquals("CURRENT", record.mediaListEntry?.status)
    }

    @Test
    fun `converts generated Int ids to domain Longs`() {
        val record = media(
            id = Int.MAX_VALUE,
            idMal = Int.MAX_VALUE,
            mediaListEntry = MediaBaseData.MediaMediaListEntry(id = Int.MAX_VALUE, status = null),
        ).toMediaDetailRecord()

        assertEquals(Int.MAX_VALUE.toLong(), record.id)
        assertEquals(Int.MAX_VALUE.toLong(), record.idMal)
        assertEquals(Int.MAX_VALUE.toLong(), record.mediaListEntry?.id)
    }

    @Test
    fun `carries null mediaListEntry when the entry block is absent`() {
        val record = media(
            id = 1,
            mediaListEntry = null,
        ).toMediaDetailRecord()

        assertEquals(1L, record.id)
        assertNull(record.mediaListEntry)
    }

    @Test
    fun `carries null entry status when the mini fragment status is absent`() {
        val record = media(
            id = 1,
            mediaListEntry = MediaBaseData.MediaMediaListEntry(id = 5, status = null),
        ).toMediaDetailRecord()

        assertEquals(5L, record.mediaListEntry?.id)
        assertNull(record.mediaListEntry?.status)
    }

    @Test
    fun `preserves nullable semantics for optional blocks`() {
        val record = media(
            id = 1,
            idMal = null,
            title = null,
            type = null,
            bannerImage = null,
            isFavourite = false,
            siteUrl = null,
            mediaListEntry = null,
        ).toMediaDetailRecord()

        assertEquals(1L, record.id)
        assertNull(record.idMal)
        assertNull(record.titleUserPreferred)
        assertNull(record.type)
        assertNull(record.bannerImage)
        assertFalse(record.isFavourite)
        assertNull(record.siteUrl)
        assertNull(record.mediaListEntry)
    }

    @Test
    fun `maps generated enums to their serialized names`() {
        val record = media(
            id = 2,
            type = MediaType.MANGA,
            mediaListEntry = MediaBaseData.MediaMediaListEntry(id = 8, status = MediaListStatus.PLANNING),
        ).toMediaDetailRecord()

        assertEquals("MANGA", record.type)
        assertEquals("PLANNING", record.mediaListEntry?.status)
    }

    private fun media(
        id: Int,
        idMal: Int? = null,
        title: MediaBaseData.MediaTitle? = null,
        type: MediaType? = null,
        bannerImage: String? = null,
        isFavourite: Boolean = false,
        siteUrl: String? = null,
        mediaListEntry: MediaBaseData.MediaMediaListEntry? = null,
    ): MediaBaseData.Media = MediaBaseData.Media(
        bannerImage = bannerImage,
        id = id,
        idMal = idMal,
        isFavourite = isFavourite,
        mediaListEntry = mediaListEntry,
        siteUrl = siteUrl,
        title = title,
        type = type,
    )
}
