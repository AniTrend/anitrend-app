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

    fun load(
        type: MediaType?,
        page: Int,
        pageLimit: Int,
        sort: String?,
        isAdult: Boolean?,
        format: String?,
        seasonYear: Int?,
        season: MediaSeason? = null,
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
                val formatEnum: MediaFormat? =
                    format?.let { runCatching { MediaFormat.valueOf(it) }.getOrNull() }
                val statusEnum: MediaStatus? =
                    status?.let { runCatching { MediaStatus.valueOf(it) }.getOrNull() }
                browseRepository.getMediaBrowse(
                    page = page,
                    perPage = pageLimit,
                    seasonYear = seasonYear,
                    season = season,
                    type = type,
                    format = formatEnum,
                    startDateLike = startDateLike,
                    isAdult = isAdult,
                    sort = sortList,
                    onList = null,
                    status = statusEnum,
                    genres = genres?.ifEmpty { null },
                    tags = tags?.ifEmpty { null },
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaBrowseViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to browse media",
                )
            }
        }
    }
}
