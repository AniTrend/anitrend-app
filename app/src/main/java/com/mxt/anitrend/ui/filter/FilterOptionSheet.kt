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
package com.mxt.anitrend.ui.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.view.sheet.MediaFilterSheetDraft

/**
 * Entry-owned filter sheet over the shared [MediaFilterSheetDraft] semantics:
 * selections are staged, Reset clears the staged selection, Apply emits the
 * staged result exactly once, and dismissal without Apply is a cancel that
 * leaves the caller's active filters untouched.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun FilterOptionSheet(
    title: String,
    options: List<String>,
    initialSelection: List<Int>,
    isMultiSelect: Boolean,
    onApply: (action: String, selectedIndices: List<Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(initialSelection) { mutableStateOf(MediaFilterSheetDraft(selectedIndices = initialSelection)) }

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
            if (isMultiSelect) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    options.forEachIndexed { index, label ->
                        FilterChip(
                            selected = index in draft.selectedIndices,
                            onClick = {
                                draft =
                                    if (index in draft.selectedIndices) {
                                        draft.select(draft.selectedIndices - index)
                                    } else {
                                        draft.select(draft.selectedIndices + index)
                                    }
                            },
                            label = { Text(label) },
                        )
                    }
                }
            } else {
                options.forEachIndexed { index, label ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = draft.selectedIndices.firstOrNull() == index,
                            onClick = { draft = draft.select(listOf(index)) },
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { draft = draft.reset() }) {
                    Text(text = stringResource(R.string.Reset), style = MaterialTheme.typography.labelLarge)
                }
                TextButton(
                    onClick = {
                        val indices =
                            if (draft.resetRequested) {
                                emptyList()
                            } else {
                                draft.selectedIndices
                            }
                        onApply(draft.action(), indices)
                    },
                ) {
                    Text(text = stringResource(R.string.Apply), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
