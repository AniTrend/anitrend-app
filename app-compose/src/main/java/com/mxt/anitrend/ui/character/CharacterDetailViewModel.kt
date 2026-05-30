package com.mxt.anitrend.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.character.CharacterDetail
import com.mxt.anitrend.data.character.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed class CharacterDetailUiState {
    data object Loading : CharacterDetailUiState()
    data class Success(val character: CharacterDetail) : CharacterDetailUiState()
    data class Error(val message: String) : CharacterDetailUiState()
}

class CharacterDetailViewModel(
    private val repository: CharacterRepository,
    private val characterId: Int,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CharacterDetailUiState>(CharacterDetailUiState.Loading)
    val uiState: StateFlow<CharacterDetailUiState> = _uiState

    init { load() }

    private fun load() {
        viewModelScope.launch {
            repository.observeCharacter(characterId)
                .onStart { _uiState.value = CharacterDetailUiState.Loading }
                .catch { e -> _uiState.value = CharacterDetailUiState.Error(e.message ?: "Error") }
                .collect {
                    if (it != null) _uiState.value = CharacterDetailUiState.Success(it)
                    else _uiState.value = CharacterDetailUiState.Error("Character not found")
                }
        }
    }
}
