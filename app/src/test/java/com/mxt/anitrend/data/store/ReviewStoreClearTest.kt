package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.review.InMemoryReviewStore
import com.mxt.anitrend.data.store.review.ReviewQueryKey
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.data.store.review.ReviewStoreState
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewSort
import com.mxt.anitrend.model.entity.anilist.Review
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
                generation = 1,
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
    ): Review = Review().apply {
        this.id = id
        this.rating = rating
        media.id = 100L + id
        media.type = MediaType.ANIME.name
        mediaType = MediaType.ANIME.name
    }

    private fun pageInfo(currentPage: Int): PageInfoRecord = PageInfoRecord(
        currentPage = currentPage,
        lastPage = 1,
        perPage = 10,
        total = 10,
        hasNextPage = false,
        hasPreviousPage = false,
    )
}
