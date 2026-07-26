package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class UserListViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val container: PageContainer<UserBase>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun loadFollowers(
        userId: Long,
        page: Int,
        perPage: Int,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                userRepository.getFollowers(
                    id = userId,
                    page = page,
                    perPage = perPage,
                ).getOrThrow()
            }.onSuccess { container ->
                _state.value = UiState.Success(container)
            }.onFailure { throwable ->
                Timber.e(throwable, "UserListViewModel followers failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load followers",
                )
            }
        }
    }

    fun loadFollowing(
        userId: Long,
        page: Int,
        perPage: Int,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                userRepository.getFollowing(
                    id = userId,
                    page = page,
                    perPage = perPage,
                ).getOrThrow()
            }.onSuccess { container ->
                _state.value = UiState.Success(container)
            }.onFailure { throwable ->
                Timber.e(throwable, "UserListViewModel following failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load following",
                )
            }
        }
    }
}
