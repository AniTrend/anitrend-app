package com.mxt.anitrend.view.sheet

import com.mxt.anitrend.domain.feed.interactor.SaveFeedRequest
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.navigation.model.FeedComposerScreenParam
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Focused tests for the composer save mapping (ADR Phase 2 lane A).
 *
 * The mapping is the single conversion point between the sheet's immutable identity/draft
 * parameter and the domain save request. It never receives a legacy [FeedList], so a failed
 * submission cannot mutate a source feed or draft.
 */
class ComposerSaveLogicTest {

    @Test
    fun `text mode edit maps identity and draft into Text request`() {
        val param = FeedComposerScreenParam(feedId = 5L, draftText = "original")

        val request = buildComposerSaveRequest(KeyUtil.MUT_SAVE_TEXT_FEED, param, "edited")

        assertEquals(SaveFeedRequest.Text(id = 5L, text = "edited"), request)
    }

    @Test
    fun `text mode new post maps to Text request without feed id`() {
        val request = buildComposerSaveRequest(KeyUtil.MUT_SAVE_TEXT_FEED, FeedComposerScreenParam(), "hello")

        assertEquals(SaveFeedRequest.Text(id = null, text = "hello"), request)
    }

    @Test
    fun `message mode maps recipient identity into Message request`() {
        val param = FeedComposerScreenParam(
            feedId = 5L,
            draftText = "original",
            recipientId = 9L,
            recipientName = "Raki",
        )

        val request = buildComposerSaveRequest(KeyUtil.MUT_SAVE_MESSAGE_FEED, param, "hi")

        assertEquals(SaveFeedRequest.Message(id = 5L, message = "hi", recipientId = 9L), request)
    }

    @Test
    fun `message mode without recipient defaults to legacy zero recipient id`() {
        val request = buildComposerSaveRequest(KeyUtil.MUT_SAVE_MESSAGE_FEED, FeedComposerScreenParam(), "hi")

        assertEquals(SaveFeedRequest.Message(id = null, message = "hi", recipientId = 0L), request)
    }

    @Test
    fun `non composer request mode maps to null`() {
        assertNull(buildComposerSaveRequest(KeyUtil.MUT_SAVE_FEED_REPLY, FeedComposerScreenParam(), "hi"))
    }

    @Test
    fun `ui model edit extracts only feed id and draft text into composer param`() {
        val item = feedItem(id = 7L, text = "original draft")

        val param = item.toComposerParam()

        assertEquals(7L, param.feedId)
        assertEquals("original draft", param.draftText)
        assertNull(param.recipientId)
        assertNull(param.recipientName)
    }

    @Test
    fun `ui model edit preserves existing recipient identity in composer param`() {
        val item = feedItem(id = 7L, text = "original draft")

        val param = item.toComposerParam(
            FeedComposerScreenParam(recipientId = 9L, recipientName = "Raki"),
        )

        assertEquals(7L, param.feedId)
        assertEquals("original draft", param.draftText)
        assertEquals(9L, param.recipientId)
        assertEquals("Raki", param.recipientName)
    }

    private fun feedItem(
        id: Long,
        text: String,
    ): FeedItemUiModel = FeedItemUiModel(
        id = id,
        type = "MESSAGE",
        headline = "headline",
        body = text,
        likeCount = 0,
        isLikedByCurrentUser = false,
        replyCount = 0,
        canEdit = true,
        canDelete = true,
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
        feedText = text,
    )
}
