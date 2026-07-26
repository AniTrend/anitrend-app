package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ReviewViewModel(
    private val browseRepository: BrowseRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<Review>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads reviews for a given media entry. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(mediaId: Long, type: MediaType?, page: Int) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                browseRepository.getReviewBrowse(
                    mediaId = mediaId,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    type = type,
                    asHtml = false,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "ReviewViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load reviews",
                )
            }
        }
    }
}
