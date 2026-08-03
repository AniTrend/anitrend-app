package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.mapper.toMediaList
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaBrowseViewModel(
    private val baseRepository: BaseRepository,
    private val browseRepository: BrowseRepository,
    private val mediaListStore: MediaListStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val content: PageContainer<MediaBase>,
            val loadedPages: Set<Int>,
            val replaceExisting: Boolean = false,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    val genreCollection: List<Genre>
        get() = baseRepository.cachedGenres
    val mediaTags: List<MediaTag>
        get() = baseRepository.cachedTags

    private data class ScreenState(
        val loadedMedia: LinkedHashMap<Long, MediaBase> = linkedMapOf(),
        val loadedPages: Set<Int> = emptySet(),
        val currentPageInfo: PageInfo? = null,
        val requestGeneration: Int = 0,
        val lastRequestedPage: Int = 1,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val hasEverLoaded: Boolean = false,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val state: StateFlow<UiState> =
        screenState
            .flatMapLatest(::observeRenderedMedia)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading,
            )

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
        val generation = screenState.value.requestGeneration.takeIf { page > 1 } ?: (screenState.value.requestGeneration + 1)
        screenState.update {
            it.copy(
                requestGeneration = generation,
                lastRequestedPage = page,
                isLoading = true,
                errorMessage = null,
                loadedMedia = if (page <= 1) linkedMapOf() else LinkedHashMap(it.loadedMedia),
                loadedPages = if (page <= 1) emptySet() else it.loadedPages,
                currentPageInfo = if (page <= 1) null else it.currentPageInfo,
            )
        }
        viewModelScope.launch(ioDispatcher) {
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
                if (generation != screenState.value.requestGeneration) {
                    return@onSuccess
                }
                screenState.update { current ->
                    val nextLoadedMedia = if (page <= 1) linkedMapOf() else LinkedHashMap(current.loadedMedia)
                    content.pageData.forEach { item ->
                        nextLoadedMedia[item.id] = item
                    }
                    current.copy(
                        loadedMedia = nextLoadedMedia,
                        loadedPages = if (page <= 1) setOf(page) else current.loadedPages + page,
                        currentPageInfo = if (content.hasPageInfo()) content.pageInfo else null,
                        isLoading = false,
                        errorMessage = null,
                        hasEverLoaded = true,
                    )
                }
            }.onFailure { throwable ->
                if (generation != screenState.value.requestGeneration) {
                    return@onFailure
                }
                Timber.e(throwable, "MediaBrowseViewModel load failed")
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to browse media",
                    )
                }
            }
        }
    }

    private fun observeRenderedMedia(screen: ScreenState): Flow<UiState> {
        val mediaItems = screen.loadedMedia.values.toList()
        val renderedMedia =
            if (mediaItems.isEmpty()) {
                flowOf(emptyList<MediaBase>())
            } else {
                combine(
                    mediaItems.map { media ->
                        mediaListStore.observeEntryByMediaId(media.id).map { entry ->
                            media.copyWithMediaListEntry(entry?.toMediaList())
                        }
                    },
                ) { rendered -> rendered.toList() }
            }

        return renderedMedia.map { updatedMedia ->
            when {
                screen.errorMessage != null -> {
                    UiState.Error(screen.errorMessage)
                }
                // Never-loaded idle state must not masquerade as a genuine empty
                // response; keep emitting Loading until the first success arrives.
                !screen.hasEverLoaded || (screen.isLoading && updatedMedia.isEmpty()) -> {
                    UiState.Loading
                }
                else -> {
                    UiState.Success(
                        content = PageContainer<MediaBase>().apply {
                            screen.currentPageInfo?.let { pageInfo = it }
                            pageData = updatedMedia
                        },
                        loadedPages = screen.loadedPages,
                        replaceExisting = screen.lastRequestedPage <= 1,
                    )
                }
            }
        }
    }

    private fun MediaBase.copyWithMediaListEntry(entry: com.mxt.anitrend.model.entity.anilist.MediaList?): MediaBase = MediaBase().also { copy ->
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
