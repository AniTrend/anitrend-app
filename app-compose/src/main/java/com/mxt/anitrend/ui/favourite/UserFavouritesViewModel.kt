package com.mxt.anitrend.ui.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.favourite.FavEntity
import com.mxt.anitrend.data.favourite.FavMedia
import com.mxt.anitrend.data.favourite.FavStudio
import com.mxt.anitrend.data.favourite.FavouriteRepository
import com.mxt.anitrend.data.favourite.UserFavourites
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

enum class FavouriteTab { Anime, Manga, Characters, Staff, Studios }

sealed class UserFavouritesUiState {
    data object Loading : UserFavouritesUiState()
    data class Success(val favourites: UserFavourites) : UserFavouritesUiState()
    data class Error(val message: String) : UserFavouritesUiState()
}

class UserFavouritesViewModel(
    private val repository: FavouriteRepository,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(FavouriteTab.Anime)
    val selectedTab: StateFlow<FavouriteTab> = _selectedTab

    private val _uiState = MutableStateFlow<UserFavouritesUiState>(UserFavouritesUiState.Loading)
    val uiState: StateFlow<UserFavouritesUiState> = _uiState

    init {
        load()
    }

    fun selectTab(tab: FavouriteTab) {
        _selectedTab.value = tab
    }

    fun load() {
        viewModelScope.launch {
            repository.observeUserFavourites()
                .onStart { _uiState.value = UserFavouritesUiState.Loading }
                .catch { e -> _uiState.value = UserFavouritesUiState.Error(e.message ?: "Unknown error") }
                .collect { fav ->
                    _uiState.value = UserFavouritesUiState.Success(fav)
                }
        }
    }
}

data class FavTabData(
    val label: String,
    val tab: FavouriteTab,
    val items: List<Any>,
)
