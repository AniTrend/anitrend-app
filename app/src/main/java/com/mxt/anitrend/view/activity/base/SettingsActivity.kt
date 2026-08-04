package com.mxt.anitrend.view.activity.base

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.FragmentSettingsM3Binding
import com.mxt.anitrend.databinding.ItemSettingsRowSwitchBinding
import com.mxt.anitrend.databinding.ItemSettingsRowValueBinding
import com.mxt.anitrend.databinding.ItemSettingsSectionCardBinding
import com.mxt.anitrend.databinding.SettingsActivityBinding
import com.mxt.anitrend.extension.applyConfiguredTheme
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.ui.commit
import com.mxt.anitrend.ui.model.FragmentItem
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.CommonActivity
import org.koin.android.ext.android.inject
import timber.log.Timber

class SettingsActivity : CommonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            FragmentItem(
                fragment = MaterialSettingsFragment::class.java,
            ).commit(R.id.settings, this)
        }
    }

    /**
     * Material 3 settings screen backed by the existing shared preference store.
     */
    class MaterialSettingsFragment :
        Fragment(),
        SharedPreferences.OnSharedPreferenceChangeListener {

        private var binding: FragmentSettingsM3Binding? = null

        private val settings by inject<Settings>()
        private val scheduler by inject<JobSchedulerUtil>()
        private val presenter by inject<BasePresenter>()

        private val sideEffectHandler by lazy(LazyThreadSafetyMode.NONE) {
            SettingsPreferenceChangeHandler(settings, scheduler, presenter)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            binding = FragmentSettingsM3Binding.inflate(inflater, container, false)
            return binding!!.root
        }

        override fun onResume() {
            super.onResume()
            settings.registerOnSharedPreferenceChangeListener(this)
            renderSections()
        }

        override fun onPause() {
            settings.unregisterOnSharedPreferenceChangeListener(this)
            super.onPause()
        }

        override fun onDestroyView() {
            binding = null
            super.onDestroyView()
        }

        override fun onSharedPreferenceChanged(
            preferences: SharedPreferences?,
            key: String?,
        ) {
            activity?.let { sideEffectHandler.handle(it, key) }
            if (view != null) {
                renderSections()
            }
        }

        private fun renderSections() {
            val binding = binding ?: return
            val context = requireContext()
            val sectionHost = binding.settingsSections
            sectionHost.removeAllViews()

            MaterialSettingsSections.build(
                isFirebaseVisible = FirebaseApp.getApps(context).isNotEmpty(),
                isUpdateChannelVisible = resources.getBoolean(R.bool.display_update_channel_pref),
                isAdultContentVisible = resources.getBoolean(R.bool.display_adult_content_pref),
            ).forEach { section ->
                val sectionBinding = ItemSettingsSectionCardBinding.inflate(layoutInflater, sectionHost, false)
                sectionBinding.sectionTitle.setText(section.titleRes)
                sectionBinding.sectionSummary.setText(section.summaryRes)

                val visibleRows = section.rows.filter { it.visible }
                visibleRows.forEachIndexed { index, row ->
                    when (row) {
                        is SettingsRow.Choice -> bindChoiceRow(sectionBinding.sectionContent, row)
                        is SettingsRow.Info -> bindInfoRow(sectionBinding.sectionContent, row)
                        is SettingsRow.Toggle -> bindToggleRow(sectionBinding.sectionContent, row)
                    }
                    if (index < visibleRows.lastIndex) {
                        addDivider(sectionBinding.sectionContent)
                    }
                }

                sectionHost.addView(sectionBinding.root)
            }
        }

        private fun bindChoiceRow(
            container: LinearLayout,
            row: SettingsRow.Choice,
        ) {
            val rowBinding = ItemSettingsRowValueBinding.inflate(layoutInflater, container, false)
            val context = rowBinding.root.context
            val labels = context.resources.getStringArray(row.entriesRes)
            val values = context.resources.getStringArray(row.valuesRes)
            val preferenceKey = context.getString(row.keyRes)
            val currentValue = settings.getString(preferenceKey, row.defaultValue) ?: row.defaultValue
            val currentLabel = labels[values.indexOf(currentValue).coerceAtLeast(0)]

            rowBinding.rowTitle.setText(row.titleRes)
            rowBinding.rowSummary.apply {
                if (row.summaryRes != null) {
                    setText(row.summaryRes)
                    visibility = View.VISIBLE
                } else {
                    visibility = View.GONE
                }
            }
            rowBinding.rowValue.text = currentLabel
            rowBinding.rowValue.visibility = View.VISIBLE
            rowBinding.rowChevron.visibility = if (row.enabled) View.VISIBLE else View.GONE
            rowBinding.root.isEnabled = row.enabled
            rowBinding.root.isClickable = row.enabled
            rowBinding.rowTitle.isEnabled = row.enabled
            rowBinding.rowSummary.isEnabled = row.enabled
            rowBinding.rowValue.isEnabled = row.enabled
            rowBinding.rowChevron.isEnabled = row.enabled
            rowBinding.root.alpha = if (row.enabled) 1f else DISABLED_ALPHA

            rowBinding.root.contentDescription = context.getString(
                R.string.settings_choice_content_description,
                context.getString(row.titleRes),
                currentLabel,
            )

            if (row.enabled) {
                rowBinding.root.setOnClickListener {
                    MaterialAlertDialogBuilder(context)
                        .setTitle(row.titleRes)
                        .setSingleChoiceItems(labels, values.indexOf(currentValue).coerceAtLeast(0)) { dialog, which ->
                            settings.edit { putString(preferenceKey, values[which]) }
                            dialog.dismiss()
                        }.setNegativeButton(R.string.Cancel, null)
                        .show()
                }
            } else {
                rowBinding.root.setOnClickListener(null)
            }

            container.addView(rowBinding.root)
        }

        private fun bindToggleRow(
            container: LinearLayout,
            row: SettingsRow.Toggle,
        ) {
            val rowBinding = ItemSettingsRowSwitchBinding.inflate(layoutInflater, container, false)
            val context = rowBinding.root.context
            val preferenceKey = context.getString(row.keyRes)
            val checked = settings.getBoolean(preferenceKey, row.defaultValue)

            rowBinding.rowTitle.setText(row.titleRes)
            rowBinding.rowSummary.apply {
                val summaryRes = row.summaryRes(checked)
                if (summaryRes != null) {
                    setText(summaryRes)
                    visibility = View.VISIBLE
                } else {
                    visibility = View.GONE
                }
            }
            rowBinding.rowSwitch.isChecked = checked
            rowBinding.root.isEnabled = row.enabled
            rowBinding.root.isClickable = row.enabled
            rowBinding.rowTitle.isEnabled = row.enabled
            rowBinding.rowSummary.isEnabled = row.enabled
            rowBinding.rowSwitch.isEnabled = row.enabled
            rowBinding.root.alpha = if (row.enabled) 1f else DISABLED_ALPHA

            rowBinding.root.contentDescription = context.getString(
                R.string.settings_toggle_content_description,
                context.getString(row.titleRes),
                context.getString(if (checked) R.string.accessibility_switch_on else R.string.accessibility_switch_off),
            )

            if (row.enabled) {
                rowBinding.root.setOnClickListener {
                    rowBinding.rowSwitch.isChecked = !rowBinding.rowSwitch.isChecked
                }
                rowBinding.rowSwitch.setOnCheckedChangeListener { _, isChecked ->
                    settings.edit { putBoolean(preferenceKey, isChecked) }
                }
            } else {
                rowBinding.root.setOnClickListener(null)
                rowBinding.rowSwitch.setOnCheckedChangeListener(null)
            }

            container.addView(rowBinding.root)
        }

        private fun bindInfoRow(
            container: LinearLayout,
            row: SettingsRow.Info,
        ) {
            val rowBinding = ItemSettingsRowValueBinding.inflate(layoutInflater, container, false)
            rowBinding.rowTitle.setText(row.titleRes)
            rowBinding.rowSummary.setText(row.summaryRes)
            rowBinding.rowSummary.visibility = View.VISIBLE
            rowBinding.rowValue.visibility = View.GONE
            rowBinding.rowChevron.visibility = View.GONE
            rowBinding.root.isEnabled = false
            rowBinding.root.isClickable = false
            rowBinding.rowTitle.isEnabled = false
            rowBinding.rowSummary.isEnabled = false
            rowBinding.root.alpha = DISABLED_ALPHA
            container.addView(rowBinding.root)
        }

        private fun addDivider(container: LinearLayout) {
            val divider = layoutInflater.inflate(R.layout.item_settings_divider, container, false)
            container.addView(divider)
        }

        /** Alpha used to communicate disabled setting rows without hiding them. */
        companion object {
            private const val DISABLED_ALPHA = 0.38f
        }
    }

    private data class SettingsSection(
        val titleRes: Int,
        val summaryRes: Int,
        val rows: List<SettingsRow>,
        val visible: Boolean = true,
    )

    private sealed class SettingsRow(val visible: Boolean) {
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
            fun summaryRes(checked: Boolean): Int? = when {
                checked && summaryOnRes != null -> summaryOnRes
                !checked && summaryOffRes != null -> summaryOffRes
                else -> summaryRes
            }
        }

        data class Info(
            val titleRes: Int,
            val summaryRes: Int,
            val rowVisible: Boolean = true,
        ) : SettingsRow(rowVisible)
    }

    private object MaterialSettingsSections {

        fun build(
            isFirebaseVisible: Boolean,
            isUpdateChannelVisible: Boolean,
            isAdultContentVisible: Boolean,
        ): List<SettingsSection> = listOf(
            customizeSection(),
            appearanceSection(),
            contentSection(),
            generalSection(isUpdateChannelVisible, isAdultContentVisible),
            notificationsSection(),
            dataSyncSection(),
            privacySection(isFirebaseVisible),
            accessibilitySection(),
        ).filter { it.visible }

        private fun customizeSection() = SettingsSection(
            titleRes = R.string.pref_header_customize,
            summaryRes = R.string.pref_header_customize_summary,
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

        private fun appearanceSection() = SettingsSection(
            titleRes = R.string.pref_header_appearance,
            summaryRes = R.string.pref_header_appearance_summary,
            rows = listOf(
                SettingsRow.Info(R.string.pref_title_accent_color, R.string.pref_summary_placeholder),
                SettingsRow.Info(R.string.pref_title_font_scale, R.string.pref_summary_placeholder),
                SettingsRow.Info(R.string.pref_title_list_density, R.string.pref_summary_placeholder),
            ),
        )

        private fun contentSection() = SettingsSection(
            titleRes = R.string.pref_header_content,
            summaryRes = R.string.pref_header_content_summary,
            rows = listOf(
                SettingsRow.Info(R.string.pref_title_autoplay, R.string.pref_summary_placeholder),
                SettingsRow.Info(R.string.pref_title_spoiler_behavior, R.string.pref_summary_placeholder),
                SettingsRow.Info(R.string.pref_title_title_language, R.string.pref_summary_placeholder),
                SettingsRow.Info(R.string.pref_title_score_format, R.string.pref_summary_placeholder),
            ),
        )

        private fun generalSection(
            isUpdateChannelVisible: Boolean,
            isAdultContentVisible: Boolean,
        ) = SettingsSection(
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
                SettingsRow.Toggle(
                    keyRes = R.string.pref_key_display_adult_content,
                    titleRes = R.string.pref_title_display_adult_content,
                    summaryRes = R.string.pref_summary_display_adult_content,
                    defaultValue = false,
                    rowVisible = isAdultContentVisible,
                ),
            ),
        )

        private fun notificationsSection() = SettingsSection(
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

        private fun accessibilitySection() = SettingsSection(
            titleRes = R.string.pref_header_accessibility,
            summaryRes = R.string.pref_header_accessibility_summary,
            rows = listOf(
                SettingsRow.Info(R.string.pref_title_reduce_motion, R.string.pref_summary_placeholder),
                SettingsRow.Info(R.string.pref_title_high_contrast, R.string.pref_summary_placeholder),
            ),
        )
    }
}

