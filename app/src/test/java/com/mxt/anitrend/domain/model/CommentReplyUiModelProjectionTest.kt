package com.mxt.anitrend.domain.model

import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommentReplyUiModelProjectionTest {

    @Test
    fun `record projection maps all visible fields`() {
        val record = createRecord()

        val model = record.toCommentReplyUiModel(isLikePending = true, isDeletePending = true)

        assertEquals(22L, model.id)
        assertEquals("Reply body", model.reply)
        assertEquals(444L, model.createdAt)
        assertEquals(3L, model.userId)
        assertEquals("reply-user", model.userName)
        assertEquals("https://avatar-large", model.userAvatar)
        assertEquals(listOf(4L, 5L), model.likes.map { it.id })
        assertEquals(2, model.likeCount)
        assertTrue(model.isLikePending)
        assertTrue(model.isDeletePending)
    }

    @Test
    fun `record projection without pending state renders idle flags`() {
        val model = createRecord().toCommentReplyUiModel(isLikePending = false, isDeletePending = false)

        assertFalse(model.isLikePending)
        assertFalse(model.isDeletePending)
    }

    @Test
    fun `record projection takes an immutable snapshot of likes`() {
        val mutableLikes = mutableListOf(
            UserSummaryRecord(id = 1L, name = "one", avatar = null, siteUrl = null),
        )
        val record = createRecord(likes = mutableLikes)

        val model = record.toCommentReplyUiModel(isLikePending = false, isDeletePending = false)

        assertNotSame(mutableLikes, model.likes)
        mutableLikes += UserSummaryRecord(id = 2L, name = "two", avatar = null, siteUrl = null)
        assertEquals(1, model.likes.size)
        assertEquals(1, model.likeCount)
    }

    @Test
    fun `like state is precomputed from current user id`() {
        val record = createRecord(likes = listOf(liker(4L), liker(5L)))

        val liked = record.toCommentReplyUiModel(isLikePending = false, isDeletePending = false, currentUserId = 4L)
        val notLiked = record.toCommentReplyUiModel(isLikePending = false, isDeletePending = false, currentUserId = 9L)
        val anonymous = record.toCommentReplyUiModel(isLikePending = false, isDeletePending = false, currentUserId = null)

        assertTrue(liked.isLikedByCurrentUser)
        assertFalse(notLiked.isLikedByCurrentUser)
        assertFalse(anonymous.isLikedByCurrentUser)
    }

    @Test
    fun `record reverse projection bridges composer edit and mention paths`() {
        val record = createRecord()

        val replyRecord = record.toCommentReplyUiModel(
            isLikePending = false,
            isDeletePending = false,
            currentUserId = 5L,
        ).toFeedReplyRecord(activityId = 11L, revision = 3L)

        assertEquals(22L, replyRecord.id)
        assertEquals(11L, replyRecord.activityId)
        assertEquals("Reply body", replyRecord.reply)
        assertEquals(444L, replyRecord.createdAt)
        assertEquals(3L, replyRecord.user?.id)
        assertEquals("reply-user", replyRecord.user?.name)
        assertEquals("https://avatar-large", replyRecord.user?.avatar)
        assertEquals(listOf(4L, 5L), replyRecord.likes.map { it.id })
        assertEquals(3L, replyRecord.revision)
    }

    @Test
    fun `record reverse projection preserves null user and likes snapshot`() {
        val record = createRecord(user = null, likes = mutableListOf(liker(4L)))

        val replyRecord = record.toCommentReplyUiModel(
            isLikePending = false,
            isDeletePending = false,
            currentUserId = null,
        ).toFeedReplyRecord()

        assertNull(replyRecord.user)
        assertEquals(1, replyRecord.likes.size)
    }

    private fun liker(id: Long) = UserSummaryRecord(id = id, name = "liker-$id", avatar = null, siteUrl = null)

    private fun createRecord(
        user: UserSummaryRecord? = UserSummaryRecord(id = 3L, name = "reply-user", avatar = "https://avatar-large", siteUrl = null),
        likes: List<UserSummaryRecord> = listOf(
            UserSummaryRecord(id = 4L, name = "liker-one", avatar = null, siteUrl = null),
            UserSummaryRecord(id = 5L, name = "liker-two", avatar = null, siteUrl = null),
        ),
    ): FeedReplyRecord = FeedReplyRecord(
        id = 22L,
        activityId = 11L,
        reply = "Reply body",
        createdAt = 444L,
        user = user,
        likes = likes,
        revision = 9L,
    )
}
