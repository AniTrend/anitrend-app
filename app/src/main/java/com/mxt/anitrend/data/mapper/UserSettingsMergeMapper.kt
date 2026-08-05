package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.UpdateUserData
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.UserOptions

/**
 * Applies the settings slice returned by the `UpdateUser` mutation to the
 * cached [User] entity.
 *
 * Only the fields returned by the mutation are touched: `name`, `avatar`,
 * `bannerImage`, `about`, `options` and `mediaListOptions`. Everything the
 * mutation does not return (`stats`, `statistics`, `unreadNotificationCount`,
 * `isFollowing`, `updatedAt` and the `mediaListOptions` inner blocks) is
 * preserved untouched.
 *
 * Null semantics:
 * - Top-level scalars and a present `options` block are applied verbatim
 *   (null means the server value is null, e.g. a cleared `about`).
 * - `mediaListOptions` only returns `scoreFormat` and `rowOrder`, so only
 *   those two values are replaced; a null subfield preserves the cached one.
 * - An absent `options` / `mediaListOptions` block leaves the cached block
 *   untouched.
 */
fun UpdateUserData.UpdateUser.applyUserSettingsTo(cached: User) {
    cached.name = name
    cached.avatar = avatar?.let { avatar ->
        ImageBase(extraLarge = null, large = avatar.large, medium = avatar.medium)
    }
    cached.bannerImage = bannerImage
    cached.about = about
    options?.let { serverOptions ->
        val existingOptions = cached.options
        cached.options = UserOptions(
            titleLanguage = serverOptions.titleLanguage?.name,
            isDisplayAdultContent = serverOptions.displayAdultContent ?: existingOptions?.isDisplayAdultContent ?: false,
            isAiringNotifications = serverOptions.airingNotifications ?: existingOptions?.isAiringNotifications ?: false,
            profileColor = serverOptions.profileColor,
        )
    }
    mediaListOptions?.let { serverMediaListOptions ->
        serverMediaListOptions.scoreFormat?.let { scoreFormat ->
            cached.mediaListOptions.scoreFormat = scoreFormat.name
        }
        serverMediaListOptions.rowOrder?.let { rowOrder ->
            cached.mediaListOptions.rowOrder = rowOrder
        }
    }
}
