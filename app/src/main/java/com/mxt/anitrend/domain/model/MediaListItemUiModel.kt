package com.mxt.anitrend.domain.model

import com.mxt.anitrend.domain.medialist.model.MediaListRecord

data class MediaListItemUiModel(
    val id: Long,
    val mediaId: Long,
    val status: String?,
    val progress: Int,
    val progressVolumes: Int,
    val score: Double,
    val repeat: Int,
    val mediaTitle: String,
    val mediaTitleEnglish: String?,
    val mediaTitleOriginal: String?,
    val mediaCoverImage: String?,
    val mediaType: String?,
    val mediaFormat: String?,
    val mediaStatus: String?,
    val mediaEpisodes: Int,
    val mediaChapters: Int,
    val mediaVolumes: Int,
    val mediaStartDate: FuzzyDateRecord?,
    val nextAiringEpisode: AiringScheduleRecord?,
    val mediaIsFavourite: Boolean,
    val isIncrementPending: Boolean,
    val isDeletePending: Boolean,
    val canIncrement: Boolean,
)

fun MediaListRecord.toMediaListItemUiModel(
    isIncrementPending: Boolean,
    isDeletePending: Boolean,
    canIncrement: Boolean,
): MediaListItemUiModel {
    val mediaSummary = media
    return MediaListItemUiModel(
        id = id,
        mediaId = mediaId,
        status = status,
        progress = progress,
        progressVolumes = progressVolumes,
        score = score,
        repeat = repeat,
        mediaTitle = mediaSummary?.titleUserPreferred
            ?: mediaSummary?.titleRomaji
            ?: mediaSummary?.titleEnglish
            ?: mediaSummary?.titleOriginal
                .orEmpty(),
        mediaTitleEnglish = mediaSummary?.titleEnglish,
        mediaTitleOriginal = mediaSummary?.titleOriginal,
        mediaCoverImage = mediaSummary?.coverImage,
        mediaType = mediaSummary?.type,
        mediaFormat = mediaSummary?.format,
        mediaStatus = mediaSummary?.status,
        mediaEpisodes = mediaSummary?.episodes ?: 0,
        mediaChapters = mediaSummary?.chapters ?: 0,
        mediaVolumes = mediaSummary?.volumes ?: 0,
        mediaStartDate = mediaSummary?.startDate,
        nextAiringEpisode = mediaSummary?.nextAiringEpisode,
        mediaIsFavourite = mediaSummary?.isFavourite == true,
        isIncrementPending = isIncrementPending,
        isDeletePending = isDeletePending,
        canIncrement = canIncrement,
    )
}

fun MediaListItemUiModel.matchesFilter(filter: String): Boolean {
    val normalizedFilter = filter.lowercase()
    return mediaTitle.lowercase().contains(normalizedFilter) ||
        mediaTitleEnglish?.lowercase()?.contains(normalizedFilter) == true ||
        mediaTitleOriginal?.lowercase()?.contains(normalizedFilter) == true
}
