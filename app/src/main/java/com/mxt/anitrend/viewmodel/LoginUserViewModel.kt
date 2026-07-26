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
 * Fetches the currently authenticated user profile after OAuth login succeeds.
 * This is a direct replacement for the legacy current-user request path.
 */
class LoginUserViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val user: User) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var loadedOnce = false

    fun loadCurrentUser() {
        if (loadedOnce) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                userRepository.getCurrentUser(asHtml = false).getOrThrow()
            }.onSuccess { user ->
                _state.value = UiState.Success(user)
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "LoginUserViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load current user",
                )
            }
        }
    }
}
