package com.mxt.anitrend.view.fragment.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.FragmentSettingsM3Binding
import com.mxt.anitrend.databinding.ItemSettingsRowValueBinding
import com.mxt.anitrend.viewmodel.CustomizeSettingsUiState
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.viewmodel.CustomizeSettingsViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The Customize settings category, backed by [CustomizeSettingsViewModel].
 *
 * The fragment renders immutable state, forwards user choices to the
 * ViewModel, and keeps the legacy preference side-effect handler.
 */
class CustomizeSettingsFragment :
    Fragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var binding: FragmentSettingsM3Binding? = null

    private val settings by inject<Settings>()
    private val scheduler by inject<JobSchedulerUtil>()
    private val presenter by inject<BasePresenter>()
    private val customizeSettingsViewModel: CustomizeSettingsViewModel by viewModel()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                customizeSettingsViewModel.state.collect(::renderRows)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        settings.registerOnSharedPreferenceChangeListener(this)
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
    }

    private fun renderRows(state: CustomizeSettingsUiState) {
        val binding = binding ?: return
        val context = requireContext()
        val sectionHost = binding.settingsSections
        sectionHost.removeAllViews()

        val rows = listOf(
            SettingsRow.Choice(
                keyRes = R.string.pref_key_app_theme,
                titleRes = R.string.pref_title_app_theme,
                entriesRes = R.array.pref_selected_theme_titles,
                valuesRes = R.array.pref_selected_theme_values,
                defaultValue = state.theme,
            ) to state.theme,
            SettingsRow.Choice(
                keyRes = R.string.pref_key_selected_language,
                titleRes = R.string.pref_title_language,
                summaryRes = R.string.pref_title_language_summary,
                entriesRes = R.array.pref_selected_language_titles,
                valuesRes = R.array.pref_selected_language_values,
                defaultValue = state.language,
            ) to state.language,
            SettingsRow.Choice(
                keyRes = R.string.pref_key_list_view_style,
                titleRes = R.string.pref_title_list_view_style,
                entriesRes = R.array.pref_selected_list_view_style_titles,
                valuesRes = R.array.pref_selected_list_view_style_values,
                defaultValue = state.listViewStyle,
            ) to state.listViewStyle,
        )

        rows.forEachIndexed { index, (row, currentValue) ->
            bindChoiceRow(sectionHost, row, currentValue) { selectedValue ->
                when (row.keyRes) {
                    R.string.pref_key_app_theme -> customizeSettingsViewModel.setTheme(selectedValue)
                    R.string.pref_key_selected_language -> customizeSettingsViewModel.setLanguage(selectedValue)
                    R.string.pref_key_list_view_style -> customizeSettingsViewModel.setListViewStyle(selectedValue)
                }
            }
            if (index < rows.lastIndex) {
                addDivider(sectionHost)
            }
        }
    }

    private fun bindChoiceRow(
        container: LinearLayout,
        row: SettingsRow.Choice,
        currentValue: String,
        onSelected: (String) -> Unit,
    ) {
        val rowBinding = ItemSettingsRowValueBinding.inflate(layoutInflater, container, false)
        val context = rowBinding.root.context
        val labels = context.resources.getStringArray(row.entriesRes)
        val values = context.resources.getStringArray(row.valuesRes)
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
        rowBinding.rowChevron.visibility = View.VISIBLE
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
                        onSelected(values[which])
                        dialog.dismiss()
                    }.setNegativeButton(R.string.Cancel, null)
                    .show()
            }
        } else {
            rowBinding.root.setOnClickListener(null)
        }

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
