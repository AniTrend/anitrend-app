package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for character destinations.
 *
 * @property characterId Stable AniList character id; the destination resolves current state by this id.
 */
@Parcelize
data class CharacterScreenParam(
    val characterId: Long,
) : ScreenParam
