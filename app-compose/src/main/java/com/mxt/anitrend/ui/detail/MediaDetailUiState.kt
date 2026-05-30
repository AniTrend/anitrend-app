package com.mxt.anitrend.ui.detail

data class MediaDetail(
    val id: Long,
    val title: String,
    val romajiTitle: String?,
    val englishTitle: String?,
    val type: String?,
    val format: String?,
    val status: String?,
    val description: String?,
    val genres: List<String>,
    val meanScore: Int?,
    val averageScore: Int?,
    val popularity: Int?,
    val favourites: Int?,
    val episodes: Int?,
    val chapters: Int?,
    val volumes: Int?,
    val duration: Int?,
    val season: String?,
    val seasonYear: Int?,
    val isAdult: Boolean,
    val isFavourite: Boolean,
    val siteUrl: String?,
    val hashtag: String?,
    val source: String?,
    val bannerImage: String?,
    val coverImageLarge: String?,
    val coverImageExtraLarge: String?,
    val trailerId: String?,
    val trailerSite: String?,
    val trailerThumbnail: String?,
    val studios: List<Studio>,
    val tags: List<Tag>,
    val startDate: String?,
    val endDate: String?,
    val nextAiringEpisode: AiringEpisode?,
    val mediaListEntry: MediaListEntry?,
) {
    data class Studio(val id: Int, val name: String, val siteUrl: String?)
    data class Tag(val id: Int, val name: String, val description: String?, val rank: Int?, val isMediaSpoiler: Boolean)
    data class AiringEpisode(val airingAt: Int, val timeUntilAiring: Int, val episode: Int)
    data class MediaListEntry(val status: String?, val score: Double?, val progress: Int?, val progressVolumes: Int?)
}

sealed class MediaDetailUiState {
    data object Loading : MediaDetailUiState()
    data class Success(val media: MediaDetail) : MediaDetailUiState()
    data class Error(val message: String) : MediaDetailUiState()
}
