package com.mxt.anitrend.domain.user.model

/**
 * Immutable canonical representation of the server-backed user settings slice.
 *
 * Mirrors the fields already read by the `CurrentUser` operation and stored on the
 * legacy entity (`User.options` / `User.mediaListOptions`): enum-backed values are
 * carried as the wire representation strings (enum `.name`), matching the legacy
 * `UserOptions.titleLanguage` and `MediaListOptions.scoreFormat` string fields and
 * the [com.mxt.anitrend.data.mapper.UserStatisticsRecordMapper] lane convention.
 *
 * The transport mapper lives in `com.mxt.anitrend.data.mapper.UserSettingsRecordMapper`.
 */
data class UserSettingsRecord(
    val id: Long,
    val about: String?,
    val titleLanguage: String?,
    val displayAdultContent: Boolean?,
    val airingNotifications: Boolean?,
    val profileColor: String?,
    val scoreFormat: String?,
    val rowOrder: String?,
)
