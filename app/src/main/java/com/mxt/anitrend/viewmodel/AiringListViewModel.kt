package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AiringListViewModel(
    private val browseRepository: BrowseRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<MediaListCollection>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads airing media list collection. Single load; not paginated.
     */
    fun load(
        type: MediaType,
        userId: Int,
        sort: String?,
        statusIn: String?,
        scoreFormat: ScoreFormat?,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                val sortList = sort?.let { runCatching { MediaListSort.valueOf(it) }.getOrNull()?.let(::listOf) }
                val statusList = statusIn?.let { runCatching { MediaListStatus.valueOf(it) }.getOrNull()?.let(::listOf) }
                browseRepository.getMediaListCollection(
                    userId = userId.toLong(),
                    type = type,
                    forceSingleCompletedList = true,
                    sort = sortList,
                    statusIn = statusList,
                    scoreFormat = scoreFormat ?: ScoreFormat.POINT_100,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "AiringListViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load airing list",
                )
            }
        }
    }
}
