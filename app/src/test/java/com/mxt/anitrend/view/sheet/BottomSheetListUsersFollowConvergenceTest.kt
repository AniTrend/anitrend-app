package com.mxt.anitrend.view.sheet

import com.mxt.anitrend.domain.model.UserRecord
import com.mxt.anitrend.model.entity.base.UserBase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Consumer-level follow state convergence for the list-follow sheet
 * ([BottomSheetListUsers]). Displayed list items are converged exclusively from the
 * canonical [com.mxt.anitrend.data.store.user.UserStore]: a successful toggle converges
 * the item to the authoritative committed record, while a failed toggle commits nothing
 * and leaves the displayed follow state unchanged.
 */
class BottomSheetListUsersFollowConvergenceTest {

    @Test
    fun `successful follow converges item to committed following state`() {
        val item = user(7L, isFollowing = false)
        val committed = record(7L, isFollowing = true)

        val changed = item.rebindFollowState(committed)

        assertTrue(changed)
        assertTrue(item.isFollowing)
    }

    @Test
    fun `successful unfollow converges item to committed non-following state`() {
        val item = user(7L, isFollowing = true)
        val committed = record(7L, isFollowing = false)

        val changed = item.rebindFollowState(committed)

        assertTrue(changed)
        assertFalse(item.isFollowing)
    }

    @Test
    fun `failed toggle leaves displayed state unchanged when store has no committed record`() {
        val item = user(7L, isFollowing = false)

        val changed = item.rebindFollowState(record = null)

        assertFalse(changed)
        assertFalse(item.isFollowing)
    }

    @Test
    fun `already converged item is not re-rendered`() {
        val item = user(7L, isFollowing = true)
        val committed = record(7L, isFollowing = true)

        val changed = item.rebindFollowState(committed)

        assertFalse(changed)
        assertTrue(item.isFollowing)
    }

    @Test
    fun `record for an unrelated user leaves item unchanged`() {
        val item = user(7L, isFollowing = false)
        val other = record(9L, isFollowing = true)

        val changed = item.rebindFollowState(other)

        assertFalse(changed)
        assertFalse(item.isFollowing)
    }

    private fun user(
        id: Long,
        isFollowing: Boolean,
    ): UserBase = UserBase(name = "user-$id", isFollowing = isFollowing).apply {
        this.id = id
    }

    private fun record(
        id: Long,
        isFollowing: Boolean,
    ): UserRecord = UserRecord(
        id = id,
        name = "user-$id",
        avatar = "avatar-$id",
        banner = "banner-$id",
        isFollowing = isFollowing,
        revision = 1L,
    )
}
