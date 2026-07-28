package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.model.api.retro.base.GiphyService
import com.mxt.anitrend.model.entity.giphy.GiphyContainer
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class GiphyViewModel(
    private val giphyService: GiphyService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val container: GiphyContainer) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Loads trending GIFs at the given offset. */
    fun loadTrending(offset: Int) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val response = giphyService.getTrending(
                        api_key = BuildConfig.GIPHY_KEY,
                        limit = KeyUtil.PAGING_LIMIT,
                        offset = offset,
                        rating = "PG",
                    ).execute()
                    if (response.isSuccessful) {
                        response.body()
                            ?: throw IllegalStateException("Empty response body")
                    } else {
                        throw RuntimeException(
                            "Giphy request failed: ${response.code()}",
                        )
                    }
                }
            }.onSuccess { container ->
                _state.value = UiState.Success(container)
            }.onFailure { throwable ->
                Timber.e(throwable, "GiphyViewModel trending failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load trending GIFs",
                )
            }
        }
    }

    /** Searches GIFs by query at the given offset. */
    fun search(query: String, offset: Int) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val response = giphyService.findGif(
                        api_key = BuildConfig.GIPHY_KEY,
                        q = query,
                        limit = KeyUtil.PAGING_LIMIT,
                        offset = offset,
                        rating = "PG",
                        lang = "en",
                    ).execute()
                    if (response.isSuccessful) {
                        response.body()
                            ?: throw IllegalStateException("Empty response body")
                    } else {
                        throw RuntimeException(
                            "Giphy search failed: ${response.code()}",
                        )
                    }
                }
            }.onSuccess { container ->
                _state.value = UiState.Success(container)
            }.onFailure { throwable ->
                Timber.e(throwable, "GiphyViewModel search failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to search GIFs",
                )
            }
        }
    }
}
