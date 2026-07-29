package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseMutation
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.MediaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MediaViewModel(
    private val mediaRepository: MediaRepository,
    private val baseRepository: BaseRepository,
    private val browseRepository: BrowseRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val media: com.mxt.anitrend.model.entity.base.MediaBase) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var loadedOnce = false

    init {
        viewModelScope.launch {
            browseRepository.mutationEvents.collect { event ->
                when (event) {
                    is BrowseMutation.MediaListSaved -> onMediaListSaved(event.entry)
                    is BrowseMutation.MediaListDeleted -> onMediaListDeleted(event.id)
                    else -> Unit
                }
            }
        }
    }

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
            _state.value = UiState.Loading
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
                _state.value = UiState.Success(media)
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media",
                )
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

    internal fun onMediaListSaved(entry: com.mxt.anitrend.model.entity.anilist.MediaList) {
        val current = _state.value as? UiState.Success ?: return
        if (current.media.id != entry.mediaId) {
            return
        }
        current.media.mediaListEntry = entry
        _state.value = UiState.Success(current.media)
    }

    internal fun onMediaListDeleted(id: Long) {
        val current = _state.value as? UiState.Success ?: return
        if (current.media.mediaListEntry?.id != id) {
            return
        }
        current.media.mediaListEntry = null
        _state.value = UiState.Success(current.media)
    }
}
