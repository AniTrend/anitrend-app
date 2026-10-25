package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.crunchy.Episode
import com.mxt.anitrend.repository.CrunchyrollRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * External episode feed owner. The RSS channel parsing and repository stay
 * unchanged; this ViewModel only owns loading and state for the Compose
 * Episodes screen.
 */
class EpisodesViewModel(
    private val crunchyrollRepository: CrunchyrollRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val episodes: List<Episode>,
            val copyright: String?,
        ) : UiState

        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Loads the external feed: popular when [popular], latest otherwise. */
    fun load(popular: Boolean) {
        _state.value = UiState.Loading
        viewModelScope.launch(ioDispatcher) {
            val result =
                if (popular) {
                    crunchyrollRepository.getPopularFeed()
                } else {
                    crunchyrollRepository.getLatestFeed()
                }
            result
                .onSuccess { rss ->
                    val episodes = rss?.channel?.episode.orEmpty()
                    _state.value =
                        UiState.Success(
                            episodes = episodes,
                            copyright = rss?.channel?.copyright,
                        )
                }
                .onFailure { throwable ->
                    if (throwable is kotlinx.coroutines.CancellationException) throw throwable
                    Timber.e(throwable, "EpisodesViewModel load failed")
                    _state.value = UiState.Error(throwable.message ?: "Failed to load episodes")
                }
        }
    }
}
