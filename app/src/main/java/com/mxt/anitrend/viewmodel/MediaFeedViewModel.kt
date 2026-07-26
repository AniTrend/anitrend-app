package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaFeedViewModel(
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<FeedList>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads media feed (social activity). Repeatable for pagination; no loadedOnce guard.
     */
    fun load(mediaId: Long, isFollowing: Boolean, page: Int, pageLimit: Int) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                mediaRepository.getMediaSocial(
                    mediaId = mediaId,
                    isFollowing = isFollowing,
                    page = page,
                    perPage = pageLimit,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaFeedViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media feed",
                )
            }
        }
    }
}
