package com.mxt.anitrend.view.fragment.settings

import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Focused tests for the settings category registry, covering navigation
 * argument behavior (known/unknown ids) and the legacy section visibility
 * rules.
 */
class SettingsCategoryRegistryTest {

    private fun sectionFor(
        categoryId: String?,
        isFirebaseVisible: Boolean = true,
        isUpdateChannelVisible: Boolean = true,
    ): SettingsSection? = SettingsCategoryRegistry.sectionFor(
        categoryId = categoryId,
        isFirebaseVisible = isFirebaseVisible,
        isUpdateChannelVisible = isUpdateChannelVisible,
    )

    // ── hub categories ──

    @Test
    fun `categories exposes all hub categories in display order`() {
        val categories = SettingsCategoryRegistry.categories(isFirebaseVisible = true)

        assertEquals(7, categories.size)
        assertEquals(
            listOf(
                SettingsCategoryRegistry.ACCOUNT,
                SettingsCategoryRegistry.APPEARANCE,
                SettingsCategoryRegistry.GENERAL,
                SettingsCategoryRegistry.NOTIFICATIONS,
                SettingsCategoryRegistry.DATA_SYNC,
                SettingsCategoryRegistry.PRIVACY,
                SettingsCategoryRegistry.ABOUT,
            ),
            categories.map { it.id },
        )
    }

    @Test
    fun `categories hides privacy category when Firebase is absent`() {
        val categories = SettingsCategoryRegistry.categories(isFirebaseVisible = false)

        assertEquals(6, categories.size)
        assertFalse(categories.any { it.id == SettingsCategoryRegistry.PRIVACY })
    }

    // ── account category ──

    @Test
    fun `categories always lists the account category first for guest sign-in`() {
        val categories = SettingsCategoryRegistry.categories(isFirebaseVisible = true)

        assertEquals(SettingsCategoryRegistry.ACCOUNT, categories.first().id)
    }

    @Test
    fun `account category is known as a navigation argument`() {
        assertTrue(SettingsCategoryRegistry.isKnown(SettingsCategoryRegistry.ACCOUNT))
    }

    @Test
    fun `sectionFor returns null for the account category since it has a dedicated destination`() {
        assertNull(sectionFor(SettingsCategoryRegistry.ACCOUNT))
    }

    @Test
    fun `about category is known and has a dedicated destination`() {
        assertTrue(SettingsCategoryRegistry.isKnown(SettingsCategoryRegistry.ABOUT))
        assertNull(sectionFor(SettingsCategoryRegistry.ABOUT))
    }

    @Test
    fun `every hub category resolves to a title and summary`() {
        SettingsCategoryRegistry.categories(isFirebaseVisible = true).forEach { category ->
            assertTrue("$category title missing", category.titleRes != 0)
            assertTrue("$category summary missing", category.summaryRes != 0)
        }
    }

    // ── navigation argument behavior ──

    @Test
    fun `isKnown accepts every hub category id`() {
        SettingsCategoryRegistry.categories(isFirebaseVisible = true).forEach { category ->
            assertTrue("${category.id} should be known", SettingsCategoryRegistry.isKnown(category.id))
        }
    }

    @Test
    fun `isKnown rejects unknown, null, and empty ids`() {
        assertFalse(SettingsCategoryRegistry.isKnown("not_a_category"))
        assertFalse(SettingsCategoryRegistry.isKnown(""))
        assertFalse(SettingsCategoryRegistry.isKnown(null))
    }

    @Test
    fun `sectionFor resolves every preference category to its section`() {
        val dedicatedDestinations = setOf(SettingsCategoryRegistry.ACCOUNT, SettingsCategoryRegistry.ABOUT)
        SettingsCategoryRegistry.categories(isFirebaseVisible = true)
            .filter { it.id !in dedicatedDestinations }
            .forEach { category ->
                val section = sectionFor(category.id)
                assertNotNull("section for ${category.id} must exist", section)
                assertEquals(category.id, section!!.id)
                assertEquals(category.titleRes, section.titleRes)
                assertEquals(category.summaryRes, section.summaryRes)
            }
    }

