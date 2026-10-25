package com.mxt.anitrend.ui.feed

import com.mxt.anitrend.ui.media.ManualGridAppendGate
import com.mxt.anitrend.domain.model.FeedItemUiModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedPageSnapshotTest {
    @Test
    fun `append gate counts only unique ids newly present in the complete snapshot`() {
        val previousIds = setOf(1L, 2L)
        val snapshot = listOf(feedItem(1L), feedItem(2L), feedItem(2L), feedItem(3L), feedItem(3L))
        val gate = ManualGridAppendGate()

        assertEquals(1, countNewFeedItems(previousIds = previousIds, snapshot = snapshot))
        assertEquals(3, countNewFeedItems(previousIds = emptySet(), snapshot = snapshot))

        gate.onRequestStarted()
        gate.onRequestSucceeded(countNewFeedItems(previousIds, listOf(feedItem(1L), feedItem(1L))))
        assertFalse("duplicate-only append must not skip ahead", gate.canRequestNextPage)

        gate.onRequestStarted()
        gate.onRequestSucceeded(countNewFeedItems(previousIds, snapshot))
        assertTrue("a productive page releases automatic paging", gate.canRequestNextPage)
    }

    private fun feedItem(id: Long) = FeedItemUiModel(
        id = id,
        type = "TEXT",
        headline = "headline",
        body = "body",
        likeCount = 0,
        isLikedByCurrentUser = false,
        replyCount = 0,
        canEdit = false,
        canDelete = false,
        isLikePending = false,
        isDeletePending = false,
        createdAt = 0L,
        userAvatarUrl = null,
        userName = null,
        userId = null,
        messengerAvatarUrl = null,
        messengerName = null,
        messengerId = null,
        recipientAvatarUrl = null,
        recipientName = null,
        recipientId = null,
        mediaId = null,
        mediaType = null,
        mediaTitleEnglish = null,
        mediaTitleOriginal = null,
        mediaCoverImageUrl = null,
        hasLikes = false,
        likes = emptyList(),
        feedText = null,
    )
}
