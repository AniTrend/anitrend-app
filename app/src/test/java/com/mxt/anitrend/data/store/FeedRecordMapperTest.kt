package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.mapper.toFeedRecord
import com.mxt.anitrend.data.mapper.toFeedReplyRecord
import com.mxt.anitrend.data.mapper.toUserSummaryRecord
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FeedRecordMapperTest {
    @Test
    fun `map FeedList to FeedRecord correctly`() {
        val feed = createFeed()

        val record = feed.toFeedRecord(revision = 7L)

        assertEquals(11L, record.id)
        assertEquals("TEXT", record.type)
        assertEquals("watched", record.status)
        assertEquals("Hello world", record.text)
        assertEquals(1234L, record.createdAt)
        assertEquals(99L, record.user?.id)
        assertEquals("alice", record.user?.name)
        assertEquals("https://avatar-large", record.user?.avatar)
        assertNull(record.user?.siteUrl)
        assertEquals(2, record.likes.size)
        assertEquals(44L, record.media?.id)
        assertEquals("Romaji", record.media?.titleRomaji)
        assertEquals("English", record.media?.titleEnglish)
        assertEquals("Original", record.media?.titleOriginal)
        assertEquals("https://cover-extra-large", record.media?.coverImage)
        assertEquals("ANIME", record.media?.type)
        assertEquals(12, record.media?.episodes)
        assertEquals(0, record.media?.chapters)
        assertEquals(0, record.media?.volumes)
        assertEquals("FINISHED", record.media?.status)
        assertEquals("https://media", record.media?.siteUrl)
        assertEquals(3, record.replyCount)
        assertEquals("https://feed", record.siteUrl)
        assertEquals(7L, record.revision)
    }

    @Test
    fun `mutate source after mapping keeps FeedRecord unchanged`() {
        val likes = mutableListOf(createUser(1L, "one"))
        val feed = createFeed(likes = likes)

        val record = feed.toFeedRecord(revision = 3L)

        feed.text = "Changed"
        feed.replyCount = 99
        feed.user?.name = "changed-user"
        feed.media?.title = MediaTitle("Changed", "Changed", "Changed", "Changed")
        likes += createUser(2L, "two")

        assertEquals("Hello world", record.text)
        assertEquals(3, record.replyCount)
        assertEquals("alice", record.user?.name)
        assertEquals("Romaji", record.media?.titleRomaji)
        assertEquals(1, record.likes.size)
    }

    @Test
    fun `map FeedReply to FeedReplyRecord correctly`() {
        val reply = FeedReply(
            id = 22L,
            text = "Reply body",
            createdAt = 444L,
            user = createUser(3L, "reply-user"),
            likes = mutableListOf(createUser(4L, "liker")),
        )

        val record = reply.toFeedReplyRecord(activityId = 11L, revision = 9L)

        assertEquals(22L, record.id)
        assertEquals(11L, record.activityId)
        assertEquals("Reply body", record.reply)
        assertEquals(444L, record.createdAt)
        assertEquals("reply-user", record.user?.name)
        assertEquals(1, record.likes.size)
        assertEquals(9L, record.revision)
    }

    @Test
    fun `map UserBase to UserSummaryRecord correctly`() {
        val user = createUser(8L, "summary-user")

        val record = user.toUserSummaryRecord()

        assertEquals(8L, record.id)
        assertEquals("summary-user", record.name)
        assertEquals("https://avatar-large", record.avatar)
        assertNull(record.siteUrl)
    }

    private fun createFeed(likes: MutableList<UserBase> = mutableListOf(createUser(7L, "bob"), createUser(8L, "eve"))): FeedList = FeedList(
        id = 11L,
        replyCount = 3,
        type = "TEXT",
        status = "watched",
        text = "Hello world",
        createdAt = 1234L,
        user = createUser(99L, "alice"),
        media = createMedia(),
        messenger = createUser(100L, "messenger"),
        recipient = createUser(101L, "recipient"),
        likes = likes,
        siteUrl = "https://feed",
    )

    private fun createUser(id: Long, name: String): UserBase = UserBase(name = name).also {
        it.id = id
        it.avatar = ImageBase(
            extraLarge = "https://avatar-extra-large",
            large = "https://avatar-large",
            medium = "https://avatar-medium",
        )
    }

    private fun createMedia(): MediaBase = MediaBase().also {
        it.id = 44L
        it.title = MediaTitle("Romaji", "English", "Original", "Preferred")
        it.coverImage = ImageBase(
            extraLarge = "https://cover-extra-large",
            large = "https://cover-large",
            medium = "https://cover-medium",
        )
        it.type = "ANIME"
        it.episodes = 12
        it.chapters = 0
        it.volumes = 0
        it.status = "FINISHED"
        it.siteUrl = "https://media"
    }
}
