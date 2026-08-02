package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.mapper.toMediaList
import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.data.store.favourite.FavouriteFlag
import com.mxt.anitrend.data.store.favourite.FavouriteStore
import com.mxt.anitrend.data.store.favourite.FavouriteStoreChange
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.domain.favourite.interactor.ToggleFavouriteInteractor
import com.mxt.anitrend.domain.favourite.model.FavouriteKey
import com.mxt.anitrend.domain.model.ToggleFavouriteCommand
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.KeyUtil
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
    private val favouriteStore: FavouriteStore,
    private val toggleFavouriteInteractor: ToggleFavouriteInteractor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val media: com.mxt.anitrend.model.entity.base.MediaBase) : UiState
        data class Error(val message: String) : UiState
    }

    private val mutableState = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = mutableState.asStateFlow()

    /**
     * Committed favourite flag for the loaded media, mirrored from the canonical
     * [FavouriteStore]. Null until the media has loaded and the store has been observed.
     */
    private val _favouriteFlag = MutableStateFlow<FavouriteFlag?>(null)
    val favouriteFlag: StateFlow<FavouriteFlag?> = _favouriteFlag.asStateFlow()

    /**
     * True while a favourite toggle for the loaded media is in flight (server call plus
     * store commit). The widget shows its bounded loading state from this flag.
     */
    private val _favouriteLoading = MutableStateFlow(false)
    val favouriteLoading: StateFlow<Boolean> = _favouriteLoading.asStateFlow()

    private var loadedOnce = false
    private var storeObservationJob: Job? = null

    /**
     * Typed favourite key resolved from the loaded media. Null until load succeeds so the
     * toggle action can fall back to the caller-provided [mediaType] before the entity is
     * available. Anime media map to [FavouriteKey.Anime] and everything else to
     * [FavouriteKey.Manga], matching the legacy widget mapping.
     */
    private var mediaKey: FavouriteKey? = null

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
                val key = favouriteKeyFor(media.id, media.type)
                mediaKey = key
                seedFavouriteFlag(key, media.isFavourite)
                observeFavouriteFlag(key)
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaViewModel load failed")
                mutableState.value = UiState.Error(throwable.message ?: "Failed to load media")
            }
        }
    }

    /**
     * Fire-and-forget favourite toggle for the loaded media. Runs the typed
     * [ToggleFavouriteCommand] through [ToggleFavouriteInteractor]; the committed result
     * surfaces via [favouriteFlag] once the canonical store applies it. The key is resolved
     * from the loaded media type, falling back to the caller-provided [mediaType] when the
     * entity has not loaded yet.
     */
    fun toggleFavouriteMedia(
        mediaId: Long,
        mediaType: String?,
    ) {
        val key = mediaKey ?: favouriteKeyFor(mediaId, mediaType)
        viewModelScope.launch {
            _favouriteLoading.value = true
            try {
                toggleFavouriteInteractor(ToggleFavouriteCommand(key))
            } finally {
                _favouriteLoading.value = false
            }
        }
    }

    /**
     * Resolves the typed favourite key for a media entity. Anime media map to
     * [FavouriteKey.Anime]; all other types (including unknown/null) map to
     * [FavouriteKey.Manga], matching the legacy widget mapping.
     */
    private fun favouriteKeyFor(
        mediaId: Long,
        mediaType: String?,
    ): FavouriteKey = if (mediaType == KeyUtil.ANIME) FavouriteKey.Anime(mediaId) else FavouriteKey.Manga(mediaId)

    /**
     * Seeds the canonical store from the initially loaded [com.mxt.anitrend.model.entity.base.MediaBase.isFavourite]
     * only when no committed store value exists yet. The legacy model is never mutated.
     */
    private suspend fun seedFavouriteFlag(
        key: FavouriteKey,
        isFavourite: Boolean,
    ) {
        if (favouriteStore.state.value.flagsByKey[key] == null) {
            favouriteStore.apply(
                FavouriteStoreChange.FavouriteFlagReplaced(
                    key = key,
                    isFavourite = isFavourite,
                    revision = FAVOURITE_SEED_REVISION,
                ),
            )
        }
    }

    private fun observeFavouriteFlag(key: FavouriteKey) {
        viewModelScope.launch {
            favouriteStore.observeFavourite(key).collect { flag ->
                _favouriteFlag.value = flag
            }
        }
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

    companion object {
        /**
         * Revision used when seeding the store from the initially loaded entity flag. It is
         * lower than the first mutation revision so a committed mutation always wins, and the
         * seed is only applied when no committed value exists for the key.
         */
        internal const val FAVOURITE_SEED_REVISION = 0L
    }
}
