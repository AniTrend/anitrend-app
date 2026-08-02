package com.mxt.anitrend.domain.model

import com.mxt.anitrend.data.mapper.toFeedRecord
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedItemUiModelProjectionTest {

    @Test
    fun `record projection maps all visible fields`() {
        val record = createRecord()

        val model = record.toFeedItemUiModel(
            isLikePending = true,
            isDeletePending = true,
            currentUserId = 100L,
        )

        assertEquals(11L, model.id)
        assertEquals("TEXT", model.type)
        assertEquals("watched Hello world of: Romaji", model.headline)
        assertEquals("Hello world", model.body)
        assertEquals(2, model.likeCount)
        assertTrue(model.isLikedByCurrentUser)
        assertEquals(3, model.replyCount)
        assertTrue(model.canEdit)
        assertTrue(model.canDelete)
        assertTrue(model.isLikePending)
        assertTrue(model.isDeletePending)
        assertEquals(1234L, model.createdAt)
        assertEquals("https://avatar-large", model.userAvatarUrl)
        assertEquals("alice", model.userName)
        assertEquals(99L, model.userId)
        assertEquals("https://messenger-large", model.messengerAvatarUrl)
        assertEquals("messenger", model.messengerName)
        assertEquals(100L, model.messengerId)
        assertEquals("https://recipient-large", model.recipientAvatarUrl)
        assertEquals("recipient", model.recipientName)
        assertEquals(101L, model.recipientId)
        assertEquals(44L, model.mediaId)
        assertEquals("ANIME", model.mediaType)
        assertEquals("English", model.mediaTitleEnglish)
        assertEquals("Original", model.mediaTitleOriginal)
        assertEquals("https://cover-large", model.mediaCoverImageUrl)
        assertTrue(model.hasLikes)
        assertEquals(listOf(100L, 8L), model.likes.map { it.id })
        assertEquals("Hello world", model.feedText)
    }

    @Test
    fun `record projection without current user disables edit and delete`() {
        val model = createRecord().toFeedItemUiModel(
            isLikePending = false,
            isDeletePending = false,
        )

        assertFalse(model.canEdit)
        assertFalse(model.canDelete)
        assertFalse(model.isLikedByCurrentUser)
    }

    @Test
    fun `record projection resolves messenger ownership before user ownership`() {
        val record = createRecord(userId = 99L, messengerId = 200L)

        val model = record.toFeedItemUiModel(
            isLikePending = false,
            isDeletePending = false,
            currentUserId = 99L,
        )

        // Messenger is set, so ownership resolves against the messenger, not the user.
        assertFalse(model.canEdit)
        assertFalse(model.canDelete)
    }

    @Test
    fun `record projection takes an immutable snapshot of likes`() {
        val mutableLikes = mutableListOf(
            UserSummaryRecord(id = 1L, name = "one", avatar = null, siteUrl = null),
        )
        val record = createRecord(likes = mutableLikes)

        val model = record.toFeedItemUiModel(isLikePending = false, isDeletePending = false)

        assertNotSame(mutableLikes, model.likes)
        mutableLikes += UserSummaryRecord(id = 2L, name = "two", avatar = null, siteUrl = null)
        assertEquals(1, model.likes.size)
    }

    @Test
    fun `legacy projection stays available and matches the record projection`() {
        val feed = createLegacyFeed()
        val record = feed.toFeedRecord(revision = 1L)

        val legacy = feed.toFeedItemUiModel(currentUserId = 99L)
        val recordModel = record.toFeedItemUiModel(
            isLikePending = false,
            isDeletePending = false,
            currentUserId = 99L,
        )

        assertEquals(legacy.id, recordModel.id)
        assertEquals(legacy.headline, recordModel.headline)
        assertEquals(legacy.body, recordModel.body)
        assertEquals(legacy.userAvatarUrl, recordModel.userAvatarUrl)
        assertEquals(legacy.userName, recordModel.userName)
        assertEquals(legacy.mediaTitleEnglish, recordModel.mediaTitleEnglish)
        assertEquals(legacy.mediaTitleOriginal, recordModel.mediaTitleOriginal)
        assertEquals(legacy.mediaCoverImageUrl, recordModel.mediaCoverImageUrl)
        assertEquals(legacy.hasLikes, recordModel.hasLikes)
        assertEquals(legacy.likes, recordModel.likes)
        assertEquals(legacy.canEdit, recordModel.canEdit)
        assertEquals(legacy.canDelete, recordModel.canDelete)
    }

    private fun createRecord(
        userId: Long = 99L,
        messengerId: Long = 100L,
        likes: List<UserSummaryRecord> = listOf(
            UserSummaryRecord(id = 100L, name = "messenger", avatar = null, siteUrl = null),
            UserSummaryRecord(id = 8L, name = "eve", avatar = null, siteUrl = null),
        ),
    ): FeedRecord = FeedRecord(
        id = 11L,
        type = "TEXT",
        status = "watched",
        text = "Hello world",
        createdAt = 1234L,
        user = UserSummaryRecord(id = userId, name = "alice", avatar = "https://avatar-large", siteUrl = null),
        messenger = UserSummaryRecord(id = messengerId, name = "messenger", avatar = "https://messenger-large", siteUrl = null),
        recipient = UserSummaryRecord(id = 101L, name = "recipient", avatar = "https://recipient-large", siteUrl = null),
        media = createMediaRecord(),
        hasLikes = true,
        likes = likes,
        replyCount = 3,
        siteUrl = "https://feed",
        revision = 1L,
    )

    private fun createMediaRecord(): MediaSummaryRecord = MediaSummaryRecord(
        id = 44L,
        titleUserPreferred = "Preferred",
        titleRomaji = "Romaji",
        titleEnglish = "English",
        titleOriginal = "Original",
        coverImage = "https://cover-large",
        bannerImage = null,
        type = "ANIME",
        format = "TV",
        episodes = 12,
        chapters = 0,
        volumes = 0,
        status = "FINISHED",
        siteUrl = "https://media",
        isFavourite = false,
    )

    private fun createLegacyFeed(): FeedList = FeedList(
        id = 11L,
        replyCount = 3,
        type = "TEXT",
        status = "watched",
        text = "Hello world",
        createdAt = 1234L,
        user = createLegacyUser(99L, "alice"),
        media = createLegacyMedia(),
        messenger = createLegacyUser(100L, "messenger"),
        recipient = createLegacyUser(101L, "recipient"),
        likes = listOf(createLegacyUser(7L, "bob"), createLegacyUser(8L, "eve")),
        siteUrl = "https://feed",
    )

    private fun createLegacyUser(id: Long, name: String): UserBase = UserBase(name = name).also {
        it.id = id
        it.avatar = ImageBase(
            extraLarge = "https://avatar-extra-large",
            large = "https://avatar-large",
            medium = "https://avatar-medium",
        )
    }

    private fun createLegacyMedia(): MediaBase = MediaBase().also {
        it.id = 44L
        it.title = MediaTitle("Romaji", "English", "Original", "Preferred")
        it.coverImage = ImageBase(
            extraLarge = "https://cover-extra-large",
            large = "https://cover-large",
            medium = "https://cover-medium",
        )
        it.type = "ANIME"
        it.format = "TV"
        it.episodes = 12
        it.chapters = 0
        it.volumes = 0
        it.status = "FINISHED"
        it.siteUrl = "https://media"
    }
}
