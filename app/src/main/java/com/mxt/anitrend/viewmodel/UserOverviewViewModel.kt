package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.store.user.UserStore
import com.mxt.anitrend.domain.model.ToggleUserFollowCommand
import com.mxt.anitrend.domain.user.interactor.ToggleUserFollowInteractor
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class UserOverviewViewModel(
    private val userRepository: UserRepository,
    private val toggleUserFollowInteractor: ToggleUserFollowInteractor,
    private val userStore: UserStore,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val user: User) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Committed follow state of the displayed profile, derived from the canonical
     * [UserStore]. Null means no committed record exists yet, so the server-loaded
     * follow value from the overview response stays the fallback.
     */
    private val _isFollowing = MutableStateFlow<Boolean?>(null)
    val isFollowing: StateFlow<Boolean?> = _isFollowing.asStateFlow()

    /** Snapshot of the authenticated user used for render-only widget wiring. */
    val currentUserSnapshot: UserBase?
        get() = userRepository.cachedCurrentUser

    private var loadedOnce = false
    private var displayedUserId: Long = 0L
    private var followObservationJob: Job? = null

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
                observeCommittedFollowState(user.id)
            }.onFailure { throwable ->
                Timber.e(throwable, "UserOverviewViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load user overview",
                )
            }
        }
    }

    /**
     * Observes the displayed profile's committed follow state in [UserStore]. A previous
     * observation is cancelled and replaced. Null records are ignored so the server-loaded
     * follow value remains the fallback, and records for other users are rejected defensively.
     */
    private fun observeCommittedFollowState(userId: Long) {
        if (userId <= 0L) return
        displayedUserId = userId
        _isFollowing.value = null
        followObservationJob?.cancel()
        followObservationJob = viewModelScope.launch {
            userStore.observeUser(userId).collect { record ->
                if (record == null || record.id != userId || userId != displayedUserId) {
                    return@collect
                }
                _isFollowing.value = record.isFollowing
            }
        }
    }

    /**
     * Fire-and-forget follow toggle for the displayed profile. IDs that do not match the
     * currently displayed profile are ignored; the authoritative committed state is
     * delivered back through [isFollowing].
     */
    fun toggleFollow(userId: Long) {
        if (userId <= 0L || userId != displayedUserId) return
        viewModelScope.launch {
            toggleUserFollowInteractor(ToggleUserFollowCommand(userId = userId))
        }
    }
}
