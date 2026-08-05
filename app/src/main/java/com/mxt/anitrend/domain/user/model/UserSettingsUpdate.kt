package com.mxt.anitrend.domain.user.model

/** Nullable fields sent to AniList's UpdateUser mutation. */
data class UserSettingsUpdate(
    val about: String? = null,
    val airingNotifications: Boolean? = null,
    val displayAdultContent: Boolean? = null,
    val profileColor: String? = null,
    val rowOrder: String? = null,
    val scoreFormat: String? = null,
    val titleLanguage: String? = null,
)
