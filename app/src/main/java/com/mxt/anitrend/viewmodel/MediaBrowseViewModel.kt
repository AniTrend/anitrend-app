package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseMutation
import com.mxt.anitrend.repository.BrowseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaBrowseViewModel(
    private val baseRepository: BaseRepository,
    private val browseRepository: BrowseRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<MediaBase>) : UiState
        data class Error(val message: String) : UiState
    }

    val genreCollection: List<Genre>
        get() = baseRepository.cachedGenres
    val mediaTags: List<MediaTag>
        get() = baseRepository.cachedTags

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()
    private val loadedMedia = linkedMapOf<Long, MediaBase>()

    init {
        viewModelScope.launch {
            browseRepository.mutationEvents.collect { event ->
                when (event) {
                    is BrowseMutation.MediaListSaved -> {
                        loadedMedia[event.entry.mediaId]?.let { media ->
                            media.mediaListEntry = event.entry
                            emitUpdatedMedia()
                        }
                    }
                    is BrowseMutation.MediaListDeleted -> {
                        loadedMedia.values.firstOrNull { media ->
                            media.mediaListEntry?.id == event.id
                        }?.let { media ->
                            media.mediaListEntry = null
                            emitUpdatedMedia()
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    fun load(
        type: MediaType?,
        page: Int,
        pageLimit: Int,
        season: String?,
        sort: String?,
        isAdult: Boolean?,
        format: String?,
        seasonYear: Int?,
        startDateLike: String?,
        status: String?,
        genres: List<String>?,
        tags: List<String>?,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                val sortList: List<MediaSort>? =
                    sort?.let { sortName -> runCatching { MediaSort.valueOf(sortName) }.getOrNull()?.let { listOf(it) } }
                val seasonEnum: MediaSeason? =
                    season?.let { runCatching { MediaSeason.valueOf(it) }.getOrNull() }
                val formatEnum: MediaFormat? =
                    format?.let { runCatching { MediaFormat.valueOf(it) }.getOrNull() }
                val statusEnum: MediaStatus? =
                    status?.let { runCatching { MediaStatus.valueOf(it) }.getOrNull() }
                val normalizedGenres = genres?.takeUnless { it.isEmpty() }
                val normalizedTags = tags?.takeUnless { it.isEmpty() }
                browseRepository.getMediaBrowse(
                    page = page,
                    perPage = pageLimit,
                    seasonYear = seasonYear,
                    type = type,
                    format = formatEnum,
                    startDateLike = startDateLike,
                    season = seasonEnum,
                    isAdult = isAdult,
                    sort = sortList,
                    onList = null,
                    status = statusEnum,
                    genres = normalizedGenres,
                    tags = normalizedTags,
                ).getOrThrow()
            }.onSuccess { content ->
                trackMedia(page, content.pageData)
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaBrowseViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to browse media",
                )
            }
        }
    }

    private fun trackMedia(
        page: Int,
        media: List<MediaBase>,
    ) {
        if (page <= 1) {
            loadedMedia.clear()
        }
        media.forEach { item ->
            loadedMedia[item.id] = item
        }
    }

    private fun emitUpdatedMedia() {
        val current = _state.value as? UiState.Success ?: return
        val updatedMedia = current.content.pageData.toList()
        _state.value = UiState.Success(
            PageContainer<MediaBase>().apply {
                if (current.content.hasPageInfo()) {
                    pageInfo = current.content.pageInfo
                }
                pageData = updatedMedia
            },
        )
        trackAllMedia(updatedMedia)
    }

    private fun trackAllMedia(media: List<MediaBase>) {
        loadedMedia.clear()
        media.forEach { item ->
            loadedMedia[item.id] = item
        }
    }
}
