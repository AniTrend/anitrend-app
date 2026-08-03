package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewStudioRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.date.DateUtil
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
        data class Success(val record: MediaOverviewRecord) : UiState
        data class Error(val message: String) : UiState
    }

    data class MediaOverviewDisplayData(
        val hashTagHtml: String?,
        val mainStudioName: String?,
        val mainStudio: MediaOverviewStudioRecord?,
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
            repository.getMediaOverviewRecord(
                id = mediaId,
                type = type,
                isAdult = isAdult,
            ).onSuccess { record ->
                _displayData.value = transformToDisplayData(record)
                _state.value = UiState.Success(record)
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaOverviewViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load media overview",
                )
            }
        }
    }

    private fun transformToDisplayData(record: MediaOverviewRecord): MediaOverviewDisplayData {
        val hashTag = record.hashtag
        val hashTagHtml = if (!hashTag.isNullOrEmpty()) {
            String.format(
                "<a href=\"https://twitter.com/search?q=%%23%s&src=typd\">%s</a>",
                hashTag.replace("#", ""),
                hashTag,
            )
        } else {
            null
        }

        val mainStudio = record.studios?.firstOrNull()
        val mainStudioName = mainStudio?.name

        val formatText = if (!record.format.isNullOrEmpty()) {
            CompatUtil.capitalizeWords(record.format)
        } else {
            null
        }

        val seasonText: String? = record.startDate?.let { startDate ->
            if (startDate.isValidDate) DateUtil.getMediaSeason(startDate) else null
        }

        val sourceText = if (!record.source.isNullOrEmpty()) {
            CompatUtil.capitalizeWords(record.source)
        } else {
            null
        }

        val statusText = if (!record.status.isNullOrEmpty()) {
            CompatUtil.capitalizeWords(record.status)
        } else {
            null
        }

        val genres = record.genres
            .orEmpty()
            .takeWhile { !it.isNullOrEmpty() }
            .map { Genre(it) }

        val isManga = record.type == KeyUtil.MANGA
        val isAnime = record.type == KeyUtil.ANIME

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
            episodeDuration = if (record.duration != null && record.duration > 0) record.duration else null,
            episodeCount = if (record.episodes != null && record.episodes > 0) record.episodes else null,
            volumeCount = if (record.volumes != null && record.volumes > 0) record.volumes else null,
            chapterCount = if (record.chapters != null && record.chapters > 0) record.chapters else null,
            meanScore = record.meanScore,
        )
    }
}
