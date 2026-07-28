package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaLatestViewModel(
    private val browseRepository: BrowseRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<MediaBase>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads latest media browse. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(type: MediaType?, page: Int, pageLimit: Int, sort: String?, isAdult: Boolean?) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                val sortList = sort?.let { runCatching { MediaSort.valueOf(it) }.getOrNull()?.let(::listOf) }
                browseRepository.getMediaBrowse(
                    page = page,
                    perPage = pageLimit,
                    type = type,
                    sort = sortList,
                    isAdult = isAdult,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaLatestViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load latest media",
                )
            }
        }
    }
}
