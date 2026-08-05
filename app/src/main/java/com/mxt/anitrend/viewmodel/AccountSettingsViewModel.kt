package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * ViewModel-first state owner for the account settings screen.
 *
 * The form seeds from [UserRepository.cachedCurrentUser] (safe for a missing
 * cache), supports a full server refresh through `getCurrentUser`, and saves
 * only the sparse set of dirty fields through the bounded, server-authoritative
 * `updateUser` mutation. Saves launch in [viewModelScope]; repeated saves are
 * rejected while one is in flight, and failed saves keep the edited form so the
 * user can retry or discard.
 *
 * The local `pref_key_display_adult_content` shared preference keeps its own
 * existing behavior and flavor gating in the legacy settings rows. This
 * ViewModel only edits the separate server-side `displayAdultContent` field
 * and never touches that preference, so the two stay independent.
 *
 * UI event concerns (navigation, toasts, dialog confirmation) stay in the
 * fragment; this ViewModel exposes state and actions only.
 *
 * The state reduction helpers ([updateDirty], [beginRefreshIfIdle],
 * [toSparseUserSettingsUpdate] and [mergeServerRecord]) live with the state
 * model in [AccountSettingsUiState]'s file.
 */
class AccountSettingsViewModel(
    private val userRepository: UserRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AccountSettingsUiState.from(userRepository.cachedCurrentUser),
    )

    val state: StateFlow<AccountSettingsUiState> = _state.asStateFlow()

    /**
     * Reloads the account settings from the server and reseeds the form.
     *
     * The loading guard is atomic ([beginRefreshIfIdle]): concurrent calls
     * cannot both pass the check before either one publishes the loading
     * state, so only one refresh is ever in flight.
     *
     * On success the form is replaced with the server state (pending edits are
     * intentionally overwritten, matching the explicit refresh action). On
     * failure the current form is kept and [AccountSettingsUiState.errorMessage]
     * is set.
     */
    fun refresh() {
        if (!_state.beginRefreshIfIdle()) return
        viewModelScope.launch {
            withContext(ioDispatcher) { userRepository.getCurrentUser(asHtml = false) }
                .onSuccess { user ->
                    _state.value = AccountSettingsUiState.from(user)
                }
                .onFailure { throwable ->
                    Timber.e(throwable)
                    _state.update {
                        it.copy(isLoading = false, errorMessage = throwable.message ?: REFRESH_FAILED_MESSAGE)
                    }
                }
        }
    }

    /** Replaces the about text and marks it dirty for the next save. */
    fun setAbout(value: String) = _state.updateDirty { it.copy(about = value, aboutDirty = true) }

    /** Accepts only [AccountSettingsOptions.PROFILE_COLORS] values. */
    fun setProfileColor(value: String) {
        if (value in AccountSettingsOptions.PROFILE_COLORS) {
            _state.updateDirty { current ->
                if (current.profileColor == value) {
                    current
                } else {
                    current.copy(profileColor = value, profileColorDirty = true)
                }
            }
        }
    }

    /** Accepts only [AccountSettingsOptions.SCORE_FORMATS] values. */
    fun setScoreFormat(value: String) {
        if (value in AccountSettingsOptions.SCORE_FORMATS) {
            _state.updateDirty { current ->
                if (current.scoreFormat == value) {
                    current
                } else {
                    current.copy(scoreFormat = value, scoreFormatDirty = true)
                }
            }
        }
    }

    /** Accepts only [AccountSettingsOptions.TITLE_LANGUAGES] values. */
    fun setTitleLanguage(value: String) {
        if (value in AccountSettingsOptions.TITLE_LANGUAGES) {
            _state.updateDirty { current ->
                if (current.titleLanguage == value) {
                    current
                } else {
                    current.copy(titleLanguage = value, titleLanguageDirty = true)
                }
            }
        }
    }

    /** Accepts only [AccountSettingsOptions.ROW_ORDERS] values. */
    fun setRowOrder(value: String) {
        if (value in AccountSettingsOptions.ROW_ORDERS) {
            _state.updateDirty { current ->
                if (current.rowOrder == value) {
                    current
                } else {
                    current.copy(rowOrder = value, rowOrderDirty = true)
                }
            }
        }
    }

    /** Sets the airing notifications preference and marks it dirty for the next save. */
    fun setAiringNotifications(value: Boolean) = _state.updateDirty { it.copy(airingNotifications = value, airingNotificationsDirty = true) }

    /**
     * Edits the server-side adult content preference. This is separate from
     * the local `pref_key_display_adult_content` preference, which keeps its
     * existing behavior; this action never writes that preference.
     */
    fun setDisplayAdultContent(value: Boolean) = _state.updateDirty { it.copy(displayAdultContent = value, displayAdultContentDirty = true) }

    /**
     * Saves the dirty fields through the server-authoritative `updateUser`
     * mutation and, on success, reduces the form to the server response.
     *
     * Repeated calls while a save is in flight are ignored. A failed save
     * keeps the edited form (fields and dirty flags) and sets
     * [AccountSettingsUiState.errorMessage].
     */
    fun save() {
        val current = _state.value
        if (current.isSaving || !current.hasDirtyFields) return
        val savedFields = current.dirtyFields
        val update = current.toSparseUserSettingsUpdate()
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            withContext(ioDispatcher) { userRepository.updateUser(update) }
                .onSuccess { record ->
                    _state.update { state -> state.mergeServerRecord(record, savedFields) }
                }
                .onFailure { throwable ->
                    Timber.e(throwable)
                    _state.update {
                        it.copy(isSaving = false, errorMessage = throwable.message ?: SAVE_FAILED_MESSAGE)
                    }
                }
        }
    }

    /**
     * Discards pending edits and reseeds the form from the cache (the last
     * committed server state, kept fresh by successful saves and refreshes).
     */
    fun discard() {
        _state.update { AccountSettingsUiState.from(userRepository.cachedCurrentUser) }
    }

    private companion object {
        const val REFRESH_FAILED_MESSAGE = "Failed to refresh account settings"
        const val SAVE_FAILED_MESSAGE = "Failed to save account settings"
    }
}
