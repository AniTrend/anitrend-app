package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.model.entity.crunchy.Episode
import com.mxt.anitrend.model.entity.crunchy.Rss
import com.mxt.anitrend.repository.CrunchyrollRepository
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.ui.episode.EpisodeRowUiState
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.collection.EpisodeUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchListViewModel(
    private val mediaRepository: MediaRepository,
    private val crunchyrollRepository: CrunchyrollRepository,
    private val settings: Settings,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    sealed interface UiState {
        data class Loading(val revision: Long) : UiState

        data class Content(
            val rows: List<EpisodeRowUiState>,
            val sourceCount: Int,
            val revision: Long,
        ) : UiState

        data class Empty(
            val reason: EmptyReason,
            val revision: Long,
        ) : UiState

        data class Error(
            val message: String?,
            val revision: Long,
        ) : UiState
    }

    enum class EmptyReason {
        NO_EPISODES,
        MISSING_EPISODE_LINKS,
    }

    data class EpisodeAction(
        val title: String,
        val description: String,
        val copyright: String?,
        val link: String?,
    )

    private data class Request(
        val mediaId: Long = 0L,
        val mediaType: String? = null,
        val isPopular: Boolean = false,
        val hasExternalLinks: Boolean = false,
        val targetLink: String? = null,
    )

    private data class VisibleEpisode(
        val sourceIndex: Int,
        val model: Episode,
    )

    private sealed interface FetchResult {
        data class Loaded(val rss: Rss, val targetLink: String) : FetchResult

        data class Empty(val reason: EmptyReason) : FetchResult

        data class Failed(val message: String?) : FetchResult
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading(revision = 0L))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var request = Request()
    private var resolvedTargetLink: String? = null
    private var requestRevision = 0L
    private var stateRevision = 0L
    private var requestJob: Job? = null
    private var searchQuery = ""
    private var sourceEpisodes: List<Episode> = emptyList()
    private var visibleEpisodes: List<VisibleEpisode> = emptyList()
    private var copyright: String? = null

    fun configure(
        mediaId: Long,
        mediaType: String?,
        isPopular: Boolean,
        isFeedRoute: Boolean,
        externalLinks: List<ExternalLink>?,
    ) {
        val nextRequest =
            Request(
                mediaId = mediaId,
                mediaType = mediaType,
                isPopular = isPopular,
                hasExternalLinks = externalLinks != null,
                targetLink = if (isFeedRoute) BuildConfig.FEEDS_LINK else externalLinks?.let(EpisodeUtil::episodeSupport),
            )
        if (request == nextRequest) return

        request = nextRequest
        resolvedTargetLink = null
        requestRevision++
        sourceEpisodes = emptyList()
        visibleEpisodes = emptyList()
        copyright = null
        showLoadingState()
    }

    fun hasContent(): Boolean = sourceEpisodes.isNotEmpty()

    fun restoreContent() {
        if (hasContent()) publishContent()
    }

    fun onSearchQueryChanged(query: String?) {
        val nextQuery = query.orEmpty()
        if (searchQuery == nextQuery) return
        searchQuery = nextQuery
        if (hasContent()) publishContent()
    }

    fun episodeActionAt(visibleIndex: Int): EpisodeAction? {
        val episode = visibleEpisodes.getOrNull(visibleIndex)?.model ?: return null
        return EpisodeAction(
            title = episode.title,
            description = episode.description,
            copyright = copyright,
            link = episode.link,
        )
    }

    fun refresh() {
        requestJob?.cancel()
        val selectedRequest = request
        val selectedTargetLink = selectedRequest.targetLink ?: resolvedTargetLink
        val revision = ++requestRevision
        if (!hasContent()) showLoadingState()

        requestJob = viewModelScope.launch {
            val result =
                try {
                    withContext(ioDispatcher) { fetch(selectedRequest, selectedTargetLink) }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    FetchResult.Failed(exception.message)
                }

            if (revision != requestRevision) return@launch
            when (result) {
                is FetchResult.Loaded -> {
                    if (selectedRequest.targetLink == null) {
                        resolvedTargetLink = result.targetLink
                    }
                    acceptRss(result.rss)
                }
                is FetchResult.Empty -> showEmptyState(result.reason)
                is FetchResult.Failed -> showErrorState(result.message)
            }
        }
    }

    private suspend fun fetch(request: Request, targetLink: String?): FetchResult {
        if (request.hasExternalLinks || targetLink != null) {
            val link = targetLink ?: return FetchResult.Empty(EmptyReason.MISSING_EPISODE_LINKS)
            return fetchRss(link, request.isPopular)
        }

        val mediaType = request.mediaType?.let { MediaType.valueOf(it) }
        val isAdult = if (settings.displayAdultContent) null else false
        val connectionResult =
            mediaRepository.getMediaEpisodes(
                id = request.mediaId,
                type = mediaType,
                isAdult = isAdult,
            )
        val connectionError = connectionResult.exceptionOrNull()
        if (connectionError != null) {
            if (connectionError is CancellationException) throw connectionError
            return FetchResult.Failed(connectionError.message)
        }
        val connections = connectionResult.getOrNull() ?: return FetchResult.Failed(null)
        if (connections.isEmpty) return FetchResult.Empty(EmptyReason.MISSING_EPISODE_LINKS)

        val resolvedLink =
            connections.connection?.let(EpisodeUtil::episodeSupport)
                ?: return FetchResult.Empty(EmptyReason.MISSING_EPISODE_LINKS)
        return fetchRss(resolvedLink, request.isPopular)
    }

    private suspend fun fetchRss(targetLink: String, isPopular: Boolean): FetchResult {
        val feed = targetLink.startsWith(BuildConfig.FEEDS_LINK)
        val result =
            when {
                feed && isPopular -> crunchyrollRepository.getPopularFeed()
                feed -> crunchyrollRepository.getLatestFeed()
                else -> crunchyrollRepository.getRss(targetLink)
            }
        val error = result.exceptionOrNull()
        if (error != null) {
            if (error is CancellationException) throw error
            return FetchResult.Failed(error.message)
        }
        val rss = result.getOrNull() ?: return FetchResult.Failed(null)
        return FetchResult.Loaded(rss = rss, targetLink = targetLink)
    }

    private fun acceptRss(rss: Rss) {
        val channel = rss.channel ?: run {
            showEmptyState(EmptyReason.NO_EPISODES)
            return
        }
        sourceEpisodes = channel.episode
        copyright = channel.copyright
        publishContent()
    }

    private fun publishContent() {
        val visible = ArrayList<VisibleEpisode>()
        sourceEpisodes.forEachIndexed { index, episode ->
            if (searchQuery.isEmpty() || episode.title.contains(searchQuery, ignoreCase = true)) {
                visible += VisibleEpisode(sourceIndex = index, model = episode)
            }
        }
        visibleEpisodes = visible
        _state.value =
            UiState.Content(
                rows = visible.map { indexed -> EpisodeRowUiState.from(indexed.sourceIndex, indexed.model) },
                sourceCount = sourceEpisodes.size,
                revision = ++stateRevision,
            )
    }

    private fun showLoadingState() {
        _state.value = UiState.Loading(revision = ++stateRevision)
    }

    private fun showEmptyState(reason: EmptyReason) {
        _state.value = UiState.Empty(reason = reason, revision = ++stateRevision)
    }

    private fun showErrorState(message: String?) {
        _state.value = UiState.Error(message = message, revision = ++stateRevision)
    }
}
