package com.mxt.anitrend.view.sheet

import com.mxt.anitrend.domain.model.MediaListDraft
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

// ----------------------------------------------------------------------------
// Status selection logic (extracted from handleStatusSelected)
// ----------------------------------------------------------------------------

/**
 * Result of computing what side effects a status selection should trigger.
 *
 * @property newStatus the status string (e.g. "CURRENT", "COMPLETED")
 * @property warningResId if non-null, the string resource ID of a warning to display
 * @property autoFillProgress if non-null, the progress value to auto-fill (episodes or chapters)
 * @property autoFillVolumes if non-null, the volumes value to auto-fill (manga only)
 */
data class StatusSelectionResult(
    val newStatus: String,
    val warningResId: Int? = null,
    val autoFillProgress: Int? = null,
    val autoFillVolumes: Int? = null,
)

/**
 * Computes the side effects of selecting a list status.
 *
 * This is a pure function: given the same inputs it always produces the
 * same result. The caller is responsible for applying view updates
 * (setting icons, showing warnings, updating progress widgets).
 *
 * @param statusIndex index into the [statuses] array
 * @param statuses ordered array of status strings (CURRENT, PLANNING, ...)
 * @param mediaBase the media entry being managed
 * @param isAnime true if anime, false if manga
 * @return the computed result, or null if the index is out of range
 */
fun computeStatusSelectionEffects(
    statusIndex: Int,
    statuses: Array<String>,
    mediaBase: MediaBase,
    isAnime: Boolean,
): StatusSelectionResult? {
    if (statusIndex < 0 || statusIndex >= statuses.size) return null
    val newStatus = statuses[statusIndex]
    val mediaStatus = mediaBase.status

    return if (isAnime) {
        computeAnimeStatusEffects(newStatus, mediaStatus, mediaBase)
    } else {
        computeMangaStatusEffects(newStatus, mediaStatus, mediaBase)
    }
}

private fun computeAnimeStatusEffects(
    newStatus: String,
    mediaStatus: String?,
    mediaBase: MediaBase,
): StatusSelectionResult = when (newStatus) {
    KeyUtil.CURRENT -> {
        if (CompatUtil.equals(mediaStatus, KeyUtil.NOT_YET_RELEASED)) {
            StatusSelectionResult(newStatus, warningResId = com.mxt.anitrend.R.string.status_warning_not_aired)
        } else {
            StatusSelectionResult(newStatus)
        }
    }
    KeyUtil.COMPLETED -> {
        if (!CompatUtil.equals(mediaStatus, KeyUtil.FINISHED)) {
            StatusSelectionResult(newStatus, warningResId = com.mxt.anitrend.R.string.status_warning_still_airing)
        } else {
            StatusSelectionResult(
                newStatus,
                autoFillProgress = mediaBase.episodes,
            )
        }
    }
    KeyUtil.PLANNING -> StatusSelectionResult(newStatus)
    else -> {
        if (CompatUtil.equals(mediaStatus, KeyUtil.NOT_YET_RELEASED)) {
            StatusSelectionResult(newStatus, warningResId = com.mxt.anitrend.R.string.status_warning_not_aired)
        } else {
            StatusSelectionResult(newStatus)
        }
    }
}

private fun computeMangaStatusEffects(
    newStatus: String,
    mediaStatus: String?,
    mediaBase: MediaBase,
): StatusSelectionResult = when (newStatus) {
    KeyUtil.CURRENT -> {
        if (CompatUtil.equals(mediaStatus, KeyUtil.NOT_YET_RELEASED)) {
            StatusSelectionResult(newStatus, warningResId = com.mxt.anitrend.R.string.status_warning_not_published)
        } else {
            StatusSelectionResult(newStatus)
        }
    }
    KeyUtil.COMPLETED -> {
        if (!CompatUtil.equals(mediaStatus, KeyUtil.FINISHED)) {
            StatusSelectionResult(newStatus, warningResId = com.mxt.anitrend.R.string.status_warning_still_publishing)
        } else {
            StatusSelectionResult(
                newStatus,
                autoFillProgress = mediaBase.chapters,
                autoFillVolumes = mediaBase.volumes.takeIf { it > 0 },
            )
        }
    }
    KeyUtil.PLANNING -> StatusSelectionResult(newStatus)
    else -> {
        if (CompatUtil.equals(mediaStatus, KeyUtil.NOT_YET_RELEASED)) {
            StatusSelectionResult(newStatus, warningResId = com.mxt.anitrend.R.string.status_warning_not_published)
        } else {
            StatusSelectionResult(newStatus)
        }
    }
}

