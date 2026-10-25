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

import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Shared preference keys backing the Appearance settings category.
 *
 * Keys are injected rather than resolved from resources so the ViewModel
 * stays free of Android context and is unit-testable.
 */
data class AppearancePreferenceKeys(
    val themeKey: String,
    val languageKey: String,
    val listViewStyleKey: String,
)

/**
 * Owns the Appearance settings category state and its reduction.
 *
 * Writes go through [Settings] (the canonical shared preference store) and
 * are applied synchronously on the calling thread, matching the legacy
 * settings behavior where preference writes, their side-effect listener
 * callbacks, and re-rendering all happen on the main thread.
 */
class AppearanceSettingsViewModel(
    private val settings: Settings,
    private val keys: AppearancePreferenceKeys,
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            AppearanceSettingsUiState(
                theme = settings.getString(keys.themeKey, KeyUtil.THEME_LIGHT) ?: KeyUtil.THEME_LIGHT,
                language = settings.getString(keys.languageKey, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE,
                listViewStyle = settings.getString(keys.listViewStyleKey, DEFAULT_LIST_VIEW_STYLE) ?: DEFAULT_LIST_VIEW_STYLE,
            ),
        )

    val state: StateFlow<AppearanceSettingsUiState> = _state.asStateFlow()

    /** Persists the selected application theme and updates the state. */
    fun setTheme(value: String) {
        persist(keys.themeKey, value) { it.copy(theme = value) }
    }

    /** Persists the selected application language and updates the state. */
    fun setLanguage(value: String) {
        persist(keys.languageKey, value) { it.copy(language = value) }
    }

    /** Persists the selected list style and updates the state. */
    fun setListViewStyle(value: String) {
        persist(keys.listViewStyleKey, value) { it.copy(listViewStyle = value) }
    }

    private fun persist(
        key: String,
        value: String,
        reduce: (AppearanceSettingsUiState) -> AppearanceSettingsUiState,
    ) {
        settings.edit {
            putString(key, value)
        }
        _state.update(reduce)
    }

    /** Defaults used when the corresponding preference has not been written. */
    companion object {
        const val DEFAULT_LANGUAGE = "en"
        const val DEFAULT_LIST_VIEW_STYLE = "0"
    }
}
