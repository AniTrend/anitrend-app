package com.mxt.anitrend.data.mapper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Type

class ReviewRecordMapperTest {

    /**
     * Mirrors the production Gson configuration used by the retrofit pipeline
     * (`ServiceFactory.gson` and the Koin `AniGraphConverter`): plain Gson with no
     * null-skipping or custom adapters. This is what deserializes `AniListContainer<Review>`
     * responses, so regression tests for the mapper must go through the same machinery.
     */
    private val productionGson: Gson =
        GsonBuilder()
            .enableComplexMapKeySerialization()
            .setLenient()
            .create()

    @Test
    fun `map legacy Review to ReviewRecord preserving all values`() {
        val review = createReview(
            id = 42L,
            summary = "A solid series",
            mediaType = "ANIME",
            body = "Full body text",
            rating = 80,
            ratingAmount = 120,
            userRating = "UP_VOTE",
            score = 90,
            isPrivate = true,
            createdAt = 1_600_000_000L,
            user = createUser(id = 7L, name = "alice"),
            media = createMedia(id = 44L),
        )

        val record = review.toReviewRecord(revision = 9L)

        assertEquals(42L, record.id)
        assertEquals("A solid series", record.summary)
        assertEquals("ANIME", record.mediaType)
        assertEquals("Full body text", record.body)
        assertEquals(80, record.rating)
        assertEquals(120, record.ratingAmount)
        assertEquals("UP_VOTE", record.userRating)
        assertEquals(90, record.score)
        assertTrue(record.isPrivate)
        assertEquals(1_600_000_000L, record.createdAt)
        assertEquals(7L, record.user?.id)
        assertEquals("alice", record.user?.name)
        assertEquals("https://avatar-large", record.user?.avatar)
        assertEquals(44L, record.media?.id)
        assertEquals("Romaji", record.media?.titleRomaji)
        assertEquals("English", record.media?.titleEnglish)
        assertEquals("Original", record.media?.titleOriginal)
        assertEquals("Preferred", record.media?.titleUserPreferred)
        assertEquals("https://cover-extra-large", record.media?.coverImage)
        assertEquals("ANIME", record.media?.type)
        assertEquals(9L, record.revision)
    }

    @Test
    fun `map default empty user and media to null nested summaries`() {
        val record = Review().toReviewRecord()

        assertNull(record.user)
        assertNull(record.media)
        assertEquals(0L, record.id)
        assertNull(record.summary)
        assertEquals(0, record.rating)
        assertEquals(0, record.ratingAmount)
        assertEquals(0, record.score)
        assertNull(record.userRating)
        assertNull(record.mediaType)
        assertFalse(record.isPrivate)
        assertEquals(0L, record.createdAt)
        assertEquals(0L, record.revision)
    }

    @Test
    fun `map real user with null name is preserved`() {
        val review = Review().apply {
            user = UserBase(name = null).also { it.id = 5L }
        }

        val record = review.toReviewRecord()

        assertEquals(5L, record.user?.id)
        assertNull(record.user?.name)
    }

    @Test
    fun `mutate source after mapping keeps ReviewRecord unchanged`() {
        val review = createReview(
            id = 42L,
            summary = "Original summary",
            user = createUser(id = 7L, name = "alice"),
            media = createMedia(id = 44L),
        )

        val record = review.toReviewRecord(revision = 3L)

        review.summary = "Changed summary"
        review.rating = 99
        review.user?.name = "changed-user"
        review.media?.id = 100L

        assertEquals("Original summary", record.summary)
        assertEquals(0, record.rating)
        assertEquals("alice", record.user?.name)
        assertEquals(44L, record.media?.id)
        assertEquals(3L, record.revision)
    }

    @Test
    fun `production Gson response with null user and media maps to null nested summaries`() {
        // Confirmed live defect: an authenticated RateReview response can contain
        // "user": null and "media": null. Plain Gson writes those nulls into the
        // declared non-null Review.user / Review.media fields via reflection, so the
        // mapper must treat a runtime null the same as the empty default instance.
        val review = productionGson.fromJson(
            """
            {"id":42,"summary":"A solid series","mediaType":"ANIME","body":"Full body text",
             "rating":55,"ratingAmount":3,"userRating":"UP_VOTE","score":90,"private":false,
             "createdAt":1600000000,"user":null,"media":null}
            """.trimIndent(),
            Review::class.java,
        )

        assertNull(review.user)
        assertNull(review.media)

        val record = review.toReviewRecord(revision = 7L)

        assertEquals(42L, record.id)
        assertEquals("A solid series", record.summary)
        assertEquals("ANIME", record.mediaType)
        assertEquals("Full body text", record.body)
        assertEquals(55, record.rating)
        assertEquals(3, record.ratingAmount)
        assertEquals("UP_VOTE", record.userRating)
        assertEquals(90, record.score)
        assertEquals(1_600_000_000L, record.createdAt)
        assertNull(record.user)
        assertNull(record.media)
        assertEquals(7L, record.revision)
    }

