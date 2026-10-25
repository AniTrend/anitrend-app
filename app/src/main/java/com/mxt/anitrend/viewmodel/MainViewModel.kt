package com.mxt.anitrend.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Owns the current-user fetch for the main navigation shell.
 * Replaces the legacy current-user request path.
 *
 * Also exposes the shell-level [UiPreferences] as a [StateFlow] so hosts and
 * routes observe theme, language, list style and the effective adult filter
 * live, without finish-and-relaunch or a restart toast. The flow is driven by
 * a single shared preference listener that is unregistered in [onCleared].
 * Observing the flow never triggers the user fetch; only the explicit
 * [loadCurrentUser] does.
 */
class MainViewModel(
    private val userRepository: UserRepository,
    private val settings: Settings,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val user: User) : UiState
        data class Error(val message: String) : UiState
    }

    /** Live shell preferences reduced from the shared preference store. */
    data class UiPreferences(
        val theme: String,
        val language: String?,
        val listViewStyle: Int,
        val displayAdultContent: Boolean,
    )

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val preferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            _uiPreferences.value = uiPreferencesFrom(settings)
        }

    private val _uiPreferences = MutableStateFlow(uiPreferencesFrom(settings))
    val uiPreferences: StateFlow<UiPreferences> = _uiPreferences.asStateFlow()

    init {
        settings.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    /**
     * Fetches the currently authenticated user. Does not throttle -- the
     * caller (MainActivity) is expected to apply its own 5-minute throttle
     * via [com.mxt.anitrend.presenter.base.BasePresenter.updateUserLastSyncTimeStampIf].
     */
    fun loadCurrentUser() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                userRepository.getCurrentUser(asHtml = false).getOrThrow()
            }.onSuccess { user ->
                _state.value = UiState.Success(user)
            }.onFailure { throwable ->
                Timber.e(throwable, "MainViewModel current user fetch failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load current user",
                )
            }
        }
    }

    fun currentUser() = userRepository.cachedCurrentUser

    override fun onCleared() {
        settings.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        super.onCleared()
    }

    private fun uiPreferencesFrom(settings: Settings) = UiPreferences(
        theme = settings.theme,
        language = settings.userLanguage,
        listViewStyle = settings.mediaListStyle,
        displayAdultContent = settings.displayAdultContent,
    )
}
