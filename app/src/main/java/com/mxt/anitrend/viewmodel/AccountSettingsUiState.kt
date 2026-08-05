package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.util.KeyUtil

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
