package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.editor.MarkdownInputEditor
import com.mxt.anitrend.base.custom.view.widget.FuzzyDateWidget
import com.mxt.anitrend.base.custom.view.widget.ProgressWidget
import com.mxt.anitrend.base.custom.view.widget.ScoreWidget
import com.mxt.anitrend.domain.model.MediaListDraft
import com.mxt.anitrend.domain.model.createEditableMediaList
import com.mxt.anitrend.domain.model.toDraft
import com.mxt.anitrend.domain.model.toSaveMediaListEntryCommand
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.media.MediaUtil
import com.mxt.anitrend.viewmodel.MediaListMutationViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlinx.coroutines.launch

/**
 * M3 BottomSheetDialogFragment for managing media list entries.
 *
 * Exposes all available [SaveMediaListEntry] fields including:
 * - status, score, scoreRaw, progress, repeat, dates, private, notes
 * - priority (Slider 0-255), hiddenFromStatusLists (MaterialSwitch)
 * - customLists (ChipGroup), advancedScores (dynamic Sliders per category)
 * - Status-selection side effects: inline helper text and auto-fill on COMPLETED
 *
 * The scoreRaw pipeline is wired through MediaList -> MediaListUtil ->
 * MediaListMutationViewModel -> SaveMediaListEntryInteractor -> BrowseRepository. No UI input populates it;
 * the AniList API derives scoreRaw from score when not explicitly provided.
 */
class BottomSheetSeriesManage : BottomSheetDialogFragment() {

    private val mediaListStatuses =
        arrayOf(KeyUtil.CURRENT, KeyUtil.PLANNING, KeyUtil.COMPLETED, KeyUtil.DROPPED, KeyUtil.PAUSED, KeyUtil.REPEATING)

    private val mediaListMutationViewModel: MediaListMutationViewModel by activityViewModel()

    private lateinit var mediaBase: MediaBase
    private lateinit var mediaListModel: MediaList
    private lateinit var mediaListDraft: MediaListDraft
    private var isAnime: Boolean = true
    private var isSaving: Boolean = false
    private var progressDialog: AlertDialog? = null
    private var lastHandledOutcomeVersion: Int = 0

    // M3 views
    private lateinit var titleView: MaterialTextView
    private lateinit var statusDropdown: MaterialAutoCompleteTextView
    private lateinit var scoreWidget: ScoreWidget
    private lateinit var progressWidget: ProgressWidget
    private lateinit var progressLabel: MaterialTextView
    private lateinit var volumesContainer: ViewGroup
    private lateinit var volumesWidget: ProgressWidget
    private lateinit var repeatWidget: ProgressWidget
    private lateinit var repeatLabel: MaterialTextView
    private lateinit var startedAtWidget: FuzzyDateWidget
    private lateinit var completedAtWidget: FuzzyDateWidget
    private lateinit var privateSwitch: MaterialSwitch
    private lateinit var hiddenFromStatusSwitch: MaterialSwitch
    private lateinit var prioritySlider: Slider
    private lateinit var notesEditor: MarkdownInputEditor
    private lateinit var saveButton: MaterialButton
    private lateinit var deleteButton: MaterialButton

    // Custom lists and advanced scores (dynamic, shown only when available)
    private lateinit var customListsContainer: ViewGroup
    private lateinit var customListsChipGroup: ChipGroup
    private lateinit var advancedScoresContainer: ViewGroup
    private lateinit var advancedScoresEntries: ViewGroup
    private val advancedScoreSliders = mutableListOf<Pair<String, Slider>>()

    // Header / status / priority UX
    private lateinit var subtitleView: MaterialTextView
    private lateinit var statusLayout: TextInputLayout
    private lateinit var statusWarning: MaterialTextView
    private lateinit var priorityValue: MaterialTextView

    private val statusIconMap =
        mapOf(
            KeyUtil.CURRENT to R.drawable.ic_remove_red_eye_white_18dp,
            KeyUtil.PLANNING to R.drawable.ic_bookmark_white_24dp,
            KeyUtil.COMPLETED to R.drawable.ic_done_all_grey_600_24dp,
            KeyUtil.DROPPED to R.drawable.ic_delete_sweep_grey_600_24dp,
            KeyUtil.PAUSED to R.drawable.ic_pause_white_18dp,
            KeyUtil.REPEATING to R.drawable.ic_repeat_white_18dp,
        )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mediaBase = arguments?.getParcelable(ARG_MEDIA_BASE) ?: error("Missing MediaBase argument")
        isAnime = mediaBase.type == null || mediaBase.type == KeyUtil.ANIME

