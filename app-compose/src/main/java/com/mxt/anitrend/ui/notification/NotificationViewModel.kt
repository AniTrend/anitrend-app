package com.mxt.anitrend.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.notification.AppNotification
import com.mxt.anitrend.data.notification.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed class NotificationUiState {
    data object Loading : NotificationUiState()
    data class Success(val items: List<AppNotification>) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

class NotificationViewModel(
    private val repository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState

    init {
        loadNotifications()
    }

    fun loadNotifications(page: Int = 1) {
        viewModelScope.launch {
            repository.observeNotifications(page)
                .onStart { _uiState.value = NotificationUiState.Loading }
                .catch { e -> _uiState.value = NotificationUiState.Error(e.message ?: "Unknown error") }
                .collect { items ->
                    _uiState.value = NotificationUiState.Success(items)
                }
        }
    }
}
