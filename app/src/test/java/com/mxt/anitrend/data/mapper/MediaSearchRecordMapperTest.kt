package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaSearchRecordMapperTest {

    @Test
    fun `maps the media identity and card fields`() {
        val item = mediaBase(id = 7L).toMediaSearchItemUiModel()

        assertEquals(7L, item.id)
        assertEquals("Preferred", item.title)
        assertEquals("English", item.titleEnglish)
        assertEquals("Original", item.titleOriginal)
        assertEquals("cover-extra", item.coverImage)
        assertEquals("ANIME", item.mediaType)
        assertEquals("TV", item.mediaFormat)
        assertEquals("RELEASING", item.mediaStatus)
        assertEquals(24, item.mediaEpisodes)
        assertEquals(0, item.mediaChapters)
        assertEquals(0, item.mediaVolumes)
        assertEquals(2024, item.mediaStartDate?.year)
        assertEquals(1, item.mediaStartDate?.month)
        assertEquals(10, item.mediaStartDate?.day)
        assertEquals(85, item.averageScore)
        assertTrue(item.isFavourite)
    }

    @Test
    fun `title fallback prefers userPreferred then romaji then english then original`() {
        assertEquals(
            "Preferred",
            mediaBase(
                titleUserPreferred = "Preferred",
                titleRomaji = "Romaji",
                titleEnglish = "English",
                titleOriginal = "Original",
            ).toMediaSearchItemUiModel().title,
        )
        assertEquals(
            "Romaji",
            mediaBase(
                titleUserPreferred = null,
                titleRomaji = "Romaji",
                titleEnglish = "English",
                titleOriginal = "Original",
            ).toMediaSearchItemUiModel().title,
        )
        assertEquals(
            "English",
            mediaBase(
                titleUserPreferred = null,
                titleRomaji = null,
                titleEnglish = "English",
                titleOriginal = "Original",
            ).toMediaSearchItemUiModel().title,
        )
        assertEquals(
            "Original",
            mediaBase(
                titleUserPreferred = null,
                titleRomaji = null,
                titleEnglish = null,
                titleOriginal = "Original",
            ).toMediaSearchItemUiModel().title,
        )
    }

    @Test
    fun `title fallback is empty when no title is present`() {
        val item = mediaBase(titleUserPreferred = null, titleRomaji = null, titleEnglish = null, titleOriginal = null)
            .apply { title = null }
            .toMediaSearchItemUiModel()

        assertEquals("", item.title)
    }

    @Test
    fun `cover image prefers extraLarge then large then medium`() {
        assertEquals(
            "extra",
            mediaBase(extraLarge = "extra", large = "large", medium = "medium").toMediaSearchItemUiModel().coverImage,
        )
        assertEquals(
            "large",
            mediaBase(extraLarge = null, large = "large", medium = "medium").toMediaSearchItemUiModel().coverImage,
        )
        assertEquals(
            "medium",
            mediaBase(extraLarge = null, large = null, medium = "medium").toMediaSearchItemUiModel().coverImage,
        )
        assertNull(mediaBase(extraLarge = null, large = null, medium = null).toMediaSearchItemUiModel().coverImage)
    }

    @Test
    fun `zero-valued start date fields map to null`() {
        val item = mediaBase().apply {
            startDate = FuzzyDate(day = 0, month = 0, year = 0)
        }.toMediaSearchItemUiModel()

        assertNull(item.mediaStartDate?.year)
        assertNull(item.mediaStartDate?.month)
        assertNull(item.mediaStartDate?.day)
    }

    @Test
    fun `absent start date maps to null`() {
        val item = mediaBase().apply { startDate = null }.toMediaSearchItemUiModel()

        assertNull(item.mediaStartDate)
    }

    @Test
    fun `non-favourite media maps to false`() {
        val item = mediaBase().apply { isFavourite = false }.toMediaSearchItemUiModel()

        assertFalse(item.isFavourite)
    }

    private fun mediaBase(
        id: Long = 7L,
        titleUserPreferred: String? = "Preferred",
        titleRomaji: String? = "Romaji",
        titleEnglish: String? = "English",
        titleOriginal: String? = "Original",
        extraLarge: String? = "cover-extra",
        large: String? = "cover-large",
        medium: String? = "cover-medium",
    ): MediaBase = MediaBase().apply {
        this.id = id
        this.title =
            MediaTitle(
                romajiRaw = titleRomaji,
                englishRaw = titleEnglish,
                originalRaw = titleOriginal,
                userPreferredRaw = titleUserPreferred,
            )
        this.coverImage = ImageBase(extraLarge = extraLarge, large = large, medium = medium)
        this.type = KeyUtil.ANIME
        this.format = KeyUtil.TV
        this.status = KeyUtil.RELEASING
        this.episodes = 24
        this.chapters = 0
        this.volumes = 0
        this.startDate = FuzzyDate(day = 10, month = 1, year = 2024)
        this.averageScore = 85
        this.isFavourite = true
    }
}
