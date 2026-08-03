package com.mxt.anitrend.domain.model

/**
 * Immutable screen-level representation of a single media recommendation row.
 *
 * Keeps only the data the recommendations card needs to render, navigate, and
 * handle long-press actions. Records with no recommended media are filtered out
 * at projection time, so every item has a stable media identity.
 */
data class RecommendationItemUiModel(
    val id: Long,
    val mediaId: Long,
    val title: String,
    val titleEnglish: String?,
    val titleOriginal: String?,
    val coverImage: String?,
    val mediaType: String?,
    val mediaFormat: String?,
    val mediaStatus: String?,
    val mediaEpisodes: Int,
    val mediaChapters: Int,
    val mediaVolumes: Int,
    val mediaStartDate: FuzzyDateRecord?,
    val averageScore: Int?,
    val isFavourite: Boolean,
)

/**
 * Projects a [RecommendationRecord] into a [RecommendationItemUiModel], or
 * returns null when the recommendation does not reference a media item.
 */
fun RecommendationRecord.toRecommendationItemUiModel(): RecommendationItemUiModel? {
    val media = mediaRecommendation ?: return null
    return RecommendationItemUiModel(
        id = id,
        mediaId = media.id,
        title = media.titleUserPreferred
            ?: media.titleRomaji
            ?: media.titleEnglish
            ?: media.titleOriginal.orEmpty(),
        titleEnglish = media.titleEnglish,
        titleOriginal = media.titleOriginal,
        coverImage = media.coverImage,
        mediaType = media.type,
        mediaFormat = media.format,
        mediaStatus = media.status,
        mediaEpisodes = media.episodes,
        mediaChapters = media.chapters,
        mediaVolumes = media.volumes,
        mediaStartDate = media.startDate,
        averageScore = media.averageScore,
        isFavourite = media.isFavourite,
    )
}
