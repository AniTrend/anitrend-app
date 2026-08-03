package com.mxt.anitrend.data.store.favourite

import com.mxt.anitrend.domain.favourite.model.FavouriteKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryFavouriteStore : FavouriteStore {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(FavouriteStoreState())

    override val state: StateFlow<FavouriteStoreState> = mutableState.asStateFlow()

    override suspend fun apply(change: FavouriteStoreChange) {
        mutex.withLock {
            mutableState.value = when (change) {
                is FavouriteStoreChange.FavouriteFlagReplaced -> reduceFavouriteFlagReplaced(change)
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            mutableState.value = FavouriteStoreState()
        }
    }

    override fun observeFavourite(key: FavouriteKey): Flow<FavouriteFlag?> = state.map { it.flagsByKey[key] }.distinctUntilChanged()

    private fun reduceFavouriteFlagReplaced(change: FavouriteStoreChange.FavouriteFlagReplaced): FavouriteStoreState {
        val currentState = mutableState.value
        val currentRevision = currentState.flagsByKey[change.key]?.revision ?: Long.MIN_VALUE
        if (change.revision < currentRevision) {
            return currentState
        }
        return currentState.copy(
            flagsByKey = currentState.flagsByKey.toMutableMap().apply {
                put(
                    change.key,
                    FavouriteFlag(
                        key = change.key,
                        isFavourite = change.isFavourite,
                        revision = change.revision,
                    ),
                )
            },
        )
    }
}
