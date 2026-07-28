package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.repository.StaffRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class StaffOverviewViewModel(
    private val staffRepository: StaffRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val staff: StaffBase) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var loadedOnce = false

    /**
     * Loads the staff overview by AniList ID. After the first successful load,
     * subsequent calls are ignored until a new ViewModel instance is created.
     * Failed loads remain retryable.
     */
    fun load(staffId: Long) {
        if (loadedOnce) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                staffRepository.getStaffOverview(id = staffId, asHtml = false).getOrThrow()
            }.onSuccess { staff ->
                _state.value = UiState.Success(staff)
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "StaffOverviewViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load staff overview",
                )
            }
        }
    }
}
