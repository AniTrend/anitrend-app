package com.mxt.anitrend.domain.favourite.model

/**
 * Typed identity of a favourite target.
 *
 * This key is deliberately scoped to favourite flags only and is shared by the
 * transitional [com.mxt.anitrend.data.store.favourite.FavouriteStore] and
 * [com.mxt.anitrend.domain.model.ToggleFavouriteCommand]. It is a compatibility
 * key on the path toward per-domain aggregate stores; when favourite state is
 * folded into those stores this type is removed together with the transitional
 * store. See the state synchronisation and mutation refactor spec for the
 * removal phase.
 */
sealed interface FavouriteKey {
    val id: Long

    data class Anime(override val id: Long) : FavouriteKey

    data class Manga(override val id: Long) : FavouriteKey

    data class Character(override val id: Long) : FavouriteKey

    data class Staff(override val id: Long) : FavouriteKey

    data class Studio(override val id: Long) : FavouriteKey
}
