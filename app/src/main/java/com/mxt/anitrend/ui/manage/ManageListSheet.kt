/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.ui.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.MediaListDraft
import com.mxt.anitrend.ui.format.mediaListStatusLabelRes
import com.mxt.anitrend.ui.medialist.FuzzyDateField
import com.mxt.anitrend.ui.medialist.MediaListNumericControl
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.sheet.validateManageForm

/**
 * Entry-owned manage sheet over a staged [MediaListDraft]. Nothing mutates
 * until Apply is pressed once; Reset, Cancel and dismissal discard the staged
 * draft and leave the saved entry untouched. A failed save keeps the draft.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManageListSheet(
    title: String,
    isAnime: Boolean,
    canDelete: Boolean,
    isSaving: Boolean,
    isDeletePending: Boolean,
    canFollow: Boolean,
    isFavourite: Boolean,
    draft: MediaListDraft,
    customLists: List<String>,
    onFollowToggle: () -> Unit,
    onDraftChange: (MediaListDraft) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var deleteArmed by remember { mutableStateOf(false) }
    var editingDateField by remember { mutableStateOf<ManageDateField?>(null) }
    val validation = validateManageForm(progress = draft.progress, repeat = draft.repeat)
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (canFollow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    TextButton(onClick = onFollowToggle) {
                        Text(
                            text = stringResource(if (isFavourite) R.string.description_favourite_action_remove else R.string.description_favourite_action_add),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            StatusPicker(
                selected = draft.status,
                onStatusSelected = { status -> onDraftChange(draft.copy(status = status)) },
            )

            NumericRow(
                label = stringResource(R.string.dialog_title_score),
                value = draft.scoreRaw ?: draft.score.toInt(),
                onValueChange = { value -> onDraftChange(draft.copy(score = value.toFloat(), scoreRaw = value)) },
            )
            NumericRow(
                label = stringResource(R.string.dialog_title_episodes),
                value = draft.progress,
                onValueChange = { value -> onDraftChange(draft.copy(progress = value)) },
            )
            if (!isAnime) {
                NumericRow(
                    label = stringResource(R.string.dialog_title_volumes_read),
                    value = draft.progressVolumes,
                    onValueChange = { value -> onDraftChange(draft.copy(progressVolumes = value)) },
                )
            }
            NumericRow(
                label = stringResource(R.string.dialog_title_rewatched),
                value = draft.repeat,
                onValueChange = { value -> onDraftChange(draft.copy(repeat = value)) },
            )
            NumericRow(
                label = stringResource(R.string.dialog_title_priority),
                value = draft.priority,
                onValueChange = { value -> onDraftChange(draft.copy(priority = value)) },
            )

            val startedAtText = draft.startedAt?.formatOrEmpty().orEmpty().ifEmpty { stringResource(R.string.TBA) }
            FuzzyDateField(
                value = startedAtText,
                contentDescription = stringResource(R.string.dialog_title_started_at),
                onClick = { editingDateField = ManageDateField.STARTED },
            )
            val completedAtText = draft.completedAt?.formatOrEmpty().orEmpty().ifEmpty { stringResource(R.string.TBA) }
            FuzzyDateField(
                value = completedAtText,
                contentDescription = stringResource(R.string.dialog_title_completed_at),
                onClick = { editingDateField = ManageDateField.COMPLETED },
            )

            ToggleRow(
                label = stringResource(R.string.dialog_title_private),
                checked = draft.isHidden,
                onCheckedChange = { checked -> onDraftChange(draft.copy(isHidden = checked)) },
            )
            ToggleRow(
                label = stringResource(R.string.dialog_title_hidden_from_status_lists),
                checked = draft.isHiddenFromStatusLists,
                onCheckedChange = { checked -> onDraftChange(draft.copy(isHiddenFromStatusLists = checked)) },
            )

            if (customLists.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.dialog_title_custom_lists),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                customLists.forEach { listName ->
                    val selected = draft.customLists.orEmpty().contains(listName)
                    ToggleRow(
                        label = listName,
                        checked = selected,
                        onCheckedChange = { checked ->
                            val updated =
                                if (checked) {
                                    draft.customLists.orEmpty() + listName
                                } else {
                                    draft.customLists.orEmpty() - listName
                                }
                            onDraftChange(draft.copy(customLists = updated))
                        },
                    )
                }
            }

            OutlinedTextField(
                value = draft.notes.orEmpty(),
                onValueChange = { notes -> onDraftChange(draft.copy(notes = notes)) },
                label = { Text(stringResource(R.string.dialog_hint_enter_your_notes)) },
                modifier = Modifier.fillMaxWidth(),
            )

            if (!validation.isValid && validation.errorResId != null) {
                Text(
                    text = stringResource(validation.errorResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (canDelete) {
                    TextButton(onClick = {
                        if (deleteArmed) onDelete() else deleteArmed = true
                    }, enabled = !isDeletePending) {
                        Text(
                            text = stringResource(R.string.Delete),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                TextButton(onClick = onReset, enabled = !isSaving) {
                    Text(text = stringResource(R.string.Reset), style = MaterialTheme.typography.labelLarge)
                }
                TextButton(onClick = onApply, enabled = !isSaving && validation.isValid) {
                    Text(text = stringResource(R.string.Apply), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    editingDateField?.let { field ->
        val current = if (field == ManageDateField.STARTED) draft.startedAt else draft.completedAt
        val dateState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = current.pickerSeedMillis(),
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { editingDateField = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = dateState.selectedDateMillis?.let(::epochMillisToFuzzyDate)
                        if (picked != null) {
                            onDraftChange(
                                if (field == ManageDateField.STARTED) draft.copy(startedAt = picked) else draft.copy(completedAt = picked),
                            )
                        }
                        editingDateField = null
                    },
                ) { Text(text = stringResource(R.string.Ok)) }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            onDraftChange(
                                if (field == ManageDateField.STARTED) draft.copy(startedAt = null) else draft.copy(completedAt = null),
                            )
                            editingDateField = null
                        },
                    ) { Text(text = stringResource(R.string.TBA)) }
                    TextButton(onClick = { editingDateField = null }) { Text(text = stringResource(R.string.Cancel)) }
                }
            },
        ) {
            androidx.compose.material3.DatePicker(state = dateState)
        }
    }
}

/** Which staged date field the picker dialog is editing. */
private enum class ManageDateField {
    STARTED,
    COMPLETED,
}

