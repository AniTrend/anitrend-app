package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.repository.CharacterRepository
import com.mxt.anitrend.repository.StaffRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.GroupingUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaFormatViewModel(
    private val characterRepository: CharacterRepository,
    private val staffRepository: StaffRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            /** Complete renderable snapshot; consumers replace adapter contents on every emission. */
            val items: List<RecyclerItem>,
            val pageInfo: PageInfo?,
            val isEmpty: Boolean,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Identity of the query the active generation loads for. */
    private data class QueryKey(
        val id: Long,
        val requestType: Int,
        val mediaType: String?,
        val onList: Boolean?,
    )

    /**
     * Complete deduplicated media snapshot keyed explicitly by [MediaBase.id].
     * First-seen order is preserved; a repeated id's later server object replaces
     * the previously stored value.
     */
    private val mediaById = linkedMapOf<Long, MediaBase>()

    /**
     * Monotonic request generation advanced by every accepted page-one/reset load.
     * Successes and failures from older generations are ignored so a refresh cannot
     * be overwritten by a pre-refresh in-flight response.
     */
    private var generation = 0L

    /** Query of the current generation; established by page-one loads. */
    private var activeQuery: QueryKey? = null

    /**
     * Highest page whose response was accepted for the active generation, or 0 before
     * any acceptance. Only the next contiguous page is ever launched; lower or
     * duplicate pages are ignored once progress exists.
     */
    private var highestAcceptedPage = 0

    /**
     * Whether the active generation ended with an empty or explicit no-next-page
     * response. Further page loads are rejected until a page-one refresh starts a
     * new generation.
     */
    private var terminal = false

    /**
     * Page currently in flight for the active generation, or null. At most one page
     * per generation is in flight, so accepted pages always apply in order and the
     * highest accepted page's metadata is never overwritten by a lower page.
     */
    private var inFlightPage: Int? = null

    /**
     * Ordered, deduplicated set of pages requested beyond the next contiguous page.
     * They are queued even when nothing is in flight and launched one at a time as
     * their predecessor is accepted, so no requested page is skipped.
     */
    private val pendingPages = sortedSetOf<Int>()

    /**
     * Loads media grouped by format. Repeatable for pagination; no loadedOnce guard.
     *
     * Page 1 establishes the active query, resets the deduplicated media snapshot and
     * advances the request generation. A repeated page 1 for the same active query
     * while the reset is still in flight is coalesced; a page 1 for a different query,
     * or once the reset has settled, starts a new generation and invalidates
     * older-generation requests. Pages greater than one must match the active query
     * and advance contiguously: the next page after the highest accepted one is
     * launched unless already in flight, any higher page is queued even when nothing
     * is in flight, and lower or duplicate pages are ignored. A terminal page (empty
     * or no next page) ends the generation and rejects further page loads. Every
     * successful response republishes the complete grouped snapshot rebuilt from the
     * accepted media, so re-collection of the state flow can never replay an append
     * delta.
     *
     * @param id The character or staff id.
     * @param onList Filter to media on the user's list (staff-only).
     * @param mediaType Raw enum name of [MediaType]; parsed internally.
     * @param page Current page number.
     * @param requestType One of [KeyUtil.CHARACTER_MEDIA_REQ] or [KeyUtil.STAFF_MEDIA_REQ].
     */
    fun load(
        id: Long,
        onList: Boolean?,
        mediaType: String?,
        page: Int,
        requestType: Int,
    ) {
        val query = QueryKey(id = id, requestType = requestType, mediaType = mediaType, onList = onList)
        val inFlight = inFlightPage
        if (page == 1) {
            // Coalesce a reset for the same active query while the reset is in flight.
            if (query == activeQuery && inFlight == 1) return
            generation++
            activeQuery = query
            mediaById.clear()
            inFlightPage = null
            pendingPages.clear()
            highestAcceptedPage = 0
            terminal = false
            launchPage(query, page)
        } else if (query != activeQuery) {
            // A page greater than one for a different query cannot merge; drop it.
            return
        } else if (terminal) {
            // The generation ended; only a page-one refresh starts a new one.
            return
        } else if (page <= highestAcceptedPage) {
            // A lower or duplicate page after accepted progress cannot change state; drop it.
            return
        } else if (page == highestAcceptedPage + 1) {
            // The next contiguous page: launch it unless it is already in flight.
            if (inFlight == null) {
                launchPage(query, page)
            }
        } else {
            // Not the next contiguous page: queue it, even when nothing is in flight.
            pendingPages.add(page)
        }
    }

    private fun launchPage(
        query: QueryKey,
        page: Int,
    ) {
        val requestGeneration = generation
        inFlightPage = page
        _state.value = UiState.Loading
        viewModelScope.launch {
            val type = query.mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
            val result = if (query.requestType == KeyUtil.CHARACTER_MEDIA_REQ) {
                characterRepository.getCharacterMedia(
                    id = query.id,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    type = type,
                )
            } else {
                staffRepository.getStaffMedia(
                    id = query.id,
                    onList = query.onList,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    type = type,
                )
            }
            result.fold(
                onSuccess = { container ->
                    if (requestGeneration != generation || query != activeQuery || page != inFlightPage) return@fold
                    inFlightPage = null
                    val pageContainer = container.connection
                    val pageInfo = pageContainer?.let { if (it.hasPageInfo()) it.pageInfo else null }
                    if (pageContainer != null && !pageContainer.isEmpty) {
                        pageContainer.pageData.forEach { media ->
                            mediaById[media.id] = media
                        }
                    }
                    val items = GroupingUtil.groupMediaByFormat(mediaById.values.toList(), null)
                    _state.value = UiState.Success(
                        items = items,
                        pageInfo = pageInfo,
                        isEmpty = pageContainer == null || pageContainer.isEmpty,
                    )
                    highestAcceptedPage = page
                    if (pageContainer == null || pageContainer.isEmpty || pageInfo?.hasNextPage() == false) {
                        // Terminal page: the generation ends; drop queued pages.
                        terminal = true
                        pendingPages.clear()
                    } else {
                        launchNextContiguousPage(query, page)
                    }
                },
                onFailure = { throwable ->
                    if (requestGeneration != generation || query != activeQuery || page != inFlightPage) return@fold
                    inFlightPage = null
                    // A failed page breaks the continuation; queued pages are dropped and the
                    // fragment's retry re-requests from the failed page.
                    pendingPages.clear()
                    Timber.e(throwable, "MediaFormatViewModel load failed")
                    _state.value = UiState.Error(
                        throwable.message ?: "Failed to load media format",
                    )
                },
            )
        }
    }

    private fun launchNextContiguousPage(
        query: QueryKey,
        page: Int,
    ) {
        val next = page + 1
        if (next in pendingPages) {
            pendingPages.remove(next)
            launchPage(query, next)
        }
    }
}
