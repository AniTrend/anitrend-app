package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.StaffBaseData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StaffRecordMapperTest {

    @Test
    fun `maps generated staff to StaffRecord preserving all values`() {
        val record = staff(
            id = 5,
            first = "Shinichiro",
            last = "Watanabe",
            siteUrl = "https://anilist.co/staff/5",
            isFavourite = true,
        ).toStaffRecord()

        assertEquals(5L, record.id)
        assertEquals("Shinichiro Watanabe", record.name)
        assertEquals("https://anilist.co/staff/5", record.siteUrl)
        assertTrue(record.isFavourite)
    }

    @Test
    fun `converts generated id Int to domain Long`() {
        val record = staff(id = Int.MAX_VALUE, first = null, last = null).toStaffRecord()

        assertEquals(Int.MAX_VALUE.toLong(), record.id)
    }

    @Test
    fun `carries null siteUrl and false favourite defaults`() {
        val record = staff(id = 1, first = "Shinichiro", last = "Watanabe", siteUrl = null, isFavourite = false).toStaffRecord()

        assertNull(record.siteUrl)
        assertFalse(record.isFavourite)
    }

    @Test
    fun `derives full name from first and last names`() {
        val record = staff(id = 1, first = "Shinichiro", last = "Watanabe").toStaffRecord()

        assertEquals("Shinichiro Watanabe", record.name)
    }

    @Test
    fun `falls back to last name when first is missing`() {
        val record = staff(id = 1, first = null, last = "Watanabe").toStaffRecord()

        assertEquals("Watanabe", record.name)
    }

    @Test
    fun `falls back to first name when last is missing`() {
        val record = staff(id = 1, first = "Shinichiro", last = null).toStaffRecord()

        assertEquals("Shinichiro", record.name)
    }

    @Test
    fun `carries null name when the generated name block is absent`() {
        val record = StaffBaseData.Staff(
            id = 1,
            image = null,
            isFavourite = false,
            language = null,
            name = null,
            siteUrl = null,
        ).toStaffRecord()

        assertNull(record.name)
    }

    @Test
    fun `carries null name when both first and last are missing`() {
        val record = staff(id = 1, first = null, last = null).toStaffRecord()

        assertNull(record.name)
    }

    @Test
    fun `preserves legacy empty string semantics for blank names`() {
        val record = staff(id = 1, first = "", last = "").toStaffRecord()

        assertEquals("", record.name)
    }

    private fun staff(
        id: Int,
        first: String?,
        last: String?,
        siteUrl: String? = "https://anilist.co/staff/$id",
        isFavourite: Boolean = false,
    ): StaffBaseData.Staff = StaffBaseData.Staff(
        id = id,
        image = null,
        isFavourite = isFavourite,
        language = null,
        name = StaffBaseData.StaffName(
            alternative = null,
            first = first,
            full = null,
            last = last,
            native = null,
        ),
        siteUrl = siteUrl,
    )
}
