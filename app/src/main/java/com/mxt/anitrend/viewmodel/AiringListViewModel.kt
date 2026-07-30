package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.mapper.toMediaList
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.mutation.MutationRegistry
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.OperationStatus
import com.mxt.anitrend.domain.model.MediaListItemUiModel
import com.mxt.anitrend.domain.model.toMediaListItemUiModel
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class AiringListViewModel(
    private val browseRepository: BrowseRepository,
    private val mediaListStore: MediaListStore,
    private val mutationRegistry: MutationRegistry,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val items: List<MediaList>,
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
        val requestGeneration: Int = 0,
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
                    val airingEntries =
                        sortEntriesIfNeeded(
                            entries = query.entries.filter { entry ->
                                CompatUtil.equals(entry.media?.status, KeyUtil.RELEASING)
                            },
                            sort = queryKey.sort,
                        )
                    val renderedItems = airingEntries.map { entry ->
                        entry.toMediaListItemUiModel(
                            isIncrementPending = operations.isIncrementPending(entry.id, entry.mediaId),
                            isDeletePending = operations.isDeletePending(entry.id, entry.mediaId),
                            canIncrement = entry.canIncrement(),
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
                                items = airingEntries.map { it.toMediaList() },
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
     * Loads airing media list collection. Single load; not paginated.
     */
    fun load(
        type: MediaType,
        userId: Int,
        sort: String?,
        statusIn: String?,
        scoreFormat: ScoreFormat?,
    ) {
        val sortList = sort?.let { runCatching { MediaListSort.valueOf(it) }.getOrNull()?.let(::listOf) }
        val statusList = statusIn?.let { runCatching { MediaListStatus.valueOf(it) }.getOrNull()?.let(::listOf) }
        val queryKey = MediaListQueryKey(
            userId = userId.toLong(),
            userName = null,
            mediaType = type,
            statuses = statusList.orEmpty().toSet(),
            sort = sortList?.firstOrNull(),
        )
        viewModelScope.launch {
            val generation = screenState.value.requestGeneration + 1
            screenState.update {
                it.copy(
                    queryKey = queryKey,
                    isLoading = true,
                    errorMessage = null,
                    requestGeneration = generation,
                )
            }
            runCatching {
                browseRepository.getMediaListCollection(
                    userId = userId.toLong(),
                    type = type,
                    forceSingleCompletedList = true,
                    sort = sortList,
                    statusIn = statusList,
                    scoreFormat = scoreFormat ?: ScoreFormat.POINT_100,
                    queryKey = queryKey,
                ).getOrThrow()
            }.onSuccess {
                if (screenState.value.requestGeneration != generation) {
                    return@onSuccess
                }
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                Timber.e(throwable, "AiringListViewModel load failed")
                if (screenState.value.requestGeneration != generation) {
                    return@onFailure
                }
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load airing list",
                    )
                }
            }
        }
    }

    private fun sortEntriesIfNeeded(
        entries: List<com.mxt.anitrend.domain.medialist.model.MediaListRecord>,
        sort: MediaListSort?,
    ): List<com.mxt.anitrend.domain.medialist.model.MediaListRecord> {
        val resolvedSort = sort ?: return entries
        if (!resolvedSort.name.startsWith("MEDIA_ID")) {
            return entries
        }

        val descending = resolvedSort.name.endsWith("_DESC")
        return entries.sortedWith { first, second ->
            val firstTitle = first.media?.titleUserPreferred ?: first.media?.titleRomaji ?: first.media?.titleEnglish ?: first.media?.titleOriginal.orEmpty()
            val secondTitle = second.media?.titleUserPreferred ?: second.media?.titleRomaji ?: second.media?.titleEnglish ?: second.media?.titleOriginal.orEmpty()
            if (descending) {
                secondTitle.compareTo(firstTitle)
            } else {
                firstTitle.compareTo(secondTitle)
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

    private fun com.mxt.anitrend.domain.medialist.model.MediaListRecord.canIncrement(): Boolean {
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
