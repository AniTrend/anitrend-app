package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.MediaBase
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.api.retro.anilist.MediaModel
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MediaViewModel(
    private val mediaService: MediaModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val media: com.mxt.anitrend.model.entity.base.MediaBase) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var loadedOnce = false

    /**
     * Loads the media by its AniList ID. Safe to call multiple times -- skips
     * the network call after the first successful load.
     *
     * @param mediaId   AniList media ID
     * @param mediaType "ANIME", "MANGA", or null
     * @param showAdult whether adult content should be included;
     *                  false (the default) excludes adult entries
     */
    fun load(
        mediaId: Long,
        mediaType: String?,
        showAdult: Boolean,
    ) {
        if (loadedOnce) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val typeEnum: MediaType? = mediaType?.let {
                        try {
                            MediaType.valueOf(it)
                        } catch (_: IllegalArgumentException) {
                            null
                        }
                    }
                    val isAdult: Boolean? = if (showAdult) null else false
                    val request = MediaBase.request(
                        id = mediaId.toInt(),
                        type = typeEnum,
                        isAdult = isAdult,
                    )
                    val response = mediaService.getMediaBase(request).execute()
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
            }.onSuccess { media ->
                _state.value = UiState.Success(media)
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media",
                )
            }
        }
    }
}
