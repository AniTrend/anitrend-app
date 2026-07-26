package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CharacterFavouritesViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: ConnectionContainer<Favourite>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads character favourites. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(userId: Long, page: Int) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                userRepository.getCharacterFavourites(
                    id = userId,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "CharacterFavouritesViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load character favourites",
                )
            }
        }
    }
}
