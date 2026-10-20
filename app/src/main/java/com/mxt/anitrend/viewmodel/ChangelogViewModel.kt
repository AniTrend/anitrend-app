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
package com.mxt.anitrend.viewmodel

import android.content.res.AssetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.ui.changelog.ChangelogDocument
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * Loads and decodes the bundled release notes exactly once, off the UI
 * thread.
 *
 * A missing or invalid bundled JSON surfaces [ChangelogUiState.Error] so the
 * screen can offer a localized load-failure message with retry; an empty
 * releases list surfaces [ChangelogUiState.Empty] while still allowing the
 * acknowledgements and Back actions.
 */
class ChangelogViewModel(
    private val assets: AssetManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface ChangelogUiState {
        data object Loading : ChangelogUiState

        data class Content(val document: ChangelogDocument) : ChangelogUiState

        data object Empty : ChangelogUiState

        data object Error : ChangelogUiState
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow<ChangelogUiState>(ChangelogUiState.Loading)
    val state: StateFlow<ChangelogUiState> = _state.asStateFlow()

    init {
        load()
    }

    /** Retries the one-shot load after a failure. */
    fun retry() = load()

    private fun load() {
        _state.value = ChangelogUiState.Loading
        viewModelScope.launch {
            _state.value =
                withContext(ioDispatcher) {
                    runCatching { decodeDocument() }
                        .onFailure { Timber.e(it, "Failed to load bundled changelog") }
                        .fold(
                            onSuccess = { document ->
                                if (document.releases.isEmpty()) {
                                    ChangelogUiState.Empty
                                } else {
                                    ChangelogUiState.Content(document)
                                }
                            },
                            onFailure = { ChangelogUiState.Error },
                        )
                }
        }
    }

    private fun decodeDocument(): ChangelogDocument = assets.open(CHANGELOG_ASSET).bufferedReader().use { reader ->
        json.decodeFromString(ChangelogDocument.serializer(), reader.readText())
    }

    private companion object {
        const val CHANGELOG_ASSET = "changelog.json"
    }
}
