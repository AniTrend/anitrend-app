package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.mapper.toMediaList
import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.MediaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MediaViewModel(
    private val mediaRepository: MediaRepository,
    private val baseRepository: BaseRepository,
    private val mediaListStore: MediaListStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val media: com.mxt.anitrend.model.entity.base.MediaBase) : UiState
        data class Error(val message: String) : UiState
    }

    private val mutableState = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = mutableState.asStateFlow()

    private var loadedOnce = false
    private var storeObservationJob: Job? = null

    /**
     * Loads the media by its AniList ID. Safe to call multiple times -- skips
     * the network call after the first successful load.
     *
     * @param mediaId   AniList media ID
     * @param mediaType "ANIME", "MANGA", or null
     * @param showAdult whether adult content should be included;
     *                  false (the default) excludes adult entries
     */
    fun load(
        mediaId: Long,
        mediaType: String?,
        showAdult: Boolean,
    ) {
        if (loadedOnce) return
        viewModelScope.launch {
            mutableState.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val typeEnum: MediaType? = mediaType?.let {
                        try {
                            MediaType.valueOf(it)
                        } catch (_: IllegalArgumentException) {
                            null
                        }
                    }
                    val isAdult: Boolean? = if (showAdult) null else false
                    mediaRepository.getMediaBase(mediaId, typeEnum, isAdult).getOrThrow()
                }
            }.onSuccess { media ->
                media.mediaListEntry?.let { mediaListEntry ->
                    mediaListStore.apply(
                        MediaListStoreChange.EntryUpserted(
                            entry = mediaListEntry.toMediaListRecord(revision = 0L),
                        ),
                    )
                }
                storeObservationJob?.cancel()
                storeObservationJob = viewModelScope.launch {
                    mediaListStore.observeEntryByMediaId(media.id).collect { entry ->
                        mutableState.value = UiState.Success(
                            media.copyWithMediaListEntry(entry?.toMediaList()),
                        )
                    }
                }
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaViewModel load failed")
                mutableState.value = UiState.Error(throwable.message ?: "Failed to load media")
            }
        }
    }

    suspend fun toggleFavourite(
        animeId: Int?,
        mangaId: Int?,
        characterId: Int?,
        staffId: Int?,
        studioId: Int?,
    ): Result<Unit> = withContext(ioDispatcher) {
        baseRepository.toggleFavourite(animeId, mangaId, characterId, staffId, studioId)
    }

    private fun com.mxt.anitrend.model.entity.base.MediaBase.copyWithMediaListEntry(
        entry: com.mxt.anitrend.model.entity.anilist.MediaList?,
    ): com.mxt.anitrend.model.entity.base.MediaBase = com.mxt.anitrend.model.entity.base.MediaBase().also { copy ->
        copy.id = id
        copy.idMal = idMal
        copy.title = title
        copy.coverImage = coverImage
        copy.bannerImage = bannerImage
        copy.type = type
        copy.format = format
        copy.season = season
        copy.status = status
        copy.siteUrl = siteUrl
        copy.meanScore = meanScore
        copy.averageScore = averageScore
        copy.startDate = startDate
        copy.endDate = endDate
        copy.episodes = episodes
        copy.duration = duration
        copy.chapters = chapters
        copy.volumes = volumes
        copy.isAdult = isAdult
        copy.isFavourite = isFavourite
        copy.nextAiringEpisode = nextAiringEpisode
        copy.mediaListEntry = entry
    }
}
