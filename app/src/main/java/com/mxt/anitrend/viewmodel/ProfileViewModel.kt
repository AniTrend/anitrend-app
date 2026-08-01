package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val user: com.mxt.anitrend.model.entity.base.UserBase) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var loadedOnce = false

    /**
     * Loads the user by AniList ID or username. At least one of [userId] or
     * [userName] should be set. Safe to call multiple times -- skips the
     * network call after the first successful load.
     */
    fun load(
        userId: Long,
        userName: String?,
    ) {
        if (loadedOnce) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                userRepository.getUserBase(
                    id = if (userId > 0) userId else null,
                    userName = userName,
                ).getOrThrow()
            }.onSuccess { user ->
                _state.value = UiState.Success(user)
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "ProfileViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load user profile",
                )
            }
        }
    }

    suspend fun loadStats(
        userId: Long,
        userName: String?,
    ): Result<UserStatisticTypes> = withContext(ioDispatcher) {
        userRepository.getUserStats(
            id = if (userId > 0) userId else null,
            userName = userName,
        )
            .mapCatching { connectionContainer ->
                connectionContainer.connection
            }
    }

    fun isCurrentUser(userId: Long, userName: String?) = userRepository.isCurrentUser(
        userId = userId,
        userName = userName,
    )
}
