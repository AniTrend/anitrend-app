package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.domain.model.NotificationPageResult
import com.mxt.anitrend.domain.model.NotificationRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    data class PageRequest(
        val id: Long,
        val page: Int,
        val isRefresh: Boolean,
    )

    data class Snapshot(
        val notifications: List<NotificationRecord> = emptyList(),
        val pageInfo: PageInfoRecord? = null,
        val lastLoadedPage: Int = 0,
        val nextPage: Int = 1,
        val hasMore: Boolean = true,
        val hasLoadedPage: Boolean = false,
        val lastPageUniqueCount: Int = 0,
    ) {
        companion object {
            val empty = Snapshot()
        }
    }

    sealed interface UiState {
        val snapshot: Snapshot

        data class Loading(
            override val snapshot: Snapshot,
            val request: PageRequest?,
        ) : UiState

        data class Success(
            override val snapshot: Snapshot,
            val request: PageRequest,
        ) : UiState

        data class Error(
            override val snapshot: Snapshot,
            val request: PageRequest,
            val message: String,
        ) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading(Snapshot.empty, request = null))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var snapshot = Snapshot.empty
    private val pages = linkedMapOf<Int, List<NotificationRecord>>()
    private var nextRequestId = 1L
    private var activeRequest: PageRequest? = null

    /**
     * Loads a notification page and publishes the complete immutable snapshot.
     * Successful pages are owned by their page number, so replaying a StateFlow
     * value only replaces the fragment's projection and never appends the page
     * again. A refresh replaces every previously loaded page; failed appends
     * leave [Snapshot.nextPage] unchanged so that the same page can be retried.
     */
    fun load(page: Int, isRefresh: Boolean) {
        if (activeRequest != null && !isRefresh) return
        if (page < 1 || (isRefresh && page != 1)) return
        if (!isRefresh && (!snapshot.hasMore || page != snapshot.nextPage)) return

        val request = PageRequest(
            id = nextRequestId++,
            page = page,
            isRefresh = isRefresh,
        )
        activeRequest = request
        _state.value = UiState.Loading(snapshot = snapshot, request = request)

        viewModelScope.launch {
            runCatching {
                userRepository.getUserNotifications(
                    page = request.page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    resetNotificationCount = true,
                ).getOrThrow()
            }.onSuccess { result ->
                if (activeRequest != request) return@onSuccess
                activeRequest = null

                if (request.isRefresh) {
                    pages.clear()
                }
                pages[request.page] = result.notifications.toList()
                snapshot = snapshotFromPages(
                    pages = pages,
                    result = result,
                    request = request,
                )
                _state.value = UiState.Success(snapshot = snapshot, request = request)
            }.onFailure { throwable ->
                if (activeRequest != request) return@onFailure
                Timber.e(throwable, "NotificationViewModel load failed")
                activeRequest = null
                _state.value = UiState.Error(
                    snapshot = snapshot,
                    request = request,
                    message = throwable.message ?: "Failed to load notifications",
                )
            }
        }
    }

    private fun snapshotFromPages(
        pages: Map<Int, List<NotificationRecord>>,
        result: NotificationPageResult,
        request: PageRequest,
    ): Snapshot {
        val uniqueById = linkedMapOf<Long, NotificationRecord>()
        pages.forEach { (_, pageItems) ->
            pageItems.forEach { item ->
                uniqueById[item.id] = item
            }
        }

        val previouslyLoadedIds = if (request.isRefresh) {
            emptySet()
        } else {
            snapshot.notifications.mapTo(mutableSetOf()) { it.id }
        }
        val seenPageIds = mutableSetOf<Long>()
        val lastPageUniqueCount = result.notifications.count { item ->
            val firstOnPage = seenPageIds.add(item.id)
            firstOnPage && item.id !in previouslyLoadedIds
        }

        return Snapshot(
            notifications = uniqueById.values.toList(),
            pageInfo = result.pageInfo,
            lastLoadedPage = request.page,
            nextPage = request.page + 1,
            hasMore = result.pageInfo?.hasNextPage ?: result.notifications.isNotEmpty(),
            hasLoadedPage = true,
            lastPageUniqueCount = lastPageUniqueCount,
        )
    }
}
