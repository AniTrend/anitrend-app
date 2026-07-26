package com.mxt.anitrend.repository

import com.mxt.anitrend.graphql.generated.MediaTagCollectionData
import com.mxt.anitrend.repository.mapper.toMediaTag
import com.mxt.anitrend.repository.mapper.toMediaTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MediaTagMappingTest {

    @Test
    fun `maps generated media tag to entity`() {
        val tag = MediaTagCollectionData.MediaTagCollection(
            id = 42,
            name = "Cyberpunk",
            description = "Future tech",
            category = "Theme",
            rank = 80,
            isGeneralSpoiler = true,
            isAdult = true,
        ).toMediaTag()

        assertEquals(42L, tag.id)
        assertEquals("Cyberpunk", tag.name)
        assertEquals("Future tech", tag.description)
        assertEquals("Theme", tag.category)
        assertEquals(80, tag.rank)
        assertEquals(true, tag.isGeneralSpoiler)
        assertEquals(true, tag.isAdult)
    }

    @Test
    fun `maps nullable values to entity defaults`() {
        val tag = MediaTagCollectionData.MediaTagCollection(
            id = 7,
            name = "Robots",
            description = null,
            category = null,
            rank = null,
            isGeneralSpoiler = null,
            isAdult = null,
        ).toMediaTag()

        assertEquals(7L, tag.id)
        assertEquals(0, tag.rank)
        assertFalse(tag.isGeneralSpoiler)
        assertFalse(tag.isAdult)
        assertFalse(tag.isMediaSpoiler)
        assertFalse(tag.isSelected)
    }

    @Test
    fun `filters null list entries`() {
        val tags = listOf(
            null,
            MediaTagCollectionData.MediaTagCollection(
                id = 1,
                name = "Action",
                description = null,
                category = null,
                rank = null,
                isGeneralSpoiler = null,
                isAdult = null,
            ),
        ).toMediaTags()

        assertEquals(1, tags.size)
        assertEquals(1L, tags.single().id)
        assertEquals("Action", tags.single().name)
    }
}