    @Test
    fun `production Gson RateReview container with null user and media unwraps and maps`() {
        val containerType: Type = object : TypeToken<AniListContainer<Review>>() {}.type
        val container = productionGson.fromJson<AniListContainer<Review>>(
            """
            {"data":{"RateReview":{"id":42,"rating":55,"ratingAmount":3,"userRating":"UP_VOTE",
             "user":null,"media":null}},"errors":null}
            """.trimIndent(),
            containerType,
        )

        val review = container.data?.result
        assertNull(review?.user)
        assertNull(review?.media)

        val record = review?.toReviewRecord(revision = 1L)

        assertEquals(42L, record?.id)
        assertEquals(55, record?.rating)
        assertEquals("UP_VOTE", record?.userRating)
        assertNull(record?.user)
        assertNull(record?.media)
        assertEquals(1L, record?.revision)
    }

    @Test
    fun `production Gson response with null media only preserves user summary`() {
        val review = productionGson.fromJson(
            """
            {"id":42,"rating":55,"user":{"id":7,"name":"alice","avatar":{"large":"https://avatar-large"}},"media":null}
            """.trimIndent(),
            Review::class.java,
        )

        assertNull(review.media)

        val record = review.toReviewRecord()

        assertEquals(42L, record.id)
        assertEquals(7L, record.user?.id)
        assertEquals("alice", record.user?.name)
        assertEquals("https://avatar-large", record.user?.avatar)
        assertNull(record.media)
    }

    @Test
    fun `production Gson response with user and media present maps full summaries`() {
        val review = productionGson.fromJson(
            """
            {"id":42,"rating":55,"user":{"id":7,"name":"alice","avatar":{"large":"https://avatar-large"}},
             "media":{"id":44,"title":{"romaji":"Romaji","english":"English","native":"Original","userPreferred":"Preferred"},
             "coverImage":{"extraLarge":"https://cover-extra-large"},"type":"ANIME"}}
            """.trimIndent(),
            Review::class.java,
        )

        val record = review.toReviewRecord(revision = 4L)

        assertEquals(7L, record.user?.id)
        assertEquals("alice", record.user?.name)
        assertEquals(44L, record.media?.id)
        assertEquals("Romaji", record.media?.titleRomaji)
        assertEquals("English", record.media?.titleEnglish)
        assertEquals("Original", record.media?.titleOriginal)
        assertEquals("Preferred", record.media?.titleUserPreferred)
        assertEquals("https://cover-extra-large", record.media?.coverImage)
        assertEquals("ANIME", record.media?.type)
        assertEquals(4L, record.revision)
    }

    private fun createReview(
        id: Long = 42L,
        summary: String? = null,
        mediaType: String? = null,
        body: String? = null,
        rating: Int = 0,
        ratingAmount: Int = 0,
        userRating: String? = null,
        score: Int = 0,
        isPrivate: Boolean = false,
        createdAt: Long = 0L,
        user: UserBase = UserBase(),
        media: MediaBase = MediaBase(),
    ): Review = Review().apply {
        this.id = id
        this.summary = summary
        this.mediaType = mediaType
        this.body = body
        this.rating = rating
        this.ratingAmount = ratingAmount
        this.userRating = userRating
        this.score = score
        this.isPrivate = isPrivate
        this.createdAt = createdAt
        this.user = user
        this.media = media
    }

    private fun createUser(id: Long, name: String): UserBase = UserBase(name = name).also {
        it.id = id
        it.avatar = ImageBase(
            extraLarge = "https://avatar-extra-large",
            large = "https://avatar-large",
            medium = "https://avatar-medium",
        )
    }

    private fun createMedia(id: Long): MediaBase = MediaBase().also {
        it.id = id
        it.title = MediaTitle("Romaji", "English", "Original", "Preferred")
        it.coverImage = ImageBase(
            extraLarge = "https://cover-extra-large",
            large = "https://cover-large",
            medium = "https://cover-medium",
        )
        it.type = "ANIME"
        it.episodes = 12
        it.chapters = 0
        it.volumes = 0
        it.status = "FINISHED"
        it.siteUrl = "https://media"
    }
}
