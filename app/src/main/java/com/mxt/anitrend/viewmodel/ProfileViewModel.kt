package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.UserBase
import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ProfileViewModel(
    private val userService: UserModel,
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
                withContext(ioDispatcher) {
                    val idParam: Int? = if (userId > 0) userId.toInt() else null
                    val request = UserBase.request(id = idParam, userName = userName)
                    val response = userService.getUserBase(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body()
                            ?: throw IllegalStateException("Empty response body")
                        val graphErrors: List<GraphError>? = body.errors
                        if (!graphErrors.isNullOrEmpty()) {
                            throw RuntimeException(
                                graphErrors.first().message
                                    ?: "GraphQL error",
                            )
                        }
                        body.data?.result
                            ?: throw IllegalStateException("Empty response body")
                    } else {
                        throw RuntimeException(response.apiError())
                    }
                }
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
}
