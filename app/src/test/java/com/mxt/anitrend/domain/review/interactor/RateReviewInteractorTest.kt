package com.mxt.anitrend.domain.review.interactor

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.data.store.review.InMemoryReviewStore
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.repository.BrowseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class RateReviewInteractorTest {

    @Test
    fun `successful rate commits ReviewRated with mapped record and revision`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryReviewStore()
        val rated = review(id = 42L, rating = 55, ratingAmount = 3, userRating = "UP_VOTE")
        doReturn(Result.success(rated))
            .`when`(repository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = RateReviewInteractor(
            browseRepository = repository,
            mutationExecutor = mutationExecutor(backgroundScope),
            reviewStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(42L, ReviewRating.UP_VOTE)

        assertEquals(MutationResult.Success, result)
        val record = store.state.value.reviewsById.getValue(42L).review
        assertEquals(42L, record.id)
        assertEquals(55, record.rating)
        assertEquals(3, record.ratingAmount)
        assertEquals("UP_VOTE", record.userRating)
        assertEquals(1L, record.revision)
        assertEquals(1L, store.state.value.reviewsById.getValue(42L).revision)
    }

    @Test
    fun `failed rate returns Failure and does not commit`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryReviewStore()
        doReturn(Result.failure<Review>(IllegalStateException("rate failed")))
            .`when`(repository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = RateReviewInteractor(
            browseRepository = repository,
            mutationExecutor = mutationExecutor(backgroundScope),
            reviewStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(42L, ReviewRating.UP_VOTE)

        assertTrue(result is MutationResult.Failure)
        assertEquals("rate failed", (result as MutationResult.Failure).message)
        assertTrue(store.state.value.reviewsById.isEmpty())
    }

    @Test
    fun `stale rate response is rejected by store`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryReviewStore()
        doReturn(Result.success(review(id = 42L, rating = 55)))
            .`when`(repository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = RateReviewInteractor(
            browseRepository = repository,
            mutationExecutor = mutationExecutor(backgroundScope),
            reviewStore = store,
            requestSequence = RequestSequence(),
        )

        interactor(42L, ReviewRating.UP_VOTE)
        store.apply(
            ReviewStoreChange.ReviewRated(
                review = store.state.value.reviewsById.getValue(42L).review.copy(rating = 10, revision = 0L),
                revision = 0L,
            ),
        )

        assertEquals(55, store.state.value.reviewsById.getValue(42L).review.rating)
        assertEquals(1L, store.state.value.reviewsById.getValue(42L).revision)
    }

    @Test
    fun `null-nested rate response preserves existing display metadata while updating rating state`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryReviewStore()
        val existing = ReviewRecord(
            id = 42L,
            summary = "A solid series",
            mediaType = "ANIME",
            body = "Full body text",
            rating = 10,
            ratingAmount = 1,
            userRating = "DOWN_VOTE",
            score = 90,
            isPrivate = true,
            createdAt = 1_600_000_000L,
            user = UserSummaryRecord(
                id = 7L,
                name = "alice",
                avatar = "https://avatar-large",
                siteUrl = "https://anilist.co/user/alice",
            ),
            media = MediaSummaryRecord(
                id = 44L,
                titleUserPreferred = "Preferred",
                titleRomaji = "Romaji",
                titleEnglish = "English",
                titleOriginal = "Original",
                coverImage = "https://cover-extra-large",
                type = "ANIME",
                episodes = 12,
                chapters = 0,
                volumes = 0,
                status = "FINISHED",
                siteUrl = "https://media",
            ),
            revision = 0L,
        )
        store.apply(ReviewStoreChange.ReviewSaved(review = existing, revision = 0L))
        // Mirrors the live defect: RateReview returns "user": null and "media": null
        // (and no body/summary/mediaType), which plain Gson writes via reflection.
        doReturn(Result.success(nullNestedRateResponse(id = 42L, rating = 55, ratingAmount = 3, userRating = "UP_VOTE")))
            .`when`(repository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = RateReviewInteractor(
            browseRepository = repository,
            mutationExecutor = mutationExecutor(backgroundScope),
            reviewStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(42L, ReviewRating.UP_VOTE)

        assertEquals(MutationResult.Success, result)
        val record = store.state.value.reviewsById.getValue(42L).review
        assertEquals(42L, record.id)
        assertEquals(55, record.rating)
        assertEquals(3, record.ratingAmount)
        assertEquals("UP_VOTE", record.userRating)
        assertEquals(1L, record.revision)
        assertEquals(1L, store.state.value.reviewsById.getValue(42L).revision)
        // Display metadata retained from the existing committed record.
        assertEquals("A solid series", record.summary)
        assertEquals("ANIME", record.mediaType)
        assertEquals("Full body text", record.body)
        assertEquals(90, record.score)
        assertTrue(record.isPrivate)
        assertEquals(1_600_000_000L, record.createdAt)
        assertEquals(7L, record.user?.id)
        assertEquals("alice", record.user?.name)
        assertEquals("https://avatar-large", record.user?.avatar)
        assertEquals(44L, record.media?.id)
        assertEquals("Preferred", record.media?.titleUserPreferred)
        assertEquals("Romaji", record.media?.titleRomaji)
        assertEquals("English", record.media?.titleEnglish)
        assertEquals("Original", record.media?.titleOriginal)
        assertEquals("https://cover-extra-large", record.media?.coverImage)
        assertEquals("ANIME", record.media?.type)
    }

    @Test
    fun `rate response with non-null mediaType and null user media still preserves existing display metadata`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryReviewStore()
        val existing = ReviewRecord(
            id = 42L,
            summary = "A solid series",
            mediaType = "MANGA",
            body = "Full body text",
            rating = 10,
            ratingAmount = 1,
            userRating = "DOWN_VOTE",
            score = 90,
            isPrivate = true,
            createdAt = 1_600_000_000L,
            user = UserSummaryRecord(
                id = 7L,
                name = "alice",
                avatar = "https://avatar-large",
                siteUrl = "https://anilist.co/user/alice",
            ),
            media = MediaSummaryRecord(
                id = 44L,
                titleUserPreferred = "Preferred",
                titleRomaji = "Romaji",
                titleEnglish = "English",
                titleOriginal = "Original",
                coverImage = "https://cover-extra-large",
                type = "MANGA",
                episodes = 12,
                chapters = 0,
                volumes = 0,
                status = "FINISHED",
                siteUrl = "https://media",
            ),
            revision = 0L,
        )
        store.apply(ReviewStoreChange.ReviewSaved(review = existing, revision = 0L))
        // Regression: the response carries a non-null mediaType ("ANIME") while
        // user/media are null. mediaType must not be used to treat the response
        // as full: the existing author/media/body/summary metadata must survive
        // and the response mediaType must not leak into the committed record.
        doReturn(
            Result.success(
                nullNestedRateResponse(
                    id = 42L,
                    rating = 55,
                    ratingAmount = 3,
                    userRating = "UP_VOTE",
                    mediaType = "ANIME",
                ),
            ),
        )
            .`when`(repository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = RateReviewInteractor(
            browseRepository = repository,
            mutationExecutor = mutationExecutor(backgroundScope),
            reviewStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(42L, ReviewRating.UP_VOTE)

        assertEquals(MutationResult.Success, result)
        val record = store.state.value.reviewsById.getValue(42L).review
        assertEquals(42L, record.id)
        assertEquals(55, record.rating)
        assertEquals(3, record.ratingAmount)
        assertEquals("UP_VOTE", record.userRating)
        assertEquals(1L, record.revision)
        assertEquals(1L, store.state.value.reviewsById.getValue(42L).revision)
        // Display metadata retained from the existing committed record,
        // including the existing mediaType instead of the response's "ANIME".
        assertEquals("A solid series", record.summary)
        assertEquals("MANGA", record.mediaType)
        assertEquals("Full body text", record.body)
        assertEquals(90, record.score)
        assertTrue(record.isPrivate)
        assertEquals(1_600_000_000L, record.createdAt)
        assertEquals(7L, record.user?.id)
        assertEquals("alice", record.user?.name)
        assertEquals("https://avatar-large", record.user?.avatar)
        assertEquals(44L, record.media?.id)
        assertEquals("Preferred", record.media?.titleUserPreferred)
        assertEquals("Romaji", record.media?.titleRomaji)
        assertEquals("English", record.media?.titleEnglish)
        assertEquals("Original", record.media?.titleOriginal)
        assertEquals("https://cover-extra-large", record.media?.coverImage)
        assertEquals("MANGA", record.media?.type)
    }

    @Test
    fun `null-nested rate response with no existing record commits mapped response as-is`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryReviewStore()
        doReturn(Result.success(nullNestedRateResponse(id = 42L, rating = 55, ratingAmount = 3, userRating = "UP_VOTE")))
            .`when`(repository)
            .rateReview(
                id = 42L,
                rating = ReviewRating.UP_VOTE,
                asHtml = false,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = RateReviewInteractor(
            browseRepository = repository,
            mutationExecutor = mutationExecutor(backgroundScope),
            reviewStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(42L, ReviewRating.UP_VOTE)

        assertEquals(MutationResult.Success, result)
        val record = store.state.value.reviewsById.getValue(42L).review
        assertEquals(42L, record.id)
        assertEquals(55, record.rating)
        assertEquals(3, record.ratingAmount)
        assertEquals("UP_VOTE", record.userRating)
        assertEquals(1L, record.revision)
        assertNull(record.user)
        assertNull(record.media)
        assertNull(record.summary)
        assertNull(record.body)
        assertNull(record.mediaType)
    }

    private fun mutationExecutor(scope: CoroutineScope) = DefaultMutationExecutor(
        applicationScope = scope,
        keyedMutex = KeyedMutex(scope),
        mutationRegistry = DefaultMutationRegistry(),
        operationIdGenerator = DefaultOperationIdGenerator(),
        sessionEpoch = SessionEpoch(),
    )

    private fun review(
        id: Long,
        rating: Int = 0,
        ratingAmount: Int = 0,
        userRating: String? = null,
    ): Review = Review().apply {
        this.id = id
        this.rating = rating
        this.ratingAmount = ratingAmount
        this.userRating = userRating
        media.id = 100L
        media.type = MediaType.ANIME.name
    }

    /**
     * Builds a Review with runtime-null `user` and `media` (and optional
     * body/summary/mediaType), mirroring how plain Gson deserializes a partial
     * RateReview response: reflection writes the nulls into the declared
     * non-null legacy properties, which Kotlin cannot express directly.
     */
    private fun nullNestedRateResponse(
        id: Long,
        rating: Int,
        ratingAmount: Int,
        userRating: String?,
        mediaType: String? = null,
    ): Review {
        val userRatingJson = userRating?.let { "\"$it\"" } ?: "null"
        val mediaTypeJson = mediaType?.let { "\"$it\"" } ?: "null"
        return productionGson.fromJson(
            """{"id":$id,"rating":$rating,"ratingAmount":$ratingAmount,"userRating":$userRatingJson,"mediaType":$mediaTypeJson,"user":null,"media":null}""",
            Review::class.java,
        )
    }

    private val productionGson: Gson =
        GsonBuilder()
            .enableComplexMapKeySerialization()
            .setLenient()
            .create()
}
