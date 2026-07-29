package com.mxt.anitrend.domain.model

data class MediaSummaryRecord(
    val id: Long,
    val titleRomaji: String?,
    val titleEnglish: String?,
    val titleOriginal: String?,
    val coverImage: String?,
    val type: String?,
    val episodes: Int,
    val chapters: Int,
    val volumes: Int,
    val status: String?,
    val siteUrl: String?,
)
