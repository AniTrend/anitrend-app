package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textview.MaterialTextView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.editor.MarkdownInputEditor
import com.mxt.anitrend.base.custom.view.widget.FuzzyDateWidget
import com.mxt.anitrend.base.custom.view.widget.ProgressWidget
import com.mxt.anitrend.base.custom.view.widget.ScoreWidget
import com.mxt.anitrend.coordinator.WidgetMutationCoordinator
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.CustomList
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.media.MediaListUtil
import com.mxt.anitrend.util.media.MediaUtil
import timber.log.Timber

/**
 * M3 BottomSheetDialogFragment for managing media list entries.
 * Gated behind [com.mxt.anitrend.util.Settings.experimentalManageLibrary].
 *
 * Exposes all available [SaveMediaListEntry] fields including:
 * - status, score, progress, repeat, dates, private, notes
 * - priority (Slider 0-255), hiddenFromStatusLists (MaterialSwitch)
 * - customLists (ChipGroup), advancedScores (dynamic Sliders per category)
 * - Status-selection side effects: warning toasts and auto-fill on COMPLETED
 *
 * Not wired: scoreRaw (the generated mutation variable exists but the app's
 * data pipeline does not expose it through MediaListUtil or WidgetMutationCoordinator).
 */
class BottomSheetSeriesManage : BottomSheetDialogFragment() {

    private val mediaListStatuses =
        arrayOf(KeyUtil.CURRENT, KeyUtil.PLANNING, KeyUtil.COMPLETED, KeyUtil.DROPPED, KeyUtil.PAUSED, KeyUtil.REPEATING)

    private lateinit var mediaBase: MediaBase
    private lateinit var mediaListModel: MediaList
    private var isAnime: Boolean = true

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
    private lateinit var deleteSpacer: View

    // Custom lists and advanced scores (dynamic, shown only when available)
    private lateinit var customListsContainer: ViewGroup
    private lateinit var customListsChipGroup: ChipGroup
    private lateinit var advancedScoresContainer: ViewGroup
    private lateinit var advancedScoresEntries: ViewGroup
    private val advancedScoreSliders = mutableListOf<Pair<String, Slider>>()

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

