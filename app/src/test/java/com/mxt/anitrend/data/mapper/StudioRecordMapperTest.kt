package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.StudioBaseData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudioRecordMapperTest {

    @Test
    fun `maps generated studio to StudioRecord preserving all values`() {
        val record = StudioBaseData.Studio(
            id = 5,
            name = "Kyoto Animation",
            isAnimationStudio = true,
            isFavourite = true,
            siteUrl = "https://anilist.co/studio/5",
        ).toStudioRecord()

        assertEquals(5L, record.id)
        assertEquals("Kyoto Animation", record.name)
        assertEquals("https://anilist.co/studio/5", record.siteUrl)
        assertTrue(record.isFavourite)
    }

    @Test
    fun `converts generated id Int to domain Long`() {
        val record = StudioBaseData.Studio(
            id = Int.MAX_VALUE,
            name = "Studio",
            isAnimationStudio = false,
            isFavourite = false,
            siteUrl = null,
        ).toStudioRecord()

        assertEquals(Int.MAX_VALUE.toLong(), record.id)
    }

    @Test
    fun `carries null siteUrl and false favourite defaults`() {
        val record = StudioBaseData.Studio(
            id = 1,
            name = "Studio",
            isAnimationStudio = false,
            isFavourite = false,
            siteUrl = null,
        ).toStudioRecord()

        assertNull(record.siteUrl)
        assertFalse(record.isFavourite)
    }
}
