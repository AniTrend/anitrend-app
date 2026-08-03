package com.mxt.anitrend.data.store.favourite

import com.mxt.anitrend.domain.favourite.model.FavouriteKey

sealed interface FavouriteStoreChange {
    data class FavouriteFlagReplaced(
        val key: FavouriteKey,
        val isFavourite: Boolean,
        val revision: Long,
    ) : FavouriteStoreChange
}
