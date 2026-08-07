package com.mxt.anitrend.view.sheet

import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.base.UserBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Identity-preservation of the users sheet boundary model ([UserSheetModel]).
 *
 * [UserBase] marks its id (and avatar) as `@IgnoredOnParcel`, so the sheet must never
 * parcel entities directly. The to/from conversions here must round trip the real
 * AniList user id, the avatar, and the server-reported follow state so follow dispatch
 * and [com.mxt.anitrend.data.store.user.UserStore] rebinding keep working.
 */
class UserSheetModelMapperTest {

    @Test
    fun `UserBase to UserSheetModel captures identity avatar and follow state`() {
        val user =
            UserBase(name = "liker", isFollowing = true).apply {
                id = 987L
                avatar = ImageBase(extraLarge = null, large = "https://example.com/avatar.png", medium = null)
            }

        val sheetModel = user.toUserSheetModel()

        assertEquals(987L, sheetModel.id)
        assertEquals("liker", sheetModel.name)
        assertEquals("https://example.com/avatar.png", sheetModel.avatar)
        assertEquals(true, sheetModel.isFollowing)
    }

    @Test
    fun `UserSheetModel round trip preserves the real user id`() {
        val original =
            UserBase(name = "other-user", isFollowing = false).apply {
                id = 42L
                avatar = ImageBase(extraLarge = null, large = "avatar-url", medium = null)
            }

        val restored = original.toUserSheetModel().toUserBase()

        assertEquals(42L, restored.id)
        assertEquals("other-user", restored.name)
        assertEquals("avatar-url", restored.avatar?.large)
        assertEquals(false, restored.isFollowing)
    }

    @Test
    fun `UserSheetModel round trip preserves initial following state`() {
        val original =
            UserBase(name = "already-following", isFollowing = true).apply {
                id = 7L
            }

        val restored = original.toUserSheetModel().toUserBase()

        assertEquals(7L, restored.id)
        assertEquals(true, restored.isFollowing)
    }

    @Test
    fun `null avatar survives the round trip`() {
        val original =
            UserBase(name = "no-avatar").apply {
                id = 3L
            }

        val restored = original.toUserSheetModel().toUserBase()

        assertEquals(3L, restored.id)
        assertNull(restored.avatar)
    }
}
