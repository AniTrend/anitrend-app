package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.store.favourite.FavouriteFlag
import com.mxt.anitrend.data.store.favourite.FavouriteStore
import com.mxt.anitrend.data.store.favourite.FavouriteStoreChange
import com.mxt.anitrend.domain.favourite.interactor.ToggleFavouriteInteractor
import com.mxt.anitrend.domain.favourite.model.FavouriteKey
import com.mxt.anitrend.domain.model.StudioRecord
import com.mxt.anitrend.domain.model.ToggleFavouriteCommand
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.StudioRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class StudioViewModel(
    private val studioRepository: StudioRepository,
    private val baseRepository: BaseRepository,
    private val favouriteStore: FavouriteStore,
    private val toggleFavouriteInteractor: ToggleFavouriteInteractor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val studio: StudioRecord) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Committed favourite flag for the loaded studio, mirrored from the canonical
     * [FavouriteStore]. Null until the studio has loaded and the store has been observed.
     */
    private val _favouriteFlag = MutableStateFlow<FavouriteFlag?>(null)
    val favouriteFlag: StateFlow<FavouriteFlag?> = _favouriteFlag.asStateFlow()

    /**
     * True while a favourite toggle for the loaded studio is in flight (server call plus
     * store commit). The widget shows its bounded loading state from this flag.
     */
    private val _favouriteLoading = MutableStateFlow(false)
    val favouriteLoading: StateFlow<Boolean> = _favouriteLoading.asStateFlow()

    private var loadedOnce = false

    /**
     * Loads the studio by its AniList ID. Safe to call multiple times — skips
     * the network call after the first successful load.
     */
    fun load(studioId: Long) {
        if (loadedOnce) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                studioRepository.getStudioBase(id = studioId).getOrThrow()
            }.onSuccess { studio ->
                _state.value = UiState.Success(studio)
                loadedOnce = true
                seedFavouriteFlag(studio.id, studio.isFavourite)
                observeFavouriteFlag(studioId)
            }.onFailure { throwable ->
                Timber.e(throwable, "StudioViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load studio",
                )
            }
        }
    }

    /**
     * Fire-and-forget favourite toggle for the loaded studio. Runs the typed
     * [ToggleFavouriteCommand] through [ToggleFavouriteInteractor]; the committed result
     * surfaces via [favouriteFlag] once the canonical store applies it.
     */
    fun toggleFavouriteStudio(studioId: Long) {
        viewModelScope.launch {
            _favouriteLoading.value = true
            try {
                toggleFavouriteInteractor(ToggleFavouriteCommand(FavouriteKey.Studio(studioId)))
            } finally {
                _favouriteLoading.value = false
            }
        }
    }

    /**
     * Seeds the canonical store from the initially loaded [StudioRecord.isFavourite] only when
     * no committed store value exists yet. The record is never mutated.
     */
    private suspend fun seedFavouriteFlag(studioId: Long, isFavourite: Boolean) {
        val key = FavouriteKey.Studio(studioId)
        if (favouriteStore.state.value.flagsByKey[key] == null) {
            favouriteStore.apply(
                FavouriteStoreChange.FavouriteFlagReplaced(
                    key = key,
                    isFavourite = isFavourite,
                    revision = FAVOURITE_SEED_REVISION,
                ),
            )
        }
    }

    private fun observeFavouriteFlag(studioId: Long) {
        viewModelScope.launch {
            favouriteStore.observeFavourite(FavouriteKey.Studio(studioId)).collect { flag ->
                _favouriteFlag.value = flag
            }
        }
    }

    fun isAuthenticated() = studioRepository.isAuthenticated()

    companion object {
        /**
         * Revision used when seeding the store from the initially loaded entity flag. It is
         * lower than the first mutation revision so a committed mutation always wins, and the
         * seed is only applied when no committed value exists for the key.
         */
        internal const val FAVOURITE_SEED_REVISION = 0L
    }
}
