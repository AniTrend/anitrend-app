package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Owns the current-user fetch for the main navigation shell.
 * Replaces the legacy current-user request path.
 */
class MainViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val user: User) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Fetches the currently authenticated user. Does not throttle -- the
     * caller (MainActivity) is expected to apply its own 5-minute throttle
     * via [com.mxt.anitrend.presenter.base.BasePresenter.updateUserLastSyncTimeStampIf].
     */
    fun loadCurrentUser() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                userRepository.getCurrentUser(asHtml = false).getOrThrow()
            }.onSuccess { user ->
                _state.value = UiState.Success(user)
            }.onFailure { throwable ->
                Timber.e(throwable, "MainViewModel current user fetch failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load current user",
                )
            }
        }
    }

    fun currentUser() = userRepository.cachedCurrentUser
}
