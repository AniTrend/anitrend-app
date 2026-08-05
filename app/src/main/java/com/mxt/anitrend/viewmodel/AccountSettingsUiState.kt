package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.domain.user.model.UserSettingsRecord
import com.mxt.anitrend.domain.user.model.UserSettingsUpdate
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

/**
 * Constrained server-backed account setting values.
 *
 * The account screen edits server-authoritative settings through the bounded
 * `UpdateUser` mutation. Profile color, score format, title language and row
 * order are constrained values: setters only accept tokens from these sets,
 * never arbitrary free text.
 */
object AccountSettingsOptions {

    /** Score format used when the cached user has no media list options. */
    const val DEFAULT_SCORE_FORMAT: String = KeyUtil.POINT_10_DECIMAL

    /** Title language used when the cached user has no options. */
    const val DEFAULT_TITLE_LANGUAGE: String = KeyUtil.ROMAJI

    /** Media list row order selections supported by this screen (wire values). */
    const val ROW_ORDER_CUSTOM: String = "CUSTOM"

    val PROFILE_COLORS: Set<String> = setOf(
        KeyUtil.BLUE,
        KeyUtil.PURPLE,
        KeyUtil.PINK,
        KeyUtil.ORANGE,
        KeyUtil.RED,
        KeyUtil.GREEN,
        KeyUtil.GREY,
    )

    val SCORE_FORMATS: Set<String> = setOf(
        KeyUtil.POINT_100,
        KeyUtil.POINT_10_DECIMAL,
        KeyUtil.POINT_10,
        KeyUtil.POINT_5,
        KeyUtil.POINT_3,
    )

    val TITLE_LANGUAGES: Set<String> = setOf(
        KeyUtil.ROMAJI,
        KeyUtil.ENGLISH,
        KeyUtil.NATIVE,
        KeyUtil.ROMAJI_STYLISED,
        KeyUtil.ENGLISH_STYLISED,
        KeyUtil.NATIVE_STYLISED,
    )

    val ROW_ORDERS: Set<String> = setOf(ROW_ORDER_CUSTOM)
}

/**
 * The editable fields of the account settings form, used for sparse dirty
 * tracking: only fields present in [AccountSettingsUiState.dirtyFields] are
 * sent to the server on save.
 */
enum class AccountSettingsField {
    ABOUT,
    PROFILE_COLOR,
    SCORE_FORMAT,
    TITLE_LANGUAGE,
    ROW_ORDER,
    AIRING_NOTIFICATIONS,
    DISPLAY_ADULT_CONTENT,
}

/**
 * Immutable form state of the account settings screen.
 *
 * Values mirror the settings slice cached on the legacy [User] entity
 * (`about`, `options` and `mediaListOptions`), while the `*Dirty` flags track
 * which fields differ from the last committed server state. The ViewModel is
 * the only writer; fragments render this state and forward user actions.
 *
 * [from] seeds the form from the cached current user and is safe for a
 * missing cache (defaults are used).
 */
data class AccountSettingsUiState(
    val about: String = "",
    val profileColor: String? = null,
    val scoreFormat: String = AccountSettingsOptions.DEFAULT_SCORE_FORMAT,
    val titleLanguage: String = AccountSettingsOptions.DEFAULT_TITLE_LANGUAGE,
    val rowOrder: String? = null,
    val airingNotifications: Boolean = false,
    val displayAdultContent: Boolean = false,
    val aboutDirty: Boolean = false,
    val profileColorDirty: Boolean = false,
    val scoreFormatDirty: Boolean = false,
    val titleLanguageDirty: Boolean = false,
    val rowOrderDirty: Boolean = false,
    val airingNotificationsDirty: Boolean = false,
    val displayAdultContentDirty: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {

    /** Fields that differ from the committed server state. */
    val dirtyFields: Set<AccountSettingsField>
        get() = buildSet {
            if (aboutDirty) add(AccountSettingsField.ABOUT)
            if (profileColorDirty) add(AccountSettingsField.PROFILE_COLOR)
            if (scoreFormatDirty) add(AccountSettingsField.SCORE_FORMAT)
            if (titleLanguageDirty) add(AccountSettingsField.TITLE_LANGUAGE)
            if (rowOrderDirty) add(AccountSettingsField.ROW_ORDER)
            if (airingNotificationsDirty) add(AccountSettingsField.AIRING_NOTIFICATIONS)
            if (displayAdultContentDirty) add(AccountSettingsField.DISPLAY_ADULT_CONTENT)
        }

    /** True when at least one form field differs from the committed state. */
    val hasDirtyFields: Boolean
        get() = dirtyFields.isNotEmpty()

    /**
     * Entry point for building the form state from the cached current user.
     *
     * [from] is safe for a missing cache (defaults are used) and is the single
     * factory used both at construction time and when reseeding the form after
     * a refresh or discard.
     */
    companion object {

        /**
         * Seeds the form from the cached current user. A missing cache (for
         * example right after login or before the first refresh) produces a
         * default form with no dirty fields.
         */
        fun from(user: User?): AccountSettingsUiState {
            val options = user?.options
            val mediaListOptions = user?.mediaListOptions
            return AccountSettingsUiState(
                about = user?.about.orEmpty(),
                profileColor = options?.profileColor,
                scoreFormat = mediaListOptions?.scoreFormat ?: AccountSettingsOptions.DEFAULT_SCORE_FORMAT,
                titleLanguage = options?.titleLanguage ?: AccountSettingsOptions.DEFAULT_TITLE_LANGUAGE,
                rowOrder = mediaListOptions?.rowOrder,
                airingNotifications = options?.isAiringNotifications ?: false,
                displayAdultContent = options?.isDisplayAdultContent ?: false,
            )
        }
    }
}

/**
 * Updates the form through [MutableStateFlow.update], clearing
 * [AccountSettingsUiState.errorMessage] unless the reducer returned the same
 * instance (a no-op selection keeps the state fully unchanged).
 */
internal fun MutableStateFlow<AccountSettingsUiState>.updateDirty(
    reduce: (AccountSettingsUiState) -> AccountSettingsUiState,
) {
    update { current ->
        val reduced = reduce(current)
        // A no-op selection returns the same instance; keep the state fully
        // unchanged (dirty flags and error message) in that case.
        if (reduced === current) reduced else reduced.copy(errorMessage = null)
    }
}

/**
 * Atomically transitions the form into the loading phase.
 *
 * The guard is folded into a single [MutableStateFlow.getAndUpdate] call, so
 * concurrent [AccountSettingsViewModel.refresh] calls cannot both pass the
 * check before either one publishes the loading state.
 *
 * @return true when this caller performed the transition (the state was not
 * loading before); false when a refresh is already in flight.
 */
internal fun MutableStateFlow<AccountSettingsUiState>.beginRefreshIfIdle(): Boolean {
    val previous = getAndUpdate { state ->
        if (state.isLoading) state else state.copy(isLoading = true, errorMessage = null)
    }
    return !previous.isLoading
}

/**
 * Builds the sparse `UpdateUser` wire payload containing only the dirty fields.
 */
internal fun AccountSettingsUiState.toSparseUserSettingsUpdate(): UserSettingsUpdate = UserSettingsUpdate(
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
 * Fields edited again while the save was in flight keep their current values
 * unless they were part of the saved set; non-saved fields are untouched.
 * Null record values preserve the current form value.
 */
internal fun AccountSettingsUiState.mergeServerRecord(
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
