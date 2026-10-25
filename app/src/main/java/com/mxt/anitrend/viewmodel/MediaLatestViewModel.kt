package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.repository.BrowseRepository
import java.util.TreeMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaLatestViewModel(
    private val browseRepository: BrowseRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState

        // These states are deliberately not data classes: `MediaBase.equals`
        // is id-only and `StateFlow` conflates equal values, so same-id reloads
        // must always carry fresh state instances.
        class Success(
            val items: List<MediaBase>,
            val hasMorePages: Boolean,
            val page: Int,
            val itemsInPage: Int,
        ) : UiState

        class Error(val message: String, val page: Int) : UiState
    }

    /** Page snapshots keyed by page and media id; latest page values replace overlaps. */
    private val pages = TreeMap<Int, LinkedHashMap<Long, MediaBase>>()
    private var requestSequence = 0L

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Loads latest media browse and publishes a complete id-keyed snapshot. */
    fun load(type: MediaType?, page: Int, pageLimit: Int, sort: String?, isAdult: Boolean?) {
        val request = ++requestSequence
        if (page <= 1) pages.clear()
        viewModelScope.launch {
            runCatching {
                val sortList = sort?.let { runCatching { MediaSort.valueOf(it) }.getOrNull()?.let(::listOf) }
                browseRepository.getMediaBrowse(
                    page = page,
                    perPage = pageLimit,
                    type = type,
                    sort = sortList,
                    isAdult = isAdult,
                ).getOrThrow()
            }.onSuccess { content ->
                if (request != requestSequence) return@onSuccess
                val pageItems = LinkedHashMap<Long, MediaBase>()
                content.pageData.forEach { item -> pageItems[item.id] = item }
                pages[page] = pageItems

                var itemsInPage = 0
                val snapshot = LinkedHashMap<Long, MediaBase>()
                pages.forEach { (loadedPage, items) ->
                    items.forEach { (id, item) ->
                        if (loadedPage == page && id !in snapshot) itemsInPage++
                        snapshot[id] = item
                    }
                }
                val pageInfo = if (content.hasPageInfo()) content.pageInfo else null
                _state.value =
                    UiState.Success(
                        items = snapshot.values.toList(),
                        hasMorePages = pageInfo?.hasNextPage() != false,
                        page = page,
                        itemsInPage = itemsInPage,
                    )
            }.onFailure { throwable ->
                if (request != requestSequence) return@onFailure
                Timber.e(throwable, "MediaLatestViewModel load failed")
                _state.value = UiState.Error(
                    message = throwable.message ?: "Failed to load latest media",
                    page = page,
                )
            }
        }
    }
}
