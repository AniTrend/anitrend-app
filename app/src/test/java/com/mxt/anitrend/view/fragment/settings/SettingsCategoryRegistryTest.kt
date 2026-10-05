package com.mxt.anitrend.view.fragment.settings

import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.viewmodel.CustomizeSettingsViewModel
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
        isAdultContentVisible: Boolean = true,
    ): SettingsSection? = SettingsCategoryRegistry.sectionFor(
        categoryId = categoryId,
        isFirebaseVisible = isFirebaseVisible,
        isUpdateChannelVisible = isUpdateChannelVisible,
        isAdultContentVisible = isAdultContentVisible,
    )

    // ── hub categories ──

    @Test
    fun `categories exposes all eight categories in display order`() {
        val categories = SettingsCategoryRegistry.categories(isFirebaseVisible = true)

        assertEquals(8, categories.size)
        assertEquals(
            listOf(
                SettingsCategoryRegistry.CUSTOMIZE,
                SettingsCategoryRegistry.APPEARANCE,
                SettingsCategoryRegistry.CONTENT,
                SettingsCategoryRegistry.GENERAL,
                SettingsCategoryRegistry.NOTIFICATIONS,
                SettingsCategoryRegistry.DATA_SYNC,
                SettingsCategoryRegistry.PRIVACY,
                SettingsCategoryRegistry.ACCESSIBILITY,
            ),
            categories.map { it.id },
        )
    }

    @Test
    fun `categories hides privacy category when Firebase is absent`() {
        val categories = SettingsCategoryRegistry.categories(isFirebaseVisible = false)

        assertEquals(7, categories.size)
        assertFalse(categories.any { it.id == SettingsCategoryRegistry.PRIVACY })
    }

    // ── account category (authenticated) ──

    @Test
    fun `categories lists the account category first when authenticated`() {
        val categories = SettingsCategoryRegistry.categories(
            isFirebaseVisible = true,
            isAuthenticated = true,
        )

        assertEquals(9, categories.size)
        assertEquals(SettingsCategoryRegistry.ACCOUNT, categories.first().id)
        // The legacy eight keep their relative order after the account entry.
        assertEquals(
            listOf(
                SettingsCategoryRegistry.ACCOUNT,
                SettingsCategoryRegistry.CUSTOMIZE,
                SettingsCategoryRegistry.APPEARANCE,
                SettingsCategoryRegistry.CONTENT,
                SettingsCategoryRegistry.GENERAL,
                SettingsCategoryRegistry.NOTIFICATIONS,
                SettingsCategoryRegistry.DATA_SYNC,
                SettingsCategoryRegistry.PRIVACY,
                SettingsCategoryRegistry.ACCESSIBILITY,
            ),
            categories.map { it.id },
        )
    }

    @Test
    fun `categories omit the account category when not authenticated`() {
        val categories = SettingsCategoryRegistry.categories(
            isFirebaseVisible = true,
            isAuthenticated = false,
        )

        assertEquals(8, categories.size)
        assertFalse(categories.any { it.id == SettingsCategoryRegistry.ACCOUNT })
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
    fun `sectionFor resolves every known category to its section`() {
        SettingsCategoryRegistry.categories(isFirebaseVisible = true).forEach { category ->
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
            isAdultContentVisible = false,
        )!!
        assertEquals(1, filtered.rows.filter { it.visible }.size)
        assertEquals(R.string.pref_key_startup_page, (filtered.rows.filter { it.visible }.first() as SettingsRow.Choice).keyRes)

        val full = sectionFor(
            SettingsCategoryRegistry.GENERAL,
            isUpdateChannelVisible = true,
            isAdultContentVisible = true,
        )!!
        assertEquals(3, full.rows.filter { it.visible }.size)
    }

    @Test
    fun `customize section keeps legacy row defaults and keys`() {
        val section = sectionFor(SettingsCategoryRegistry.CUSTOMIZE)!!
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
    fun `notification workaround row stays disabled`() {
        val section = sectionFor(SettingsCategoryRegistry.NOTIFICATIONS)!!
        val workaround = section.rows.filterIsInstance<SettingsRow.Toggle>()
            .first { it.keyRes == R.string.pref_key_notification_work_around }

        assertFalse(workaround.enabled)
    }

    // ── canonical customize rows ──

    @Test
    fun `customizeRows contains each persisted customize key exactly once`() {
        val rows = SettingsCategoryRegistry.customizeRows()

        assertEquals(3, rows.size)
        val keys = rows.map { it.keyRes }
        assertEquals(keys.size, keys.toSet().size)
        assertTrue(R.string.pref_key_app_theme in keys)
        assertTrue(R.string.pref_key_selected_language in keys)
        assertTrue(R.string.pref_key_list_view_style in keys)
    }

    @Test
    fun `customize section renderer consumes the canonical customize rows`() {
        val canonical: List<SettingsRow> = SettingsCategoryRegistry.customizeRows()
        val sectionRows = sectionFor(SettingsCategoryRegistry.CUSTOMIZE)!!.rows

        assertEquals(canonical, sectionRows)
    }

    @Test
    fun `customize rows carry the same defaults as the customize view model`() {
        val defaults = SettingsCategoryRegistry.customizeRows().associate { it.keyRes to it.defaultValue }

        assertEquals(KeyUtil.THEME_LIGHT, defaults.getValue(R.string.pref_key_app_theme))
        assertEquals(CustomizeSettingsViewModel.DEFAULT_LANGUAGE, defaults.getValue(R.string.pref_key_selected_language))
        assertEquals(CustomizeSettingsViewModel.DEFAULT_LIST_VIEW_STYLE, defaults.getValue(R.string.pref_key_list_view_style))
    }
}
