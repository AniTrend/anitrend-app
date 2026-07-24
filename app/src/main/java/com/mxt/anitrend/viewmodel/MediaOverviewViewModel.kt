package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.util.media.MediaUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaOverviewViewModel(
    private val repository: MediaRepository,
    private val settings: Settings,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val media: Media) : UiState
        data class Error(val message: String) : UiState
    }

    data class MediaOverviewDisplayData(
        val hashTagHtml: String?,
        val mainStudioName: String?,
        val mainStudio: StudioBase?,
        val formatText: String?,
        val seasonText: String?,
        val sourceText: String?,
        val statusText: String?,
        val genres: List<Genre>,
        val isManga: Boolean,
        val isAnime: Boolean,
        val episodeDuration: Int?,
        val episodeCount: Int?,
        val volumeCount: Int?,
        val chapterCount: Int?,
        val meanScore: Int?,
    )

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _displayData = MutableStateFlow<MediaOverviewDisplayData?>(null)
    val displayData: StateFlow<MediaOverviewDisplayData?> = _displayData.asStateFlow()

    private var loadedOnce = false

    fun load(mediaId: Long, mediaType: String?) {
        if (loadedOnce) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
            val isAdult: Boolean? = if (settings.displayAdultContent) null else false
            repository.getMediaOverview(
                id = mediaId,
                type = type,
                isAdult = isAdult,
            ).onSuccess { media ->
                _displayData.value = transformToDisplayData(media)
                _state.value = UiState.Success(media)
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaOverviewViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media overview",
                )
            }
        }
    }

    private fun transformToDisplayData(media: Media): MediaOverviewDisplayData {
        val hashTag = media.hashTag
        val hashTagHtml = if (!hashTag.isNullOrEmpty()) {
            String.format(
                "<a href=\"https://twitter.com/search?q=%%23%s&src=typd\">%s</a>",
                hashTag.replace("#", ""),
                hashTag,
            )
        } else {
            null
        }

        val studioContainer: ConnectionContainer<List<StudioBase>>? = media.studios
        val mainStudio = studioContainer?.connection?.firstOrNull()
        val mainStudioName = mainStudio?.name

        val formatText = if (!media.format.isNullOrEmpty()) {
            CompatUtil.capitalizeWords(media.format)
        } else {
            null
        }

        val seasonText: String? = media.startDate?.let { startDate ->
            if (startDate.isValidDate) DateUtil.getMediaSeason(startDate) else null
        }

        val sourceText = if (!media.source.isNullOrEmpty()) {
            CompatUtil.capitalizeWords(media.source)
        } else {
            null
        }

        val statusText = if (!media.status.isNullOrEmpty()) {
            CompatUtil.capitalizeWords(media.status)
        } else {
            null
        }

        val genres = media.genres
            .orEmpty()
            .takeWhile { it.isNotEmpty() }
            .map { Genre(it) }

        val isManga = MediaUtil.isMangaType(media)
        val isAnime = MediaUtil.isAnimeType(media)

        return MediaOverviewDisplayData(
            hashTagHtml = hashTagHtml,
            mainStudioName = mainStudioName,
            mainStudio = mainStudio,
            formatText = formatText,
            seasonText = seasonText,
            sourceText = sourceText,
            statusText = statusText,
            genres = genres,
            isManga = isManga,
            isAnime = isAnime,
            episodeDuration = if (media.duration != null && media.duration > 0) media.duration else null,
            episodeCount = if (media.episodes != null && media.episodes > 0) media.episodes else null,
            volumeCount = if (media.volumes != null && media.volumes > 0) media.volumes else null,
            chapterCount = if (media.chapters != null && media.chapters > 0) media.chapters else null,
            meanScore = media.meanScore,
        )
    }
}
