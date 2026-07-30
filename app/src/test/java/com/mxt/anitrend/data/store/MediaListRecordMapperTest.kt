package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.CustomList
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaListRecordMapperTest {
    @Test
    fun `map MediaList to MediaListRecord correctly`() {
        val mediaList = createMediaList()

        val record = mediaList.toMediaListRecord(revision = 5L)

        assertEquals(12L, record.id)
        assertEquals(34L, record.mediaId)
        assertEquals("CURRENT", record.status)
        assertEquals(8.5, record.score, 0.0)
        assertEquals(85, record.scoreRaw)
        assertEquals(6, record.progress)
        assertEquals(2, record.progressVolumes)
        assertEquals(1, record.repeat)
        assertEquals(3, record.priority)
        assertEquals(true, record.`private`)
        assertEquals(true, record.hiddenFromStatusLists)
        assertEquals(listOf("Favourites"), record.customLists)
        assertEquals(mapOf("Story" to 9.5, "Art" to 8.0), record.advancedScores)
        assertEquals("notes", record.notes)
        assertEquals(2025, record.startedAt?.year)
        assertEquals(2026, record.completedAt?.year)
        assertEquals("Romaji", record.media?.titleRomaji)
        assertEquals("https://cover-extra-large", record.media?.coverImage)
        assertEquals(5L, record.revision)
    }

    @Test
    fun `mutate source after mapping keeps MediaListRecord unchanged`() {
        val mediaList = createMediaList()
        val customLists = mediaList.customLists as MutableList<CustomList>
        val advancedScores = mediaList.advancedScores as MutableMap<String, Float>

        val record = mediaList.toMediaListRecord(revision = 2L)

        mediaList.status = "COMPLETED"
        mediaList.progress = 99
        mediaList.media.title = MediaTitle("Changed", "Changed", "Changed", "Changed")
        customLists += CustomList(name = "New", isEnabled = true)
        advancedScores["Story"] = 1.0f

        assertEquals("CURRENT", record.status)
        assertEquals(6, record.progress)
        assertEquals("Romaji", record.media?.titleRomaji)
        assertEquals(listOf("Favourites"), record.customLists)
        assertEquals(9.5, record.advancedScores["Story"] ?: 0.0, 0.0)
    }

    private fun createMediaList(): MediaList = MediaList().also { mediaList ->
        mediaList.id = 12L
        mediaList.mediaId = 34L
        mediaList.status = "CURRENT"
        mediaList.score = 8.5f
        mediaList.scoreRaw = 85
        mediaList.progress = 6
        mediaList.progressVolumes = 2
        mediaList.repeat = 1
        mediaList.priority = 3
        mediaList.isHidden = true
        mediaList.isHiddenFromStatusLists = true
        mediaList.customLists = mutableListOf(
            CustomList(name = "Favourites", isEnabled = true),
            CustomList(name = "Ignored", isEnabled = false),
        )
        mediaList.advancedScores = mutableMapOf(
            "Story" to 9.5f,
            "Art" to 8.0f,
        )
        mediaList.notes = "notes"
        mediaList.startedAt = FuzzyDate(day = 2, month = 1, year = 2025)
        mediaList.completedAt = FuzzyDate(day = 3, month = 2, year = 2026)
        mediaList.media = MediaBase().also { media ->
            media.id = 34L
            media.title = MediaTitle("Romaji", "English", "Original", "Preferred")
            media.coverImage = ImageBase(
                extraLarge = "https://cover-extra-large",
                large = "https://cover-large",
                medium = "https://cover-medium",
            )
            media.type = "ANIME"
            media.episodes = 12
            media.status = "RELEASING"
            media.siteUrl = "https://media"
        }
    }
}
