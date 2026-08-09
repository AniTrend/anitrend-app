package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mxt.anitrend.data.paging.MediaSearchPagingSource
import com.mxt.anitrend.domain.model.MediaSearchItemUiModel
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.repository.SearchRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

/**
 * ViewModel-first owner of the media search screen's query and paging stream.
 *
 * Paging 3 owns all network page orchestration: this ViewModel only establishes the
 * query identity, rebuilds the [Pager] when the query changes, and exposes the cached
 * [PagingData] stream. The fragment renders load states and the adapter renders
 * immutable items. There is no manual page state machine, no in-memory item cache, and
 * no local store or RemoteMediator behind this stream.
 *
 * Calling [load] with the same query identity is a no-op for the flow (StateFlow does
 * not re-emit equal values), so repeated establishment from view recreation never
 * restarts an active generation. Calling [load] with a different identity cancels the
 * previous [Pager] and starts a fresh one through the query flow. Each search-tab
 * instance (anime and manga) owns its fragment-scoped ViewModel, so the two tabs keep
 * independent query identities.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaSearchViewModel(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    /** Identity of the search query the active [Pager] loads for. */
    private data class MediaSearchQuery(
        val search: String,
        val type: MediaType?,
        val isAdult: Boolean?,
    )

    private val queryFlow = MutableStateFlow<MediaSearchQuery?>(null)

    /**
     * Cached network-only [PagingData] stream for the active query.
     *
     * The stream stays silent until [load] establishes a query (the empty-query
     * no-op lives at the call site), rebuilds its [Pager] on query changes, and is
     * cached in the ViewModel scope so view recreation and re-subscription replay
     * the current generation instead of restarting it.
     */
    val pagingDataFlow: Flow<PagingData<MediaSearchItemUiModel>> =
        queryFlow
            .filterNotNull()
            .flatMapLatest { query ->
                Pager(
                    config =
                    PagingConfig(
                        // One network page per refresh, appending one page at a time
                        // near the list end, mirroring the legacy screen's single-page
                        // request shape (perPage 21, demand-driven next page).
                        pageSize = KeyUtil.PAGING_LIMIT,
                        initialLoadSize = KeyUtil.PAGING_LIMIT,
                        prefetchDistance = 9,
                        enablePlaceholders = false,
                    ),
                    pagingSourceFactory = {
                        MediaSearchPagingSource(
                            searchRepository = searchRepository,
                            search = query.search,
                            type = query.type,
                            isAdult = query.isAdult,
                        )
                    },
                ).flow
            }
            .cachedIn(viewModelScope)

    /**
     * Establishes the media search query.
     *
     * Idempotent for the same identity; a different identity replaces the active query
     * and rebuilds the [Pager]. Paging owns page orchestration, so there is no page
     * parameter.
     *
     * @param search The search text.
     * @param type Filter to a media type.
     * @param isAdult When true the server includes adult content; when null the filter
     *   is omitted. When false adult content is explicitly excluded.
     */
    fun load(
        search: String,
        type: MediaType?,
        isAdult: Boolean?,
    ) {
        queryFlow.value = MediaSearchQuery(search = search, type = type, isAdult = isAdult)
    }
}
