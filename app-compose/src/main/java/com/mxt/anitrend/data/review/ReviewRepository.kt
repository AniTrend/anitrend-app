package com.mxt.anitrend.data.review

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class ReviewItem(
    val id: Long,
    val mediaTitle: String,
    val summary: String,
    val rating: Int,
    val userName: String,
)

interface ReviewRepository {
    fun observeReviews(): Flow<List<ReviewItem>>
}

class MockReviewRepository : ReviewRepository {
    override fun observeReviews(): Flow<List<ReviewItem>> = flow {
        emit(
            listOf(
                ReviewItem(1, "Fullmetal Alchemist: Brotherhood", "A masterpiece of storytelling with incredible character development and a satisfying conclusion.", 95, "animefan42"),
                ReviewItem(2, "Steins;Gate", "Mind-bending time travel narrative that rewards patient viewers with an unforgettable experience.", 90, "scifi_lover"),
                ReviewItem(3, "Your Lie in April", "Beautiful and heartbreaking. The music and animation create an emotional journey like no other.", 88, "melody_watcher"),
                ReviewItem(4, "Hunter x Hunter", "The best shonen series ever made. The Chimera Ant arc is a work of genius.", 93, "nen_user"),
                ReviewItem(5, "Violet Evergarden", "Visually stunning with deeply moving episodic stories about love and loss.", 87, "kyoto_fan"),
            )
        )
    }
}
