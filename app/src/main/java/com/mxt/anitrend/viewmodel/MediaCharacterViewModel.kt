package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.MediaCharacters
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.api.retro.anilist.MediaModel
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MediaCharacterViewModel(
    private val mediaService: MediaModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: ConnectionContainer<EdgeContainer<CharacterEdge>>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads media characters. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(mediaId: Long, type: MediaType?, page: Int, isAdult: Boolean?) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val request = MediaCharacters.request(
                        id = mediaId.toInt(),
                        type = type,
                        page = page,
                        perPage = KeyUtil.PAGING_LIMIT,
                        isAdult = isAdult,
                    )
                    val response = mediaService.getMediaCharacters(request).execute()
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
                Timber.e(throwable, "MediaCharacterViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media characters",
                )
            }
        }
    }
}
