package com.mxt.anitrend.view.fragment.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.FragmentSettingsM3Binding
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.SettingsCategoryScreenParam
import com.mxt.anitrend.ui.settings.SettingsDivider
import com.mxt.anitrend.ui.settings.SettingsValueRow
import com.mxt.anitrend.ui.settings.addSettingsComposeContent
import com.mxt.anitrend.ui.settings.SettingsSectionCard
import com.mxt.anitrend.ui.settings.SettingsToggleRow
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.Settings
import org.koin.android.ext.android.inject

/**
 * Renders a settings category with Compose while retaining the existing
 * preference storage and side-effect handling.
 */

class SettingsCategoryLegacyFragment :
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
        val sectionHost = binding.settingsSections
        sectionHost.removeAllViews()

        val categoryId = fromBundle(arguments)?.categoryId ?: return
        val section = SettingsCategoryRegistry.sectionFor(
            categoryId = categoryId,
            isFirebaseVisible = FirebaseApp.getApps(requireContext()).isNotEmpty(),
            isUpdateChannelVisible = resources.getBoolean(R.bool.display_update_channel_pref),
            isAdultContentVisible = resources.getBoolean(R.bool.display_adult_content_pref),
        ) ?: return
        activity?.title = getString(section.titleRes)

        val visibleRows = section.rows.filter { it.visible }
        sectionHost.addSettingsComposeContent {
            val context = LocalContext.current
            SettingsSectionCard(
                title = context.getString(section.titleRes),
                summary = context.getString(section.summaryRes),
                iconRes = iconForSection(section.id),
            ) {
                visibleRows.forEachIndexed { index, row ->
                    when (row) {
                        is SettingsRow.Choice -> renderChoiceRow(row)
                        is SettingsRow.Info -> renderInfoRow(row)
                        is SettingsRow.Toggle -> renderToggleRow(row)
                    }
                    if (index < visibleRows.lastIndex) {
                        SettingsDivider()
                    }
                }
            }
        }
    }

    @Composable
    private fun renderChoiceRow(row: SettingsRow.Choice) {
        val context = LocalContext.current
        val labels = context.resources.getStringArray(row.entriesRes)
        val values = context.resources.getStringArray(row.valuesRes)
        val preferenceKey = context.getString(row.keyRes)
        val currentValue = settings.getString(preferenceKey, row.defaultValue) ?: row.defaultValue
        val currentValueIndex = values.indexOf(currentValue).coerceAtLeast(0)
        val currentLabel = labels[currentValueIndex]
        val title = context.getString(row.titleRes)
        val contentDescription =
            context.getString(
                R.string.settings_choice_content_description,
                title,
                currentLabel,
            )

        SettingsValueRow(
            title = title,
            summary = row.summaryRes?.let { context.getString(it) },
            value = currentLabel,
            enabled = row.enabled,
            contentDescription = contentDescription,
            showChevron = row.enabled,
            onClick =
            if (row.enabled) {
                {
                    MaterialAlertDialogBuilder(context)
                        .setTitle(row.titleRes)
                        .setSingleChoiceItems(labels, currentValueIndex) { dialog, which ->
                            settings.edit { putString(preferenceKey, values[which]) }
                            dialog.dismiss()
                        }.setNegativeButton(R.string.Cancel, null)
                        .show()
                }
            } else {
                null
            },
        )
    }

    @Composable
    private fun renderToggleRow(row: SettingsRow.Toggle) {
        val context = LocalContext.current
        val preferenceKey = context.getString(row.keyRes)
        val checked = settings.getBoolean(preferenceKey, row.defaultValue)
        val accessibilityStateRes =
            if (checked) R.string.accessibility_switch_on else R.string.accessibility_switch_off

        SettingsToggleRow(
            title = context.getString(row.titleRes),
            summary = row.summaryRes(checked)?.let { context.getString(it) },
            checked = checked,
            enabled = row.enabled,
            contentDescription =
            context.getString(
                R.string.settings_toggle_content_description,
                context.getString(row.titleRes),
                context.getString(accessibilityStateRes),
            ),
            onCheckedChange = { newValue ->
                settings.edit { putBoolean(preferenceKey, newValue) }
            },
        )
    }

    @Composable
    private fun renderInfoRow(row: SettingsRow.Info) {
        val context = LocalContext.current
        SettingsValueRow(
            title = context.getString(row.titleRes),
            summary = context.getString(row.summaryRes),
            value = null,
            enabled = false,
            contentDescription = null,
            showChevron = false,
            onClick = null,
        )
    }

    @DrawableRes
    private fun iconForSection(categoryId: String): Int = when (categoryId) {
        SettingsCategoryRegistry.CUSTOMIZE -> R.drawable.ic_format_color_fill_grey_600_24dp
        SettingsCategoryRegistry.APPEARANCE -> R.drawable.ic_format_size_grey_600_24dp
        SettingsCategoryRegistry.CONTENT -> R.drawable.ic_format_list_bulleted_grey_600_24dp
        SettingsCategoryRegistry.GENERAL -> R.drawable.ic_build_grey_600_24dp
        SettingsCategoryRegistry.NOTIFICATIONS -> R.drawable.ic_notifications_active_grey_600_24dp
        SettingsCategoryRegistry.DATA_SYNC -> R.drawable.ic_sync_grey_600_24dp
        SettingsCategoryRegistry.PRIVACY -> R.drawable.ic_privacy_grey_600_24dp
        SettingsCategoryRegistry.ACCESSIBILITY -> R.drawable.ic_touch_app_grey_600_24dp
        else -> R.drawable.ic_build_grey_600_24dp
    }

    companion object {

        /**
         * Resolves the settings category from the fragment arguments.
         *
         * The typed [SettingsCategoryScreenParam] wins when present; otherwise the
         * legacy [SettingsCategoryRegistry.ARG_CATEGORY_ID] extra is bridged.
         */
        fun fromBundle(bundle: Bundle?): SettingsCategoryScreenParam? = resolve(
            typed = bundle?.screenParam<SettingsCategoryScreenParam>(),
            legacyCategoryId = bundle?.getString(SettingsCategoryRegistry.ARG_CATEGORY_ID),
        )

        @VisibleForTesting
        internal fun resolve(typed: SettingsCategoryScreenParam?, legacyCategoryId: String?): SettingsCategoryScreenParam? {
            typed?.let { return it }
            return legacyCategoryId?.let { SettingsCategoryScreenParam(categoryId = it) }
        }
    }
}
