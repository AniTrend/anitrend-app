package com.mxt.anitrend.ui.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.favourite.FavouriteRepository
import com.mxt.anitrend.data.favourite.MediaListGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed class FavouritesUiState {
    data object Loading : FavouritesUiState()
    data class Success(val groups: List<MediaListGroup>) : FavouritesUiState()
    data class Error(val message: String) : FavouritesUiState()
}

class FavouritesViewModel(
    private val repository: FavouriteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavouritesUiState>(FavouritesUiState.Loading)
    val uiState: StateFlow<FavouritesUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            repository.observeMediaListCollection()
                .onStart { _uiState.value = FavouritesUiState.Loading }
                .catch { e -> _uiState.value = FavouritesUiState.Error(e.message ?: "Unknown error") }
                .collect { groups ->
                    _uiState.value = FavouritesUiState.Success(groups)
                }
        }
    }
}