@Composable
private fun StatusPicker(
    selected: String?,
    onStatusSelected: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val statuses =
            listOf(
                KeyUtil.CURRENT,
                KeyUtil.PLANNING,
                KeyUtil.COMPLETED,
                KeyUtil.DROPPED,
                KeyUtil.PAUSED,
                KeyUtil.REPEATING,
            )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            statuses.take(3).forEach { status ->
                FilterChip(
                    selected = selected == status,
                    onClick = { onStatusSelected(status) },
                    label = { Text(stringResource(mediaListStatusLabelRes(status))) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            statuses.drop(3).forEach { status ->
                FilterChip(
                    selected = selected == status,
                    onClick = { onStatusSelected(status) },
                    label = { Text(stringResource(mediaListStatusLabelRes(status))) },
                )
            }
        }
    }
}

@Composable
private fun NumericRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        MediaListNumericControl(
            value = TextFieldValue(value.toString()),
            maximumLabel = null,
            keyboardType = KeyboardType.Number,
            onValueChange = { field -> onValueChange(field.text.filter { it.isDigit() }.toIntOrNull() ?: 0) },
            onDecrement = { onValueChange((value - 1).coerceAtLeast(0)) },
            onIncrement = { onValueChange(value + 1) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate.formatOrEmpty(): String {
    val yearValue = year ?: 0
    if (yearValue == 0) return ""
    val monthValue = (month ?: 0).coerceIn(0, 12)
    val dayValue = (day ?: 0).coerceIn(0, 31)
    return String.format(java.util.Locale.US, "%04d-%02d-%02d", yearValue, monthValue, dayValue)
}

/**
 * UTC epoch millis that seed the manage-sheet date picker for this staged date,
 * or null when the staged value is absent or yearless so the picker defaults to today.
 */
internal fun com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate?.pickerSeedMillis(): Long? {
    val date = this ?: return null
    if (date.year == 0) return null
    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    calendar.set(date.year, date.month.coerceAtLeast(1) - 1, date.day.coerceAtLeast(1), 0, 0, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/** Maps picker UTC epoch [millis] back into a staged date. */
internal fun epochMillisToFuzzyDate(millis: Long): com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate {
    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = millis
    return com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate(
        day = calendar.get(java.util.Calendar.DAY_OF_MONTH),
        month = calendar.get(java.util.Calendar.MONTH) + 1,
        year = calendar.get(java.util.Calendar.YEAR),
    )
}
