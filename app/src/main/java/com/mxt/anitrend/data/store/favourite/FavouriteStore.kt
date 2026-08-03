package com.mxt.anitrend.data.store.favourite

import com.mxt.anitrend.domain.favourite.model.FavouriteKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Transitional store scoped to favourite flags only.
 *
 * Holds the favourite flag per typed [FavouriteKey] with revision semantics so
 * stale mutation responses are rejected. This store is a compatibility bridge
 * toward per-domain aggregate stores (media, character, staff, studio) where
 * favourite state will eventually live; it must not accumulate entity data.
 * Removal is tracked in the state synchronisation and mutation refactor spec.
 */
interface FavouriteStore {
    val state: StateFlow<FavouriteStoreState>

    suspend fun apply(change: FavouriteStoreChange)

    suspend fun clear()

    fun observeFavourite(key: FavouriteKey): Flow<FavouriteFlag?>
}
