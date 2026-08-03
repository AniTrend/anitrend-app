package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.domain.model.NotificationPageResult
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: NotificationPageResult) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads user notifications. Repeatable for pagination; no loadedOnce guard.
     *
     * The repository boundary returns immutable [NotificationPageResult] records;
     * the fragment projects them into read-aware UI models and owns the local
     * `NotificationHistory` read state.
     */
    fun load(page: Int) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                userRepository.getUserNotifications(
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    resetNotificationCount = true,
                ).getOrThrow()
            }.onSuccess { result ->
                _state.value = UiState.Success(result)
            }.onFailure { throwable ->
                Timber.e(throwable, "NotificationViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load notifications",
                )
            }
        }
    }
}
