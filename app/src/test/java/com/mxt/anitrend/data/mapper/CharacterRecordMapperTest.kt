package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.CharacterBaseData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterRecordMapperTest {

    @Test
    fun `maps generated character to CharacterRecord preserving all values`() {
        val record = character(
            id = 5,
            first = "Spike",
            last = "Spiegel",
            siteUrl = "https://anilist.co/character/5",
            isFavourite = true,
        ).toCharacterRecord()

        assertEquals(5L, record.id)
        assertEquals("Spike Spiegel", record.name)
        assertEquals("https://anilist.co/character/5", record.siteUrl)
        assertTrue(record.isFavourite)
    }

    @Test
    fun `converts generated id Int to domain Long`() {
        val record = character(id = Int.MAX_VALUE, first = null, last = null).toCharacterRecord()

        assertEquals(Int.MAX_VALUE.toLong(), record.id)
    }

    @Test
    fun `carries null siteUrl and false favourite defaults`() {
        val record = character(id = 1, first = "Spike", last = "Spiegel", siteUrl = null, isFavourite = false).toCharacterRecord()

        assertNull(record.siteUrl)
        assertFalse(record.isFavourite)
    }

    @Test
    fun `derives full name from first and last names`() {
        val record = character(id = 1, first = "Spike", last = "Spiegel").toCharacterRecord()

        assertEquals("Spike Spiegel", record.name)
    }

    @Test
    fun `falls back to last name when first is missing`() {
        val record = character(id = 1, first = null, last = "Spiegel").toCharacterRecord()

        assertEquals("Spiegel", record.name)
    }

    @Test
    fun `falls back to first name when last is missing`() {
        val record = character(id = 1, first = "Spike", last = null).toCharacterRecord()

        assertEquals("Spike", record.name)
    }

    @Test
    fun `carries null name when the generated name block is absent`() {
        val record = CharacterBaseData.Character(
            id = 1,
            name = null,
            image = null,
            isFavourite = false,
            siteUrl = null,
        ).toCharacterRecord()

        assertNull(record.name)
    }

    @Test
    fun `carries null name when both first and last are missing`() {
        val record = character(id = 1, first = null, last = null).toCharacterRecord()

        assertNull(record.name)
    }

    @Test
    fun `preserves legacy empty string semantics for blank names`() {
        val record = character(id = 1, first = "", last = "").toCharacterRecord()

        assertEquals("", record.name)
    }

    private fun character(
        id: Int,
        first: String?,
        last: String?,
        siteUrl: String? = "https://anilist.co/character/$id",
        isFavourite: Boolean = false,
    ): CharacterBaseData.Character = CharacterBaseData.Character(
        id = id,
        image = null,
        isFavourite = isFavourite,
        name = CharacterBaseData.CharacterName(
            alternative = null,
            first = first,
            last = last,
            native = null,
        ),
        siteUrl = siteUrl,
    )
}
