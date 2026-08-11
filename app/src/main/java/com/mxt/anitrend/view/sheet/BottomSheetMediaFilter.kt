package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.radiobutton.MaterialRadioButton
import com.mxt.anitrend.R

/**
 * M3 selection sheet for Discover and media-list filters.
 *
 * The sheet owns only draft selection state. Hosts listen for [RESULT_KEY] on the sheet's
 * FragmentManager and apply [MediaFilterSheetResult] only after APPLY or RESET. Dismissal emits
 * CANCEL, so a host never has to inspect views to tell a committed change from a canceled one.
 * [selectedIndices] uses the same positions as the supplied [options], preserving existing
 * single-choice and checklist mapping behavior.
 */
class BottomSheetMediaFilter : BottomSheetDialogFragment() {

    private lateinit var options: ArrayList<String>
    private lateinit var requestId: String
    private var isMultiSelect: Boolean = false
    private var draft = MediaFilterSheetDraft()
    private var draftInitialized = false
    private var resultSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            draft = MediaFilterSheetDraft(
                selectedIndices = it.getIntArray(STATE_SELECTED_INDICES)?.toList().orEmpty(),
                resetRequested = it.getBoolean(STATE_RESET_REQUESTED),
            )
            draftInitialized = true
            resultSent = it.getBoolean(STATE_RESULT_SENT)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putIntArray(STATE_SELECTED_INDICES, draft.selectedIndices.toIntArray())
        outState.putBoolean(STATE_RESET_REQUESTED, draft.resetRequested)
        outState.putBoolean(STATE_RESULT_SENT, resultSent)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.sheet_media_filter_m3, null)
        bind(view)
        dialog.setContentView(view)
        dialog.behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isHideable = true
        }
        return dialog
    }

    private fun bind(view: View) {
        val arguments = requireArguments()
        options = ArrayList(arguments.getStringArrayList(ARG_OPTIONS).orEmpty())
        requestId = arguments.getString(ARG_REQUEST_ID).orEmpty()
        isMultiSelect = arguments.getBoolean(ARG_MULTI_SELECT)
        if (!draftInitialized) {
            draft = if (isMultiSelect) {
                MediaFilterSheetDraft(
                    selectedIndices = arguments.getIntArray(ARG_SELECTED_INDICES)?.toList().orEmpty(),
                )
            } else {
                MediaFilterSheetDraft(
                    selectedIndices = arguments.getInt(ARG_SELECTED_INDEX)
                        .takeIf { it >= 0 }
                        ?.let(::listOf)
                        .orEmpty(),
                )
            }
            draftInitialized = true
        }

        view.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.sheet_filter_title)
            .setText(arguments.getInt(ARG_TITLE))

        val chipGroup = view.findViewById<ChipGroup>(R.id.sheet_filter_multi_options)
        val radioGroup = view.findViewById<android.widget.RadioGroup>(R.id.sheet_filter_single_options)
        if (isMultiSelect) {
            chipGroup.visibility = View.VISIBLE
            radioGroup.visibility = View.GONE
            populateChips(chipGroup, draft.selectedIndices.toIntArray())
            chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                if (checkedIds.isNotEmpty()) {
                    draft = draft.select(checkedIds.map { chipGroup.findViewById<Chip>(it).tag as Int })
                } else {
                    draft = draft.copy(selectedIndices = emptyList())
                }
            }
        } else {
            chipGroup.visibility = View.GONE
            radioGroup.visibility = View.VISIBLE
            populateRadioButtons(radioGroup, draft.selectedIndices.firstOrNull() ?: -1)
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != View.NO_ID) {
                    draft = draft.select(listOf(group.findViewById<MaterialRadioButton>(checkedId).tag as Int))
                } else {
                    draft = draft.copy(selectedIndices = emptyList())
                }
            }
        }

        view.findViewById<MaterialButton>(R.id.sheet_filter_reset).setOnClickListener {
            draft = draft.reset()
            if (isMultiSelect) chipGroup.clearCheck() else radioGroup.clearCheck()
        }
        view.findViewById<MaterialButton>(R.id.sheet_filter_cancel).setOnClickListener { dismiss() }
        view.findViewById<MaterialButton>(R.id.sheet_filter_apply).setOnClickListener {
            val indices = if (draft.resetRequested) intArrayOf() else draft.selectedIndices.toIntArray()
            sendResult(
                MediaFilterSheetResult(
                    requestId = requestId,
                    action = draft.action(),
                    selectedIndices = indices,
                    selectedValues = ArrayList(indices.map(options::get)),
                ),
            )
            dismiss()
        }
    }

    private fun populateChips(group: ChipGroup, selectedIndices: IntArray) {
        val selected = selectedIndices.toSet()
        options.forEachIndexed { index, label ->
            val chip = Chip(
                ContextThemeWrapper(requireContext(), R.style.Widget_AniTrend_ManageSheet_CustomListChip),
            ).apply {
                id = View.generateViewId()
                tag = index
                text = label
                isCheckable = true
                isChecked = index in selected
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelMedium)
            }
            group.addView(chip)
        }
    }

    private fun populateRadioButtons(group: android.widget.RadioGroup, selectedIndex: Int) {
        options.forEachIndexed { index, label ->
            group.addView(
                MaterialRadioButton(requireContext()).apply {
                    id = View.generateViewId()
                    tag = index
                    text = label
                    isChecked = index == selectedIndex
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                },
            )
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        sendResult(MediaFilterSheetResult(requestId, MediaFilterSheetResult.ACTION_CANCEL))
        super.onCancel(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!resultSent) sendResult(MediaFilterSheetResult(requestId, MediaFilterSheetResult.ACTION_CANCEL))
        super.onDismiss(dialog)
    }

    private fun sendResult(result: MediaFilterSheetResult) {
        if (resultSent) return
        resultSent = true
        parentFragmentManager.setFragmentResult(
            RESULT_KEY,
            Bundle().apply { putParcelable(RESULT_BUNDLE_KEY, result) },
        )
    }

    companion object {
        const val RESULT_KEY = "media_filter_sheet_result"
        const val RESULT_BUNDLE_KEY = "media_filter_sheet_result_value"

        private const val ARG_TITLE = "arg_title"
        private const val ARG_REQUEST_ID = "arg_request_id"
        private const val ARG_OPTIONS = "arg_options"
        private const val ARG_SELECTED_INDICES = "arg_selected_indices"
        private const val ARG_SELECTED_INDEX = "arg_selected_index"
        private const val ARG_MULTI_SELECT = "arg_multi_select"
        private const val STATE_SELECTED_INDICES = "state_selected_indices"
        private const val STATE_RESET_REQUESTED = "state_reset_requested"
        private const val STATE_RESULT_SENT = "state_result_sent"

        /**
         * Creates a sheet while keeping the caller's existing option labels and selection indices.
         * [requestId] lets a host associate the result with the exact invocation it opened.
         */
        fun newInstance(
            title: Int,
            options: List<String>,
            selectedIndices: Collection<Int> = emptyList(),
            multiSelect: Boolean,
            requestId: String = DEFAULT_REQUEST_ID,
        ): BottomSheetMediaFilter = BottomSheetMediaFilter().apply {
            arguments = Bundle().apply {
                putInt(ARG_TITLE, title)
                putString(ARG_REQUEST_ID, requestId)
                putStringArrayList(ARG_OPTIONS, ArrayList(options))
                putBoolean(ARG_MULTI_SELECT, multiSelect)
                if (multiSelect) {
                    putIntArray(ARG_SELECTED_INDICES, selectedIndices.toIntArray())
                } else {
                    putInt(ARG_SELECTED_INDEX, selectedIndices.firstOrNull() ?: -1)
                }
            }
        }

        private const val DEFAULT_REQUEST_ID = ""
    }
}