// ----------------------------------------------------------------------------
// Form validation logic (extracted from handleSave)
// ----------------------------------------------------------------------------

/**
 * Result of validating the manage-sheet form inputs.
 *
 * @property isValid true if the form passes all validation rules
 * @property errorResId if non-null, the string resource ID of the first error found
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorResId: Int? = null,
) {
    companion object {
        val valid = ValidationResult(isValid = true)
    }
}

/**
 * Validates the manage-sheet form inputs.
 *
 * Rules: progress >= 0, repeat >= 0.
 *
 * @return [ValidationResult] with the first error found, or [ValidationResult.valid]
 */
fun validateManageForm(
    progress: Int,
    repeat: Int,
): ValidationResult {
    if (progress < 0) {
        return ValidationResult(
            isValid = false,
            errorResId = com.mxt.anitrend.R.string.validation_progress_negative,
        )
    }
    if (repeat < 0) {
        return ValidationResult(
            isValid = false,
            errorResId = com.mxt.anitrend.R.string.validation_repeat_negative,
        )
    }
    return ValidationResult.valid
}

// ----------------------------------------------------------------------------
// Notes HTML decode logic (extracted from populateFromModel)
// ----------------------------------------------------------------------------

/**
 * Returns true if [input] appears to be HTML-encoded and should be decoded.
 *
 * This check is testable in unit tests. The actual [androidx.core.text.HtmlCompat.fromHtml]
 * call requires an Android instrumentation context.
 */
fun shouldDecodeHtml(input: String): Boolean = input.contains("&") && (input.contains(";") || input.contains("#"))

/**
 * Decodes HTML entities from [notes] if it appears to be HTML-encoded.
 *
 * The server may return HTML-encoded notes. Because [com.mxt.anitrend.base.custom.view.editor.MarkdownInputEditor]
 * encodes again via its [formattedText] getter, raw HTML entities would be double-encoded.
 * This function guards against that by decoding first.
 *
 * Plain-text notes pass through unchanged.
 *
 * @param notes the raw notes string from the model
 * @return decoded text, or the original string if not HTML-encoded, or null if input is null
 */
fun decodeNotesIfEncoded(notes: String?): String? {
    if (notes == null) return null
    return if (shouldDecodeHtml(notes)) {
        androidx.core.text.HtmlCompat.fromHtml(notes, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    } else {
        notes
    }
}

// ----------------------------------------------------------------------------
// Model-building logic (extracted from handleSave)
// ----------------------------------------------------------------------------

/**
 * Builds a [MediaListDraft] from the manage-sheet form inputs.
 *
 * Returns a new draft value and leaves the committed model untouched. Does NOT perform validation;
 * call [validateManageForm] before calling this.
 *
 * @param draft the current editable draft state
 * @param statusIndex index into [statuses]
 * @param statuses ordered array of status strings
 * @param progress progress value from widget
 * @param repeat repeat value from widget
 * @param score score value from widget
 * @param progressVolumes volumes progress from widget (ignored for anime)
 * @param isAnime true for anime, false for manga
 * @param startedAt start date from widget
 * @param completedAt end date from widget
 * @param isHidden private flag from switch
 * @param isHiddenFromStatusLists hidden-from-status-lists flag from switch
 * @param priority priority slider value
 * @param notes formatted notes text
 * @param advancedScores collected advanced scores map (category -> value)
 * @return the updated draft value
 */
@Suppress("LongParameterList")
fun buildMediaListFromForm(
    draft: MediaListDraft,
    statusIndex: Int,
    statuses: Array<String>,
    progress: Int,
    repeat: Int,
    score: Float,
    progressVolumes: Int,
    isAnime: Boolean,
    startedAt: FuzzyDate?,
    completedAt: FuzzyDate?,
    isHidden: Boolean,
    isHiddenFromStatusLists: Boolean,
    priority: Int,
    notes: String?,
    advancedScores: Map<String, Float>?,
): MediaListDraft = draft.copy(
    status = statuses[statusIndex],
    score = score,
    progress = progress,
    progressVolumes = if (isAnime) draft.progressVolumes else progressVolumes,
    repeat = repeat,
    priority = priority,
    isHidden = isHidden,
    isHiddenFromStatusLists = isHiddenFromStatusLists,
    notes = notes,
    advancedScores = advancedScores ?: draft.advancedScores,
    startedAt = startedAt,
    completedAt = completedAt,
)
