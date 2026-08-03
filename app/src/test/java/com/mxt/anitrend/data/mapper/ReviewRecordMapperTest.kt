package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewRecordMapperTest {

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
