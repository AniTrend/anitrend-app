package com.mxt.anitrend.domain.model

import com.mxt.anitrend.domain.feed.model.FeedRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedItemUiModelProjectionTest {

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
    fun `message sender owns actions but the recipient does not`() {
        val record = createRecord().copy(type = "MESSAGE")
        val sender = record.toFeedItemUiModel(isLikePending = false, isDeletePending = false, currentUserId = 100L)
        val recipient = record.toFeedItemUiModel(isLikePending = false, isDeletePending = false, currentUserId = 101L)

        assertTrue(sender.canEdit)
        assertTrue(sender.canDelete)
        assertFalse(recipient.canEdit)
        assertFalse(recipient.canDelete)
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

        mutableLikes += UserSummaryRecord(id = 2L, name = "two", avatar = null, siteUrl = null)
        assertEquals(listOf(1L), model.likes.map { it.id })
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
        media = null,
        hasLikes = true,
        likes = likes,
        replyCount = 3,
        siteUrl = "https://feed",
        revision = 1L,
    )
}
