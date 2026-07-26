package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaStatsViewModel(
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val media: Media) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var loadedOnce = false

    /**
     * Loads media stats. After the first successful load, subsequent calls are
     * ignored until a new ViewModel instance is created. Failed loads remain retryable.
     */
    fun load(mediaId: Long, type: MediaType?, isAdult: Boolean?) {
        if (loadedOnce) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                mediaRepository.getMediaStats(
                    id = mediaId,
                    type = type,
                    isAdult = isAdult,
                ).getOrThrow()
            }.onSuccess { media ->
                _state.value = UiState.Success(media)
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaStatsViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media stats",
                )
            }
        }
    }
}
