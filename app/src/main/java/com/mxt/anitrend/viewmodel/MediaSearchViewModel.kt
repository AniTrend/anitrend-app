package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.SearchRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaSearchViewModel(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<MediaBase>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads media search results. Repeatable for pagination; no loadedOnce guard.
     *
     * @param isAdult When true the server includes adult content; when null the filter is omitted.
     *                When false adult content is explicitly excluded.
     */
    fun load(
        search: String?,
        type: MediaType?,
        page: Int,
        isAdult: Boolean?,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                searchRepository.searchMedia(
                    search = search,
                    type = type,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    isAdult = isAdult,
                    sort = listOf(MediaSort.SEARCH_MATCH),
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaSearchViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media search",
                )
            }
        }
    }
}
