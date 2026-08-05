package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.domain.user.model.UserSettingsRecord
import com.mxt.anitrend.domain.user.model.UserSettingsUpdate
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
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
     * On success the form is replaced with the server state (pending edits are
     * intentionally overwritten, matching the explicit refresh action). On
     * failure the current form is kept and [AccountSettingsUiState.errorMessage]
     * is set.
     */
    fun refresh() {
        if (!markRefreshStarted()) return
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

    /**
     * Atomically transitions the state into the loading phase.
     *
     * The guard is folded into a single [kotlinx.coroutines.flow.MutableStateFlow.getAndUpdate]
     * call, so concurrent [refresh] calls cannot both pass the check before
     * either one publishes the loading state.
     *
     * @return true when this caller performed the transition (the state was
     * not loading before); false when a refresh is already in flight.
     */
    private fun markRefreshStarted(): Boolean {
        val previous = _state.getAndUpdate { state ->
            if (state.isLoading) state else state.copy(isLoading = true, errorMessage = null)
        }
        return !previous.isLoading
    }

    fun setAbout(value: String) = updateDirty { it.copy(about = value, aboutDirty = true) }

    /** Accepts only [AccountSettingsOptions.PROFILE_COLORS] values. */
    fun setProfileColor(value: String) {
        if (value in AccountSettingsOptions.PROFILE_COLORS) {
            updateDirty { current ->
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
            updateDirty { current ->
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
            updateDirty { current ->
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
            updateDirty { current ->
                if (current.rowOrder == value) {
                    current
                } else {
                    current.copy(rowOrder = value, rowOrderDirty = true)
                }
            }
        }
    }

    fun setAiringNotifications(value: Boolean) = updateDirty { it.copy(airingNotifications = value, airingNotificationsDirty = true) }

    /**
     * Edits the server-side adult content preference. This is separate from
     * the local `pref_key_display_adult_content` preference, which keeps its
     * existing behavior; this action never writes that preference.
     */
    fun setDisplayAdultContent(value: Boolean) = updateDirty { it.copy(displayAdultContent = value, displayAdultContentDirty = true) }

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
        val update = current.toUpdate()
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            withContext(ioDispatcher) { userRepository.updateUser(update) }
                .onSuccess { record ->
                    _state.update { state -> state.applyServerRecord(record, savedFields) }
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

    private fun updateDirty(reduce: (AccountSettingsUiState) -> AccountSettingsUiState) {
        _state.update { current ->
            val reduced = reduce(current)
            // A no-op selection returns the same instance; keep the state fully
            // unchanged (dirty flags and error message) in that case.
            if (reduced === current) reduced else reduced.copy(errorMessage = null)
        }
    }

    /** Builds the sparse wire payload containing only the dirty fields. */
    private fun AccountSettingsUiState.toUpdate(): UserSettingsUpdate = UserSettingsUpdate(
        about = if (AccountSettingsField.ABOUT in dirtyFields) about else null,
        profileColor = if (AccountSettingsField.PROFILE_COLOR in dirtyFields) profileColor else null,
        scoreFormat = if (AccountSettingsField.SCORE_FORMAT in dirtyFields) scoreFormat else null,
        titleLanguage = if (AccountSettingsField.TITLE_LANGUAGE in dirtyFields) titleLanguage else null,
        rowOrder = if (AccountSettingsField.ROW_ORDER in dirtyFields) rowOrder else null,
        airingNotifications = if (AccountSettingsField.AIRING_NOTIFICATIONS in dirtyFields) airingNotifications else null,
        displayAdultContent = if (AccountSettingsField.DISPLAY_ADULT_CONTENT in dirtyFields) displayAdultContent else null,
    )

    /**
     * Reduces the form to the server record for the fields that were saved.
     * Fields edited again while the save was in flight keep their current
     * values unless they were part of the saved set; non-saved fields are
     * untouched. Null record values preserve the current form value.
     */
    private fun AccountSettingsUiState.applyServerRecord(
        record: UserSettingsRecord,
        savedFields: Set<AccountSettingsField>,
    ): AccountSettingsUiState {
        var next = copy(isSaving = false, errorMessage = null)
        if (AccountSettingsField.ABOUT in savedFields) {
            next = next.copy(about = record.about.orEmpty(), aboutDirty = false)
        }
        if (AccountSettingsField.PROFILE_COLOR in savedFields) {
            next = next.copy(profileColor = record.profileColor ?: next.profileColor, profileColorDirty = false)
        }
        if (AccountSettingsField.SCORE_FORMAT in savedFields) {
            next = next.copy(scoreFormat = record.scoreFormat ?: next.scoreFormat, scoreFormatDirty = false)
        }
        if (AccountSettingsField.TITLE_LANGUAGE in savedFields) {
            next = next.copy(titleLanguage = record.titleLanguage ?: next.titleLanguage, titleLanguageDirty = false)
        }
        if (AccountSettingsField.ROW_ORDER in savedFields) {
            next = next.copy(rowOrder = record.rowOrder ?: next.rowOrder, rowOrderDirty = false)
        }
        if (AccountSettingsField.AIRING_NOTIFICATIONS in savedFields) {
            next = next.copy(
                airingNotifications = record.airingNotifications ?: next.airingNotifications,
                airingNotificationsDirty = false,
            )
        }
        if (AccountSettingsField.DISPLAY_ADULT_CONTENT in savedFields) {
            next = next.copy(
                displayAdultContent = record.displayAdultContent ?: next.displayAdultContent,
                displayAdultContentDirty = false,
            )
        }
        return next
    }

    private companion object {
        const val REFRESH_FAILED_MESSAGE = "Failed to refresh account settings"
        const val SAVE_FAILED_MESSAGE = "Failed to save account settings"
    }
}
