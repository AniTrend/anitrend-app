package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for user destinations.
 *
 * @property userId Stable AniList user id; the destination resolves current state by this id.
 * @property initialName Optional user name. Shown before data loads and used as the
 * lookup identity when [userId] is absent (e.g. name-based deep links).
 */
@Parcelize
data class UserScreenParam(
    val userId: Long,
    val initialName: String? = null,
) : ScreenParam
