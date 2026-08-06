package com.mxt.anitrend.view.fragment.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.FragmentSettingsM3Binding
import com.mxt.anitrend.databinding.ItemSettingsRowSwitchBinding
import com.mxt.anitrend.databinding.ItemSettingsRowValueBinding
import com.mxt.anitrend.databinding.ItemSettingsSectionCardBinding
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.SettingsCategoryScreenParam
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.Settings
import org.koin.android.ext.android.inject

/**
 * Renders a single legacy settings section for the category resolved from
 * the navigation argument [SettingsCategoryRegistry.ARG_CATEGORY_ID].
 *
 * Keeps the legacy row rendering, preference writes, and side-effect
 * handling exactly as the previous single-screen settings did.
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

        val sectionBinding = ItemSettingsSectionCardBinding.inflate(layoutInflater, sectionHost, false)
        sectionBinding.sectionTitle.setText(section.titleRes)
        sectionBinding.sectionSummary.setText(section.summaryRes)
        sectionBinding.sectionIcon.setImageResource(iconForSection(section.id))

        val visibleRows = section.rows.filter { it.visible }
        visibleRows.forEachIndexed { index, row ->
            when (row) {
                is SettingsRow.Choice -> bindChoiceRow(sectionBinding.sectionContent, row)
                is SettingsRow.Info -> bindInfoRow(sectionBinding.sectionContent, row)
                is SettingsRow.Toggle -> bindToggleRow(sectionBinding.sectionContent, row)
            }
            if (index < visibleRows.lastIndex) {
                val divider = layoutInflater.inflate(R.layout.item_settings_divider, sectionBinding.sectionContent, false)
                sectionBinding.sectionContent.addView(divider)
            }
        }

        sectionHost.addView(sectionBinding.root)
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

    /** Alpha used to communicate disabled setting rows without hiding them. */
    companion object {
        private const val DISABLED_ALPHA = 0.38f

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
