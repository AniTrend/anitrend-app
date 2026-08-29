package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/** Loads anime and manga favourites for a user. */
class MediaFavouritesViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    /** State emitted while loading a media favourites page. */
    sealed interface UiState {
        /** Media type associated with the current request, when known. */
        val mediaType: String?

        /** Indicates that a favourites request is in progress. */
        data class Loading(override val mediaType: String? = null) : UiState

        /** Contains a successfully loaded favourites page. */
        data class Success(
            val content: ConnectionContainer<Favourite>,
            override val mediaType: String,
        ) : UiState

        /** Contains the error from a failed favourites request. */
        data class Error(
            val message: String,
            override val mediaType: String? = null,
        ) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading())

    /** Current loading, success, or error state. */
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads media favourites. Repeatable for pagination; no loadedOnce guard.
     *
     * @param mediaType One of [KeyUtil.ANIME] or [KeyUtil.MANGA]; determines which endpoint to call.
     * @param userId AniList user id whose favourites should be loaded.
     * @param page One-based page number to request.
     */
    fun load(
        userId: Long,
        page: Int,
        @KeyUtil.MediaType mediaType: String,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading(mediaType)
            runCatching {
                if (CompatUtil.equals(mediaType, KeyUtil.ANIME)) {
                    userRepository.getAnimeFavourites(
                        id = userId,
                        page = page,
                        perPage = KeyUtil.PAGING_LIMIT,
                    )
                } else {
                    userRepository.getMangaFavourites(
                        id = userId,
                        page = page,
                        perPage = KeyUtil.PAGING_LIMIT,
                    )
                }.getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content, mediaType)
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaFavouritesViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media favourites",
                    mediaType,
                )
            }
        }
    }
}
