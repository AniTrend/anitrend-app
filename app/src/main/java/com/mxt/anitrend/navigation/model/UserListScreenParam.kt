package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for the user list bottom sheet (followers/following).
 *
 * @property userId Stable AniList user id whose connections are listed.
 * @property requestType Request discriminator selecting the connection query
 * (followers vs following).
 */
@Parcelize
data class UserListScreenParam(
    val userId: Long,
    val requestType: Int = 0,
) : ScreenParam
