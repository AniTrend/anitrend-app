package com.mxt.anitrend.domain.model

import com.mxt.anitrend.domain.favourite.model.FavouriteKey

/**
 * Describes the intent to toggle the favourite flag of a typed favourite target
 * (anime, manga, character, staff, or studio).
 *
 * Data type only for now; repository calls and widget wiring are introduced by
 * a later phase.
 */
data class ToggleFavouriteCommand(
    val key: FavouriteKey,
)
