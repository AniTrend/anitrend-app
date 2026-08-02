package com.mxt.anitrend.domain.model

import com.mxt.anitrend.adapter.recycler.detail.CommentListAdapter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommentListAdapterDiffTest {

    @Test
    fun `diff callback matches ids and full content equality`() {
        val original = model(id = 1L)
        val sameIdDifferentContent = original.copy(reply = "updated")
        val differentId = original.copy(id = 2L)

        assertTrue(CommentListAdapter.DIFF_CALLBACK.areItemsTheSame(original, sameIdDifferentContent))
        assertFalse(CommentListAdapter.DIFF_CALLBACK.areContentsTheSame(original, sameIdDifferentContent))
        assertFalse(CommentListAdapter.DIFF_CALLBACK.areItemsTheSame(original, differentId))
        assertTrue(CommentListAdapter.DIFF_CALLBACK.areContentsTheSame(original, original.copy()))
    }

    @Test
    fun `diff callback treats pending flags as content changes`() {
        val original = model(id = 1L)
        val likePending = original.copy(isLikePending = true)

        assertTrue(CommentListAdapter.DIFF_CALLBACK.areItemsTheSame(original, likePending))
        assertFalse(CommentListAdapter.DIFF_CALLBACK.areContentsTheSame(original, likePending))
    }

    @Test
    fun `diff callback treats precomputed like state as content changes`() {
        val original = model(id = 1L)
        val nowLiked = original.copy(isLikedByCurrentUser = true)

        assertTrue(CommentListAdapter.DIFF_CALLBACK.areItemsTheSame(original, nowLiked))
        assertFalse(CommentListAdapter.DIFF_CALLBACK.areContentsTheSame(original, nowLiked))
    }

    private fun model(id: Long) = CommentReplyUiModel(
        id = id,
        reply = "reply",
        createdAt = 1L,
        userId = 10L,
        userName = "user",
        userAvatar = null,
        likes = emptyList(),
        likeCount = 0,
        isLikedByCurrentUser = false,
        isLikePending = false,
        isDeletePending = false,
    )
}
