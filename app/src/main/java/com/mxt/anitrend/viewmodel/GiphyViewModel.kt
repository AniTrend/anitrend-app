package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.model.api.retro.base.GiphyService
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.model.entity.giphy.GiphyContainer
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

        /** Complete snapshot, including all committed pages for the current query. */
        data class Success(val container: GiphyContainer) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()
    private var items: List<Giphy> = emptyList()
    private var query: String? = null
    private var nextOffset = 0
    private var canAppend = false
    private var inFlight = false
    private var request: Job? = null

    /** Reattaching the dialog must not restart an existing query or request. */
    fun loadIfNeeded() {
        if (request == null) refresh(null)
    }

    /** A submission or retry starts at offset zero, replacing the prior result. */
    fun refresh(query: String?) {
        request?.cancel()
        this.query = query?.takeIf(String::isNotEmpty)
        canAppend = false
        load(offset = 0)
    }

    fun loadMore() {
        if (inFlight || !canAppend) return
        load(nextOffset)
    }

    private fun load(offset: Int) {
        val requestedQuery = query
        inFlight = true
        canAppend = false
        _state.value = UiState.Loading
        request = viewModelScope.launch {
            try {
                val container = withContext(ioDispatcher) {
                    val response = if (requestedQuery == null) {
                        giphyService.getTrending(BuildConfig.GIPHY_KEY, KeyUtil.PAGING_LIMIT, offset, "PG")
                    } else {
                        giphyService.findGif(BuildConfig.GIPHY_KEY, requestedQuery, KeyUtil.PAGING_LIMIT, offset, "PG", "en")
                    }
                    if (!response.isSuccessful) {
                        throw IllegalStateException("Giphy request failed: ${response.code()}")
                    }
                    response.body() ?: throw IllegalStateException("Empty response body")
                }
                val previous = if (offset == 0) emptyList() else items
                val knownIds = previous.mapNotNullTo(HashSet()) { it.id }
                val newItems = container.data.filter { it.id?.let(knownIds::add) ?: true }
                items = previous + newItems
                val count = container.pagination?.count?.takeIf { it > 0 } ?: container.data.size
                nextOffset = offset + count
                canAppend = newItems.isNotEmpty() &&
                    (container.pagination?.let { nextOffset < it.total_count } ?: true)
                inFlight = false
                _state.value = UiState.Success(GiphyContainer(items, container.pagination, container.meta))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                inFlight = false
                Timber.e(error, "Giphy request failed")
                _state.value = UiState.Error(error.message ?: "Failed to load GIFs")
            }
        }
    }
}
