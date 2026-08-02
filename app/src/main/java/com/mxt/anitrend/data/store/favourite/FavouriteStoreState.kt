package com.mxt.anitrend.data.store.favourite

import com.mxt.anitrend.domain.favourite.model.FavouriteKey

data class FavouriteFlag(
    val key: FavouriteKey,
    val isFavourite: Boolean,
    val revision: Long,
)

data class FavouriteStoreState(
    val flagsByKey: Map<FavouriteKey, FavouriteFlag> = emptyMap(),
)
