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
package com.mxt.anitrend.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.composer.MarkdownComposer
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.util.markdown.MarkDownUtil

/**
 * Shared content composer: the editable shared resource over Plain/Link/
 * Image/YouTube/Video type chips, applying the existing MarkDownUtil
 * transformations on selection. The subject seeds the draft only when it
 * differs from the shared text. Apply/Close commit once; dismissal without
 * Apply posts nothing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SharedContentScreen(
    sharedText: String?,
    subject: String?,
    onSubmit: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typeLabels =
        listOf(
            R.string.post_share_type_plain_text,
            R.string.post_share_type_website,
            R.string.post_share_type_picture,
            R.string.post_share_type_youtube,
            R.string.post_share_type_video,
        )

    var selectedType by rememberSaveable { mutableStateOf(0) }
    // The subject seeds the draft only when it differs from the shared text.
    var draft by rememberSaveable {
        mutableStateOf(if (subject != null && subject != sharedText) subject else "")
    }
    var appliedText by rememberSaveable { mutableStateOf<String?>(null) }

    fun applySelection(text: String, position: Int) {
        appliedText =
            when (position) {
                0 -> text
                1 -> MarkDownUtil.convertLink(text)
                2 -> MarkDownUtil.convertImage(text)
                3 -> MarkDownUtil.convertYoutube(text)
                4 -> MarkDownUtil.convertVideo(text)
                else -> text
            }
    }

    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.Dismiss),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        appliedText?.let { text -> onSubmit(text) }
                    },
                    enabled = appliedText != null,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.Apply),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.menu_title_new_activity_post),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = sharedText.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScrollable()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                typeLabels.forEachIndexed { index, labelRes ->
                    FilterChip(
                        selected = selectedType == index,
                        onClick = {
                            selectedType = index
                            applySelection(sharedText.orEmpty(), index)
                        },
                        label = { Text(text = stringResource(labelRes)) },
                    )
                }
            }

            if (appliedText != null) {
                var composerDraft by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                    mutableStateOf(TextFieldValue(appliedText.orEmpty()))
                }
                MarkdownComposer(
                    draft = composerDraft,
                    onDraftChange = { composerDraft = it },
                    enabled = true,
                    placeholder = stringResource(R.string.menu_title_new_activity_post),
                    onAttach = {},
                    onGiphy = {},
                )
            }
            draft.let {
                if (it.isNotEmpty() && appliedText == null) {
                    // Subject-seeded draft remains visible until Apply.
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.horizontalScrollable(): Modifier = then(horizontalScroll(rememberScrollState()))
