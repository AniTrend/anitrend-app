package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.review.InMemoryReviewStore
import com.mxt.anitrend.data.store.review.ReviewQueryKey
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewSort
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewStoreTest {
    private val queryKey = ReviewQueryKey(
        mediaId = null,
        mediaType = MediaType.ANIME,
        sort = ReviewSort.ID_DESC,
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

        store.apply(ReviewStoreChange.ReviewSaved(review(12L), revision = 1L, isCreate = true))

        assertEquals(listOf(12L), store.state.value.queries.getValue(queryKey).orderedReviewIds)
        assertTrue(store.state.value.reviewsById.containsKey(12L))
    }

    @Test
    fun `null sort normalizes to server default for key identity`() {
        val explicit = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC)
        val implicit = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = null)

        assertEquals(explicit, implicit)
        assertEquals(explicit.hashCode(), implicit.hashCode())
        assertEquals(ReviewSort.CREATED_AT_DESC, implicit.sort)
    }

    @Test
    fun `equivalent null sort and default sort queries share one snapshot`() = runTest {
        val store = InMemoryReviewStore()
        val implicit = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = null)
        val explicit = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC)

        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = implicit,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L), review(2L)),
                pageInfo = pageInfo(1),
            ),
        )
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = explicit,
                page = 2,
                token = 1L,
                reviews = listOf(review(3L)),
                pageInfo = pageInfo(2),
            ),
        )

        assertEquals(1, store.state.value.queries.size)
        assertEquals(listOf(1L, 2L, 3L), store.state.value.queries.getValue(implicit).orderedReviewIds)
        assertEquals(listOf(1L, 2L, 3L), store.state.value.queries.getValue(explicit).orderedReviewIds)
    }

    @Test
    fun `saved review membership is media id and media type only`() = runTest {
        val store = InMemoryReviewStore()
        val mediaKey = ReviewQueryKey(mediaId = 100L, mediaType = MediaType.ANIME, sort = ReviewSort.ID_DESC)
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = mediaKey,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L)),
                pageInfo = pageInfo(1),
            ),
        )

        // Different media id: no match.
        store.apply(ReviewStoreChange.ReviewSaved(review(2L), revision = 1L, isCreate = true))
        assertFalse(store.state.value.queries.getValue(mediaKey).orderedReviewIds.contains(2L))

        // Matching media id, different media type: no match.
        store.apply(
            ReviewStoreChange.ReviewSaved(
                review = review(3L).copy(media = mediaSummary(103L).copy(type = MediaType.MANGA.name)),
                revision = 1L,
                isCreate = true,
            ),
        )
        assertFalse(store.state.value.queries.getValue(mediaKey).orderedReviewIds.contains(3L))

        // Matching media id and type: inserted.
        store.apply(ReviewStoreChange.ReviewSaved(review(4L).copy(media = mediaSummary(100L)), revision = 1L, isCreate = true))
        assertTrue(store.state.value.queries.getValue(mediaKey).orderedReviewIds.contains(4L))
    }

    @Test
    fun `sort is not membership`() = runTest {
        val store = InMemoryReviewStore()
        val otherSortKey = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.ID_DESC)
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = otherSortKey,
                page = 1,
                token = 1L,
                reviews = emptyList(),
                pageInfo = pageInfo(1),
            ),
        )

        store.apply(ReviewStoreChange.ReviewSaved(review(5L), revision = 1L, isCreate = true))

        assertEquals(listOf(5L), store.state.value.queries.getValue(otherSortKey).orderedReviewIds)
        assertFalse(store.state.value.queries.getValue(otherSortKey).stale)
    }

    @Test
    fun `same token page replacement replaces that page and preserves others`() = runTest {
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
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 2,
                token = 1L,
                reviews = listOf(review(5L), review(6L)),
                pageInfo = pageInfo(2),
            ),
        )

        val snapshot = store.state.value.queries.getValue(queryKey)
        assertEquals(listOf(1L, 2L, 5L, 6L), snapshot.orderedReviewIds)
        assertEquals(mapOf(1 to listOf(1L, 2L), 2 to listOf(5L, 6L)), snapshot.pageReviewIds)
        assertEquals(setOf(1, 2), snapshot.loadedPages)
        assertEquals(pageInfo(2), snapshot.pageInfo)
    }

    @Test
    fun `page one refresh replaces page one and clears later pages`() = runTest {
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
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 2L,
                reviews = listOf(review(7L), review(8L)),
                pageInfo = pageInfo(1),
            ),
        )

        val snapshot = store.state.value.queries.getValue(queryKey)
        assertEquals(listOf(7L, 8L), snapshot.orderedReviewIds)
        assertEquals(setOf(1), snapshot.loadedPages)
        assertEquals(pageInfo(1), snapshot.pageInfo)
    }

    @Test
    fun `older token page load is rejected`() = runTest {
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 2L,
                reviews = listOf(review(1L)),
                pageInfo = pageInfo(1),
            ),
        )
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 2,
                token = 2L,
                reviews = listOf(review(3L)),
                pageInfo = pageInfo(2),
            ),
        )
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 2,
                token = 1L,
                reviews = listOf(review(9L)),
                pageInfo = pageInfo(2),
            ),
        )

        val snapshot = store.state.value.queries.getValue(queryKey)
        assertEquals(listOf(1L, 3L), snapshot.orderedReviewIds)
        assertEquals(setOf(1, 2), snapshot.loadedPages)
        assertEquals(2L, snapshot.token)
    }

    @Test
    fun `retry after failed page commits with same token`() = runTest {
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
        // The failed page-2 fetch never reached the store; the same-token retry is accepted.
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 2,
                token = 1L,
                reviews = listOf(review(3L), review(4L)),
                pageInfo = pageInfo(2),
            ),
        )

        val snapshot = store.state.value.queries.getValue(queryKey)
        assertEquals(listOf(1L, 2L, 3L, 4L), snapshot.orderedReviewIds)
        assertEquals(setOf(1, 2), snapshot.loadedPages)
    }

    @Test
    fun `tombstoned review is re-admitted only by a newer token`() = runTest {
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
        store.apply(ReviewStoreChange.ReviewDeleted(reviewId = 2L, revision = 5L))

        // An older page token cannot re-admit the tombstoned review.
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 4L,
                reviews = listOf(review(1L), review(2L)),
                pageInfo = pageInfo(1),
            ),
        )
        assertFalse(store.state.value.reviewsById.containsKey(2L))
        assertFalse(store.state.value.queries.getValue(queryKey).orderedReviewIds.contains(2L))

        // A save with an older revision is rejected by the tombstone.
        store.apply(ReviewStoreChange.ReviewSaved(review(2L), revision = 4L, isCreate = false))
        assertFalse(store.state.value.reviewsById.containsKey(2L))

        // A newer token re-admits the review and clears the tombstone.
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 6L,
                reviews = listOf(review(1L), review(2L)),
                pageInfo = pageInfo(1),
            ),
        )
        assertTrue(store.state.value.reviewsById.containsKey(2L))
        assertEquals(listOf(1L, 2L), store.state.value.queries.getValue(queryKey).orderedReviewIds)
    }

    @Test
    fun `saved review placement follows provable id sort direction`() = runTest {
        val descendingKey = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.ID_DESC)
        val descendingStore = InMemoryReviewStore()
        descendingStore.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = descendingKey,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L), review(2L)),
                pageInfo = pageInfo(1),
            ),
        )
        descendingStore.apply(ReviewStoreChange.ReviewSaved(review(3L), revision = 1L, isCreate = true))

        val descendingSnapshot = descendingStore.state.value.queries.getValue(descendingKey)
        assertEquals(listOf(3L, 1L, 2L), descendingSnapshot.orderedReviewIds)
        assertFalse(descendingSnapshot.stale)

        val ascendingKey = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.ID)
        val ascendingStore = InMemoryReviewStore()
        ascendingStore.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = ascendingKey,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L), review(2L)),
                pageInfo = pageInfo(1),
            ),
        )
        ascendingStore.apply(ReviewStoreChange.ReviewSaved(review(3L), revision = 1L, isCreate = true))

        val ascendingSnapshot = ascendingStore.state.value.queries.getValue(ascendingKey)
        assertEquals(listOf(1L, 2L, 3L), ascendingSnapshot.orderedReviewIds)
        assertFalse(ascendingSnapshot.stale)
    }

    @Test
    fun `saved review for non provable sorts marks query stale instead of inserting`() = runTest {
        listOf(
            ReviewSort.RATING,
            ReviewSort.RATING_DESC,
            ReviewSort.SCORE,
            ReviewSort.SCORE_DESC,
            ReviewSort.UPDATED_AT,
            ReviewSort.UPDATED_AT_DESC,
        ).forEach { sort ->
            val store = InMemoryReviewStore()
            val key = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = sort)
            store.apply(
                ReviewStoreChange.PageLoaded(
                    queryKey = key,
                    page = 1,
                    token = 1L,
                    reviews = listOf(review(1L), review(2L)),
                    pageInfo = pageInfo(1),
                ),
            )
            store.apply(ReviewStoreChange.ReviewSaved(review(3L), revision = 1L, isCreate = true))

            val snapshot = store.state.value.queries.getValue(key)
            assertEquals("sort $sort", listOf(1L, 2L), snapshot.orderedReviewIds)
            assertTrue("sort $sort", snapshot.stale)
        }
    }

    @Test
    fun `id desc creates render highest id first regardless of arrival order`() = runTest {
        listOf(listOf(3L, 4L), listOf(4L, 3L)).forEach { arrival ->
            val store = InMemoryReviewStore()
            val key = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.ID_DESC)
            store.apply(
                ReviewStoreChange.PageLoaded(
                    queryKey = key,
                    page = 1,
                    token = 1L,
                    reviews = listOf(review(1L), review(2L)),
                    pageInfo = pageInfo(1),
                ),
            )
            arrival.forEach { id ->
                store.apply(ReviewStoreChange.ReviewSaved(review(id), revision = 1L, isCreate = true))
            }

            val snapshot = store.state.value.queries.getValue(key)
            assertEquals("arrival $arrival", listOf(4L, 3L, 1L, 2L), snapshot.orderedReviewIds)
            assertFalse("arrival $arrival", snapshot.stale)
        }
    }

    @Test
    fun `id creates render ascending regardless of arrival order`() = runTest {
        listOf(listOf(3L, 4L), listOf(4L, 3L)).forEach { arrival ->
            val store = InMemoryReviewStore()
            val key = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.ID)
            store.apply(
                ReviewStoreChange.PageLoaded(
                    queryKey = key,
                    page = 1,
                    token = 1L,
                    reviews = listOf(review(1L), review(2L)),
                    pageInfo = pageInfo(1),
                ),
            )
            arrival.forEach { id ->
                store.apply(ReviewStoreChange.ReviewSaved(review(id), revision = 1L, isCreate = true))
            }

            val snapshot = store.state.value.queries.getValue(key)
            assertEquals("arrival $arrival", listOf(1L, 2L, 3L, 4L), snapshot.orderedReviewIds)
            assertFalse("arrival $arrival", snapshot.stale)
        }
    }

    @Test
    fun `created at sorts mark query stale instead of locally inserting`() = runTest {
        listOf(ReviewSort.CREATED_AT, ReviewSort.CREATED_AT_DESC).forEach { sort ->
            val store = InMemoryReviewStore()
            val key = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = sort)
            store.apply(
                ReviewStoreChange.PageLoaded(
                    queryKey = key,
                    page = 1,
                    token = 1L,
                    reviews = listOf(review(1L), review(2L)),
                    pageInfo = pageInfo(1),
                ),
            )
            store.apply(ReviewStoreChange.ReviewSaved(review(3L), revision = 1L, isCreate = true))

            val snapshot = store.state.value.queries.getValue(key)
            assertEquals("sort $sort", listOf(1L, 2L), snapshot.orderedReviewIds)
            assertTrue("sort $sort", snapshot.stale)
        }
    }

    @Test
    fun `update of absent review marks query stale instead of inserting`() = runTest {
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L)),
                pageInfo = pageInfo(1),
            ),
        )

        store.apply(ReviewStoreChange.ReviewSaved(review(2L), revision = 1L, isCreate = false))

        val snapshot = store.state.value.queries.getValue(queryKey)
        assertEquals(listOf(1L), snapshot.orderedReviewIds)
        assertTrue(snapshot.stale)
        assertTrue(store.state.value.reviewsById.containsKey(2L))
    }

    @Test
    fun `update of non matching absent review does not affect query`() = runTest {
        val store = InMemoryReviewStore()
        val mediaKey = ReviewQueryKey(mediaId = 100L, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC)
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = mediaKey,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L)),
                pageInfo = pageInfo(1),
            ),
        )

        store.apply(ReviewStoreChange.ReviewSaved(review(4L), revision = 1L, isCreate = false))

        val snapshot = store.state.value.queries.getValue(mediaKey)
        assertEquals(listOf(1L), snapshot.orderedReviewIds)
        assertFalse(snapshot.stale)
    }

    @Test
    fun `rating existing review marks rating sorted query stale and preserves order`() = runTest {
        val key = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.RATING)
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = key,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L, rating = 10), review(2L, rating = 20)),
                pageInfo = pageInfo(1),
            ),
        )

        store.apply(ReviewStoreChange.ReviewRated(review(1L, rating = 80), revision = 2L))

        val snapshot = store.state.value.queries.getValue(key)
        assertEquals(listOf(1L, 2L), snapshot.orderedReviewIds)
        assertEquals(80, store.state.value.reviewsById.getValue(1L).review.rating)
        assertTrue(snapshot.stale)
    }

    @Test
    fun `rating existing review does not mark non rating sorted query stale`() = runTest {
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L, rating = 10), review(2L, rating = 20)),
                pageInfo = pageInfo(1),
            ),
        )

        store.apply(ReviewStoreChange.ReviewRated(review(1L, rating = 80), revision = 2L))

        val snapshot = store.state.value.queries.getValue(queryKey)
        assertEquals(listOf(1L, 2L), snapshot.orderedReviewIds)
        assertFalse(snapshot.stale)
    }

    @Test
    fun `saving existing review marks score sorted query stale and preserves order`() = runTest {
        val key = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.SCORE)
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = key,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L)),
                pageInfo = pageInfo(1),
            ),
        )

        store.apply(ReviewStoreChange.ReviewSaved(review(1L).copy(score = 90), revision = 2L, isCreate = false))

        val snapshot = store.state.value.queries.getValue(key)
        assertEquals(listOf(1L), snapshot.orderedReviewIds)
        assertEquals(90, store.state.value.reviewsById.getValue(1L).review.score)
        assertTrue(snapshot.stale)
    }

    @Test
    fun `saving existing review does not mark created at sorted query stale`() = runTest {
        val createdKey = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC)
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = createdKey,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L)),
                pageInfo = pageInfo(1),
            ),
        )

        store.apply(ReviewStoreChange.ReviewSaved(review(1L).copy(score = 90), revision = 2L, isCreate = false))

        val snapshot = store.state.value.queries.getValue(createdKey)
        assertEquals(listOf(1L), snapshot.orderedReviewIds)
        assertFalse(snapshot.stale)
    }

    @Test
    fun `stale flag clears on page one refresh`() = runTest {
        val key = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.RATING)
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = key,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L), review(2L)),
                pageInfo = pageInfo(1),
            ),
        )
        store.apply(ReviewStoreChange.ReviewRated(review(1L, rating = 80), revision = 2L))
        assertTrue(store.state.value.queries.getValue(key).stale)

        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = key,
                page = 1,
                token = 3L,
                reviews = listOf(review(1L, rating = 80), review(2L)),
                pageInfo = pageInfo(1),
            ),
        )
        assertFalse(store.state.value.queries.getValue(key).stale)
    }

    @Test
    fun `older page one response after sort key mutation does not clear stale state or rewrite membership`() = runTest {
        val key = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.RATING)
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = key,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L, rating = 10), review(2L, rating = 20)),
                pageInfo = pageInfo(1),
            ),
        )
        // Rating mutation on the active sort key invalidates the query.
        store.apply(ReviewStoreChange.ReviewRated(review(1L, rating = 80), revision = 2L))
        assertTrue(store.state.value.queries.getValue(key).stale)

        // The page-one response issued before the mutation lands late with the old token.
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = key,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L, rating = 10), review(2L, rating = 20)),
                pageInfo = pageInfo(1),
            ),
        )

        val snapshot = store.state.value.queries.getValue(key)
        assertTrue(snapshot.stale)
        assertEquals(2L, snapshot.staleSinceRevision)
        assertEquals(listOf(1L, 2L), snapshot.orderedReviewIds)
        assertEquals(80, store.state.value.reviewsById.getValue(1L).review.rating)
        assertEquals(2L, store.state.value.reviewsById.getValue(1L).revision)
    }

    @Test
    fun `older page one response after created at create does not clear stale state or drop created review`() = runTest {
        val key = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC)
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = key,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L), review(2L)),
                pageInfo = pageInfo(1),
            ),
        )
        // A create under an unprovable sort marks the query stale at the mutation revision.
        store.apply(ReviewStoreChange.ReviewSaved(review(3L), revision = 2L, isCreate = true))
        assertTrue(store.state.value.queries.getValue(key).stale)
        assertTrue(store.state.value.reviewsById.containsKey(3L))

        // The page-one response issued before the create lands late with the old token.
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = key,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L), review(2L)),
                pageInfo = pageInfo(1),
            ),
        )

        val snapshot = store.state.value.queries.getValue(key)
        assertTrue(snapshot.stale)
        assertEquals(2L, snapshot.staleSinceRevision)
        assertEquals(listOf(1L, 2L), snapshot.orderedReviewIds)
        assertTrue(store.state.value.reviewsById.containsKey(3L))
    }

    @Test
    fun `inserted review survives later page load`() = runTest {
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
        store.apply(ReviewStoreChange.ReviewSaved(review(3L), revision = 1L, isCreate = true))
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 2,
                token = 1L,
                reviews = listOf(review(4L)),
                pageInfo = pageInfo(2),
            ),
        )

        assertEquals(listOf(3L, 1L, 2L, 4L), store.state.value.queries.getValue(queryKey).orderedReviewIds)
    }

    @Test
    fun `deleted inserted review is removed from insertion buckets`() = runTest {
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L)),
                pageInfo = pageInfo(1),
            ),
        )
        store.apply(ReviewStoreChange.ReviewSaved(review(2L), revision = 1L, isCreate = true))
        assertEquals(listOf(2L, 1L), store.state.value.queries.getValue(queryKey).orderedReviewIds)

        store.apply(ReviewStoreChange.ReviewDeleted(reviewId = 2L, revision = 2L))

        assertEquals(listOf(1L), store.state.value.queries.getValue(queryKey).orderedReviewIds)
    }

    @Test
    fun `observeQuery exposes stale flag`() = runTest {
        val key = ReviewQueryKey(mediaId = null, mediaType = MediaType.ANIME, sort = ReviewSort.RATING)
        val store = InMemoryReviewStore()
        store.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = key,
                page = 1,
                token = 1L,
                reviews = listOf(review(1L)),
                pageInfo = pageInfo(1),
            ),
        )
        store.apply(ReviewStoreChange.ReviewRated(review(1L, rating = 80), revision = 2L))

        assertTrue(store.observeQuery(key).first().stale)
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
