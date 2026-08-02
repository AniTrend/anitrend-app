package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.RecommendationMediaData
import com.mxt.anitrend.graphql.generated.RecommendationRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationRecordMapperTest {

    @Test
    fun `maps generated node to RecommendationRecord preserving all values`() {
        val record = node(
            id = 42,
            mediaRecommendation = mediaRecommendation(
                id = 7,
                title = title(),
                coverImage = coverImage(extraLarge = "xl.jpg", large = "large.jpg", medium = "medium.jpg"),
                type = MediaType.ANIME,
                format = MediaFormat.TV,
                episodes = 24,
                chapters = null,
                volumes = null,
                status = MediaStatus.FINISHED,
                siteUrl = "https://anilist.co/anime/7",
                isFavourite = true,
                averageScore = 78,
            ),
            rating = 96,
            user = user(
                id = 9,
                name = "John Doe",
                avatar = avatar(large = "avatar-large.jpg", medium = "avatar-medium.jpg"),
            ),
            userRating = RecommendationRating.RATE_UP,
        ).toRecommendationRecord()

        assertEquals(42L, record.id)
        assertEquals(7L, record.mediaRecommendation?.id)
        assertEquals("Anime Title", record.mediaRecommendation?.titleUserPreferred)
        assertEquals("Romaji Title", record.mediaRecommendation?.titleRomaji)
        assertEquals("English Title", record.mediaRecommendation?.titleEnglish)
        assertEquals("Native Title", record.mediaRecommendation?.titleOriginal)
        assertEquals("xl.jpg", record.mediaRecommendation?.coverImage)
        assertEquals("ANIME", record.mediaRecommendation?.type)
        assertEquals("TV", record.mediaRecommendation?.format)
        assertEquals(24, record.mediaRecommendation?.episodes)
        assertEquals(0, record.mediaRecommendation?.chapters)
        assertEquals(0, record.mediaRecommendation?.volumes)
        assertEquals("FINISHED", record.mediaRecommendation?.status)
        assertEquals("https://anilist.co/anime/7", record.mediaRecommendation?.siteUrl)
        assertTrue(record.mediaRecommendation?.isFavourite == true)
        assertEquals(78, record.mediaRecommendation?.averageScore)
        assertEquals(96, record.rating)
        assertEquals(9L, record.user?.id)
        assertEquals("John Doe", record.user?.name)
        assertEquals("avatar-large.jpg", record.user?.avatar)
        assertEquals("RATE_UP", record.userRating)
    }

    @Test
    fun `converts generated Int ids to domain Longs`() {
        val record = node(
            id = Int.MAX_VALUE,
            mediaRecommendation = mediaRecommendation(id = Int.MAX_VALUE, isFavourite = false),
            user = user(id = Int.MAX_VALUE, name = "User"),
        ).toRecommendationRecord()

        assertEquals(Int.MAX_VALUE.toLong(), record.id)
        assertEquals(Int.MAX_VALUE.toLong(), record.mediaRecommendation?.id)
        assertEquals(Int.MAX_VALUE.toLong(), record.user?.id)
    }

    @Test
    fun `carries null rating user and mediaRecommendation when blocks are absent`() {
        val record = RecommendationMediaData.MediaRecommendationsNodes(
            id = 1,
            mediaRecommendation = null,
            rating = null,
            user = null,
            userRating = null,
        ).toRecommendationRecord()

        assertEquals(1L, record.id)
        assertNull(record.mediaRecommendation)
        assertNull(record.rating)
        assertNull(record.user)
        assertNull(record.userRating)
    }

    @Test
    fun `falls back to medium avatar when large is missing`() {
        val record = node(
            id = 1,
            user = user(
                id = 2,
                name = "User",
                avatar = avatar(large = null, medium = "medium-avatar.jpg"),
            ),
        ).toRecommendationRecord()

        assertEquals("medium-avatar.jpg", record.user?.avatar)
    }

    @Test
    fun `carries null avatar when the generated avatar block is absent`() {
        val record = node(
            id = 1,
            user = RecommendationMediaData.MediaRecommendationsNodesUser(
                avatar = null,
                bannerImage = null,
                id = 2,
                isFollowing = false,
                name = "User",
                updatedAt = null,
            ),
        ).toRecommendationRecord()

        assertNull(record.user?.avatar)
    }

    @Test
    fun `maps generated page info to PageInfoRecord preserving paging metadata`() {
        val pageInfo = RecommendationMediaData.MediaRecommendationsPageInfo(
            currentPage = 2,
            hasNextPage = true,
            lastPage = 5,
            perPage = 10,
            total = 50,
        ).toPageInfoRecord()

        assertEquals(2, pageInfo.currentPage)
        assertEquals(5, pageInfo.lastPage)
        assertEquals(10, pageInfo.perPage)
        assertEquals(50, pageInfo.total)
        assertTrue(pageInfo.hasNextPage)
        assertTrue(pageInfo.hasPreviousPage)
    }

    @Test
    fun `maps absent page info flags to false`() {
        val pageInfo = RecommendationMediaData.MediaRecommendationsPageInfo(
            currentPage = null,
            hasNextPage = null,
            lastPage = null,
            perPage = null,
            total = null,
        ).toPageInfoRecord()

        assertFalse(pageInfo.hasNextPage)
        assertFalse(pageInfo.hasPreviousPage)
        assertNull(pageInfo.currentPage)
    }

    private fun node(
        id: Int,
        mediaRecommendation: RecommendationMediaData.MediaRecommendationsNodesMediaRecommendation? = null,
        rating: Int? = null,
        user: RecommendationMediaData.MediaRecommendationsNodesUser? = null,
        userRating: RecommendationRating? = null,
    ): RecommendationMediaData.MediaRecommendationsNodes = RecommendationMediaData.MediaRecommendationsNodes(
        id = id,
        mediaRecommendation = mediaRecommendation,
        rating = rating,
        user = user,
        userRating = userRating,
    )
    private fun mediaRecommendation(
        id: Int,
        title: RecommendationMediaData.MediaRecommendationsNodesMediaRecommendationTitle? = null,
        coverImage: RecommendationMediaData.MediaRecommendationsNodesMediaRecommendationCoverImage? = null,
        type: MediaType? = null,
        format: MediaFormat? = null,
        episodes: Int? = null,
        chapters: Int? = null,
        volumes: Int? = null,
        status: MediaStatus? = null,
        siteUrl: String? = null,
        isFavourite: Boolean = false,
        averageScore: Int? = null,
    ): RecommendationMediaData.MediaRecommendationsNodesMediaRecommendation = RecommendationMediaData.MediaRecommendationsNodesMediaRecommendation(
        averageScore = averageScore,
        bannerImage = null,
        chapters = chapters,
        coverImage = coverImage,
        endDate = null,
        episodes = episodes,
        format = format,
        id = id,
        isAdult = null,
        isFavourite = isFavourite,
        meanScore = null,
        mediaListEntry = null,
        nextAiringEpisode = null,
        season = null,
        siteUrl = siteUrl,
        startDate = null,
        status = status,
        title = title,
        type = type,
        updatedAt = null,
        volumes = volumes,
    )

    private fun title(
        english: String = "English Title",
        native: String = "Native Title",
        romaji: String = "Romaji Title",
        userPreferred: String = "Anime Title",
    ): RecommendationMediaData.MediaRecommendationsNodesMediaRecommendationTitle = RecommendationMediaData.MediaRecommendationsNodesMediaRecommendationTitle(
        english = english,
        native = native,
        romaji = romaji,
        userPreferred = userPreferred,
    )

    private fun coverImage(
        extraLarge: String?,
        large: String?,
        medium: String?,
    ): RecommendationMediaData.MediaRecommendationsNodesMediaRecommendationCoverImage = RecommendationMediaData.MediaRecommendationsNodesMediaRecommendationCoverImage(
        color = null,
        extraLarge = extraLarge,
        large = large,
        medium = medium,
    )

    private fun user(
        id: Int,
        name: String,
        avatar: RecommendationMediaData.MediaRecommendationsNodesUserAvatar? = null,
    ): RecommendationMediaData.MediaRecommendationsNodesUser = RecommendationMediaData.MediaRecommendationsNodesUser(
        avatar = avatar,
        bannerImage = null,
        id = id,
        isFollowing = false,
        name = name,
        updatedAt = null,
    )

    private fun avatar(
        large: String?,
        medium: String?,
    ): RecommendationMediaData.MediaRecommendationsNodesUserAvatar = RecommendationMediaData.MediaRecommendationsNodesUserAvatar(
        large = large,
        medium = medium,
    )
}