        return dialog
    }

    private fun initModel() {
        mediaListModel =
            mediaBase.mediaListEntry?.apply {
                if (media.id == 0L) media = mediaBase
            } ?: MediaList().apply {
                mediaId = mediaBase.id
                media = mediaBase
            }
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
        deleteSpacer = view.findViewById(R.id.sheet_manage_delete_spacer)
        customListsContainer = view.findViewById(R.id.sheet_manage_custom_lists_container)
        customListsChipGroup = view.findViewById(R.id.sheet_manage_custom_lists_chips)
        advancedScoresContainer = view.findViewById(R.id.sheet_manage_advanced_scores_container)
        advancedScoresEntries = view.findViewById(R.id.sheet_manage_advanced_scores_entries)
    }

    private fun setupStatusDropdown() {
        val statusNames = CompatUtil.getStringList(requireContext(), R.array.media_list_status)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusNames)
        statusDropdown.setAdapter(adapter)

        val currentStatus = mediaListModel.status ?: KeyUtil.PLANNING
        val statusIndex = mediaListStatuses.indexOf(currentStatus).coerceAtLeast(0)
        statusDropdown.setText(statusNames[statusIndex], false)

        statusDropdown.setOnItemClickListener { _, _, position, _ ->
            handleStatusSelected(position)
        }
    }

    private fun handleStatusSelected(statusIndex: Int) {
        if (statusIndex < 0 || statusIndex >= mediaListStatuses.size) return
        val newStatus = mediaListStatuses[statusIndex]
        val mediaStatus = mediaBase.status

        if (isAnime) {
            when (newStatus) {
                KeyUtil.CURRENT -> {
                    if (CompatUtil.equals(mediaStatus, KeyUtil.NOT_YET_RELEASED)) {
                        NotifyUtil.makeText(
                            requireContext(), R.string.warning_anime_not_airing, Toast.LENGTH_LONG,
                        ).show()
                    }
                }
                KeyUtil.COMPLETED -> {
                    if (!CompatUtil.equals(mediaStatus, KeyUtil.FINISHED)) {
                        NotifyUtil.makeText(
                            requireContext(), R.string.warning_anime_is_airing, Toast.LENGTH_LONG,
                        ).show()
                    } else {
                        val total = mediaBase.episodes
                        mediaListModel.progress = total
                        progressWidget.setProgressCurrent(total)
                    }
                }
                KeyUtil.PLANNING -> { /* no side effect */ }
                else -> {
                    if (CompatUtil.equals(mediaStatus, KeyUtil.NOT_YET_RELEASED)) {
                        NotifyUtil.makeText(
                            requireContext(), R.string.warning_anime_not_airing, Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        } else {
            when (newStatus) {
                KeyUtil.CURRENT -> {
                    if (CompatUtil.equals(mediaStatus, KeyUtil.NOT_YET_RELEASED)) {
                        NotifyUtil.makeText(
                            requireContext(), R.string.warning_manga_not_publishing, Toast.LENGTH_LONG,
                        ).show()
                    }
                }
                KeyUtil.COMPLETED -> {
                    if (!CompatUtil.equals(mediaStatus, KeyUtil.FINISHED)) {
                        NotifyUtil.makeText(
                            requireContext(), R.string.warning_manga_publishing, Toast.LENGTH_LONG,
                        ).show()
                    } else {
                        val chaptersTotal = mediaBase.chapters
                        mediaListModel.progress = chaptersTotal
                        progressWidget.setProgressCurrent(chaptersTotal)
                        val volumesTotal = mediaBase.volumes
                        if (volumesTotal > 0) {
                            mediaListModel.progressVolumes = volumesTotal
                            volumesWidget.setProgressCurrent(volumesTotal)
                        }
                    }
                }
                KeyUtil.PLANNING -> { /* no side effect */ }
                else -> {
                    if (CompatUtil.equals(mediaStatus, KeyUtil.NOT_YET_RELEASED)) {
                        NotifyUtil.makeText(
                            requireContext(), R.string.warning_manga_not_publishing, Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        }
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
        val model = mediaListModel
        val media = model.media

        titleView.text = MediaUtil.getMediaListTitle(model)

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

        // Notes
        notesEditor.setText(model.notes)

        // Custom lists and advanced scores (NEW)
        populateCustomListsAndAdvancedScores()
    }

    private fun populateCustomListsAndAdvancedScores() {
        val model = mediaListModel
        val mediaListOptions = runCatching {
            koinOf<WidgetMutationCoordinator>().databaseHelper.currentUser?.mediaListOptions
        }.getOrNull() ?: return

        val typeOptions = (if (isAnime) mediaListOptions.animeList else mediaListOptions.mangaList) ?: return

        val availableCustomLists = typeOptions.customLists.orEmpty()
        val advancedScoringCategories = typeOptions.advancedScoring.orEmpty()
        val isAdvancedScoringEnabled = typeOptions.isAdvancedScoringEnabled

        // --- Custom Lists ---
        if (availableCustomLists.isNotEmpty()) {
            customListsContainer.visibility = View.VISIBLE
            customListsChipGroup.removeAllViews()

            val existingCustomLists = model.customLists.orEmpty()
            for (listName in availableCustomLists) {
                if (listName.isEmpty()) continue
                val isEnabled = existingCustomLists.any { it.name == listName && it.isEnabled }
                val chip = Chip(requireContext()).apply {
                    text = listName
                    isChecked = isEnabled
                    isCheckable = true
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

            val existingScores = model.advancedScores ?: emptyMap()
            val topMargin = CompatUtil.dipToPx(4f)
            for (category in advancedScoringCategories) {
                if (category.isEmpty()) continue
                val label = MaterialTextView(requireContext()).apply {
                    text = category
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelMedium)
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

        saveButton.text = getString(if (isNewEntry) R.string.Add else R.string.Update)

        if (isNewEntry) {
            deleteButton.visibility = View.GONE
            deleteSpacer.visibility = View.GONE
        } else {
            deleteButton.visibility = View.VISIBLE
            deleteSpacer.visibility = View.VISIBLE
            deleteButton.setOnClickListener { handleDelete() }
        }

        saveButton.setOnClickListener { handleSave() }
    }

    private fun handleSave() {
        val model = mediaListModel
        val statusNames = CompatUtil.getStringList(requireContext(), R.array.media_list_status)
        val selectedStatusIndex = statusNames.indexOf(statusDropdown.text.toString()).coerceAtLeast(0)

        // Update model from M3 views
        model.progress = progressWidget.progressCurrent
        model.repeat = repeatWidget.progressCurrent
        model.score = scoreWidget.scoreCurrent
        if (!isAnime) {
            model.progressVolumes = volumesWidget.progressCurrent
        }
        model.startedAt = startedAtWidget.date
        model.completedAt = completedAtWidget.date
        model.isHidden = privateSwitch.isChecked
        model.isHiddenFromStatusLists = hiddenFromStatusSwitch.isChecked
        model.priority = prioritySlider.value.toInt()
        model.notes = notesEditor.formattedText
        model.status = mediaListStatuses[selectedStatusIndex]

        // Collect advanced scores from sliders
        if (advancedScoresContainer.visibility == View.VISIBLE && advancedScoreSliders.isNotEmpty()) {
            model.advancedScores = advancedScoreSliders.associate { (category, slider) ->
                category to slider.value
            }
        }

        val scoreFmt = resolveScoreFormat()
        val params = MediaListUtil.getMediaListParams(model, scoreFmt)

        // Build enabled custom list names directly from chips to distinguish
        // "all unchecked" (empty list = clear all) from "no custom lists UI
        // shown" (null = don't update). This bypasses the stringListValue
        // empty-to-null conversion that would silently preserve existing lists.
        val enabledCustomListNames: List<String?>? =
            if (customListsContainer.visibility == View.VISIBLE && customListsChipGroup.childCount > 0) {
                (0 until customListsChipGroup.childCount)
                    .mapNotNull { customListsChipGroup.getChildAt(it) as? Chip }
                    .filter { it.isChecked }
                    .mapNotNull { it.text?.toString()?.takeIf(String::isNotEmpty) }
                    .takeIf { it.isNotEmpty() }
            } else {
                params.stringListValue(KeyUtil.arg_listCustom)
            }

        val progressDialog = NotifyUtil.createProgressDialog(requireContext(), R.string.text_processing_request)
        progressDialog.show()

        koinOf<WidgetMutationCoordinator>().saveMediaListEntry(
            id = params.intValue(KeyUtil.arg_id),
            mediaId = params.longValue(KeyUtil.arg_mediaId),
            status = params.enumValue<MediaListStatus>(KeyUtil.arg_listStatus),
            score = params.doubleValue(KeyUtil.arg_listScore),
            progress = params.intValue(KeyUtil.arg_listProgress),
            progressVolumes = params.intValue(KeyUtil.arg_listProgressVolumes),
            repeat = params.intValue(KeyUtil.arg_listRepeat),
            priority = params.intValue(KeyUtil.arg_listPriority),
            private = params.boolValue(KeyUtil.arg_listPrivate) ?: false,
            hiddenFromStatusLists = params.boolValue(KeyUtil.arg_listHiddenFromStatusLists) ?: false,
            customLists = enabledCustomListNames,
            advancedScores = params.doubleListValue(KeyUtil.arg_listAdvancedScore),
            notes = params.stringValue(KeyUtil.arg_listNotes),
            startedAt = params.fuzzyDateInputValue(KeyUtil.arg_startedAt),
            completedAt = params.fuzzyDateInputValue(KeyUtil.arg_completedAt),
        ) { result ->
            try {
                progressDialog.dismiss()
                result
                    .onSuccess {
                        NotifyUtil
                            .makeText(
                                requireContext(),
                                getString(R.string.text_changes_saved),
                                R.drawable.ic_check_circle_white_24dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                        dismiss()
                    }.onFailure { throwable ->
                        Timber.e(throwable)
                        NotifyUtil
                            .makeText(
                                requireContext(),
                                getString(R.string.text_error_request),
                                R.drawable.ic_warning_white_18dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun handleDelete() {
        val progressDialog = NotifyUtil.createProgressDialog(requireContext(), R.string.text_processing_request)
        progressDialog.show()

        koinOf<WidgetMutationCoordinator>().deleteMediaListEntry(mediaListModel.id) { result ->
            try {
                progressDialog.dismiss()
                result
                    .onSuccess { deleteState ->
                        if (deleteState.isDeleted) {
                            NotifyUtil
                                .makeText(
                                    requireContext(),
                                    getString(R.string.text_changes_saved),
                                    R.drawable.ic_check_circle_white_24dp,
                                    Toast.LENGTH_SHORT,
                                ).show()
                            dismiss()
                        }
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        NotifyUtil
                            .makeText(
                                requireContext(),
                                getString(R.string.text_error_request),
                                R.drawable.ic_warning_white_18dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    /**
     * Resolves the user's score format from the current user's media list options.
     * Falls back to [KeyUtil.POINT_100] if the user data cannot be resolved.
     */
    private fun resolveScoreFormat(): String {
        return runCatching {
            koinOf<WidgetMutationCoordinator>().databaseHelper.currentUser?.mediaListOptions?.scoreFormat
        }.getOrNull() ?: KeyUtil.POINT_100
    }

    // ----------------------------------------------------------------------------
    // Bundle extraction helpers (mirrored from MediaDialogUtil)
    // ----------------------------------------------------------------------------

    @Suppress("DEPRECATION")
    private fun Bundle.intValue(key: String): Int? {
        val raw = if (containsKey(key)) get(key) else null
        return when (raw) {
            is Number -> raw.toInt()
            is String -> raw.toIntOrNull()
            else -> null
        }
    }

    @Suppress("DEPRECATION")
    private fun Bundle.longValue(key: String): Long? {
        val raw = if (containsKey(key)) get(key) else null
        return when (raw) {
            is Number -> raw.toLong()
            is String -> raw.toLongOrNull()
            else -> null
        }
    }

    @Suppress("DEPRECATION")
    private fun Bundle.stringValue(key: String): String? {
        return if (containsKey(key)) get(key)?.toString() else null
    }

    @Suppress("DEPRECATION")
    private fun Bundle.boolValue(key: String): Boolean? {
        val raw = if (containsKey(key)) get(key) else null
        return when (raw) {
            is Boolean -> raw
            is String -> raw.toBooleanStrictOrNull()
            else -> null
        }
    }

    @Suppress("DEPRECATION")
    private fun Bundle.doubleValue(key: String): Double? {
        val raw = if (containsKey(key)) get(key) else null
        return when (raw) {
            is Number -> raw.toDouble()
            is String -> raw.toDoubleOrNull()
            else -> null
        }
    }

    @Suppress("DEPRECATION")
    private fun Bundle.stringListValue(key: String): List<String?>? {
        val raw = if (containsKey(key)) get(key) else null
        return when (raw) {
            is Iterable<*> -> raw.map { it?.toString() }.takeIf { it.isNotEmpty() }
            is Array<*> -> raw.map { it?.toString() }.takeIf { it.isNotEmpty() }
            else -> raw?.toString()?.let(::listOf)
        }
    }

    @Suppress("DEPRECATION")
    private fun Bundle.doubleListValue(key: String): List<Double?>? {
        val raw = if (containsKey(key)) get(key) else null
        return when (raw) {
            is Iterable<*> ->
                raw.mapNotNull { v ->
                    when (v) {
                        is Number -> v.toDouble()
                        is String -> v.toDoubleOrNull()
                        else -> null
                    }
                }.takeIf { it.isNotEmpty() }
            is DoubleArray -> raw.toList().takeIf { it.isNotEmpty() }
            is FloatArray -> raw.map { it.toDouble() }.takeIf { it.isNotEmpty() }
            else -> doubleValue(key)?.let(::listOf)
        }
    }

    @Suppress("DEPRECATION")
    private fun Bundle.fuzzyDateInputValue(key: String): FuzzyDateInput? {
        val raw = if (containsKey(key)) get(key) else null
        return when (raw) {
            is FuzzyDateInput -> raw
            is FuzzyDate -> raw.takeIf { it.isValidDate }?.let { d -> FuzzyDateInput(d.day, d.month, d.year) }
            else -> null
        }
    }

    private inline fun <reified T : Enum<T>> Bundle.enumValue(key: String): T? {
        val raw = if (containsKey(key)) get(key) else null
        val enumName = raw?.toString() ?: return null
        return runCatching { enumValueOf<T>(enumName) }
            .onFailure { Timber.tag(TAG).w(it, "Unknown %s value: %s", T::class.java.simpleName, enumName) }
            .getOrNull()
    }

    companion object {
        private const val TAG = "BtmSheetSeriesManage"
        private const val ARG_MEDIA_BASE = "arg_media_base"

        fun newInstance(mediaBase: MediaBase): BottomSheetSeriesManage =
            BottomSheetSeriesManage().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MEDIA_BASE, mediaBase)
                }
            }
    }
}
