package com.mxt.anitrend.domain.model

data class AiringScheduleRecord(
    val airingAt: Long,
    val timeUntilAiring: Long,
    val episode: Int,
)
