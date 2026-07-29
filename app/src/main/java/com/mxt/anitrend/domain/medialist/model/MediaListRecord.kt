package com.mxt.anitrend.domain.medialist.model

import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.MediaSummaryRecord

data class MediaListRecord(
    val id: Long,
    val mediaId: Long,
    val status: String?,
    val score: Double,
    val scoreRaw: Int?,
    val progress: Int,
    val progressVolumes: Int,
    val repeat: Int,
    val priority: Int,
    val private: Boolean,
    val hiddenFromStatusLists: Boolean,
    val customLists: List<String>,
    val advancedScores: Map<String, Double>,
    val notes: String?,
    val startedAt: FuzzyDateRecord?,
    val completedAt: FuzzyDateRecord?,
    val media: MediaSummaryRecord?,
    val revision: Long,
)
