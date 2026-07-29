package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.repository.BrowseMutation
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaListUtil
import com.mxt.anitrend.util.media.MediaUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MediaListViewModel(
    private val browseRepository: BrowseRepository,
    private val userRepository: UserRepository,
    private val settings: Settings,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val items: List<MediaList>,
            val pageInfo: PageInfo?,
            val isEmpty: Boolean,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var currentItems: List<MediaList> = emptyList()
    private var currentPageInfo: PageInfo? = null

    private var lastUserId: Long = 0
    private var lastUserName: String? = null
    private var lastMediaType: String? = null
    private var lastStatusIn: String? = null

    init {
        viewModelScope.launch {
            browseRepository.mutationEvents.collect { event ->
                when (event) {
                    is BrowseMutation.MediaListSaved -> onMediaListSaved(event.entry)
                    is BrowseMutation.MediaListDeleted -> {
                        if (currentItems.isNotEmpty()) {
                            load(lastUserId, lastUserName, lastMediaType, lastStatusIn)
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    /**
     * Loads media list collection. Single load; not paginated.
     * Argument assembly, sorting, and preference reads are handled internally.
     */
    fun load(
        userId: Long,
        userName: String?,
        mediaType: String?,
        statusIn: String?,
    ) {
        lastUserId = userId
        lastUserName = userName
        lastMediaType = mediaType
        lastStatusIn = statusIn
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val mediaListSort = settings.mediaListSort ?: KeyUtil.PROGRESS
                    val sortString = if (!MediaListUtil.isTitleSort(mediaListSort)) {
                        mediaListSort + settings.sortOrder
                    } else {
                        KeyUtil.MEDIA_ID + settings.sortOrder
                    }
                    val sort: List<MediaListSort>? =
                        runCatching { listOf(MediaListSort.valueOf(sortString)) }.getOrNull()
                    val statusList: List<MediaListStatus>? =
                        statusIn?.let { runCatching { listOf(MediaListStatus.valueOf(it)) }.getOrNull() }
                    val type: MediaType? = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
                    val queryKey = MediaListQueryKey(
                        userId = if (userId != 0L) userId else null,
                        userName = if (userId == 0L) userName else null,
                        mediaType = type,
                        statuses = statusList.orEmpty().toSet(),
                        sort = sort?.firstOrNull(),
                    )
                    val scoreFormat: ScoreFormat =
                        userRepository.cachedCurrentUser?.mediaListOptions?.let { options ->
                            runCatching { ScoreFormat.valueOf(options.scoreFormat) }.getOrNull()
                        } ?: ScoreFormat.POINT_100
                    val result = browseRepository.getMediaListCollection(
                        userId = if (userId != 0L) userId else null,
                        userName = if (userId == 0L) userName else null,
                        type = type,
                        forceSingleCompletedList = true,
                        sort = sort,
                        statusIn = statusList,
                        scoreFormat = scoreFormat,
                        queryKey = queryKey,
                    )
                    result.getOrThrow()
                }
            }.onSuccess { content ->
                val pageInfo = if (content.hasPageInfo()) content.pageInfo else null
                val entries = if (!content.isEmpty) {
                    content.pageData.firstOrNull()?.entries.orEmpty()
                } else {
                    emptyList()
                }
                emitSuccess(entries, pageInfo, content.isEmpty)
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaListViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media list",
                )
            }
        }
    }

    /**
     * Re-sorts the current items in-place when a title-based sort preference changes.
     * Does not re-fetch from the API.
     */
    fun onSortPreferenceChanged() {
        if (currentItems.isEmpty()) return
        val mediaListSort = settings.mediaListSort ?: KeyUtil.PROGRESS
        if (MediaListUtil.isTitleSort(mediaListSort)) {
            val sorted = sortMediaListByTitle(currentItems, settings.sortOrder)
            currentItems = sorted
            _state.value = UiState.Success(
                items = sorted,
                pageInfo = currentPageInfo,
                isEmpty = sorted.isEmpty(),
            )
        }
    }

    fun isCurrentUser(
        userId: Long,
        userName: String?,
    ): Boolean = settings.isAuthenticated &&
        userRepository.cachedCurrentUser != null &&
        (
            userName?.let { userRepository.cachedCurrentUser?.name == it }
                ?: (userId != 0L && userRepository.cachedCurrentUser?.id == userId)
            )

    internal fun onMediaListSaved(entry: MediaList) {
        val current = _state.value as? UiState.Success ?: return
        val existingIndex = currentItems.indexOfFirst { item ->
            item.id == entry.id || item.mediaId == entry.mediaId
        }
        val matchesFilters = matchesLoadedFilters(entry)
        if (existingIndex == -1 && !matchesFilters) {
            return
        }

        val updatedItems = currentItems.toMutableList()
        when {
            existingIndex >= 0 && matchesFilters -> updatedItems[existingIndex].mergeFrom(entry)
            existingIndex >= 0 -> updatedItems.removeAt(existingIndex)
            matchesFilters -> updatedItems.add(entry)
        }

        emitSuccess(
            items = updatedItems,
            pageInfo = current.pageInfo,
            isEmpty = updatedItems.isEmpty(),
        )
    }

    private fun matchesLoadedFilters(entry: MediaList): Boolean {
        val loadedMediaType = lastMediaType
        val loadedStatusIn = lastStatusIn
        val matchesMediaType =
            loadedMediaType?.let { CompatUtil.equals(entry.media.type, it) } ?: true
        val matchesStatus =
            loadedStatusIn?.let { CompatUtil.equals(entry.status, it) } ?: true
        return matchesMediaType && matchesStatus
    }

    private fun emitSuccess(
        items: List<MediaList>,
        pageInfo: PageInfo?,
        isEmpty: Boolean,
    ) {
        val mediaListSort = settings.mediaListSort ?: KeyUtil.PROGRESS
        val sorted = if (MediaListUtil.isTitleSort(mediaListSort)) {
            sortMediaListByTitle(items, settings.sortOrder)
        } else {
            items
        }
        currentItems = sorted
        currentPageInfo = pageInfo
        _state.value = UiState.Success(
            items = sorted,
            pageInfo = pageInfo,
            isEmpty = isEmpty,
        )
    }

    private fun MediaList.mergeFrom(entry: MediaList) {
        id = entry.id
        mediaId = entry.mediaId
        status = entry.status
        score = entry.score
        scoreRaw = entry.scoreRaw
        progress = entry.progress
        progressVolumes = entry.progressVolumes
        repeat = entry.repeat
        priority = entry.priority
        notes = entry.notes
        isHidden = entry.isHidden
        isHiddenFromStatusLists = entry.isHiddenFromStatusLists
        advancedScores = entry.advancedScores
        customLists = entry.customLists
        startedAt = entry.startedAt
        completedAt = entry.completedAt
        updatedAt = entry.updatedAt
        createdAt = entry.createdAt
        media = entry.media
    }

    companion object {
        fun sortMediaListByTitle(
            mediaLists: List<MediaList>,
            sortOrder: String,
        ): List<MediaList> = mediaLists.sortedWith { first, second ->
            val firstTitle = MediaUtil.getMediaTitle(first.media)
            val secondTitle = MediaUtil.getMediaTitle(second.media)
            if (CompatUtil.equals(sortOrder, KeyUtil.ASC)) {
                firstTitle.compareTo(secondTitle)
            } else {
                secondTitle.compareTo(firstTitle)
            }
        }
    }
}
