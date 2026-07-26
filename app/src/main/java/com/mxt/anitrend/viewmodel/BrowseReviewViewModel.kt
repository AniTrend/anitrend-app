package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewSort
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class BrowseReviewViewModel(
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
     * Loads a browsable list of reviews. Repeatable for pagination; no loadedOnce guard.
     *
     * @param sort Combined sort string (e.g. "ID_DESC"); parsed to [ReviewSort] enum.
     */
    fun load(type: MediaType?, page: Int, sort: String?) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                val reviewSort = sort?.let {
                    runCatching { ReviewSort.valueOf(it) }.getOrNull()?.let(::listOf)
                } ?: listOf(ReviewSort.CREATED_AT_DESC)
                browseRepository.getReviewBrowse(
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    type = type,
                    sort = reviewSort,
                    asHtml = false,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "BrowseReviewViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load reviews",
                )
            }
        }
    }
}
