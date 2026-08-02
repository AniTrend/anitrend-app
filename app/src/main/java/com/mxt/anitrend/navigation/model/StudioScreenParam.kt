package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for studio destinations.
 *
 * @property studioId Stable AniList studio id; the destination resolves current state by this id.
 */
@Parcelize
data class StudioScreenParam(
    val studioId: Long,
) : ScreenParam
