package com.mxt.anitrend.ui.manage

import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Manage-sheet date picker mapping contract: the staged [FuzzyDate] seeds the
 * M3 picker in UTC and a confirmed pick maps back without field drift. The
 * absent or yearless staged value seeds nothing so the picker defaults to
 * today, mirroring the legacy clear-to-current-date behavior.
 */
class ManageListDateMappingTest {

    @Test
    fun `absent staged date seeds nothing so the picker defaults to today`() {
        assertNull(null.pickerSeedMillis())
    }

    @Test
    fun `yearless staged date seeds nothing`() {
        assertNull(FuzzyDate(day = 0, month = 0, year = 0).pickerSeedMillis())
    }

    @Test
    fun `staged date round trips through picker millis without field drift`() {
        val staged = FuzzyDate(day = 15, month = 6, year = 2024)

        val restored = epochMillisToFuzzyDate(requireNotNull(staged.pickerSeedMillis()))

        assertEquals(15, restored.day)
        assertEquals(6, restored.month)
        assertEquals(2024, restored.year)
    }

    @Test
    fun `partial staged month and day seed as january first`() {
        val staged = FuzzyDate(day = 0, month = 0, year = 2024)

        val millis = requireNotNull(staged.pickerSeedMillis())
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = millis

        assertEquals(2024, calendar.get(Calendar.YEAR))
        assertEquals(0, calendar.get(Calendar.MONTH))
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `picked millis map back in utc regardless of the device time zone`() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(2025, Calendar.OCTOBER, 19, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val restored = epochMillisToFuzzyDate(calendar.timeInMillis)

        assertEquals(19, restored.day)
        assertEquals(10, restored.month)
        assertEquals(2025, restored.year)
    }
}
