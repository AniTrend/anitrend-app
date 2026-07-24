package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.api.retro.anilist.BrowseModel
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.mxt.anitrend.graphql.generated.MediaListCollection as GenMediaListCollection

class AiringListViewModel(
    private val browseService: BrowseModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
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
                withContext(ioDispatcher) {
                    val sortList: List<MediaListSort?>? =
                        sort?.let { runCatching { MediaListSort.valueOf(it) }.getOrNull()?.let { listOf(it) } }
                    val statusList: List<MediaListStatus?>? =
                        statusIn?.let { runCatching { MediaListStatus.valueOf(it) }.getOrNull()?.let { listOf(it) } }
                    val request = GenMediaListCollection.request(
                        userId = userId,
                        type = type,
                        forceSingleCompletedList = true,
                        sort = sortList,
                        statusIn = statusList,
                        scoreFormat = scoreFormat,
                    )
                    val response = browseService.getMediaListCollection(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body()
                            ?: throw IllegalStateException("Empty response body")
                        val graphErrors: List<GraphError>? = body.errors
                        if (!graphErrors.isNullOrEmpty()) {
                            throw RuntimeException(
                                graphErrors.first().message
                                    ?: "GraphQL error",
                            )
                        }
                        body.data?.result
                            ?: throw IllegalStateException("Empty response body")
                    } else {
                        throw RuntimeException(response.apiError())
                    }
                }
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
