package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.core.content.edit
import com.mxt.anitrend.model.entity.settings.CustomizeSettingsUiState
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Shared preference keys backing the Customize settings category.
 *
 * Keys are injected rather than resolved from resources so the ViewModel
 * stays free of Android context and is unit-testable.
 */
data class CustomizePreferenceKeys(
    val themeKey: String,
    val languageKey: String,
    val listViewStyleKey: String,
)

/**
 * Owns the Customize settings category state and its reduction.
 *
 * Writes go through [Settings] (the canonical shared preference store) and
 * are applied synchronously on the calling thread, matching the legacy
 * settings behavior where preference writes, their side-effect listener
 * callbacks, and re-rendering all happen on the main thread.
 */
class CustomizeSettingsViewModel(
    private val settings: Settings,
    private val keys: CustomizePreferenceKeys,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CustomizeSettingsUiState(
            theme = settings.getString(keys.themeKey, KeyUtil.THEME_LIGHT) ?: KeyUtil.THEME_LIGHT,
            language = settings.getString(keys.languageKey, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE,
            listViewStyle = settings.getString(keys.listViewStyleKey, DEFAULT_LIST_VIEW_STYLE) ?: DEFAULT_LIST_VIEW_STYLE,
        ),
    )

    val state: StateFlow<CustomizeSettingsUiState> = _state.asStateFlow()

    fun setTheme(value: String) {
        persist(keys.themeKey, value) { it.copy(theme = value) }
    }

    fun setLanguage(value: String) {
        persist(keys.languageKey, value) { it.copy(language = value) }
    }

    fun setListViewStyle(value: String) {
        persist(keys.listViewStyleKey, value) { it.copy(listViewStyle = value) }
    }

    private fun persist(
        key: String,
        value: String,
        reduce: (CustomizeSettingsUiState) -> CustomizeSettingsUiState,
    ) {
        settings.edit {
            putString(key, value)
        }
        _state.update(reduce)
    }

    companion object {
        const val DEFAULT_LANGUAGE = "en"
        const val DEFAULT_LIST_VIEW_STYLE = "0"
    }
}
