package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.adapter.recycler.index.FeedListAdapter
import com.mxt.anitrend.domain.model.FeedItemUiModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedItemUiModelDiffTest {

    @Test
    fun `diff callback matches ids and full content equality`() {
        val original = model(id = 1L)
        val sameIdDifferentContent = original.copy(body = "updated")
        val differentId = original.copy(id = 2L)

        assertTrue(FeedListAdapter.DIFF_CALLBACK.areItemsTheSame(original, sameIdDifferentContent))
        assertFalse(FeedListAdapter.DIFF_CALLBACK.areContentsTheSame(original, sameIdDifferentContent))
        assertFalse(FeedListAdapter.DIFF_CALLBACK.areItemsTheSame(original, differentId))
        assertTrue(FeedListAdapter.DIFF_CALLBACK.areContentsTheSame(original, original.copy()))
    }

    private fun model(
        id: Long,
        body: CharSequence? = "body",
    ) = FeedItemUiModel(
        id = id,
        type = "TEXT",
        headline = "headline",
        body = body,
        likeCount = 1,
        isLikedByCurrentUser = false,
        replyCount = 2,
        canEdit = true,
        canDelete = true,
        isLikePending = false,
        isDeletePending = false,
    )
}
