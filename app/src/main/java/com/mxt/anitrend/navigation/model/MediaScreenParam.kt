package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for media destinations.
 *
 * @property mediaId Stable AniList media id; the destination resolves current state by this id.
 * @property mediaType Optional media type hint (ANIME or MANGA) used before data loads.
 */
@Parcelize
data class MediaScreenParam(
    val mediaId: Long,
    val mediaType: String? = null,
) : ScreenParam
