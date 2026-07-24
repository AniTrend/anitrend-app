package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.UserFollowers
import com.mxt.anitrend.graphql.generated.UserFollowing
import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class UserListViewModel(
    private val userService: UserModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
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
                withContext(ioDispatcher) {
                    val request = UserFollowers.request(
                        id = userId.toInt(),
                        page = page,
                        perPage = perPage,
                    )
                    val response = userService.getFollowers(request).execute()
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
                withContext(ioDispatcher) {
                    val request = UserFollowing.request(
                        id = userId.toInt(),
                        page = page,
                        perPage = perPage,
                    )
                    val response = userService.getFollowing(request).execute()
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
