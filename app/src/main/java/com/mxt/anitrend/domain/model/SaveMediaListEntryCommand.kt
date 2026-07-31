package com.mxt.anitrend.domain.model

import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaListStatus

data class SaveMediaListEntryCommand(
    val id: Int?,
    val mediaId: Long?,
    val status: MediaListStatus?,
    val score: Double?,
    val scoreRaw: Int? = null,
    val progress: Int?,
    val progressVolumes: Int?,
    val repeat: Int?,
    val priority: Int?,
    val isPrivate: Boolean,
    val hiddenFromStatusLists: Boolean,
    val customLists: List<String?>?,
    val advancedScores: List<Double?>?,
    val notes: String?,
    val startedAt: FuzzyDateInput?,
    val completedAt: FuzzyDateInput?,
)
