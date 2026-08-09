package com.mxt.anitrend.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mxt.anitrend.data.mapper.toMediaSearchItemUiModel
import com.mxt.anitrend.domain.model.MediaSearchItemUiModel
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.repository.SearchRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Network-only [PagingSource] for the media search screen.
 *
 * ### One-directional paging
 *
 * Keys are 1-based page numbers. Refresh always starts at page one: [getRefreshKey]
 * returns null, so Paging never anchors a refresh to a scrolled position. Every page
 * reports `prevKey = null`, so no prepend behavior exists. `hasNextPage` from the
 * legacy page info controls the next key, and a missing page info block is treated
 * as the terminal page.
 *
 * ### Per-source identity and overlap policy
 *
 * Query parameters (search text, media type, adult filter) are immutable per
 * instance: every source loads exactly one query identity. Items are identified by
 * [MediaSearchItemUiModel.id], the same media identity the adapter diffs on. When
 * the backend returns the same media id on a later page of the same query, the
 * later projection is dropped so the list never contains the id twice; first-seen
 * order is preserved. Dedup state is per source instance, never shared or global,
 * and a refresh resets it before re-seeding the new first page.
 *
 * The repository load and the dedup-state mutation are serialized by a per-source
 * [Mutex], so concurrent loads never interleave them.
 *
 * ### Generations and cancellation
 *
 * Paging invalidates a source and creates a fresh instance per generation, which is
 * this source's generation isolation; no generation state lives here. Cancellation
 * is never wrapped: a [CancellationException] from the repository result or from
 * projection is rethrown so the load cancels instead of surfacing as a
 * [LoadResult.Error]. Non-cancellation failures map to [LoadResult.Error].
 *
 * @param searchRepository Repository the source pages through.
 * @param search The search text of the query identity.
 * @param type Filter to a media type.
 * @param isAdult Adult content filter.
 * @param project Projection from the legacy search entity to the UI model.
 *   Injectable for focused error-path tests, defaults to the search mapper.
 */
class MediaSearchPagingSource(
    private val searchRepository: SearchRepository,
    private val search: String,
    private val type: MediaType?,
    private val isAdult: Boolean?,
    private val project: (MediaBase) -> MediaSearchItemUiModel = MediaBase::toMediaSearchItemUiModel,
) : PagingSource<Int, MediaSearchItemUiModel>() {

    /** Ids already emitted by this source instance since its last refresh reset, in first-seen order. */
    private val emittedIds = linkedSetOf<Long>()

    /**
     * Serializes the repository load and the dedup-state mutation for this source
     * instance, so concurrent loads never interleave them.
     */
    private val loadMutex = Mutex()

    /** One-directional paging: refresh always restarts from page one, never an anchor page. */
    override fun getRefreshKey(state: PagingState<Int, MediaSearchItemUiModel>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaSearchItemUiModel> {
        val page = params.key ?: 1
        return try {
            loadMutex.withLock {
                val result = searchRepository.searchMedia(
                    search = search,
                    type = type,
                    page = page,
                    perPage = params.loadSize,
                    isAdult = isAdult,
                    sort = listOf(MediaSort.SEARCH_MATCH),
                )
                result.fold(
                    onSuccess = { content ->
                        val items = content.pageData.map(project)
                        if (params is LoadParams.Refresh) {
                            // A refresh restarts this source's generation: reset the
                            // dedup state before re-seeding it with the new first page.
                            emittedIds.clear()
                        }
                        val pageData = items.filter { emittedIds.add(it.id) }
                        LoadResult.Page(
                            data = pageData,
                            prevKey = null,
                            nextKey = if (content.hasPageInfo() && content.pageInfo.hasNextPage()) page + 1 else null,
                        )
                    },
                    onFailure = { throwable ->
                        if (throwable is CancellationException) throw throwable
                        LoadResult.Error(throwable)
                    },
                )
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Exception) {
            LoadResult.Error(throwable)
        }
    }
}
