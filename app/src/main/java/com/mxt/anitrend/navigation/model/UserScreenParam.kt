package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for user destinations.
 *
 * @property userId Stable AniList user id; the destination resolves current state by this id.
 * @property initialName Optional display name shown before data loads.
 */
@Parcelize
data class UserScreenParam(
    val userId: Long,
    val initialName: String? = null,
) : ScreenParam
