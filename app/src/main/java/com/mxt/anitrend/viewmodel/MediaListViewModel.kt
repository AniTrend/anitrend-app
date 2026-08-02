package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.mutation.MutationRegistry
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.OperationStatus
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.domain.model.MediaListItemUiModel
import com.mxt.anitrend.domain.model.toMediaListItemUiModel
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaListUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MediaListViewModel(
    private val browseRepository: BrowseRepository,
    private val mediaListStore: MediaListStore,
    private val mutationRegistry: MutationRegistry,
    private val userRepository: UserRepository,
    private val settings: Settings,
    private val requestSequence: RequestSequence,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val entries: List<MediaListRecord>,
            val renderedItems: List<MediaListItemUiModel>,
            val pageInfo: com.mxt.anitrend.model.entity.container.attribute.PageInfo?,
            val isEmpty: Boolean,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private data class ScreenState(
        val queryKey: MediaListQueryKey? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val requestToken: Long = 0L,
        val isCurrentUser: Boolean = false,
        val titleSortVersion: Int = 0,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val state: StateFlow<UiState> =
        screenState
            .flatMapLatest { screen ->
                val queryKey = screen.queryKey ?: return@flatMapLatest flowOf(
                    if (screen.errorMessage != null) {
                        UiState.Error(screen.errorMessage)
                    } else {
                        UiState.Loading
                    },
                )

                combine(
                    mediaListStore.observeQuery(queryKey),
                    mutationRegistry.state,
                    flowOf(screen),
                ) { query, operations, currentScreen ->
                    val sortedEntries = sortEntriesIfNeeded(query.entries)
                    val renderedItems = sortedEntries.map { entry ->
                        entry.toMediaListItemUiModel(
                            isIncrementPending = operations.isIncrementPending(entry.id, entry.mediaId),
                            isDeletePending = operations.isDeletePending(entry.id, entry.mediaId),
                            canIncrement = entry.canIncrement(currentScreen.isCurrentUser),
                        )
                    }
                    when {
                        currentScreen.errorMessage != null -> {
                            UiState.Error(currentScreen.errorMessage)
                        }
                        currentScreen.isLoading && renderedItems.isEmpty() -> {
                            UiState.Loading
                        }
                        else -> {
                            UiState.Success(
                                entries = sortedEntries,
                                renderedItems = renderedItems,
                                pageInfo = query.pageInfo?.toPageInfo(),
                                isEmpty = renderedItems.isEmpty(),
                            )
                        }
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading,
            )

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
        val isCurrentUser = isCurrentUser(userId, userName)
        val token = requestSequence.next()
        val mediaListSort = settings.mediaListSort ?: KeyUtil.PROGRESS
        val sortString = if (!MediaListUtil.isTitleSort(mediaListSort)) {
            mediaListSort + settings.sortOrder
        } else {
            KeyUtil.MEDIA_ID + settings.sortOrder
        }
        val sort: List<MediaListSort>? = runCatching { listOf(MediaListSort.valueOf(sortString)) }.getOrNull()
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
        screenState.update {
            it.copy(
                queryKey = queryKey,
                isLoading = true,
                errorMessage = null,
                requestToken = token,
                isCurrentUser = isCurrentUser,
            )
        }

        viewModelScope.launch {
            runCatching {
                withContext(ioDispatcher) {
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
                        readToken = token,
                    )
                    result.getOrThrow()
                }
            }.onSuccess {
                if (screenState.value.requestToken != token) {
                    return@onSuccess
                }
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                if (screenState.value.requestToken != token) {
                    return@onFailure
                }
                Timber.e(throwable, "MediaListViewModel load failed")
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load media list",
                    )
                }
            }
        }
    }

    /**
     * Re-sorts the current items in-place when a title-based sort preference changes.
     * Does not re-fetch from the API.
     */
    fun onSortPreferenceChanged() {
        val mediaListSort = settings.mediaListSort ?: KeyUtil.PROGRESS
        if (MediaListUtil.isTitleSort(mediaListSort)) {
            screenState.update { current ->
                current.copy(titleSortVersion = current.titleSortVersion + 1)
            }
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

    private fun sortEntriesIfNeeded(entries: List<MediaListRecord>): List<MediaListRecord> {
        val mediaListSort = settings.mediaListSort ?: KeyUtil.PROGRESS
        if (!MediaListUtil.isTitleSort(mediaListSort)) {
            return entries
        }

        return entries.sortedWith { first, second ->
            val firstTitle = first.media?.titleUserPreferred ?: first.media?.titleRomaji ?: first.media?.titleEnglish ?: first.media?.titleOriginal.orEmpty()
            val secondTitle = second.media?.titleUserPreferred ?: second.media?.titleRomaji ?: second.media?.titleEnglish ?: second.media?.titleOriginal.orEmpty()
            if (CompatUtil.equals(settings.sortOrder, KeyUtil.ASC)) {
                firstTitle.compareTo(secondTitle)
            } else {
                secondTitle.compareTo(firstTitle)
            }
        }
    }

    private fun Map<OperationKey, OperationStatus>.isIncrementPending(
        entryId: Long,
        mediaId: Long,
    ): Boolean = listOfNotNull(
        OperationKey.mediaListIncrementProgress(mediaId),
        OperationKey.mediaListSave(mediaId),
        entryId.takeIf { it > 0 }?.let(OperationKey::mediaListSaveById),
    ).any { this[it] is OperationStatus.Running }

    private fun Map<OperationKey, OperationStatus>.isDeletePending(
        entryId: Long,
        mediaId: Long,
    ): Boolean = listOfNotNull(
        OperationKey.mediaListDeleteByMedia(mediaId),
        entryId.takeIf { it > 0 }?.let(OperationKey::mediaListDelete),
    ).any { this[it] is OperationStatus.Running }

    private fun MediaListRecord.canIncrement(isCurrentUser: Boolean): Boolean {
        if (!isCurrentUser) {
            return false
        }

        val mediaSummary = media ?: return false
        if (CompatUtil.equals(mediaSummary.status, KeyUtil.NOT_YET_RELEASED)) {
            return false
        }

        return if (CompatUtil.equals(mediaSummary.type, KeyUtil.ANIME)) {
            mediaSummary.episodes == 0 || progress < mediaSummary.episodes
        } else {
            mediaSummary.chapters == 0 || progress < mediaSummary.chapters
        }
    }
}
