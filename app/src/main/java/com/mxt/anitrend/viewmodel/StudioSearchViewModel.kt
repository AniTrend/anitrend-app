package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.StudioSort
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.SearchRepository
import com.mxt.anitrend.util.KeyUtil
import java.util.TreeMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class StudioSearchViewModel(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState

        // Entity equality is id-only, so StateFlow must not conflate fresh projections for the same ids.
        class Success(
            val content: PageContainer<StudioBase>,
            val search: String?,
            val page: Int,
            val items: List<StudioBase>,
            val itemsInPage: Int,
        ) : UiState

        data class Error(
            val message: String,
            val search: String?,
            val page: Int,
        ) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val pages = TreeMap<Int, LinkedHashMap<Long, StudioBase>>()
    private var loadedSearch: String? = null
    private var requestSequence = 0L

    /** Loads one request identity and publishes a complete id-keyed snapshot. */
    fun load(search: String?, page: Int) {
        val request = ++requestSequence
        if (page <= 1 || loadedSearch != search) {
            pages.clear()
            loadedSearch = search
        }
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                searchRepository.searchStudio(
                    search = search,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    sort = listOf(StudioSort.SEARCH_MATCH),
                ).getOrThrow()
            }.onSuccess { content ->
                if (request != requestSequence) return@onSuccess
                val pageItems = LinkedHashMap<Long, StudioBase>()
                content.pageData.forEach { item -> pageItems[item.id] = item }
                pages[page] = pageItems

                var itemsInPage = 0
                val snapshot = LinkedHashMap<Long, StudioBase>()
                pages.forEach { (loadedPage, items) ->
                    items.forEach { (id, item) ->
                        if (loadedPage == page && id !in snapshot) itemsInPage++
                        snapshot[id] = item
                    }
                }
                _state.value = UiState.Success(
                    content = content,
                    search = search,
                    page = page,
                    items = snapshot.values.toList(),
                    itemsInPage = itemsInPage,
                )
            }.onFailure { throwable ->
                if (request != requestSequence) return@onFailure
                Timber.e(throwable, "StudioSearchViewModel load failed")
                _state.value = UiState.Error(
                    message = throwable.message ?: "Failed to load studio search",
                    search = search,
                    page = page,
                )
            }
        }
    }
}
