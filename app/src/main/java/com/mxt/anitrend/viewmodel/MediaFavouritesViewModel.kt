package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.AnimeFavourites
import com.mxt.anitrend.graphql.generated.MangaFavourites
import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.util.CompatUtil
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

class MediaFavouritesViewModel(
    private val userService: UserModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: ConnectionContainer<Favourite>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads media favourites. Repeatable for pagination; no loadedOnce guard.
     *
     * @param mediaType One of [KeyUtil.ANIME] or [KeyUtil.MANGA]; determines which endpoint to call.
     */
    fun load(
        userId: Long,
        page: Int,
        @KeyUtil.MediaType mediaType: String,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val response =
                        if (CompatUtil.equals(mediaType, KeyUtil.ANIME)) {
                            userService.getAnimeFavourites(
                                AnimeFavourites.request(
                                    id = userId.toInt(),
                                    page = page,
                                    perPage = KeyUtil.PAGING_LIMIT,
                                ),
                            )
                        } else {
                            userService.getMangaFavourites(
                                MangaFavourites.request(
                                    id = userId.toInt(),
                                    page = page,
                                    perPage = KeyUtil.PAGING_LIMIT,
                                ),
                            )
                        }.execute()
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
                Timber.e(throwable, "MediaFavouritesViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media favourites",
                )
            }
        }
    }
}
