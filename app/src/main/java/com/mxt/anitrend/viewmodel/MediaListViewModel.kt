package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.repository.BrowseMutation
import com.mxt.anitrend.repository.BrowseRepository
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
    private val settings: Settings,
    private val databaseHelper: DatabaseHelper = DatabaseHelper(),
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
                if (currentItems.isNotEmpty() &&
                    (event is BrowseMutation.MediaListSaved || event is BrowseMutation.MediaListDeleted)
                ) {
                    load(lastUserId, lastUserName, lastMediaType, lastStatusIn)
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
                    val scoreFormat: ScoreFormat =
                        databaseHelper.currentUser?.mediaListOptions?.let { options ->
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
                val mediaListSort = settings.mediaListSort ?: KeyUtil.PROGRESS
                val sorted = if (MediaListUtil.isTitleSort(mediaListSort)) {
                    sortMediaListByTitle(entries, settings.sortOrder)
                } else {
                    entries
                }
                currentItems = sorted
                currentPageInfo = pageInfo
                _state.value = UiState.Success(
                    items = sorted,
                    pageInfo = pageInfo,
                    isEmpty = content.isEmpty,
                )
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
        databaseHelper.currentUser != null &&
        (
            userName?.let { databaseHelper.currentUser?.name == it }
                ?: (userId != 0L && databaseHelper.currentUser?.id == userId)
            )

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
