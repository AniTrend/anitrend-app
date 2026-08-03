package com.mxt.anitrend.base.custom.view.widget

import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Focused tests for the review vote rating transition forwarded through
 * [VoteWidget.Listener.onRateReview] (Reviews Phase 3).
 *
 * The widget renders immutable [com.mxt.anitrend.domain.model.ReviewRecord] state and
 * forwards the computed next rating; the transition decision is pure and covered here.
 */
class VoteWidgetRatingLogicTest {

    @Test
    fun `tapping active thumb clears the vote`() {
        assertEquals(KeyUtil.NO_VOTE, VoteWidget.nextRating(KeyUtil.UP_VOTE, KeyUtil.UP_VOTE))
        assertEquals(KeyUtil.NO_VOTE, VoteWidget.nextRating(KeyUtil.DOWN_VOTE, KeyUtil.DOWN_VOTE))
    }

    @Test
    fun `tapping inactive thumb activates it`() {
        assertEquals(KeyUtil.UP_VOTE, VoteWidget.nextRating(null, KeyUtil.UP_VOTE))
        assertEquals(KeyUtil.UP_VOTE, VoteWidget.nextRating(KeyUtil.DOWN_VOTE, KeyUtil.UP_VOTE))
        assertEquals(KeyUtil.DOWN_VOTE, VoteWidget.nextRating(null, KeyUtil.DOWN_VOTE))
        assertEquals(KeyUtil.DOWN_VOTE, VoteWidget.nextRating(KeyUtil.UP_VOTE, KeyUtil.DOWN_VOTE))
    }

    @Test
    fun `convert to text keeps the formatted count`() {
        assertEquals(" 80 ", VoteWidget.convertToText(80))
        assertEquals(" 0 ", VoteWidget.convertToText(0))
    }
}
