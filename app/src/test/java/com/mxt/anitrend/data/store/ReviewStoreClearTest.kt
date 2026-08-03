package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.review.InMemoryReviewStore
import com.mxt.anitrend.data.store.review.ReviewQueryKey
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.data.store.review.ReviewStoreState
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewSort
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewStoreClearTest {
    private val queryKey = ReviewQueryKey(
        mediaId = null,
        mediaType = MediaType.ANIME,
        sort = ReviewSort.CREATED_AT_DESC,
    )

    @Test
    fun `given populated store when clear called then state and deletion revisions reset`() = runTest {
        val store = InMemoryReviewStore()
        val review = review(id = 1L)

        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 1L,
                reviews = listOf(review),
                pageInfo = pageInfo(1),
            ),
        )
        store.apply(ReviewStoreChange.ReviewDeleted(reviewId = 1L, revision = 5L))

        store.clear()

        assertEquals(ReviewStoreState(), store.state.value)

        store.apply(ReviewStoreChange.ReviewRated(review(id = 1L, rating = 50), revision = 1L))

        assertTrue(store.state.value.reviewsById.containsKey(1L))
    }

    private fun review(
        id: Long,
        rating: Int = 0,
    ): ReviewRecord = ReviewRecord(
        id = id,
        summary = null,
        mediaType = MediaType.ANIME.name,
        body = null,
        rating = rating,
        ratingAmount = 0,
        userRating = null,
        score = 0,
        isPrivate = false,
        createdAt = 0L,
        user = null,
        media = MediaSummaryRecord(
            id = 100L + id,
            titleUserPreferred = null,
            titleRomaji = null,
            titleEnglish = null,
            titleOriginal = null,
            coverImage = null,
            type = MediaType.ANIME.name,
            format = null,
            episodes = 0,
            chapters = 0,
            volumes = 0,
            status = null,
            siteUrl = null,
            isFavourite = false,
            startDate = null,
            nextAiringEpisode = null,
            averageScore = null,
        ),
        revision = 0L,
    )

    private fun pageInfo(currentPage: Int): PageInfoRecord = PageInfoRecord(
        currentPage = currentPage,
        lastPage = 1,
        perPage = 10,
        total = 10,
        hasNextPage = false,
        hasPreviousPage = false,
    )
}
