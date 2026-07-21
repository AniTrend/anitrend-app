package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.VisibleForTesting
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.StudioMedia
import com.mxt.anitrend.model.api.retro.anilist.StudioModel
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
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

class StudioMediaViewModel(
    private val studioService: StudioModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val container: ConnectionContainer<PageContainer<MediaBase>>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads studio media by AniList ID with pagination and sort.
     * @param sort the combined sort key from settings (e.g. "POPULARITY_DESC").
     */
    fun load(
        studioId: Long,
        page: Int,
        perPage: Int,
        sort: String?,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val sortList = resolveMediaSort(sort)
                    val request = StudioMedia.request(
                        id = studioId.toInt(),
                        page = page,
                        perPage = perPage,
                        sort = sortList,
                    )
                    val response = studioService.getStudioMedia(request).execute()
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
            }.onSuccess { container ->
                _state.value = UiState.Success(container)
            }.onFailure { throwable ->
                Timber.e(throwable, "StudioMediaViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load studio media",
                )
            }
        }
    }

    @VisibleForTesting
    internal fun resolveMediaSort(sort: String?): kotlin.collections.List<MediaSort> {
        return sort?.let {
            runCatching {
                listOf(MediaSort.valueOf(it))
            }.getOrDefault(listOf(MediaSort.POPULARITY))
        } ?: listOf(MediaSort.POPULARITY)
    }
}
