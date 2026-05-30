package com.mxt.anitrend.ui.genre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.genre.GenreItem
import com.mxt.anitrend.data.genre.GenreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed class GenreListUiState {
    data object Loading : GenreListUiState()
    data class Success(val items: List<GenreItem>) : GenreListUiState()
    data class Error(val message: String) : GenreListUiState()
}

class GenreListViewModel(
    private val repository: GenreRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GenreListUiState>(GenreListUiState.Loading)
    val uiState: StateFlow<GenreListUiState> = _uiState

    init {
        loadGenres()
    }

    fun loadGenres() {
        viewModelScope.launch {
            repository.observeGenres()
                .onStart { _uiState.value = GenreListUiState.Loading }
                .catch { e -> _uiState.value = GenreListUiState.Error(e.message ?: "Unknown error") }
                .collect { items -> _uiState.value = GenreListUiState.Success(items) }
        }
    }
}
