package com.mxt.anitrend.ui.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.forum.ThreadItem
import com.mxt.anitrend.data.forum.ThreadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed class ThreadsUiState {
    data object Loading : ThreadsUiState()
    data class Success(val items: List<ThreadItem>) : ThreadsUiState()
    data class Error(val message: String) : ThreadsUiState()
}

class ThreadsViewModel(
    private val repository: ThreadRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ThreadsUiState>(ThreadsUiState.Loading)
    val uiState: StateFlow<ThreadsUiState> = _uiState

    init {
        loadThreads()
    }

    fun loadThreads() {
        viewModelScope.launch {
            repository.observeThreads()
                .onStart { _uiState.value = ThreadsUiState.Loading }
                .catch { e -> _uiState.value = ThreadsUiState.Error(e.message ?: "Unknown error") }
                .collect { items -> _uiState.value = ThreadsUiState.Success(items) }
        }
    }
}
