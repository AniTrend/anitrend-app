package com.mxt.anitrend.view.fragment.list

import com.mxt.anitrend.domain.model.FeedItemUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Regression coverage for the FeedListFragment single-projection contract.
 *
 * The fragment's [FeedListFragment.handleSuccess] submits the ViewModel's canonical
 * [FeedItemUiModel] list to the adapter directly. The only transformation between the
 * store-backed items and the adapter submission is invalid-type filtering via
 * [renderableFeedItems]; store records are never re-projected and pending flags are
 * never merged a second time.
 */
class FeedListRenderableItemsTest {

    @Test
    fun `typed items pass through unchanged with no re projection`() {
        val typed = feedItem(id = 1L, type = "TEXT")
        val media = feedItem(id = 2L, type = "MEDIA_LIST")

        val rendered = renderableFeedItems(listOf(typed, media))

        assertEquals(listOf(1L, 2L), rendered.map { it.id })
        // The exact immutable instances are submitted: no copy or re-projection occurs.
        assertSame(typed, rendered[0])
        assertSame(media, rendered[1])
    }

    @Test
    fun `blank and null item types are filtered out`() {
        val typed = feedItem(id = 1L, type = "TEXT")
        val blank = feedItem(id = 2L, type = " ")
        val nullTyped = feedItem(id = 3L, type = null)

        val rendered = renderableFeedItems(listOf(blank, typed, nullTyped))

        assertEquals(listOf(1L), rendered.map { it.id })
    }

    private fun feedItem(
        id: Long,
        type: String?,
    ): FeedItemUiModel = FeedItemUiModel(
        id = id,
        type = type,
        headline = "headline",
        body = null,
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
