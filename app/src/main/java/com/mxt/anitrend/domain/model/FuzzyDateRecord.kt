package com.mxt.anitrend.domain.model

data class FuzzyDateRecord(
    val year: Int?,
    val month: Int?,
    val day: Int?,
) {
    /** Mirrors [com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate.isValidDate]; a null field is treated as 0. */
    val isValidDate: Boolean
        get() = (day ?: 0) != 0 || (month ?: 0) != 0 || (year ?: 0) != 0
}
