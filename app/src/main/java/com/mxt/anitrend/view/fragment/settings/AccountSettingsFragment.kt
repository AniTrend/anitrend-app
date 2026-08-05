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

    /** Label/value pairs offered by a constrained single-choice row. */
    private data class ChoiceOptions(
        val titles: Array<String>,
        val values: Array<String>,
    )

    private var binding: FragmentAccountSettingsBinding? = null

    private val accountSettingsViewModel: AccountSettingsViewModel by viewModel()

    /** True while the ViewModel is loading or saving, used to gate input. */
    private var isInputBlocked: Boolean = false

    /** Guards the about field watcher while the form is being reseeded. */
    private var isReseedingAbout: Boolean = false

    /** Tracks the previous save state to detect a successful save transition. */
    private var wasSavingOnLastRender: Boolean = false

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            confirmDiscardAndClose()
        }
    }

    // Constrained choice tables, cached once for label resolution and dialogs.
    private val profileColorChoices by lazy {
        ChoiceOptions(
            titles = resources.getStringArray(R.array.account_profile_color_titles),
            values = resources.getStringArray(R.array.account_profile_color_values),
        )
    }
    private val scoreFormatChoices by lazy {
        ChoiceOptions(
            titles = resources.getStringArray(R.array.account_score_format_titles),
            values = resources.getStringArray(R.array.account_score_format_values),
        )
    }
    private val titleLanguageChoices by lazy {
        ChoiceOptions(
            titles = resources.getStringArray(R.array.account_title_language_titles),
            values = resources.getStringArray(R.array.account_title_language_values),
        )
    }
    private val rowOrderChoices by lazy {
        ChoiceOptions(
            titles = resources.getStringArray(R.array.account_row_order_titles),
            values = resources.getStringArray(R.array.account_row_order_values),
        )
    }

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

        setupUiBindings()

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

    private fun setupUiBindings() {
        val binding = binding ?: return
        binding.accountAboutEdit.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                override fun afterTextChanged(s: Editable?) {
                    if (isReseedingAbout) return
                    accountSettingsViewModel.setAbout(s?.toString().orEmpty())
                }
            },
        )

        bindChoiceRowClick(binding.accountProfileColorRow, R.string.account_title_profile_color, profileColorChoices, { accountSettingsViewModel.state.value.profileColor }) { accountSettingsViewModel.setProfileColor(it) }
        bindChoiceRowClick(binding.accountScoreFormatRow, R.string.pref_title_score_format, scoreFormatChoices, { accountSettingsViewModel.state.value.scoreFormat }) { accountSettingsViewModel.setScoreFormat(it) }
        bindChoiceRowClick(binding.accountTitleLanguageRow, R.string.pref_title_title_language, titleLanguageChoices, { accountSettingsViewModel.state.value.titleLanguage }) { accountSettingsViewModel.setTitleLanguage(it) }
        bindChoiceRowClick(binding.accountRowOrderRow, R.string.account_title_row_order, rowOrderChoices, { accountSettingsViewModel.state.value.rowOrder }) { accountSettingsViewModel.setRowOrder(it) }

        binding.accountAiringRow.setOnClickListener {
            if (!isInputBlocked) binding.accountAiringSwitch.isChecked = !binding.accountAiringSwitch.isChecked
        }
        binding.accountAiringSwitch.setOnCheckedChangeListener { _, checked ->
            accountSettingsViewModel.setAiringNotifications(checked)
        }
        binding.accountAdultRow.setOnClickListener {
            if (!isInputBlocked) binding.accountAdultSwitch.isChecked = !binding.accountAdultSwitch.isChecked
        }
        binding.accountAdultSwitch.setOnCheckedChangeListener { _, checked ->
            accountSettingsViewModel.setDisplayAdultContent(checked)
        }

        binding.accountSaveButton.setOnClickListener {
            if (!isInputBlocked) accountSettingsViewModel.save()
        }
        binding.accountCancelButton.setOnClickListener {
            if (!isInputBlocked) confirmDiscardAndClose()
        }
        binding.accountErrorRetry.setOnClickListener {
            if (!isInputBlocked) accountSettingsViewModel.refresh()
        }
    }

    private fun bindChoiceRowClick(
        row: View,
        @androidx.annotation.StringRes titleRes: Int,
        options: ChoiceOptions,
        currentValue: () -> String?,
        onSelected: (String) -> Unit,
    ) {
        row.setOnClickListener {
            if (!isInputBlocked) {
                val checkedIndex = options.values.indexOf(currentValue()).coerceAtLeast(0)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(titleRes)
                    .setSingleChoiceItems(options.titles, checkedIndex) { dialog, which ->
                        onSelected(options.values[which])
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.Cancel, null)
                    .show()
            }
        }
    }

    // ── rendering ──

    private fun render(state: AccountSettingsUiState) {
        val binding = binding ?: return
        isInputBlocked = state.isLoading || state.isSaving

        // About: reseed only when it actually differs, preserving the caret.
        isReseedingAbout = true
        if (binding.accountAboutEdit.text?.toString() != state.about) {
            binding.accountAboutEdit.setText(state.about)
        }
        isReseedingAbout = false

        renderSettingRows(binding, state)

        // Loading / saving overlay.
        val busy = state.isLoading || state.isSaving
        binding.accountLoadingOverlay.visibility = if (busy) View.VISIBLE else View.GONE
        if (busy) {
            val captionRes = if (state.isSaving) R.string.account_saving else R.string.account_loading
            val descriptionRes =
                if (state.isSaving) R.string.account_saving_content_description else R.string.account_loading_content_description
            binding.accountLoadingCaption.setText(captionRes)
            binding.accountLoadingOverlay.contentDescription = getString(descriptionRes)
        }

        // Error banner.
        val message = state.errorMessage
        if (message != null) {
            binding.accountErrorText.text = message
            binding.accountErrorBanner.visibility = View.VISIBLE
        } else {
            binding.accountErrorBanner.visibility = View.GONE
        }

        renderActionBar(binding, state)
    }

    private fun renderSettingRows(
        binding: FragmentAccountSettingsBinding,
        state: AccountSettingsUiState,
    ) {
        renderChoiceRow(binding.accountProfileColorRow, binding.accountProfileColorValue, R.string.account_title_profile_color, profileColorChoices, state.profileColor)
        renderChoiceRow(binding.accountScoreFormatRow, binding.accountScoreFormatValue, R.string.pref_title_score_format, scoreFormatChoices, state.scoreFormat)
        renderChoiceRow(binding.accountTitleLanguageRow, binding.accountTitleLanguageValue, R.string.pref_title_title_language, titleLanguageChoices, state.titleLanguage)
        renderChoiceRow(binding.accountRowOrderRow, binding.accountRowOrderValue, R.string.account_title_row_order, rowOrderChoices, state.rowOrder, getString(R.string.account_row_order_default))

        // Switches. Detach/reattach to avoid the listener firing on reseed.
        binding.accountAiringSwitch.setOnCheckedChangeListener(null)
        binding.accountAiringSwitch.isChecked = state.airingNotifications
        binding.accountAiringSwitch.setOnCheckedChangeListener { _, isChecked ->
            accountSettingsViewModel.setAiringNotifications(isChecked)
        }
        binding.accountAiringRow.contentDescription = getString(
            R.string.settings_toggle_content_description,
            getString(R.string.account_title_airing_notifications),
            getString(if (state.airingNotifications) R.string.accessibility_switch_on else R.string.accessibility_switch_off),
        )
        binding.accountAdultSwitch.setOnCheckedChangeListener(null)
        binding.accountAdultSwitch.isChecked = state.displayAdultContent
        binding.accountAdultSwitch.setOnCheckedChangeListener { _, isChecked ->
            accountSettingsViewModel.setDisplayAdultContent(isChecked)
        }
        binding.accountAdultRow.contentDescription = getString(
            R.string.settings_toggle_content_description,
            getString(R.string.account_title_display_adult_content),
            getString(if (state.displayAdultContent) R.string.accessibility_switch_on else R.string.accessibility_switch_off),
        )
    }

    private fun renderChoiceRow(
        row: View,
        valueView: com.google.android.material.textview.MaterialTextView,
        @androidx.annotation.StringRes titleRes: Int,
        options: ChoiceOptions,
        value: String?,
        fallbackLabel: String? = null,
    ) {
        val index = options.values.indexOf(value)
        val label = when {
            index >= 0 -> options.titles[index]
            fallbackLabel != null -> fallbackLabel
            else -> options.titles[0.coerceIn(0, options.titles.lastIndex)]
        }
        valueView.text = label
        row.contentDescription = getString(
            R.string.settings_choice_content_description,
            getString(titleRes),
            label,
        )
    }

    private fun renderActionBar(
        binding: FragmentAccountSettingsBinding,
        state: AccountSettingsUiState,
    ) {
        // Action bar + field enabled state.
        val editable = !isInputBlocked
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
        if (wasSavingOnLastRender && !state.isSaving && state.errorMessage == null) {
            view?.let { root ->
                Snackbar.make(root, R.string.text_changes_saved, Snackbar.LENGTH_SHORT).show()
            }
        }
        wasSavingOnLastRender = state.isSaving
    }

    // ── dialogs and navigation ──

    private fun confirmDiscardAndClose() {
        if (!accountSettingsViewModel.state.value.hasDirtyFields) {
            findNavController().popBackStack()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.account_dialog_discard_title)
            .setMessage(R.string.account_dialog_discard_message)
            .setPositiveButton(R.string.account_action_discard) { _, _ ->
                accountSettingsViewModel.discard()
                findNavController().popBackStack()
            }
            .setNegativeButton(R.string.account_action_keep_editing, null)
            .show()
    }
}
