package com.mxt.anitrend.domain.user.model

import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.graphql.generated.UserTitleLanguage

/** Nullable fields sent to AniList's UpdateUser mutation. */
data class UserSettingsUpdate(
    val about: String? = null,
    val airingNotifications: Boolean? = null,
    val displayAdultContent: Boolean? = null,
    val profileColor: String? = null,
    val rowOrder: String? = null,
    val scoreFormat: ScoreFormat? = null,
    val titleLanguage: UserTitleLanguage? = null,
)
