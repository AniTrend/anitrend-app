package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.RecommendationBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaRecommendationsViewModel(
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: ConnectionContainer<PageContainer<RecommendationBase>>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads media recommendations. Not paginated by the fragment; no loadedOnce guard.
     */
    fun load(mediaId: Long, type: MediaType?, isAdult: Boolean?) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                mediaRepository.getMediaRecommendations(
                    id = mediaId,
                    type = type,
                    isAdult = isAdult,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaRecommendationsViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load recommendations",
                )
            }
        }
    }
}
