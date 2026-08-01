package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.StudioRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class StudioViewModel(
    private val studioRepository: StudioRepository,
    private val baseRepository: BaseRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val studio: com.mxt.anitrend.model.entity.base.StudioBase) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

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
            }.onFailure { throwable ->
                Timber.e(throwable, "StudioViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load studio",
                )
            }
        }
    }

    suspend fun toggleFavourite(
        animeId: Int?,
        mangaId: Int?,
        characterId: Int?,
        staffId: Int?,
        studioId: Int?,
    ): Result<Unit> = withContext(ioDispatcher) {
        baseRepository.toggleFavourite(animeId, mangaId, characterId, staffId, studioId)
    }

    fun isAuthenticated() = studioRepository.isAuthenticated()
}
