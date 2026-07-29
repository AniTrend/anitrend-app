package com.mxt.anitrend.data.store.medialist

import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.data.store.StoreInvariantValidator
import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryMediaListStore : MediaListStore {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(MediaListStoreState())
    private val deletionRevisions = mutableMapOf<Long, Long>()

    override val state: StateFlow<MediaListStoreState> = mutableState.asStateFlow()

    override suspend fun apply(change: MediaListStoreChange) {
        mutex.withLock {
            val updatedState =
                when (change) {
                    is MediaListStoreChange.CollectionLoaded -> reduceCollectionLoaded(change)
                    is MediaListStoreChange.EntryUpserted -> reduceEntryUpserted(change.entry)
                    is MediaListStoreChange.EntryDeleted -> reduceEntryDeleted(change.entryId, change.mediaId, change.revision)
                }

            if (BuildConfig.DEBUG) {
                StoreInvariantValidator.validateMediaListState(updatedState)
            }

            mutableState.value = updatedState
        }
    }

    override fun observeEntryByMediaId(mediaId: Long): Flow<MediaListRecord?> =
        state.map { currentState ->
            currentState.entryIdByMediaId[mediaId]?.let(currentState.entriesById::get)
        }.distinctUntilChanged()

    override fun observeQuery(key: MediaListQueryKey): Flow<MediaListQueryResult> =
        state.map { currentState ->
            val snapshot = currentState.queries[key]
            MediaListQueryResult(
                entries = snapshot?.orderedEntryIds.orEmpty().mapNotNull(currentState.entriesById::get),
                pageInfo = snapshot?.pageInfo,
            )
        }.distinctUntilChanged()

    private fun reduceCollectionLoaded(change: MediaListStoreChange.CollectionLoaded): MediaListStoreState {
        val currentState = mutableState.value
        val entriesById = currentState.entriesById.toMutableMap()
        val entryIdByMediaId = currentState.entryIdByMediaId.toMutableMap()
        val acceptedEntryIds = mutableListOf<Long>()

        change.entries.forEach { entry ->
            val entryKey = effectiveEntryKey(entry)
            val staleRevision = maxOf(
                currentRevisionForEntry(entry, currentState),
                deletionRevisions[entryKey] ?: Long.MIN_VALUE,
                deletionRevisions[entry.mediaId] ?: Long.MIN_VALUE,
            )
            if (entry.revision >= staleRevision) {
                deletionRevisions.remove(entryKey)
                deletionRevisions.remove(entry.mediaId)
                val previousKey = entryIdByMediaId[entry.mediaId]
                if (previousKey != null && previousKey != entryKey) {
                    entriesById.remove(previousKey)
                }
                entriesById[entryKey] = entry.copy(id = entryKey)
                entryIdByMediaId[entry.mediaId] = entryKey
            }
            if (entriesById.containsKey(entryKey)) {
                acceptedEntryIds += entryKey
            }
        }

        val existingSnapshot = currentState.queries[change.queryKey]
        val currentPage = change.pageInfo?.currentPage ?: 1
        val orderedEntryIds =
            if (currentPage <= 1) {
                acceptedEntryIds.distinct()
            } else {
                (existingSnapshot?.orderedEntryIds.orEmpty() + acceptedEntryIds).distinct()
            }
        val loadedPages =
            if (currentPage <= 1) {
                setOf(currentPage)
            } else {
                existingSnapshot?.loadedPages.orEmpty() + currentPage
            }
        val queries = currentState.queries.toMutableMap().apply {
            put(
                change.queryKey,
                MediaListQuerySnapshot(
                    orderedEntryIds = orderedEntryIds,
                    pageInfo = change.pageInfo,
                    loadedPages = loadedPages,
                    lastUpdatedAtMillis = System.currentTimeMillis(),
                ),
            )
        }

        return currentState.copy(
            entriesById = entriesById,
            entryIdByMediaId = entryIdByMediaId,
            queries = queries,
        )
    }

    private fun reduceEntryUpserted(entry: MediaListRecord): MediaListStoreState {
        val currentState = mutableState.value
        val entryKey = effectiveEntryKey(entry)
        val currentRevision = maxOf(
            currentRevisionForEntry(entry, currentState),
            deletionRevisions[entryKey] ?: Long.MIN_VALUE,
            deletionRevisions[entry.mediaId] ?: Long.MIN_VALUE,
        )
        if (entry.revision < currentRevision) {
            return currentState
        }

        deletionRevisions.remove(entryKey)
        deletionRevisions.remove(entry.mediaId)

        val entriesById = currentState.entriesById.toMutableMap()
        val entryIdByMediaId = currentState.entryIdByMediaId.toMutableMap()
        val previousKey = entryIdByMediaId[entry.mediaId]
        if (previousKey != null && previousKey != entryKey) {
            entriesById.remove(previousKey)
        }
        entriesById[entryKey] = entry.copy(id = entryKey)
        entryIdByMediaId[entry.mediaId] = entryKey

        val interimState = currentState.copy(
            entriesById = entriesById,
            entryIdByMediaId = entryIdByMediaId,
        )
        val queries = updateQueryMembership(interimState, entry.copy(id = entryKey))

        return interimState.copy(queries = queries)
    }

    private fun reduceEntryDeleted(
        entryId: Long,
        mediaId: Long?,
        revision: Long,
    ): MediaListStoreState {
        val currentState = mutableState.value
        val resolvedEntryId = resolveEntryKey(entryId = entryId, mediaId = mediaId, state = currentState)
        val currentEntry = resolvedEntryId?.let(currentState.entriesById::get)
        val effectiveMediaId = mediaId ?: currentEntry?.mediaId
        val currentRevision = maxOf(
            currentEntry?.revision ?: Long.MIN_VALUE,
            resolvedEntryId?.let { deletionRevisions[it] } ?: Long.MIN_VALUE,
            effectiveMediaId?.let { deletionRevisions[it] } ?: Long.MIN_VALUE,
        )
        if (revision < currentRevision) {
            return currentState
        }

        resolvedEntryId?.let { deletionRevisions[it] = revision }
        effectiveMediaId?.let { deletionRevisions[it] = revision }

        val entriesById = currentState.entriesById.toMutableMap().apply {
            resolvedEntryId?.let(::remove)
        }
        val entryIdByMediaId = currentState.entryIdByMediaId.toMutableMap().apply {
            if (effectiveMediaId != null) {
                remove(effectiveMediaId)
            }
        }
        val queries = currentState.queries.mapValues { (_, snapshot) ->
            snapshot.copy(
                orderedEntryIds = snapshot.orderedEntryIds.filterNot { it == resolvedEntryId },
            )
        }

        return currentState.copy(
            entriesById = entriesById,
            entryIdByMediaId = entryIdByMediaId,
            queries = queries,
        )
    }

    private fun currentRevisionForEntry(
        entry: MediaListRecord,
        state: MediaListStoreState,
    ): Long {
        val existingKey = state.entryIdByMediaId[entry.mediaId]
        return maxOf(
            state.entriesById[effectiveEntryKey(entry)]?.revision ?: Long.MIN_VALUE,
            existingKey?.let(state.entriesById::get)?.revision ?: Long.MIN_VALUE,
        )
    }

    private fun effectiveEntryKey(entry: MediaListRecord): Long =
        if (entry.id != 0L) entry.id else entry.mediaId

    private fun resolveEntryKey(
        entryId: Long,
        mediaId: Long?,
        state: MediaListStoreState,
    ): Long? = when {
        entryId != 0L && state.entriesById.containsKey(entryId) -> entryId
        mediaId != null -> state.entryIdByMediaId[mediaId] ?: mediaId.takeIf { state.entriesById.containsKey(it) }
        entryId != 0L -> entryId
        else -> null
    }

    private fun updateQueryMembership(
        state: MediaListStoreState,
        entry: MediaListRecord,
    ): Map<MediaListQueryKey, MediaListQuerySnapshot> =
        state.queries.mapValues { (queryKey, snapshot) ->
            val existingIds = snapshot.orderedEntryIds.filterNot { it == entry.id }
            val currentlyContained = snapshot.orderedEntryIds.contains(entry.id)
            val matches = entry.matches(queryKey)
            val shouldAdd = matches && queryKey.userId == null && queryKey.userName == null

            val nextIds = when {
                currentlyContained && matches -> existingIds + entry.id
                currentlyContained && !matches -> existingIds
                shouldAdd -> existingIds + entry.id
                else -> existingIds
            }

            snapshot.copy(
                orderedEntryIds = reorderEntryIds(nextIds.distinct(), state.entriesById + (entry.id to entry), queryKey.sort),
            )
        }

    private fun reorderEntryIds(
        entryIds: List<Long>,
        entriesById: Map<Long, MediaListRecord>,
        sort: MediaListSort?,
    ): List<Long> {
        if (sort == null) {
            return entryIds
        }

        val comparator = mediaListRecordComparator(sort) ?: return entryIds
        return entryIds
            .mapNotNull(entriesById::get)
            .sortedWith(comparator)
            .map(::effectiveEntryKey)
    }

    private fun mediaListRecordComparator(sort: MediaListSort): Comparator<MediaListRecord>? {
        val descending = sort.name.endsWith("_DESC")
        val baseComparator =
            when (sort.name.removeSuffix("_DESC")) {
                "ADDED_TIME" -> compareBy<MediaListRecord> { it.revision }
                "FINISHED_ON" -> compareBy<MediaListRecord>({ it.completedAt?.year }, { it.completedAt?.month }, { it.completedAt?.day })
                "MEDIA_ID" -> compareBy<MediaListRecord> { it.mediaId }
                "MEDIA_POPULARITY" -> null
                "MEDIA_TITLE_ENGLISH" -> compareBy<MediaListRecord> { it.media?.titleEnglish.orEmpty() }
                "MEDIA_TITLE_NATIVE" -> compareBy<MediaListRecord> { it.media?.titleOriginal.orEmpty() }
                "MEDIA_TITLE_ROMAJI" -> compareBy<MediaListRecord> { it.media?.titleRomaji.orEmpty() }
                "PRIORITY" -> compareBy<MediaListRecord> { it.priority }
                "PROGRESS" -> compareBy<MediaListRecord> { it.progress }
                "PROGRESS_VOLUMES" -> compareBy<MediaListRecord> { it.progressVolumes }
                "REPEAT" -> compareBy<MediaListRecord> { it.repeat }
                "SCORE" -> compareBy<MediaListRecord> { it.score }
                "STARTED_ON" -> compareBy<MediaListRecord>({ it.startedAt?.year }, { it.startedAt?.month }, { it.startedAt?.day })
                "STATUS" -> compareBy<MediaListRecord> { it.status.orEmpty() }
                "UPDATED_TIME" -> compareBy<MediaListRecord> { it.revision }
                else -> null
            } ?: return null

        val comparator = baseComparator.thenBy { it.mediaId }
        return if (descending) comparator.reversed() else comparator
    }

    private fun MediaListRecord.matches(queryKey: MediaListQueryKey): Boolean {
        val matchesMediaType = queryKey.mediaType?.name == null || media?.type == queryKey.mediaType.name
        val matchesStatus =
            if (queryKey.statuses.isEmpty()) {
                true
            } else {
                status?.let { rawStatus ->
                    runCatching { MediaListStatus.valueOf(rawStatus) }.getOrNull() in queryKey.statuses
                } == true
            }
        return matchesMediaType && matchesStatus
    }
}
