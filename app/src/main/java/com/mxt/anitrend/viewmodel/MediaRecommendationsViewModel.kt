package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.RecommendationItemUiModel
import com.mxt.anitrend.domain.model.toRecommendationItemUiModel
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaRecommendationsViewModel(
    private val mediaRepository: MediaRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val items: List<RecommendationItemUiModel>,
            val pageInfo: PageInfoRecord?,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var requestRevision = 0L
    private var currentItems = emptyList<RecommendationItemUiModel>()
    private var currentPageInfo: PageInfoRecord? = null

    /**
     * Loads media recommendations for the given media. Pass [page] to append
     * a paginated page; omit or pass page one to replace the current items.
     * Stale responses are ignored when a newer request has already started.
     */
    fun load(
        mediaId: Long,
        type: MediaType?,
        isAdult: Boolean?,
        page: Int? = null,
    ) {
        viewModelScope.launch(dispatcher) {
            val revision = ++requestRevision
            _state.value = UiState.Loading
            runCatching {
                mediaRepository.getMediaRecommendations(
                    id = mediaId,
                    type = type,
                    isAdult = isAdult,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    sort = null,
                ).getOrThrow()
            }.onSuccess { content ->
                if (revision != requestRevision) return@onSuccess
                val pageItems = content.recommendations.mapNotNull { it.toRecommendationItemUiModel() }
                currentItems = if (page == null || page <= 1) pageItems else currentItems + pageItems
                currentPageInfo = content.pageInfo
                _state.value = UiState.Success(currentItems, currentPageInfo)
            }.onFailure { throwable ->
                if (revision != requestRevision) return@onFailure
                Timber.e(throwable, "MediaRecommendationsViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load recommendations",
                )
            }
        }
    }
}
