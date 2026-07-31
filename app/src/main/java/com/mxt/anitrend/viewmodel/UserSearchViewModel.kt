package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.UserSort
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.repository.SearchRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class UserSearchViewModel(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<UserBase>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads user search results. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(search: String?, page: Int) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                searchRepository.searchUser(
                    search = search,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    sort = listOf(UserSort.SEARCH_MATCH),
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "UserSearchViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load user search",
                )
            }
        }
    }

    fun toggleFollow(
        userId: Long,
        onResult: (Result<UserBase>) -> Unit,
    ) {
        viewModelScope.launch {
            onResult(userRepository.toggleFollow(userId))
        }
    }
}
