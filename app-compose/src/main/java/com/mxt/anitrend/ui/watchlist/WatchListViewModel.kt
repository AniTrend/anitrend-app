package com.mxt.anitrend.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.watchlist.WatchListGroup
import com.mxt.anitrend.data.watchlist.WatchListRepository
import com.mxt.anitrend.data.watchlist.WatchMediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed class WatchListUiState {
    data object Loading : WatchListUiState()
    data class Success(val groups: List<WatchListGroup>) : WatchListUiState()
    data class Error(val message: String) : WatchListUiState()
}

class WatchListViewModel(
    private val repository: WatchListRepository,
) : ViewModel() {

    private val _selectedType = MutableStateFlow(WatchMediaType.ANIME)
    val selectedType: StateFlow<WatchMediaType> = _selectedType

    private val _uiState = MutableStateFlow<WatchListUiState>(WatchListUiState.Loading)
    val uiState: StateFlow<WatchListUiState> = _uiState

    init {
        load()
    }

    fun selectType(type: WatchMediaType) {
        _selectedType.value = type
        load()
    }

    fun load() {
        viewModelScope.launch {
            repository.observeWatchList(_selectedType.value)
                .onStart { _uiState.value = WatchListUiState.Loading }
                .catch { e -> _uiState.value = WatchListUiState.Error(e.message ?: "Unknown error") }
                .collect { groups ->
                    _uiState.value = WatchListUiState.Success(groups)
                }
        }
    }
}
