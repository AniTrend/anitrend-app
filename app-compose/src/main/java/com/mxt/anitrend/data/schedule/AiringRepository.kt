package com.mxt.anitrend.data.schedule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class AiringItem(
    val id: Long,
    val title: String,
    val episode: Int,
    val airingAt: Long,
    val coverMedium: String?,
)

interface AiringRepository {
    fun observeSchedule(): Flow<List<AiringItem>>
}

class MockAiringRepository : AiringRepository {
    override fun observeSchedule(): Flow<List<AiringItem>> = flow {
        val now = System.currentTimeMillis() / 1000
        emit(
            listOf(
                AiringItem(1, "Attack on Titan Final Season", 87, now + 3600, null),
                AiringItem(2, "Demon Slayer: Swordsmith Village", 11, now + 7200, null),
                AiringItem(3, "Jujutsu Kaisen Season 2", 23, now + 10800, null),
                AiringItem(4, "One Piece", 1071, now + 14400, null),
                AiringItem(5, "Spy x Family Season 2", 6, now + 18000, null),
            )
        )
    }
}
