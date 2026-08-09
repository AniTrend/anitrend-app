package com.mxt.anitrend.domain.model

/**
 * Immutable screen-level representation of a single media search result row.
 *
 * Keeps only the data the media search card needs to render, navigate, and
 * handle long-press actions. [id] is the stable media identity from the
 * backend and is the identity the paging source deduplicates on and the
 * adapter diffs on. The legacy mutable
 * [com.mxt.anitrend.model.entity.base.MediaBase] lane remains for the shared
 * media adapters and navigation consumers until they are migrated.
 */
data class MediaSearchItemUiModel(
    val id: Long,
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
