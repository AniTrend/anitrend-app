package com.mxt.anitrend.view.fragment.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.FragmentAccountSettingsBinding
import com.mxt.anitrend.viewmodel.AccountSettingsUiState
import com.mxt.anitrend.viewmodel.AccountSettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Account settings screen for the current signed-in user.
 *
 * Renders the server-authoritative AniList account settings form (profile and
 * list/content options) from [AccountSettingsViewModel.state] and forwards
 * user actions back to the ViewModel. Constrained fields (profile color, score
 * format, title language, row order) only ever offer the ViewModel's accepted
 * tokens through single-choice dialogs, never free text.
 *
 * UI event concerns live here: the discard confirmation dialog, navigation,
 * and the success snackbar. The ViewModel exposes state and actions only.
 */
class AccountSettingsFragment : Fragment() {

    private var binding: FragmentAccountSettingsBinding? = null

    private val accountSettingsViewModel: AccountSettingsViewModel by viewModel()

    /** True while the ViewModel is loading or saving, used to gate input. */
    private var isBusy: Boolean = false

    /** Guards the about field watcher while the form is being reseeded. */
    private var isRendering: Boolean = false

    /** Tracks the previous save state to detect a successful save transition. */
    private var previousIsSaving: Boolean = false

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            confirmDiscardAndClose()
        }
    }

    // Constrained choice tables, cached once for label resolution and dialogs.
    private val profileColorTitles by lazy { resources.getStringArray(R.array.account_profile_color_titles) }
    private val profileColorValues by lazy { resources.getStringArray(R.array.account_profile_color_values) }
    private val scoreFormatTitles by lazy { resources.getStringArray(R.array.account_score_format_titles) }
    private val scoreFormatValues by lazy { resources.getStringArray(R.array.account_score_format_values) }
    private val titleLanguageTitles by lazy { resources.getStringArray(R.array.account_title_language_titles) }
    private val titleLanguageValues by lazy { resources.getStringArray(R.array.account_title_language_values) }
    private val rowOrderTitles by lazy { resources.getStringArray(R.array.account_row_order_titles) }
    private val rowOrderValues by lazy { resources.getStringArray(R.array.account_row_order_values) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return

        // Keep the sticky action bar clear of the navigation bar on edge-to-edge.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.accountActionBar.updatePadding(bottom = navigationBars.bottom)
            insets
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        setupAboutField()
        setupChoiceRows()
        setupSwitchRows()
        setupActionBar()

        // The ViewModel seeds from cache. When the cache looks empty (for example
        // right after login), pull the fresh server state so the form is usable.
        val seeded = accountSettingsViewModel.state.value
        if (seeded.profileColor == null && seeded.about.isEmpty()) {
            accountSettingsViewModel.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountSettingsViewModel.state.collect(::render)
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    // ── field wiring ──

    private fun setupAboutField() {
        val binding = binding ?: return
        binding.accountAboutEdit.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isRendering) return
                    accountSettingsViewModel.setAbout(s?.toString().orEmpty())
                }
            },
        )
    }

    private fun setupChoiceRows() {
        val binding = binding ?: return
        binding.accountProfileColorRow.setOnClickListener {
            if (isBusy) return@setOnClickListener
            showChoiceDialog(
                titleRes = R.string.account_title_profile_color,
                titles = profileColorTitles,
                values = profileColorValues,
                currentValue = accountSettingsViewModel.state.value.profileColor,
            ) { accountSettingsViewModel.setProfileColor(it) }
        }
        binding.accountScoreFormatRow.setOnClickListener {
            if (isBusy) return@setOnClickListener
            showChoiceDialog(
                titleRes = R.string.pref_title_score_format,
                titles = scoreFormatTitles,
                values = scoreFormatValues,
                currentValue = accountSettingsViewModel.state.value.scoreFormat,
            ) { accountSettingsViewModel.setScoreFormat(it) }
        }
        binding.accountTitleLanguageRow.setOnClickListener {
            if (isBusy) return@setOnClickListener
            showChoiceDialog(
                titleRes = R.string.pref_title_title_language,
                titles = titleLanguageTitles,
                values = titleLanguageValues,
                currentValue = accountSettingsViewModel.state.value.titleLanguage,
            ) { accountSettingsViewModel.setTitleLanguage(it) }
        }
        binding.accountRowOrderRow.setOnClickListener {
            if (isBusy) return@setOnClickListener
            showChoiceDialog(
                titleRes = R.string.account_title_row_order,
                titles = rowOrderTitles,
                values = rowOrderValues,
                currentValue = accountSettingsViewModel.state.value.rowOrder,
            ) { accountSettingsViewModel.setRowOrder(it) }
        }
    }

    private fun setupSwitchRows() {
        val binding = binding ?: return
        binding.accountAiringRow.setOnClickListener {
            if (isBusy) return@setOnClickListener
            binding.accountAiringSwitch.isChecked = !binding.accountAiringSwitch.isChecked
        }
        binding.accountAiringSwitch.setOnCheckedChangeListener { _, checked ->
            accountSettingsViewModel.setAiringNotifications(checked)
        }
        binding.accountAdultRow.setOnClickListener {
            if (isBusy) return@setOnClickListener
            binding.accountAdultSwitch.isChecked = !binding.accountAdultSwitch.isChecked
        }
        binding.accountAdultSwitch.setOnCheckedChangeListener { _, checked ->
            accountSettingsViewModel.setDisplayAdultContent(checked)
        }
    }

    private fun setupActionBar() {
        val binding = binding ?: return
        binding.accountSaveButton.setOnClickListener {
            if (isBusy) return@setOnClickListener
            accountSettingsViewModel.save()
        }
        binding.accountCancelButton.setOnClickListener {
            if (isBusy) return@setOnClickListener
            confirmDiscardAndClose()
        }
        binding.accountErrorRetry.setOnClickListener {
            if (isBusy) return@setOnClickListener
            accountSettingsViewModel.refresh()
        }
    }

    // ── rendering ──

    private fun render(state: AccountSettingsUiState) {
        val binding = binding ?: return
        isBusy = state.isLoading || state.isSaving

        // About: reseed only when it actually differs, preserving the caret.
        isRendering = true
        if (binding.accountAboutEdit.text?.toString() != state.about) {
            binding.accountAboutEdit.setText(state.about)
        }
        isRendering = false

        // Constrained choice values.
        renderChoiceRow(
            row = binding.accountProfileColorRow,
            valueView = binding.accountProfileColorValue,
            titleRes = R.string.account_title_profile_color,
            titles = profileColorTitles,
            values = profileColorValues,
            value = state.profileColor,
            fallbackIndex = 0,
        )
        renderChoiceRow(
            row = binding.accountScoreFormatRow,
            valueView = binding.accountScoreFormatValue,
            titleRes = R.string.pref_title_score_format,
            titles = scoreFormatTitles,
            values = scoreFormatValues,
            value = state.scoreFormat,
            fallbackIndex = 0,
        )
        renderChoiceRow(
            row = binding.accountTitleLanguageRow,
            valueView = binding.accountTitleLanguageValue,
            titleRes = R.string.pref_title_title_language,
            titles = titleLanguageTitles,
            values = titleLanguageValues,
            value = state.titleLanguage,
            fallbackIndex = 0,
        )
        renderChoiceRow(
            row = binding.accountRowOrderRow,
            valueView = binding.accountRowOrderValue,
            titleRes = R.string.account_title_row_order,
            titles = rowOrderTitles,
            values = rowOrderValues,
            value = state.rowOrder,
            fallbackLabel = getString(R.string.account_row_order_default),
        )

        // Switches. Detach/reattach to avoid the listener firing on reseed.
        renderSwitchRow(
            row = binding.accountAiringRow,
            switch = binding.accountAiringSwitch,
            titleRes = R.string.account_title_airing_notifications,
            checked = state.airingNotifications,
        ) { accountSettingsViewModel.setAiringNotifications(it) }
        renderSwitchRow(
            row = binding.accountAdultRow,
            switch = binding.accountAdultSwitch,
            titleRes = R.string.account_title_display_adult_content,
            checked = state.displayAdultContent,
        ) { accountSettingsViewModel.setDisplayAdultContent(it) }

        // Loading / saving overlay.
        renderBusyOverlay(state)

        // Error banner.
        val message = state.errorMessage
        if (message != null) {
            binding.accountErrorText.text = message
            binding.accountErrorBanner.visibility = View.VISIBLE
        } else {
            binding.accountErrorBanner.visibility = View.GONE
        }

        // Action bar + field enabled state.
        val editable = !isBusy
        binding.accountAboutEdit.isEnabled = editable
        binding.accountProfileColorRow.isEnabled = editable
        binding.accountScoreFormatRow.isEnabled = editable
        binding.accountTitleLanguageRow.isEnabled = editable
        binding.accountRowOrderRow.isEnabled = editable
        binding.accountAiringRow.isEnabled = editable
        binding.accountAdultRow.isEnabled = editable
        binding.accountSaveButton.isEnabled = editable && state.hasDirtyFields
        binding.accountCancelButton.isEnabled = !state.isSaving
        binding.accountSaveButton.text = getString(
            if (state.isSaving) R.string.account_saving else R.string.action_save,
        )

        // Back confirms discard only when there are unsaved edits.
        backCallback.isEnabled = state.hasDirtyFields

        // Success snackbar: fire once when a save completes cleanly.
        if (previousIsSaving && !state.isSaving && state.errorMessage == null) {
            view?.let { root ->
                Snackbar.make(root, R.string.text_changes_saved, Snackbar.LENGTH_SHORT).show()
            }
        }
        previousIsSaving = state.isSaving
    }

    private fun renderChoiceRow(
        row: View,
        valueView: com.google.android.material.textview.MaterialTextView,
        @androidx.annotation.StringRes titleRes: Int,
        titles: Array<String>,
        values: Array<String>,
        value: String?,
        fallbackIndex: Int = 0,
        fallbackLabel: String? = null,
    ) {
        val index = values.indexOf(value)
        val label = when {
            index >= 0 -> titles[index]
            fallbackLabel != null -> fallbackLabel
            else -> titles[fallbackIndex.coerceIn(0, titles.lastIndex)]
        }
        valueView.text = label
        row.contentDescription = getString(
            R.string.settings_choice_content_description,
            getString(titleRes),
            label,
        )
    }

    private fun renderSwitchRow(
        row: View,
        switch: com.google.android.material.materialswitch.MaterialSwitch,
        @androidx.annotation.StringRes titleRes: Int,
        checked: Boolean,
        onChecked: (Boolean) -> Unit,
    ) {
        switch.setOnCheckedChangeListener(null)
        switch.isChecked = checked
        switch.setOnCheckedChangeListener { _, isChecked -> onChecked(isChecked) }
        row.contentDescription = getString(
            R.string.settings_toggle_content_description,
            getString(titleRes),
            getString(if (checked) R.string.accessibility_switch_on else R.string.accessibility_switch_off),
        )
    }

    private fun renderBusyOverlay(state: AccountSettingsUiState) {
        val binding = binding ?: return
        val busy = state.isLoading || state.isSaving
        binding.accountLoadingOverlay.visibility = if (busy) View.VISIBLE else View.GONE
        if (busy) {
            val captionRes = if (state.isSaving) R.string.account_saving else R.string.account_loading
            val descriptionRes =
                if (state.isSaving) R.string.account_saving_content_description else R.string.account_loading_content_description
            binding.accountLoadingCaption.setText(captionRes)
            binding.accountLoadingOverlay.contentDescription = getString(descriptionRes)
        }
    }

    // ── dialogs and navigation ──

    private fun showChoiceDialog(
        @androidx.annotation.StringRes titleRes: Int,
        titles: Array<String>,
        values: Array<String>,
        currentValue: String?,
        onSelected: (String) -> Unit,
    ) {
        val context = requireContext()
        val checkedIndex = values.indexOf(currentValue).coerceAtLeast(0)
        MaterialAlertDialogBuilder(context)
            .setTitle(titleRes)
            .setSingleChoiceItems(titles, checkedIndex) { dialog, which ->
                onSelected(values[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.Cancel, null)
            .show()
    }

    private fun confirmDiscardAndClose() {
        if (!accountSettingsViewModel.state.value.hasDirtyFields) {
            navigateBack()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.account_dialog_discard_title)
            .setMessage(R.string.account_dialog_discard_message)
            .setPositiveButton(R.string.account_action_discard) { _, _ ->
                accountSettingsViewModel.discard()
                navigateBack()
            }
            .setNegativeButton(R.string.account_action_keep_editing, null)
            .show()
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }
}
