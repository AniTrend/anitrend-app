package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.review.InMemoryReviewStore
import com.mxt.anitrend.data.store.review.ReviewQueryKey
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewSort
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewStoreTest {
    private val queryKey = ReviewQueryKey(
        mediaId = null,
        mediaType = MediaType.ANIME,
        sort = ReviewSort.CREATED_AT_DESC,
    )

    @Test
    fun `query page merge preserves order`() = runTest {
        val store = InMemoryReviewStore()

        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L), review(2L)),
                pageInfo = pageInfo(1),
            ),
        )
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 2,
                token = 1L,
                reviews = listOf(review(3L), review(4L)),
                pageInfo = pageInfo(2),
            ),
        )

        assertEquals(listOf(1L, 2L, 3L, 4L), store.state.value.queries.getValue(queryKey).orderedReviewIds)
    }

    @Test
    fun `review delete removes query references`() = runTest {
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L), review(2L)),
                pageInfo = pageInfo(1),
            ),
        )

        store.apply(ReviewStoreChange.ReviewDeleted(reviewId = 2L, revision = 1L))

        assertFalse(store.state.value.reviewsById.containsKey(2L))
        assertFalse(store.state.value.queries.getValue(queryKey).orderedReviewIds.contains(2L))
    }

    @Test
    fun `stale revisions are rejected`() = runTest {
        val store = InMemoryReviewStore()
        store.apply(ReviewStoreChange.ReviewRated(review(9L, rating = 50), revision = 5L))
        store.apply(ReviewStoreChange.ReviewRated(review(9L, rating = 10), revision = 4L))

        assertEquals(50, store.state.value.reviewsById.getValue(9L).review.rating)
        assertEquals(5L, store.state.value.reviewsById.getValue(9L).revision)
    }

    @Test
    fun `saved review is inserted into matching queries`() = runTest {
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 1L,
                reviews = emptyList(),
                pageInfo = pageInfo(1),
            ),
        )

        store.apply(ReviewStoreChange.ReviewSaved(review(12L), revision = 1L))

        assertEquals(listOf(12L), store.state.value.queries.getValue(queryKey).orderedReviewIds)
        assertTrue(store.state.value.reviewsById.containsKey(12L))
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
        media = mediaSummary(100L + id),
        revision = 0L,
    )

    private fun mediaSummary(id: Long): MediaSummaryRecord = MediaSummaryRecord(
        id = id,
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
    )

    private fun pageInfo(currentPage: Int): PageInfoRecord = PageInfoRecord(
        currentPage = currentPage,
        lastPage = 3,
        perPage = 10,
        total = 30,
        hasNextPage = currentPage < 3,
        hasPreviousPage = currentPage > 1,
    )
}
