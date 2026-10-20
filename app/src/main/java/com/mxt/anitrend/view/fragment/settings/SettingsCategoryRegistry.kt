package com.mxt.anitrend.view.fragment.settings

import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil

/**
 * Stable identity of a settings category destination. Categories carry only
 * presentation resources and a stable string id; no mutable state or domain
 * entities are passed through navigation.
 */
data class SettingsCategory(
    val id: String,
    val titleRes: Int,
    val summaryRes: Int,
    val visible: Boolean = true,
)

/**
 * Pure mapping between stable settings category ids and the legacy
 * preference sections they render.
 *
 * Kept free of Android context so navigation argument behavior and section
 * visibility rules are unit-testable.
 */
object SettingsCategoryRegistry {

    const val ACCOUNT = "account"
    const val APPEARANCE = "appearance"
    const val GENERAL = "general"
    const val NOTIFICATIONS = "notifications"
    const val DATA_SYNC = "data_sync"
    const val PRIVACY = "privacy"
    const val ABOUT = "about"

    /**
     * Legacy category id of the removed Customize route. Inputs carrying it
     * resolve to [APPEARANCE], whose section owns the same rows.
     */
    const val LEGACY_CUSTOMIZE = "customize"

    /** Argument key carried by the category destination for identity-only navigation. */
    const val ARG_CATEGORY_ID = "categoryId"

    /**
     * The category hub entries in display order.
     *
     * AniList ([ACCOUNT]) is always listed first: signed-out users see a
     * sign-in card in that destination, authenticated users the account
     * editor. The privacy category is hidden when the Firebase build is not
     * present, mirroring the legacy single-screen visibility rule. About is
     * a dedicated destination, not a preference section.
     */
    fun categories(isFirebaseVisible: Boolean): List<SettingsCategory> = buildList {
        add(
            SettingsCategory(
                id = ACCOUNT,
                titleRes = R.string.pref_header_account,
                summaryRes = R.string.pref_header_account_summary,
            ),
        )
        addAll(
            listOf(
                SettingsCategory(APPEARANCE, R.string.pref_header_appearance, R.string.pref_header_appearance_summary),
                SettingsCategory(GENERAL, R.string.pref_header_general, R.string.pref_header_general_summary),
                SettingsCategory(NOTIFICATIONS, R.string.pref_header_notifications, R.string.pref_header_notifications_summary),
                SettingsCategory(DATA_SYNC, R.string.pref_header_data_sync, R.string.pref_header_data_sync_summary),
                SettingsCategory(PRIVACY, R.string.pref_header_privacy, R.string.pref_header_privacy_summary, visible = isFirebaseVisible),
                SettingsCategory(ABOUT, R.string.menu_title_about, R.string.settings_about_summary),
            ),
        )
    }.filter { it.visible }

    /** Whether [categoryId] is a known category, i.e. a valid navigation argument. */
    fun isKnown(categoryId: String?): Boolean = categories(isFirebaseVisible = true).any { it.id == categoryId } || categoryId == LEGACY_CUSTOMIZE

    /**
     * Resolves the settings section rendered for [categoryId], honoring the
     * same resource-gated visibility rules as the legacy single-screen
     * settings. The removed Customize category id aliases to Appearance,
     * whose section owns the same rows. Returns null for unknown ids so
     * callers can fall back safely.
     */
    fun sectionFor(
        categoryId: String?,
        isFirebaseVisible: Boolean,
        isUpdateChannelVisible: Boolean,
    ): SettingsSection? {
        val resolvedId = if (categoryId == LEGACY_CUSTOMIZE) APPEARANCE else categoryId
        return SettingsSections.build(
            isFirebaseVisible = isFirebaseVisible,
            isUpdateChannelVisible = isUpdateChannelVisible,
        ).firstOrNull { it.id == resolvedId }
    }
}

/** The settings row models rendered inside a section. */
sealed class SettingsRow(val visible: Boolean) {
    /** A selectable preference row with string entries and values. */
    data class Choice(
        val keyRes: Int,
        val titleRes: Int,
        val entriesRes: Int,
        val valuesRes: Int,
        val defaultValue: String,
        val summaryRes: Int? = null,
        val enabled: Boolean = true,
        val rowVisible: Boolean = true,
    ) : SettingsRow(rowVisible)

    /** A boolean preference row with optional state-specific summaries. */
    data class Toggle(
        val keyRes: Int,
        val titleRes: Int,
        val defaultValue: Boolean,
        val summaryRes: Int? = null,
        val summaryOnRes: Int? = null,
        val summaryOffRes: Int? = null,
        val enabled: Boolean = true,
        val rowVisible: Boolean = true,
    ) : SettingsRow(rowVisible) {
        /** Returns the summary resource appropriate for the current switch state. */
        fun summaryRes(checked: Boolean): Int? = when {
            checked && summaryOnRes != null -> summaryOnRes
            !checked && summaryOffRes != null -> summaryOffRes
            else -> summaryRes
        }
    }

    /** A non-interactive informational row. */
    data class Info(
        val titleRes: Int,
        val summaryRes: Int,
        val rowVisible: Boolean = true,
    ) : SettingsRow(rowVisible)
}

/** A rendered settings section keyed by its stable category id. */
data class SettingsSection(
    val id: String,
    val titleRes: Int,
    val summaryRes: Int,
    val rows: List<SettingsRow>,
    val visible: Boolean = true,
)

