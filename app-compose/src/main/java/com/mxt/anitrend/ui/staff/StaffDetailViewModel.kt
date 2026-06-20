package com.mxt.anitrend.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.staff.StaffDetail
import com.mxt.anitrend.data.staff.StaffRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed class StaffDetailUiState {
    data object Loading : StaffDetailUiState()
    data class Success(val staff: StaffDetail) : StaffDetailUiState()
    data class Error(val message: String) : StaffDetailUiState()
}

class StaffDetailViewModel(
    private val repository: StaffRepository,
    private val staffId: Int,
) : ViewModel() {
    private val _uiState = MutableStateFlow<StaffDetailUiState>(StaffDetailUiState.Loading)
    val uiState: StateFlow<StaffDetailUiState> = _uiState
    init { load() }
    private fun load() {
        viewModelScope.launch {
            repository.observeStaff(staffId)
                .onStart { _uiState.value = StaffDetailUiState.Loading }
                .catch { e -> _uiState.value = StaffDetailUiState.Error(e.message ?: "Error") }
                .collect { if (it != null) _uiState.value = StaffDetailUiState.Success(it) else _uiState.value = StaffDetailUiState.Error("Not found") }
        }
    }
}
