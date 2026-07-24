package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.MediaBrowse
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.api.retro.anilist.BrowseModel
import com.mxt.anitrend.model.entity.base.MediaBase
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

class MediaBrowseViewModel(
    private val browseService: BrowseModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<MediaBase>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun load(
        type: MediaType?,
        page: Int,
        pageLimit: Int,
        sort: String?,
        isAdult: Boolean?,
        format: String?,
        seasonYear: Int?,
        startDateLike: String?,
        status: String?,
        genres: List<String?>?,
        tags: List<String?>?,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val sortList: List<MediaSort?>? =
                        sort?.let { runCatching { MediaSort.valueOf(it) }.getOrNull()?.let { listOf(it) } }
                    val formatEnum: MediaFormat? =
                        format?.let { runCatching { MediaFormat.valueOf(it) }.getOrNull() }
                    val statusEnum: MediaStatus? =
                        status?.let { runCatching { MediaStatus.valueOf(it) }.getOrNull() }
                    val request = MediaBrowse.request(
                        type = type,
                        page = page,
                        perPage = pageLimit,
                        sort = sortList,
                        isAdult = isAdult,
                        format = formatEnum,
                        genres = genres,
                        tags = tags,
                        seasonYear = seasonYear,
                        startDateLike = startDateLike,
                        status = statusEnum,
                    )
                    val response = browseService.getMediaBrowse(request).execute()
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
                Timber.e(throwable, "MediaBrowseViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to browse media",
                )
            }
        }
    }
}
