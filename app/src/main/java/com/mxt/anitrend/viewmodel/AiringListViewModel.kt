package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseMutation
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.CompatUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AiringListViewModel(
    private val browseRepository: BrowseRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<MediaListCollection>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()
    private var lastType: MediaType? = null
    private var lastUserId: Int? = null
    private var lastSort: String? = null
    private var lastStatusIn: String? = null
    private var lastScoreFormat: ScoreFormat? = null

    init {
        viewModelScope.launch {
            browseRepository.mutationEvents.collect { event ->
                when (event) {
                    is BrowseMutation.MediaListSaved -> onMediaListSaved(event.entry)
                    is BrowseMutation.MediaListDeleted -> reloadIfPossible()
                    else -> Unit
                }
            }
        }
    }

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
        lastType = type
        lastUserId = userId
        lastSort = sort
        lastStatusIn = statusIn
        lastScoreFormat = scoreFormat
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                val sortList = sort?.let { runCatching { MediaListSort.valueOf(it) }.getOrNull()?.let(::listOf) }
                val statusList = statusIn?.let { runCatching { MediaListStatus.valueOf(it) }.getOrNull()?.let(::listOf) }
                browseRepository.getMediaListCollection(
                    userId = userId.toLong(),
                    type = type,
                    forceSingleCompletedList = true,
                    sort = sortList,
                    statusIn = statusList,
                    scoreFormat = scoreFormat ?: ScoreFormat.POINT_100,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "AiringListViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load airing list",
                )
            }
        }
    }

    private fun reloadIfPossible() {
        val type = lastType ?: return
        val userId = lastUserId ?: return
        if (_state.value !is UiState.Success) {
            return
        }
        load(
            type = type,
            userId = userId,
            sort = lastSort,
            statusIn = lastStatusIn,
            scoreFormat = lastScoreFormat,
        )
    }

    internal fun onMediaListSaved(entry: MediaList) {
        val current = (_state.value as? UiState.Success)?.content ?: return
        val matchesFilters = matchesLoadedFilters(entry)
        var didChange = false
        val updatedCollections = current.pageData.toMutableList()

        for ((index, collection) in current.pageData.withIndex()) {
            val existingEntries = collection.entries.orEmpty()
            val existingIndex = existingEntries.indexOfFirst { item ->
                item.id == entry.id || item.mediaId == entry.mediaId
            }

            if (existingIndex == -1 && !matchesFilters) {
                continue
            }

            val updatedEntries = existingEntries.toMutableList()
            when {
                existingIndex >= 0 && matchesFilters -> updatedEntries[existingIndex].mergeFrom(entry)
                existingIndex >= 0 -> updatedEntries.removeAt(existingIndex)
                matchesFilters && shouldAppendToCollection(collection, entry) -> updatedEntries.add(entry)
                else -> continue
            }

            updatedCollections[index] = collection.copyWithEntries(updatedEntries)
            didChange = true
        }

        if (!didChange) {
            return
        }

        _state.value = UiState.Success(
            PageContainer<MediaListCollection>().apply {
                if (current.hasPageInfo()) {
                    pageInfo = current.pageInfo
                }
                pageData = updatedCollections
            },
        )
    }

    private fun matchesLoadedFilters(entry: MediaList): Boolean {
        val loadedTypeName = lastType?.name
        val loadedStatusIn = lastStatusIn
        val matchesType = loadedTypeName?.let { CompatUtil.equals(entry.media.type, it) } ?: true
        val matchesStatus = loadedStatusIn?.let { CompatUtil.equals(entry.status, it) } ?: true
        return matchesType && matchesStatus
    }

    private fun shouldAppendToCollection(
        collection: MediaListCollection,
        entry: MediaList,
    ): Boolean = collection.status?.let { status ->
        entry.status?.let { entryStatus -> CompatUtil.equals(status, entryStatus) }
    } ?: (
        lastStatusIn?.let { loadedStatus ->
            entry.status?.let { entryStatus -> CompatUtil.equals(entryStatus, loadedStatus) }
        } ?: true
        )

    private fun MediaListCollection.copyWithEntries(entries: List<MediaList>): MediaListCollection = apply {
        setEntries(entries)
    }

    private fun MediaListCollection.setEntries(entries: List<MediaList>) {
        entriesField.set(this, entries)
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

    /** Reflection handle used to update collection entries in place. */
    companion object {
        private val entriesField =
            MediaListCollection::class.java.getDeclaredField("entries").apply {
                isAccessible = true
            }
    }
}
