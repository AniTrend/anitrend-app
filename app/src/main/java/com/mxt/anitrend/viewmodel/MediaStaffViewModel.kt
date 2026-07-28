package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaStaffViewModel(
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: ConnectionContainer<EdgeContainer<StaffEdge>>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads media staff. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(mediaId: Long, type: MediaType?, page: Int, isAdult: Boolean?) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                mediaRepository.getMediaStaff(
                    id = mediaId,
                    type = type,
                    isAdult = isAdult,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaStaffViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media staff",
                )
            }
        }
    }
}
