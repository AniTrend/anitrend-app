package com.mxt.anitrend.ui.activitydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.social.ActivityDetail
import com.mxt.anitrend.data.social.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed class ActivityDetailUiState {
    data object Loading : ActivityDetailUiState()
    data class Success(val activity: ActivityDetail) : ActivityDetailUiState()
    data class Error(val message: String) : ActivityDetailUiState()
}

class ActivityDetailViewModel(
    private val activityRepository: ActivityRepository,
    private val activityId: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivityDetailUiState>(ActivityDetailUiState.Loading)
    val uiState: StateFlow<ActivityDetailUiState> = _uiState

    init {
        loadActivity()
    }

    fun loadActivity() {
        viewModelScope.launch {
            activityRepository.observeActivity(activityId)
                .onStart { _uiState.value = ActivityDetailUiState.Loading }
                .catch { e -> _uiState.value = ActivityDetailUiState.Error(e.message ?: "Unknown error") }
                .collect { detail ->
                    if (detail != null) {
                        _uiState.value = ActivityDetailUiState.Success(detail)
                    } else {
                        _uiState.value = ActivityDetailUiState.Error("Activity not found")
                    }
                }
        }
    }
}
