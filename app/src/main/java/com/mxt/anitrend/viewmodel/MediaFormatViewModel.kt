package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
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

    private val acceptedMediaById = linkedMapOf<Long, MediaBase>()

    private var requestGeneration = 0L

    private var activeQueryKey: QueryKey? = null

    private var lastAcceptedPage = 0

    private var generationEnded = false

    private var pageInFlight: Int? = null

    private val queuedPages = sortedSetOf<Int>()

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
        val inFlight = pageInFlight
        if (page == 1) {
            // Coalesce a reset for the same active query while the reset is in flight.
            if (query == activeQueryKey && inFlight == 1) return
            requestGeneration++
            activeQueryKey = query
            acceptedMediaById.clear()
            pageInFlight = null
            queuedPages.clear()
            lastAcceptedPage = 0
            generationEnded = false
            launchPage(query, page)
        } else if (query != activeQueryKey) {
            // A page greater than one for a different query cannot merge; drop it.
            return
        } else if (generationEnded) {
            // The generation ended; only a page-one refresh starts a new one.
            return
        } else if (page <= lastAcceptedPage) {
            // A lower or duplicate page after accepted progress cannot change state; drop it.
            return
        } else if (page == lastAcceptedPage + 1) {
            // The next contiguous page: launch it unless it is already in flight.
            if (inFlight == null) {
                launchPage(query, page)
            }
        } else {
            // Not the next contiguous page: queue it, even when nothing is in flight.
            queuedPages.add(page)
        }
    }

    private fun launchPage(
        query: QueryKey,
        page: Int,
    ) {
        val issuedGeneration = requestGeneration
        pageInFlight = page
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
                    applyAcceptedPage(container, query, page, issuedGeneration)
                },
                onFailure = { throwable ->
                    applyFailedPage(throwable, query, page, issuedGeneration)
                },
            )
        }
    }

    private fun applyAcceptedPage(
        container: ConnectionContainer<PageContainer<MediaBase>>,
        query: QueryKey,
        page: Int,
        issuedGeneration: Long,
    ) {
        // Ignore responses that are no longer current: the generation moved on,
        // the query changed, or the page is no longer the one in flight, so a
        // refresh cannot be overwritten by a pre-refresh in-flight response.
        if (issuedGeneration != requestGeneration || query != activeQueryKey || page != pageInFlight) return
        pageInFlight = null
        val pageContainer = container.connection
        val pageInfo = pageContainer?.let { if (it.hasPageInfo()) it.pageInfo else null }
        if (pageContainer != null && !pageContainer.isEmpty) {
            // First-seen order is preserved; a repeated id's later server object
            // replaces the previously stored value.
            pageContainer.pageData.forEach { media ->
                acceptedMediaById[media.id] = media
            }
        }
        val items = GroupingUtil.groupMediaByFormat(acceptedMediaById.values.toList(), null)
        _state.value = UiState.Success(
            items = items,
            pageInfo = pageInfo,
            isEmpty = pageContainer == null || pageContainer.isEmpty,
        )
        lastAcceptedPage = page
        if (pageContainer == null || pageContainer.isEmpty || pageInfo?.hasNextPage() == false) {
            // Terminal page: the generation ends; drop queued pages.
            generationEnded = true
            queuedPages.clear()
        } else {
            launchNextContiguousPage(query, page)
        }
    }

    private fun applyFailedPage(
        throwable: Throwable,
        query: QueryKey,
        page: Int,
        issuedGeneration: Long,
    ) {
        // Drop responses that are no longer current, mirroring applyAcceptedPage.
        if (issuedGeneration != requestGeneration || query != activeQueryKey || page != pageInFlight) return
        pageInFlight = null
        // A failed page breaks the continuation; queued pages are dropped and the
        // fragment's retry re-requests from the failed page.
        queuedPages.clear()
        Timber.e(throwable, "MediaFormatViewModel load failed")
        _state.value = UiState.Error(
            throwable.message ?: "Failed to load media format",
        )
    }

    private fun launchNextContiguousPage(
        query: QueryKey,
        page: Int,
    ) {
        val next = page + 1
        if (next in queuedPages) {
            queuedPages.remove(next)
            launchPage(query, next)
        }
    }
}