        initModel()

        val view = layoutInflater.inflate(R.layout.sheet_series_manage_m3, null)
        bindViews(view)

        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setContentView(view)

        dialog.behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isHideable = true
        }

        setupStatusDropdown()
        configureForMediaType()
        populateFromModel()
        setupButtons()
        observeMutationState()

        return dialog
    }

    private fun initModel() {
        mediaListModel = createEditableMediaList(mediaBase.mediaListEntry, mediaBase)
        mediaListDraft = mediaListModel.toDraft()
    }

    private fun bindViews(view: View) {
        titleView = view.findViewById(R.id.sheet_manage_title)
        statusDropdown = view.findViewById(R.id.sheet_manage_status)
        scoreWidget = view.findViewById(R.id.sheet_manage_score)
        progressWidget = view.findViewById(R.id.sheet_manage_progress)
        progressLabel = view.findViewById(R.id.sheet_manage_progress_label)
        volumesContainer = view.findViewById(R.id.sheet_manage_volumes_container)
        volumesWidget = view.findViewById(R.id.sheet_manage_volumes)
        repeatWidget = view.findViewById(R.id.sheet_manage_repeat)
        repeatLabel = view.findViewById(R.id.sheet_manage_repeat_label)
        startedAtWidget = view.findViewById(R.id.sheet_manage_started_at)
        completedAtWidget = view.findViewById(R.id.sheet_manage_completed_at)
        privateSwitch = view.findViewById(R.id.sheet_manage_private)
        hiddenFromStatusSwitch = view.findViewById(R.id.sheet_manage_hidden_from_status)
        prioritySlider = view.findViewById(R.id.sheet_manage_priority)
        notesEditor = view.findViewById(R.id.sheet_manage_notes)
        saveButton = view.findViewById(R.id.sheet_manage_save)
        view.findViewById<MaterialButton>(R.id.sheet_manage_cancel).setOnClickListener { dismiss() }
        deleteButton = view.findViewById(R.id.sheet_manage_delete)
        customListsContainer = view.findViewById(R.id.sheet_manage_custom_lists_container)
        customListsChipGroup = view.findViewById(R.id.sheet_manage_custom_lists_chips)
        advancedScoresContainer = view.findViewById(R.id.sheet_manage_advanced_scores_container)
        advancedScoresEntries = view.findViewById(R.id.sheet_manage_advanced_scores_entries)

        subtitleView = view.findViewById(R.id.sheet_manage_subtitle)
        statusLayout = view.findViewById(R.id.sheet_manage_status_layout)
        statusWarning = view.findViewById(R.id.sheet_manage_status_warning)
        priorityValue = view.findViewById(R.id.sheet_manage_priority_value)

        prioritySlider.addOnChangeListener { _, value, _ ->
            updatePriorityValue(value)
        }
    }

    private fun setupStatusDropdown() {
        val statusNames = CompatUtil.getStringList(requireContext(), R.array.media_list_status)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusNames)
        statusDropdown.setAdapter(adapter)

        val currentStatus = mediaListDraft.status ?: KeyUtil.PLANNING
        val statusIndex = mediaListStatuses.indexOf(currentStatus).coerceAtLeast(0)
        statusDropdown.setText(statusNames[statusIndex], false)
        statusIconMap[currentStatus]?.let { icon -> statusLayout.setStartIconDrawable(icon) }
        statusWarning.visibility = View.GONE

        statusDropdown.setOnItemClickListener { _, _, position, _ ->
            handleStatusSelected(position)
        }
    }

    private fun handleStatusSelected(statusIndex: Int) {
        val result = computeStatusSelectionEffects(
            statusIndex,
            mediaListStatuses,
            mediaBase,
            isAnime,
        ) ?: return

        statusIconMap[result.newStatus]?.let { icon -> statusLayout.setStartIconDrawable(icon) }
        statusWarning.visibility = View.GONE

        var updatedDraft = mediaListDraft.copy(status = result.newStatus)

        if (result.warningResId != null) {
            showStatusWarning(result.warningResId)
        }
        result.autoFillProgress?.let { total ->
            updatedDraft = updatedDraft.copy(progress = total)
            progressWidget.setProgressCurrent(total)
        }
        result.autoFillVolumes?.let { volumesTotal ->
            updatedDraft = updatedDraft.copy(progressVolumes = volumesTotal)
            volumesWidget.setProgressCurrent(volumesTotal)
        }
        mediaListDraft = updatedDraft
    }

    private fun showStatusWarning(messageRes: Int) {
        statusWarning.text = getString(messageRes)
        statusWarning.visibility = View.VISIBLE
    }

    private fun configureForMediaType() {
        if (isAnime) {
            progressLabel.text = getString(R.string.dialog_title_episodes)
            repeatLabel.text = getString(R.string.dialog_title_rewatched)
            volumesContainer.visibility = View.GONE
        } else {
            progressLabel.text = getString(R.string.dialog_title_chapters_read)
            repeatLabel.text = getString(R.string.dialog_title_reread)
            volumesContainer.visibility = View.VISIBLE
        }
    }

    private fun populateFromModel() {
        val model = mediaListDraft
        val media = mediaListModel.media

        titleView.text = MediaUtil.getMediaListTitle(mediaListModel)

        // Score
        val scoreFmt = resolveScoreFormat()
        scoreWidget.setScoreFormat(scoreFmt)
        scoreWidget.setScoreCurrent(model.score)

        // Progress (episodes/chapters)
        if (isAnime && media.episodes > 0) {
            progressWidget.setProgressMaximum(media.episodes)
        } else if (!isAnime && media.chapters > 0) {
            progressWidget.setProgressMaximum(media.chapters)
        }
        progressWidget.setProgressCurrent(model.progress)

        // Volumes (manga only)
        if (!isAnime) {
            if (media.volumes > 0) {
                volumesWidget.setProgressMaximum(media.volumes)
            }
            volumesWidget.setProgressCurrent(model.progressVolumes)
        }

        // Repeat
        repeatWidget.setProgressCurrent(model.repeat)

        // Dates
        startedAtWidget.setDate(model.startedAt)
        completedAtWidget.setDate(model.completedAt)

        // Privacy
        privateSwitch.isChecked = model.isHidden
        hiddenFromStatusSwitch.isChecked = model.isHiddenFromStatusLists

        // Priority (NEW) -- clamp to slider range to avoid crash on out-of-range API values
        prioritySlider.value = model.priority.toFloat().coerceIn(0f, 255f)
        updatePriorityValue(prioritySlider.value)

        // Notes -- decode HTML entities to avoid double-encoding.
        // The server may return HTML-encoded notes, and formattedText encodes again.
        notesEditor.setText(decodeNotesIfEncoded(model.notes))

        // Custom lists and advanced scores (NEW)
        populateCustomListsAndAdvancedScores()
    }

    private fun populateCustomListsAndAdvancedScores() {
        val committedModel = mediaListModel
        val draft = mediaListDraft
        val mediaListOptions = mediaListMutationViewModel.currentUserMediaListOptions ?: return

        val typeOptions = (if (isAnime) mediaListOptions.animeList else mediaListOptions.mangaList) ?: return

        val availableCustomLists = typeOptions.customLists.orEmpty()
        val advancedScoringCategories = typeOptions.advancedScoring.orEmpty()
        val isAdvancedScoringEnabled = typeOptions.isAdvancedScoringEnabled

        // --- Custom Lists ---
        if (availableCustomLists.isNotEmpty()) {
            customListsContainer.visibility = View.VISIBLE
            customListsChipGroup.removeAllViews()

            val existingCustomLists = committedModel.customLists.orEmpty()
            for (listName in availableCustomLists) {
                if (listName.isEmpty()) continue
                val isEnabled = existingCustomLists.any { it.name == listName && it.isEnabled }
                val chip = Chip(
                    ContextThemeWrapper(requireContext(), R.style.Widget_AniTrend_ManageSheet_CustomListChip),
                    null,
                    com.google.android.material.R.attr.chipStyle,
                ).apply {
                    text = listName
                    isChecked = isEnabled
                    isCheckable = true
                    isCheckedIconVisible = true
                }
                customListsChipGroup.addView(chip)
            }
        } else {
            customListsContainer.visibility = View.GONE
        }

        // --- Advanced Scores ---
        if (isAdvancedScoringEnabled && advancedScoringCategories.isNotEmpty()) {
            advancedScoresContainer.visibility = View.VISIBLE
            advancedScoresEntries.removeAllViews()
            advancedScoreSliders.clear()

            val existingScores = draft.advancedScores ?: emptyMap()
            val topMargin = CompatUtil.dipToPx(4f)
            for (category in advancedScoringCategories) {
                if (category.isEmpty()) continue
                val label = MaterialTextView(requireContext()).apply {
                    text = category
                    setTextAppearance(R.style.TextAppearance_AniTrend_LabelMedium)
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).apply { this.topMargin = topMargin }
                }
                advancedScoresEntries.addView(label)

                val scoreValue = existingScores[category] ?: 0f
                val slider = Slider(requireContext()).apply {
                    valueFrom = 0f
                    valueTo = 100f
                    stepSize = 1f
                    value = scoreValue.coerceIn(0f, 100f)
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                }
                advancedScoresEntries.addView(slider)
                advancedScoreSliders.add(category to slider)
            }
        } else {
            advancedScoresContainer.visibility = View.GONE
        }
    }

    private fun setupButtons() {
        val isNewEntry = mediaBase.mediaListEntry == null

        subtitleView.text = getString(if (isNewEntry) R.string.sheet_title_add else R.string.sheet_title_edit)
        saveButton.text = getString(if (isNewEntry) R.string.action_add_to_list else R.string.action_save)
        deleteButton.text = getString(R.string.action_remove)

        if (isNewEntry) {
            deleteButton.visibility = View.GONE
        } else {
            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                if (isSaving) return@setOnClickListener
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.action_remove)
                    .setMessage(R.string.dialog_remove_entry_message)
                    .setPositiveButton(R.string.action_remove) { _, _ -> handleDelete() }
                    .setNegativeButton(R.string.Cancel, null)
                    .show()
            }
        }

        saveButton.setOnClickListener { handleSave() }
    }

    private fun updatePriorityValue(value: Float) {
        priorityValue.text = getString(R.string.priority_value, value.toInt())
    }

    private fun handleSave() {
        if (isSaving) {
            return
        }

        val statusNames = CompatUtil.getStringList(requireContext(), R.array.media_list_status)
        val selectedStatusIndex = statusNames.indexOf(statusDropdown.text.toString()).coerceAtLeast(0)

        // Read widget values (view layer)
        val progress = progressWidget.progressCurrent
        val repeat = repeatWidget.progressCurrent
        val score = scoreWidget.scoreCurrent
        val progressVolumes = if (!isAnime) volumesWidget.progressCurrent else 0
        val startedAt = startedAtWidget.date
        val completedAt = completedAtWidget.date
        val isHidden = privateSwitch.isChecked
        val isHiddenFromStatusLists = hiddenFromStatusSwitch.isChecked
        val priority = prioritySlider.value.toInt()
        val notes = notesEditor.formattedText

        // Form validation (extracted)
        val validation = validateManageForm(progress, repeat)
        if (!validation.isValid) {
            context?.let { safeCtx ->
                NotifyUtil.makeText(
                    safeCtx,
                    validation.errorResId ?: R.string.text_error_request,
                    R.drawable.ic_warning_white_18dp,
                    Toast.LENGTH_SHORT,
                ).show()
            }
            return
        }

        // Collect advanced scores from sliders
        val collectedAdvancedScores: Map<String, Float>? =
            if (advancedScoresContainer.isVisible && advancedScoreSliders.isNotEmpty()) {
                advancedScoreSliders.associate { (category, slider) ->
                    category to slider.value
                }
            } else {
                null
            }

        mediaListDraft = buildMediaListFromForm(
            draft = mediaListDraft,
            statusIndex = selectedStatusIndex,
            statuses = mediaListStatuses,
            progress = progress,
            repeat = repeat,
            score = score,
            progressVolumes = progressVolumes,
            isAnime = isAnime,
            startedAt = startedAt,
            completedAt = completedAt,
            isHidden = isHidden,
            isHiddenFromStatusLists = isHiddenFromStatusLists,
            priority = priority,
            notes = notes,
            advancedScores = collectedAdvancedScores,
        )

        // Build enabled custom list names directly from chips
        val enabledCustomListNames: List<String?>? =
            if (customListsContainer.isVisible && customListsChipGroup.isNotEmpty()) {
                (0 until customListsChipGroup.childCount)
                    .mapNotNull { customListsChipGroup.getChildAt(it) as? Chip }
                    .filter { it.isChecked }
                    .mapNotNull { it.text?.toString()?.takeIf(String::isNotEmpty) }
                    .takeIf { it.isNotEmpty() }
            } else {
                mediaListModel.customLists
                    ?.filter { it.isEnabled }
                    ?.mapNotNull { it.name?.takeIf(String::isNotEmpty) }
                    ?.takeIf { it.isNotEmpty() }
            }

        val command = mediaListDraft.toSaveMediaListEntryCommand(mediaListModel, enabledCustomListNames)
        mediaListMutationViewModel.save(command)
    }

    private fun handleDelete() {
        if (isSaving) {
            return
        }

        mediaListMutationViewModel.delete(
            entryId = mediaListModel.id,
            mediaId = mediaBase.id,
        )
    }

    private fun setSavingState(isSaving: Boolean) {
        this.isSaving = isSaving
        saveButton.isEnabled = !isSaving
        deleteButton.isEnabled = !isSaving
    }

    /**
     * Resolves the user's score format from the current user's media list options.
     * Falls back to [KeyUtil.POINT_100] if the user data cannot be resolved.
     */
    private fun resolveScoreFormat(): String = mediaListMutationViewModel.currentUserMediaListOptions?.scoreFormat ?: KeyUtil.POINT_100

    private fun observeMutationState() {
        mediaListMutationViewModel.reset(mediaBase.id)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaListMutationViewModel.observeTarget(
                    mediaId = mediaBase.id,
                    entryId = mediaListModel.id.takeIf { it > 0 },
                ).collect { state ->
                    val isMutating = state.isSaveRunning || state.isDeleteRunning
                    setSavingState(isMutating)
                    updateProgressDialog(isMutating)

                    if (state.outcomeVersion == 0 || state.outcomeVersion == lastHandledOutcomeVersion) {
                        return@collect
                    }
                    lastHandledOutcomeVersion = state.outcomeVersion

                    when {
                        state.completedAction != null -> {
                            context?.let { safeCtx ->
                                NotifyUtil
                                    .makeText(
                                        safeCtx,
                                        getString(R.string.text_changes_saved),
                                        R.drawable.ic_check_circle_white_24dp,
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                            dismiss()
                        }
                        !state.errorMessage.isNullOrBlank() -> {
                            context?.let { safeCtx ->
                                NotifyUtil
                                    .makeText(
                                        safeCtx,
                                        state.errorMessage,
                                        R.drawable.ic_warning_white_18dp,
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateProgressDialog(isMutating: Boolean) {
        val ctx = context ?: return
        if (isMutating) {
            if (progressDialog?.isShowing != true) {
                progressDialog = NotifyUtil.createProgressDialog(ctx, R.string.text_processing_request).also { dialog ->
                    dialog.show()
                }
            }
        } else {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    override fun onDestroy() {
        progressDialog?.dismiss()
        progressDialog = null
        super.onDestroy()
    }

    companion object {
        /**
         * Entity navigation boundary (documented Phase 2 decision): this sheet
         * edits the media-list entry state derived from the full [MediaBase]
         * (mediaListEntry, titles, episode/volume counts), so an identity-only
         * param cannot reconstruct it without a repository fetch. The only caller
         * (MediaDialogUtil) is outside the navigation-migration scope, so the
         * entity channel is kept as a compatibility bridge with its stable wire
         * key unchanged. Revisit when callers can supply identity-only values and
         * the sheet resolves entry state from a store.
         */
        @VisibleForTesting
        internal const val ARG_MEDIA_BASE = "arg_media_base"

        fun newInstance(mediaBase: MediaBase): BottomSheetSeriesManage = BottomSheetSeriesManage().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_MEDIA_BASE, mediaBase)
            }
        }
    }
}