    @Test
    fun `sectionFor returns null for unknown and empty ids`() {
        assertNull(sectionFor("not_a_category"))
        assertNull(sectionFor(""))
        assertNull(sectionFor(null))
    }

    @Test
    fun `sectionFor hides privacy section when Firebase is absent`() {
        assertNull(sectionFor(SettingsCategoryRegistry.PRIVACY, isFirebaseVisible = false))
        assertNotNull(sectionFor(SettingsCategoryRegistry.PRIVACY, isFirebaseVisible = true))
    }

    // ── legacy visibility rules inside sections ──

    @Test
    fun `general section filters rows by resource-gated visibility flags`() {
        val filtered = sectionFor(
            SettingsCategoryRegistry.GENERAL,
            isUpdateChannelVisible = false,
        )!!
        assertEquals(1, filtered.rows.filter { it.visible }.size)
        assertEquals(R.string.pref_key_startup_page, (filtered.rows.filter { it.visible }.first() as SettingsRow.Choice).keyRes)

        val full = sectionFor(
            SettingsCategoryRegistry.GENERAL,
            isUpdateChannelVisible = true,
        )!!
        assertEquals(2, full.rows.filter { it.visible }.size)
    }

    @Test
    fun `appearance section keeps legacy row defaults and keys`() {
        val section = sectionFor(SettingsCategoryRegistry.APPEARANCE)!!
        val rows = section.rows.filter { it.visible }

        assertEquals(3, rows.size)

        val theme = rows[0] as SettingsRow.Choice
        assertEquals(R.string.pref_key_app_theme, theme.keyRes)
        assertEquals(KeyUtil.THEME_LIGHT, theme.defaultValue)

        val language = rows[1] as SettingsRow.Choice
        assertEquals(R.string.pref_key_selected_language, language.keyRes)
        assertEquals("en", language.defaultValue)

        val listStyle = rows[2] as SettingsRow.Choice
        assertEquals(R.string.pref_key_list_view_style, listStyle.keyRes)
        assertEquals("0", listStyle.defaultValue)
    }

    @Test
    fun `legacy customize input aliases to the appearance section`() {
        val aliased = sectionFor(SettingsCategoryRegistry.LEGACY_CUSTOMIZE)
        val appearance = sectionFor(SettingsCategoryRegistry.APPEARANCE)

        assertNotNull(aliased)
        assertEquals(appearance!!.id, aliased!!.id)
        assertEquals(appearance.rows, aliased.rows)
    }

    @Test
    fun `notification workaround row stays disabled`() {
        val section = sectionFor(SettingsCategoryRegistry.NOTIFICATIONS)!!
        val workaround = section.rows.filterIsInstance<SettingsRow.Toggle>()
            .first { it.keyRes == R.string.pref_key_notification_work_around }

        assertFalse(workaround.enabled)
    }

    @Test
    fun `privacy summaries follow the checked state`() {
        val privacyRows = sectionFor(SettingsCategoryRegistry.PRIVACY)!!.rows.filterIsInstance<SettingsRow.Toggle>()
        val crashReports = privacyRows.first { it.keyRes == R.string.pref_key_crash_reports }
        val usageAnalytics = privacyRows.first { it.keyRes == R.string.pref_key_usage_analytics }

        assertEquals(R.string.pref_crash_reports_summary_on, crashReports.summaryRes(checked = true))
        assertEquals(R.string.pref_crash_reports_summary_off, crashReports.summaryRes(checked = false))
        assertEquals(R.string.pref_usage_analytics_summary_on, usageAnalytics.summaryRes(checked = true))
        assertEquals(R.string.pref_usage_analytics_summary_off, usageAnalytics.summaryRes(checked = false))
    }
}
