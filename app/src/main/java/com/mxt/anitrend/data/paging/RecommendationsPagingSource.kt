package com.mxt.anitrend.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mxt.anitrend.domain.model.RecommendationItemUiModel
import com.mxt.anitrend.domain.model.RecommendationRecord
import com.mxt.anitrend.domain.model.toRecommendationItemUiModel
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.repository.MediaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Network-only [PagingSource] for the media recommendations screen.
 *
 * ### One-directional paging
 *
 * Keys are 1-based page numbers. Refresh always starts at page one: [getRefreshKey]
 * returns null, so Paging never anchors a refresh to a scrolled position. Every page
 * reports `prevKey = null`, so no prepend behavior exists. `hasNextPage` from the
 * repository's page info controls the next key, and a null
 * [com.mxt.anitrend.domain.model.PageInfoRecord] is treated as the terminal page.
 *
 * ### Per-source identity and overlap policy
 *
 * Query parameters are immutable per instance: every source loads exactly one query
 * identity (media, type, adult filter). Items are identified by
 * [RecommendationItemUiModel.id], the same identity the adapter diffs on. When the
 * backend returns the same recommendation id on a later page of the same query, the
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
 * [LoadResult.Error]. Non-cancellation runtime failures map to [LoadResult.Error].
 *
 * @param mediaRepository Repository the source pages through.
 * @param mediaId The media the recommendations belong to.
 * @param type Filter to a media type.
 * @param isAdult Adult content filter.
 * @param project Projection from a raw recommendation record to the UI model; records
 *   without a recommended media project to null and are dropped. Injectable for
 *   focused error-path tests, defaults to the domain projection.
 */
class RecommendationsPagingSource(
    private val mediaRepository: MediaRepository,
    private val mediaId: Long,
    private val type: MediaType?,
    private val isAdult: Boolean?,
    private val project: (RecommendationRecord) -> RecommendationItemUiModel? =
        RecommendationRecord::toRecommendationItemUiModel,
) : PagingSource<Int, RecommendationItemUiModel>() {

    /** Recommendation ids already emitted by this source instance since its last refresh reset, in first-seen order. */
    private val emittedRecommendationIds = linkedSetOf<Long>()

    /**
     * Serializes the repository load and the dedup-state mutation for this source
     * instance, so concurrent loads never interleave them.
     */
    private val repositoryLoadMutex = Mutex()

    /** One-directional paging: refresh always restarts from page one, never an anchor page. */
    override fun getRefreshKey(state: PagingState<Int, RecommendationItemUiModel>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecommendationItemUiModel> {
        val page = params.key ?: 1
        return try {
            repositoryLoadMutex.withLock {
                val result = mediaRepository.getMediaRecommendations(
                    id = mediaId,
                    type = type,
                    isAdult = isAdult,
                    page = page,
                    perPage = params.loadSize,
                    sort = null,
                )
                result.fold(
                    onSuccess = { content ->
                        val items = content.recommendations.mapNotNull(project)
                        if (params is LoadParams.Refresh) {
                            // A refresh restarts this source's generation: reset the
                            // dedup state before re-seeding it with the new first page.
                            emittedRecommendationIds.clear()
                        }
                        val pageData = items.filter { emittedRecommendationIds.add(it.id) }
                        LoadResult.Page(
                            data = pageData,
                            prevKey = null,
                            nextKey = if (content.pageInfo?.hasNextPage == true) page + 1 else null,
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
        } catch (throwable: RuntimeException) {
            LoadResult.Error(throwable)
        }
    }
}
