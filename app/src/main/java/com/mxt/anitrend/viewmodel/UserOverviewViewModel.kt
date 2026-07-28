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

class UserOverviewViewModel(
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

    /**
     * Loads the user overview by AniList ID or username. After the first successful load,
     * subsequent calls are ignored until a new ViewModel instance is created.
     * Failed loads remain retryable.
     */
    fun load(userId: Long = 0, userName: String = "") {
        if (loadedOnce) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                userRepository.getUserOverview(
                    id = if (userId > 0) userId else null,
                    userName = if (userName.isNotBlank()) userName else null,
                    asHtml = false,
                ).getOrThrow()
            }.onSuccess { user ->
                _state.value = UiState.Success(user)
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "UserOverviewViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load user overview",
                )
            }
        }
    }
}
