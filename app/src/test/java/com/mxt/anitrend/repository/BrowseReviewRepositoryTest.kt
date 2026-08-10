package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import com.mxt.anitrend.data.mapper.toReviewRecord
import com.mxt.anitrend.data.store.review.InMemoryReviewStore
import com.mxt.anitrend.data.store.review.ReviewQueryKey
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.graphql.generated.DeleteReview
import com.mxt.anitrend.graphql.generated.DeleteReviewData
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.RateReview
import com.mxt.anitrend.graphql.generated.RateReviewData
import com.mxt.anitrend.graphql.generated.ReviewBrowse
import com.mxt.anitrend.graphql.generated.ReviewBrowseData
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.graphql.generated.ReviewSort
import com.mxt.anitrend.graphql.generated.SaveReview
import com.mxt.anitrend.graphql.generated.SaveReviewData
import com.mxt.anitrend.model.api.retro.anilist.BrowseService
import com.mxt.anitrend.model.entity.anilist.Review
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseReviewRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(BrowseService::class.java)
    private val reviewStore = InMemoryReviewStore()
    private val repository = BrowseRepository(
        browseService = service,
        ioDispatcher = testDispatcher,
        mediaListStore = null,
        reviewStore = reviewStore,
    )

    @Test
    fun `getReviewBrowse returns legacy page and commits PageLoaded with ReviewRecords`() = runTest {
        val review = review(id = 33L, mediaId = 100L, rating = 20)
        val request = ReviewBrowse.request(
            page = 1,
            perPage = 20,
            mediaId = 100,
            type = MediaType.ANIME,
            sort = listOf(ReviewSort.CREATED_AT_DESC),
            asHtml = false,
        )
        val response = success(GraphQLResponse(data = GraphQLData.Present(reviewBrowseData(review)), errors = emptyList()))
        `when`(service.getReviewBrowse(request)).thenReturn(response)
        val queryKey = ReviewQueryKey(mediaId = 100L, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC)

        val result = repository.getReviewBrowse(
            page = 1,
            perPage = 20,
            mediaId = 100L,
            type = MediaType.ANIME,
            sort = listOf(ReviewSort.CREATED_AT_DESC),
            asHtml = false,
            commitToStore = true,
            queryKey = queryKey,
            readToken = 3L,
        )

        assertTrue(result.isSuccess)
        assertEquals(33L, result.getOrThrow().pageData.single().id)

        val storeRecord = reviewStore.state.value.reviewsById.getValue(33L)
        assertEquals(33L, storeRecord.review.id)
        assertEquals(20, storeRecord.review.rating)
        assertEquals(100L, storeRecord.review.media?.id)
        assertEquals(3L, storeRecord.review.revision)
        assertEquals(3L, storeRecord.revision)
        assertEquals(listOf(33L), reviewStore.state.value.queries.getValue(queryKey).orderedReviewIds)
        assertEquals(3L, reviewStore.state.value.queries.getValue(queryKey).token)
    }

    @Test
    fun `getReviewBrowse with null page fails with empty response body and leaves store unchanged`() = runTest {
        val request = ReviewBrowse.request(
            page = 1,
            perPage = 20,
            mediaId = 100,
            type = MediaType.ANIME,
            sort = listOf(ReviewSort.CREATED_AT_DESC),
            asHtml = false,
        )
        val response = success(GraphQLResponse(data = GraphQLData.Present(ReviewBrowseData(page = null)), errors = emptyList()))
        `when`(service.getReviewBrowse(request)).thenReturn(response)

        val result = repository.getReviewBrowse(
            page = 1,
            perPage = 20,
            mediaId = 100L,
            type = MediaType.ANIME,
            sort = listOf(ReviewSort.CREATED_AT_DESC),
            asHtml = false,
            commitToStore = true,
            queryKey = ReviewQueryKey(mediaId = 100L, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC),
            readToken = 3L,
        )

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
        assertTrue(reviewStore.state.value.queries.isEmpty())
        assertTrue(reviewStore.state.value.reviewsById.isEmpty())
    }

    @Test
    fun `rateReview returns legacy Review and commits ReviewRated with mapped record`() = runTest {
        val rated = ratedReview(id = 42L, mediaId = 100L, rating = 55, ratingAmount = 3, userRating = ReviewRating.UP_VOTE)
        val request = RateReview.request(id = 42, rating = ReviewRating.UP_VOTE, asHtml = false)
        val response = success(GraphQLResponse(data = GraphQLData.Present(rated), errors = emptyList()))
        `when`(service.rateReview(request)).thenReturn(response)

        val result = repository.rateReview(
            id = 42L,
            rating = ReviewRating.UP_VOTE,
            asHtml = false,
            commitToStore = true,
            revision = 2L,
        )

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrThrow().id)

        val record = reviewStore.state.value.reviewsById.getValue(42L).review
        assertEquals(55, record.rating)
        assertEquals(3, record.ratingAmount)
        assertEquals("UP_VOTE", record.userRating)
        assertEquals(2L, record.revision)
        assertEquals(2L, reviewStore.state.value.reviewsById.getValue(42L).revision)
    }

    @Test
    fun `saveReview returns legacy Review and commits ReviewSaved with mapped record`() = runTest {
        val saved = savedReview(id = 12L, mediaId = 100L, summary = "saved summary", score = 80)
        val request = SaveReview.request(
            id = null,
            mediaId = 100,
            body = "body",
            summary = "saved summary",
            score = 80,
            privateValue = false,
            asHtml = false,
        )
        val response = success(GraphQLResponse(data = GraphQLData.Present(saved), errors = emptyList()))
        `when`(service.saveReview(request)).thenReturn(response)

        val result = repository.saveReview(
            id = null,
            mediaId = 100L,
            body = "body",
            summary = "saved summary",
            score = 80,
            private = false,
            asHtml = false,
            commitToStore = true,
            revision = 2L,
        )

        assertTrue(result.isSuccess)
        assertEquals(12L, result.getOrThrow().id)

        val record = reviewStore.state.value.reviewsById.getValue(12L).review
        assertEquals("saved summary", record.summary)
        assertEquals(80, record.score)
        assertEquals(2L, record.revision)
    }

    @Test
    fun `saveReview with null id commits create and inserts at provable boundary`() = runTest {
        val queryKey = ReviewQueryKey(mediaId = 100L, mediaType = MediaType.ANIME, sort = ReviewSort.ID_DESC)
        reviewStore.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 1L,
                reviews = listOf(legacyReview(id = 9L, mediaId = 100L).toReviewRecord(revision = 1L)),
                pageInfo = null,
            ),
        )
        val saved = savedReview(id = 12L, mediaId = 100L, summary = "saved summary", score = 80)
        val request = SaveReview.request(
            id = null,
            mediaId = 100,
            body = "body",
            summary = "saved summary",
            score = 80,
            privateValue = false,
            asHtml = false,
        )
        val response = success(GraphQLResponse(data = GraphQLData.Present(saved), errors = emptyList()))
        `when`(service.saveReview(request)).thenReturn(response)

        val result = repository.saveReview(
            id = null,
            mediaId = 100L,
            body = "body",
            summary = "saved summary",
            score = 80,
            private = false,
            asHtml = false,
            commitToStore = true,
            revision = 2L,
        )

        assertTrue(result.isSuccess)
        val snapshot = reviewStore.state.value.queries.getValue(queryKey)
        assertEquals(listOf(12L, 9L), snapshot.orderedReviewIds)
        assertFalse(snapshot.stale)
    }

    @Test
    fun `saveReview with non null id commits update without local boundary insertion`() = runTest {
        val queryKey = ReviewQueryKey(mediaId = 100L, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC)
        reviewStore.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 1L,
                reviews = listOf(legacyReview(id = 9L, mediaId = 100L).toReviewRecord(revision = 1L)),
                pageInfo = null,
            ),
        )
        val saved = savedReview(id = 12L, mediaId = 100L, summary = "updated summary", score = 90)
        val request = SaveReview.request(
            id = 12,
            mediaId = 100,
            body = "body",
            summary = "updated summary",
            score = 90,
            privateValue = false,
            asHtml = false,
        )
        val response = success(GraphQLResponse(data = GraphQLData.Present(saved), errors = emptyList()))
        `when`(service.saveReview(request)).thenReturn(response)

        val result = repository.saveReview(
            id = 12,
            mediaId = 100L,
            body = "body",
            summary = "updated summary",
            score = 90,
            private = false,
            asHtml = false,
            commitToStore = true,
            revision = 2L,
        )

        assertTrue(result.isSuccess)
        val snapshot = reviewStore.state.value.queries.getValue(queryKey)
        assertEquals(listOf(9L), snapshot.orderedReviewIds)
        assertTrue(snapshot.stale)
        assertEquals(90, reviewStore.state.value.reviewsById.getValue(12L).review.score)
    }

    @Test
    fun `deleteReview commits ReviewDeleted when deleted`() = runTest {
        val queryKey = ReviewQueryKey(mediaId = 100L, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC)
        reviewStore.apply(
            ReviewStoreChange.PageLoaded(
                queryKey = queryKey,
                page = 1,
                token = 1L,
                reviews = listOf(legacyReview(id = 9L, mediaId = 100L).toReviewRecord(revision = 1L)),
                pageInfo = null,
            ),
        )
        val request = DeleteReview.request(id = 9)
        val response = success(
            GraphQLResponse(
                data = GraphQLData.Present(DeleteReviewData(deleteReview = DeleteReviewData.DeleteReview(deleted = true))),
                errors = emptyList(),
            ),
        )
        `when`(service.deleteReview(request)).thenReturn(response)

        val result = repository.deleteReview(id = 9L, commitToStore = true, revision = 2L)

        assertTrue(result.isSuccess)
        assertFalse(reviewStore.state.value.reviewsById.containsKey(9L))
        assertFalse(reviewStore.state.value.queries.getValue(queryKey).orderedReviewIds.contains(9L))
    }

    @Test
    fun `stale page load does not overwrite newer rated state`() = runTest {
        reviewStore.apply(
            ReviewStoreChange.ReviewRated(
                review = legacyReview(id = 7L, mediaId = 100L, rating = 88).toReviewRecord(revision = 5L),
                revision = 5L,
            ),
        )
        val stale = review(id = 7L, mediaId = 100L, rating = 10)
        val request = ReviewBrowse.request(
            page = 1,
            perPage = 20,
            mediaId = 100,
            type = MediaType.ANIME,
            sort = listOf(ReviewSort.CREATED_AT_DESC),
            asHtml = false,
        )
        val response = success(GraphQLResponse(data = GraphQLData.Present(reviewBrowseData(stale)), errors = emptyList()))
        `when`(service.getReviewBrowse(request)).thenReturn(response)

        repository.getReviewBrowse(
            page = 1,
            perPage = 20,
            mediaId = 100L,
            type = MediaType.ANIME,
            sort = listOf(ReviewSort.CREATED_AT_DESC),
            asHtml = false,
            commitToStore = true,
            queryKey = ReviewQueryKey(mediaId = 100L, mediaType = MediaType.ANIME, sort = ReviewSort.CREATED_AT_DESC),
            readToken = 4L,
        )

        assertEquals(88, reviewStore.state.value.reviewsById.getValue(7L).review.rating)
        assertEquals(5L, reviewStore.state.value.reviewsById.getValue(7L).revision)
    }

    @Test
    fun `review store is never committed legacy Review entities`() = runTest {
        val rated = ratedReview(id = 55L, mediaId = 100L, rating = 77)
        val request = RateReview.request(id = 55, rating = ReviewRating.UP_VOTE, asHtml = false)
        val response = success(GraphQLResponse(data = GraphQLData.Present(rated), errors = emptyList()))
        `when`(service.rateReview(request)).thenReturn(response)

        val result = repository.rateReview(
            id = 55L,
            rating = ReviewRating.UP_VOTE,
            asHtml = false,
            commitToStore = true,
            revision = 1L,
        )

        val committed = reviewStore.state.value.reviewsById.getValue(55L)
        assertEquals(55L, committed.review.id)
        assertEquals(77, committed.review.rating)
        // Legacy entity mutation after commit must not affect the immutable record.
        result.getOrThrow().rating = 1
        assertEquals(77, reviewStore.state.value.reviewsById.getValue(55L).review.rating)
    }

    private fun legacyReview(
        id: Long,
        mediaId: Long,
        rating: Int = 0,
    ): Review = Review().apply {
        this.id = id
        this.rating = rating
        media.id = mediaId
        media.type = MediaType.ANIME.name
    }

    private fun review(
        id: Long,
        mediaId: Long,
        rating: Int = 0,
        ratingAmount: Int = 0,
        userRating: ReviewRating? = null,
    ): ReviewBrowseData.PageReviews = ReviewBrowseData.PageReviews(
        body = null,
        createdAt = 1,
        id = id.toInt(),
        media = ReviewBrowseData.PageReviewsMedia(
            averageScore = null,
            bannerImage = null,
            chapters = null,
            coverImage = null,
            endDate = null,
            episodes = null,
            format = null,
            id = mediaId.toInt(),
            isAdult = null,
            isFavourite = false,
            meanScore = null,
            mediaListEntry = null,
            nextAiringEpisode = null,
            season = null,
            siteUrl = null,
            startDate = null,
            status = null,
            title = null,
            type = MediaType.ANIME,
            updatedAt = null,
            volumes = null,
        ),
        mediaType = MediaType.ANIME,
        privateValue = false,
        rating = rating,
        ratingAmount = ratingAmount,
        score = 0,
        siteUrl = null,
        summary = null,
        updatedAt = 1,
        user = null,
        userRating = userRating,
    )

    private fun reviewBrowseData(review: ReviewBrowseData.PageReviews): ReviewBrowseData = ReviewBrowseData(
        page = ReviewBrowseData.Page(
            pageInfo = ReviewBrowseData.PagePageInfo(
                currentPage = 1,
                hasNextPage = false,
                lastPage = 1,
                perPage = 1,
                total = 1,
            ),
            reviews = listOf(review),
        ),
    )

    private fun ratedReview(
        id: Long,
        mediaId: Long,
        rating: Int = 0,
        ratingAmount: Int = 0,
        userRating: ReviewRating? = null,
    ): RateReviewData = RateReviewData(
        rateReview = RateReviewData.RateReview(
            body = null,
            createdAt = 1,
            id = id.toInt(),
            media = RateReviewData.RateReviewMedia(
                averageScore = null,
                bannerImage = null,
                chapters = null,
                coverImage = null,
                endDate = null,
                episodes = null,
                format = null,
                id = mediaId.toInt(),
                isAdult = null,
                isFavourite = false,
                meanScore = null,
                mediaListEntry = null,
                nextAiringEpisode = null,
                season = null,
                siteUrl = null,
                startDate = null,
                status = null,
                title = null,
                type = MediaType.ANIME,
                updatedAt = null,
                volumes = null,
            ),
            mediaType = MediaType.ANIME,
            privateValue = false,
            rating = rating,
            ratingAmount = ratingAmount,
            score = 0,
            siteUrl = null,
            summary = null,
            updatedAt = 1,
            user = null,
            userRating = userRating,
        ),
    )

    private fun savedReview(
        id: Long,
        mediaId: Long,
        summary: String,
        score: Int,
    ): SaveReviewData = SaveReviewData(
        saveReview = SaveReviewData.SaveReview(
            body = null,
            createdAt = 1,
            id = id.toInt(),
            media = SaveReviewData.SaveReviewMedia(
                averageScore = null,
                bannerImage = null,
                chapters = null,
                coverImage = null,
                endDate = null,
                episodes = null,
                format = null,
                id = mediaId.toInt(),
                isAdult = null,
                isFavourite = false,
                meanScore = null,
                mediaListEntry = null,
                nextAiringEpisode = null,
                season = null,
                siteUrl = null,
                startDate = null,
                status = null,
                title = null,
                type = MediaType.ANIME,
                updatedAt = null,
                volumes = null,
            ),
            mediaType = MediaType.ANIME,
            privateValue = false,
            rating = 0,
            ratingAmount = 0,
            score = score,
            siteUrl = null,
            summary = summary,
            updatedAt = 1,
            user = null,
            userRating = null,
        ),
    )

    private fun <T> success(body: T): Response<T> = Response.success(body)
}
