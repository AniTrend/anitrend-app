package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.user.model.UserSettingsRecord
import com.mxt.anitrend.graphql.generated.UpdateUserData

/**
 * Maps the generated `UpdateUser` mutation transport payload to the immutable
 * [UserSettingsRecord]. Absent nested blocks (`options`, `mediaListOptions`) and
 * absent scalar fields are preserved as null so consumers can distinguish
 * "unchanged by server" from explicit values.
 */
fun UpdateUserData.UpdateUser.toUserSettingsRecord(): UserSettingsRecord = UserSettingsRecord(
    id = id.toLong(),
    about = about,
    titleLanguage = options?.titleLanguage?.name,
    displayAdultContent = options?.displayAdultContent,
    airingNotifications = options?.airingNotifications,
    profileColor = options?.profileColor,
    scoreFormat = mediaListOptions?.scoreFormat?.name,
    rowOrder = mediaListOptions?.rowOrder,
)
