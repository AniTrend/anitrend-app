package com.mxt.anitrend.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.schedule.AiringItem
import com.mxt.anitrend.data.schedule.AiringRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed class AiringUiState {
    data object Loading : AiringUiState()
    data class Success(val items: List<AiringItem>) : AiringUiState()
    data class Error(val message: String) : AiringUiState()
}

class AiringViewModel(
    private val repository: AiringRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiringUiState>(AiringUiState.Loading)
    val uiState: StateFlow<AiringUiState> = _uiState

    init {
        loadSchedule()
    }

    fun loadSchedule() {
        viewModelScope.launch {
            repository.observeSchedule()
                .onStart { _uiState.value = AiringUiState.Loading }
                .catch { e -> _uiState.value = AiringUiState.Error(e.message ?: "Unknown error") }
                .collect { items -> _uiState.value = AiringUiState.Success(items) }
        }
    }
}
