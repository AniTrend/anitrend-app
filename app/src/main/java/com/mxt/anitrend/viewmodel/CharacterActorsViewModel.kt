package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.repository.CharacterRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CharacterActorsViewModel(
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: ConnectionContainer<EdgeContainer<MediaEdge>>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads character actors with their media roles. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(id: Long, page: Int) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                characterRepository.getCharacterActors(
                    id = id,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "CharacterActorsViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load character actors",
                )
            }
        }
    }
}
