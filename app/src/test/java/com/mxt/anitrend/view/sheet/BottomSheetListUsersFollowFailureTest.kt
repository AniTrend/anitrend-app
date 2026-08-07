package com.mxt.anitrend.view.sheet

import com.mxt.anitrend.data.store.mutation.MutationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Failure reporting for [BottomSheetListUsers] follow toggles.
 *
 * A failed mutation commits nothing to the canonical
 * [com.mxt.anitrend.data.store.user.UserStore], so it must be surfaced immediately
 * through the sheet's notification convention instead of leaving the follow control
 * in its loading state until the widget's bounded fallback expires. Successful
 * mutations converge via the store observation and are not surfaced.
 */
class BottomSheetListUsersFollowFailureTest {

    @Test
    fun `failure result is surfaced with its message`() {
        val messages = mutableListOf<String>()

        reportFollowFailure(
            result = MutationResult.Failure(message = "Unable to toggle follow"),
            notify = messages::add,
        )

        assertEquals(listOf("Unable to toggle follow"), messages)
    }

    @Test
    fun `success result is not surfaced`() {
        val messages = mutableListOf<String>()

        reportFollowFailure(
            result = MutationResult.Success,
            notify = messages::add,
        )

        assertTrue(messages.isEmpty())
    }
}