/**
 * Builds the legacy settings sections backed by the existing shared
 * preference store. Pure resource-id assembly, no Android context required.
 */
object SettingsSections {

    /** Builds all visible legacy-compatible settings sections. */
    fun build(
        isFirebaseVisible: Boolean,
        isUpdateChannelVisible: Boolean,
    ): List<SettingsSection> = listOf(
        appearanceSection(),
        generalSection(isUpdateChannelVisible),
        notificationsSection(),
        dataSyncSection(),
        privacySection(isFirebaseVisible),
    ).filter { it.visible }

    private fun appearanceSection() = SettingsSection(
        id = SettingsCategoryRegistry.APPEARANCE,
        titleRes = R.string.pref_header_appearance,
        summaryRes = R.string.pref_header_appearance_summary,
        rows = listOf(
            SettingsRow.Choice(
                keyRes = R.string.pref_key_app_theme,
                titleRes = R.string.pref_title_app_theme,
                entriesRes = R.array.pref_selected_theme_titles,
                valuesRes = R.array.pref_selected_theme_values,
                defaultValue = KeyUtil.THEME_LIGHT,
            ),
            SettingsRow.Choice(
                keyRes = R.string.pref_key_selected_language,
                titleRes = R.string.pref_title_language,
                summaryRes = R.string.pref_title_language_summary,
                entriesRes = R.array.pref_selected_language_titles,
                valuesRes = R.array.pref_selected_language_values,
                defaultValue = "en",
            ),
            SettingsRow.Choice(
                keyRes = R.string.pref_key_list_view_style,
                titleRes = R.string.pref_title_list_view_style,
                entriesRes = R.array.pref_selected_list_view_style_titles,
                valuesRes = R.array.pref_selected_list_view_style_values,
                defaultValue = "0",
            ),
        ),
    )

    private fun generalSection(isUpdateChannelVisible: Boolean) = SettingsSection(
        id = SettingsCategoryRegistry.GENERAL,
        titleRes = R.string.pref_header_general,
        summaryRes = R.string.pref_header_general_summary,
        rows = listOf(
            SettingsRow.Choice(
                keyRes = R.string.pref_key_startup_page,
                titleRes = R.string.pref_title_startup_page,
                entriesRes = R.array.pref_titles_menu_entries,
                valuesRes = R.array.pref_values_menu_entries,
                defaultValue = "3",
            ),
            SettingsRow.Choice(
                keyRes = R.string.pref_key_update_channel,
                titleRes = R.string.pref_title_update_channel,
                entriesRes = R.array.pref_titles_channel_entries,
                valuesRes = R.array.pref_values_channel_entries,
                defaultValue = "master",
                rowVisible = isUpdateChannelVisible,
            ),
        ),
    )

    private fun notificationsSection() = SettingsSection(
        id = SettingsCategoryRegistry.NOTIFICATIONS,
        titleRes = R.string.pref_header_notifications,
        summaryRes = R.string.pref_header_notifications_summary,
        rows = listOf(
            SettingsRow.Toggle(
                keyRes = R.string.pref_key_new_message_notifications,
                titleRes = R.string.pref_title_new_message_notifications,
                defaultValue = true,
            ),
            SettingsRow.Toggle(
                keyRes = R.string.pref_key_clear_notification_on_dismiss,
                titleRes = R.string.pref_title_clear_notification_on_dismiss,
                summaryRes = R.string.pref_summary_clear_notification_on_dismiss,
                defaultValue = false,
            ),
            SettingsRow.Toggle(
                keyRes = R.string.pref_key_notification_work_around,
                titleRes = R.string.pref_title_notification_work_around,
                summaryRes = R.string.pref_summary_notification_work_around,
                defaultValue = false,
                enabled = false,
            ),
        ),
    )

    private fun dataSyncSection() = SettingsSection(
        id = SettingsCategoryRegistry.DATA_SYNC,
        titleRes = R.string.pref_header_data_sync,
        summaryRes = R.string.pref_header_data_sync_summary,
        rows = listOf(
            SettingsRow.Choice(
                keyRes = R.string.pref_key_sync_frequency,
                titleRes = R.string.pref_title_sync_frequency,
                entriesRes = R.array.pref_sync_frequency_titles,
                valuesRes = R.array.pref_sync_frequency_values,
                defaultValue = "15",
            ),
        ),
    )

    private fun privacySection(isFirebaseVisible: Boolean) = SettingsSection(
        id = SettingsCategoryRegistry.PRIVACY,
        titleRes = R.string.pref_header_privacy,
        summaryRes = R.string.pref_header_privacy_summary,
        rows = listOf(
            SettingsRow.Toggle(
                keyRes = R.string.pref_key_crash_reports,
                titleRes = R.string.pref_title_crash_reports,
                summaryOnRes = R.string.pref_crash_reports_summary_on,
                summaryOffRes = R.string.pref_crash_reports_summary_off,
                defaultValue = true,
            ),
            SettingsRow.Toggle(
                keyRes = R.string.pref_key_usage_analytics,
                titleRes = R.string.pref_title_usage_analytics,
                summaryOnRes = R.string.pref_usage_analytics_summary_on,
                summaryOffRes = R.string.pref_usage_analytics_summary_off,
                defaultValue = false,
            ),
        ),
        visible = isFirebaseVisible,
    )
}
