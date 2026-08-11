package com.mxt.anitrend.view.sheet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Stable result returned by [BottomSheetMediaFilter] through [BottomSheetMediaFilter.RESULT_KEY].
 *
 * [action] is one of [ACTION_APPLY], [ACTION_RESET], or [ACTION_CANCEL]. For APPLY, the
 * selected option positions and their labels are returned in option order. RESET always returns
 * empty selections, making a clear operation distinct from an empty-list request workaround.
 * [requestId] is the opaque invocation ID supplied when the sheet is created.
 */
@Parcelize
data class MediaFilterSheetResult(
    val requestId: String,
    val action: String,
    val selectedIndices: IntArray = intArrayOf(),
    val selectedValues: ArrayList<String> = arrayListOf(),
) : Parcelable {
    companion object {
        const val ACTION_APPLY = "apply"
        const val ACTION_RESET = "reset"
        const val ACTION_CANCEL = "cancel"
    }
}

/** Draft-only state used by the sheet to keep reset and re-selection semantics explicit. */
internal data class MediaFilterSheetDraft(
    val selectedIndices: List<Int> = emptyList(),
    val resetRequested: Boolean = false,
) {
    fun reset(): MediaFilterSheetDraft = copy(
        selectedIndices = emptyList(),
        resetRequested = true,
    )

    fun select(indices: Collection<Int>): MediaFilterSheetDraft = copy(selectedIndices = indices.sorted(), resetRequested = false)

    fun action(): String = if (resetRequested) {
        MediaFilterSheetResult.ACTION_RESET
    } else {
        MediaFilterSheetResult.ACTION_APPLY
    }
}
