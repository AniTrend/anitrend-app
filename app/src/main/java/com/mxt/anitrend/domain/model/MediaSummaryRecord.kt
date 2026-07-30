package com.mxt.anitrend.domain.model

data class MediaSummaryRecord(
    val id: Long,
    val titleUserPreferred: String? = null,
    val titleRomaji: String?,
    val titleEnglish: String?,
    val titleOriginal: String?,
    val coverImage: String?,
    val type: String?,
    val format: String? = null,
    val episodes: Int,
    val chapters: Int,
    val volumes: Int,
    val status: String?,
    val siteUrl: String?,
    val isFavourite: Boolean = false,
    val startDate: FuzzyDateRecord? = null,
    val nextAiringEpisode: AiringScheduleRecord? = null,
)
