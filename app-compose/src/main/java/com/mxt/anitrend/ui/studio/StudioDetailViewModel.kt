package com.mxt.anitrend.ui.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.studio.StudioDetail
import com.mxt.anitrend.data.studio.StudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed class StudioDetailUiState {
    data object Loading : StudioDetailUiState()
    data class Success(val studio: StudioDetail) : StudioDetailUiState()
    data class Error(val message: String) : StudioDetailUiState()
}

class StudioDetailViewModel(
    private val repository: StudioRepository,
    private val studioId: Int,
) : ViewModel() {
    private val _uiState = MutableStateFlow<StudioDetailUiState>(StudioDetailUiState.Loading)
    val uiState: StateFlow<StudioDetailUiState> = _uiState
    init { load() }
    private fun load() {
        viewModelScope.launch {
            repository.observeStudio(studioId)
                .onStart { _uiState.value = StudioDetailUiState.Loading }
                .catch { e -> _uiState.value = StudioDetailUiState.Error(e.message ?: "Error") }
                .collect { if (it != null) _uiState.value = StudioDetailUiState.Success(it) else _uiState.value = StudioDetailUiState.Error("Not found") }
        }
    }
}
