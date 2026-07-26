package com.mxt.anitrend.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.StudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class StudioMediaViewModel(
    private val studioRepository: StudioRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val container: ConnectionContainer<PageContainer<MediaBase>>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads studio media by AniList ID with pagination and sort.
     * @param sort the combined sort key from settings (e.g. "POPULARITY_DESC").
     */
    fun load(
        studioId: Long,
        page: Int,
        perPage: Int,
        sort: String?,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                studioRepository.getStudioMedia(
                    id = studioId,
                    page = page,
                    perPage = perPage,
                    sort = resolveMediaSort(sort),
                ).getOrThrow()
            }.onSuccess { container ->
                _state.value = UiState.Success(container)
            }.onFailure { throwable ->
                Timber.e(throwable, "StudioMediaViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load studio media",
                )
            }
        }
    }

    @VisibleForTesting
    internal fun resolveMediaSort(sort: String?): kotlin.collections.List<MediaSort> = sort?.let {
        runCatching {
            listOf(MediaSort.valueOf(it))
        }.getOrDefault(listOf(MediaSort.POPULARITY))
    } ?: listOf(MediaSort.POPULARITY)
}
