package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.mapper.toUserRecord
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.base.UserBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRecordMapperTest {

    @Test
    fun `map UserBase to UserRecord correctly`() {
        val user = createUser(id = 8L, name = "alice", isFollowing = true)

        val record = user.toUserRecord(revision = 3L)

        assertEquals(8L, record.id)
        assertEquals("alice", record.name)
        assertEquals("https://avatar-large", record.avatar)
        assertEquals("https://banner", record.banner)
        assertTrue(record.isFollowing)
        assertEquals(3L, record.revision)
    }

    @Test
    fun `avatar falls back to medium then extraLarge and default revision is zero`() {
        val mediumOnly = createUser(1L, "medium", large = null, medium = "https://medium")
        assertEquals("https://medium", mediumOnly.toUserRecord().avatar)

        val extraLargeOnly = createUser(1L, "extra", large = null, medium = null, extraLarge = "https://extra")
        assertEquals("https://extra", extraLargeOnly.toUserRecord().avatar)

        val noAvatar = createUser(1L, "none", extraLarge = null, large = null, medium = null)
        assertEquals(null, noAvatar.toUserRecord().avatar)
        assertEquals(0L, noAvatar.toUserRecord().revision)
    }

    @Test
    fun `defaults are carried for isFollowing and banner`() {
        val record = createUser(2L, "bob", isFollowing = false, banner = null).toUserRecord(revision = 1L)

        assertFalse(record.isFollowing)
        assertEquals(null, record.banner)
    }

    private fun createUser(
        id: Long,
        name: String,
        isFollowing: Boolean = false,
        extraLarge: String? = "https://avatar-extra-large",
        large: String? = "https://avatar-large",
        medium: String? = "https://avatar-medium",
        banner: String? = "https://banner",
    ): UserBase = UserBase(name = name, bannerImage = banner, isFollowing = isFollowing).also {
        it.id = id
        it.avatar = ImageBase(
            extraLarge = extraLarge,
            large = large,
            medium = medium,
        )
    }
}