private class SettingsPreferenceChangeHandler(
    private val settings: Settings,
    private val scheduler: JobSchedulerUtil,
    private val presenter: BasePresenter,
) {

    fun handle(
        fragmentActivity: FragmentActivity,
        key: String?,
    ) {
        when (key) {
            fragmentActivity.getString(R.string.pref_key_display_adult_content),
            fragmentActivity.getString(R.string.pref_key_crash_reports),
            fragmentActivity.getString(R.string.pref_key_usage_analytics),
            fragmentActivity.getString(R.string.pref_key_list_view_style),
            -> {
                requireRestartNotice(fragmentActivity)
            }
            fragmentActivity.getString(R.string.pref_key_selected_language) -> {
                val locales = LocaleListCompat.forLanguageTags(settings.userLanguage)
                AppCompatDelegate.setApplicationLocales(locales)
            }
            fragmentActivity.getString(R.string.pref_key_startup_page) -> {
                if (!settings.isAuthenticated) {
                    NotifyUtil.makeText(fragmentActivity, R.string.info_login_req, Toast.LENGTH_SHORT).show()
                } else {
                    requireRestartNotice(fragmentActivity)
                }
            }
            fragmentActivity.getString(R.string.pref_key_app_theme) -> {
                fragmentActivity.applyConfiguredTheme()
            }
            fragmentActivity.getString(R.string.pref_key_sync_frequency) -> {
                scheduler.cancelNotificationJob(fragmentActivity.applicationContext)
                scheduler.cancelTagJob(fragmentActivity.applicationContext)
                scheduler.cancelGenreJob(fragmentActivity.applicationContext)
                scheduler.scheduleNotificationJob(fragmentActivity.applicationContext)
                scheduler.scheduleGenreJob(fragmentActivity.applicationContext)
                scheduler.scheduleTagJob(fragmentActivity.applicationContext)
            }
            fragmentActivity.getString(R.string.pref_key_new_message_notifications) -> {
                if (settings.isNotificationEnabled) {
                    scheduler.scheduleNotificationJob(fragmentActivity.applicationContext)
                } else {
                    scheduler.cancelNotificationJob(fragmentActivity.applicationContext)
                }
            }
            fragmentActivity.getString(R.string.pref_key_update_channel) -> {
                presenter.database.remoteVersion = null
            }
            else -> Timber.i("$key not registered in this sharedPreferenceChange listener")
        }
    }

    private fun requireRestartNotice(fragmentActivity: FragmentActivity) {
        DialogUtil
            .createDefaultDialog(fragmentActivity)
            .setPositiveButton(R.string.Ok) { d, _ -> d.dismiss() }
            .setMessage(R.string.text_application_restart_required)
            .show()
    }
}
