package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaOverviewData
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaSource
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaOverviewRecordMapperTest {

    @Test
    fun `maps generated Media to MediaOverviewRecord preserving all values`() {
        val record = media(
            id = 21,
            title = MediaOverviewData.MediaTitle(
                english = "Sword Art Online",
                native = "ソードアート・オンライン",
                romaji = "Sword Art Online",
                userPreferred = "Sword Art Online",
            ),
            bannerImage = "banner.jpg",
            coverImage = MediaOverviewData.MediaCoverImage(
                color = "#ffffff",
                extraLarge = "extra-large.jpg",
                large = "large.jpg",
                medium = "medium.jpg",
            ),
            type = MediaType.ANIME,
            format = MediaFormat.TV,
            season = MediaSeason.SPRING,
            status = MediaStatus.FINISHED,
            meanScore = 75,
            averageScore = 76,
            startDate = MediaOverviewData.MediaStartDate(year = 2012, month = 7, day = 8),
            endDate = MediaOverviewData.MediaEndDate(year = 2012, month = 12, day = 23),
            episodes = 25,
            chapters = 0,
            volumes = 0,
            isAdult = false,
            isFavourite = true,
            nextAiringEpisode = MediaOverviewData.MediaNextAiringEpisode(
                airingAt = 1_700_000_000,
                episode = 26,
                id = 55,
                mediaId = 21,
                timeUntilAiring = 3_600,
            ),
            mediaListEntry = MediaOverviewData.MediaMediaListEntry(id = 99, status = MediaListStatus.CURRENT),
            siteUrl = "https://anilist.co/anime/21",
            updatedAt = 1_600_000_000,
            genres = listOf("Action", "Adventure"),
            tags = listOf(
                MediaOverviewData.MediaTags(
                    category = "Themes",
                    description = "description",
                    id = 7,
                    isAdult = false,
                    isGeneralSpoiler = true,
                    name = "Video Games",
                    rank = 78,
                ),
            ),
            trailer = MediaOverviewData.MediaTrailer(id = "abc123", site = "youtube", thumbnail = "thumb.jpg"),
            duration = 24,
            hashtag = "#sao",
            source = MediaSource.ORIGINAL,
            studios = MediaOverviewData.MediaStudios(
                nodes = listOf(
                    MediaOverviewData.MediaStudiosNodes(
                        id = 11,
                        isAnimationStudio = true,
                        isFavourite = true,
                        name = "A-1 Pictures",
                        siteUrl = "https://anilist.co/studio/11",
                    ),
                ),
            ),
            description = "Full description",
        ).toMediaOverviewRecord()

        assertEquals(21L, record.id)
        assertEquals("Sword Art Online", record.titleUserPreferred)
        assertEquals("Sword Art Online", record.titleRomaji)
        assertEquals("Sword Art Online", record.titleEnglish)
        assertEquals("ソードアート・オンライン", record.titleOriginal)
        assertEquals("banner.jpg", record.bannerImage)
        assertEquals("#ffffff", record.coverImage?.color)
        assertEquals("extra-large.jpg", record.coverImage?.extraLarge)
        assertEquals("large.jpg", record.coverImage?.large)
        assertEquals("medium.jpg", record.coverImage?.medium)
        assertEquals("ANIME", record.type)
        assertEquals("TV", record.format)
        assertEquals("SPRING", record.season)
        assertEquals("FINISHED", record.status)
        assertEquals(75, record.meanScore)
        assertEquals(76, record.averageScore)
        assertEquals(2012, record.startDate?.year)
        assertEquals(7, record.startDate?.month)
        assertEquals(8, record.startDate?.day)
        assertEquals(2012, record.endDate?.year)
        assertEquals(12, record.endDate?.month)
        assertEquals(23, record.endDate?.day)
        assertEquals(25, record.episodes)
        assertEquals(0, record.chapters)
        assertEquals(0, record.volumes)
        assertEquals(false, record.isAdult)
        assertTrue(record.isFavourite)
        assertEquals(1_700_000_000L, record.nextAiringEpisode?.airingAt)
        assertEquals(3_600L, record.nextAiringEpisode?.timeUntilAiring)
        assertEquals(26, record.nextAiringEpisode?.episode)
        assertEquals(99L, record.mediaListEntry?.id)
        assertEquals("CURRENT", record.mediaListEntry?.status)
        assertEquals("https://anilist.co/anime/21", record.siteUrl)
        assertEquals(1_600_000_000L, record.updatedAt)
        assertEquals(listOf("Action", "Adventure"), record.genres)
        assertEquals(1, record.tags?.size)
        assertEquals(7L, record.tags?.first()?.id)
        assertEquals("Video Games", record.tags?.first()?.name)
        assertEquals("Themes", record.tags?.first()?.category)
        assertEquals(78, record.tags?.first()?.rank)
        assertTrue(record.tags?.first()?.isGeneralSpoiler == true)
        assertFalse(record.tags?.first()?.isAdult == true)
        assertEquals("abc123", record.trailer?.id)
        assertEquals("youtube", record.trailer?.site)
        assertEquals("thumb.jpg", record.trailer?.thumbnail)
        assertEquals(24, record.duration)
        assertEquals("#sao", record.hashtag)
        assertEquals("ORIGINAL", record.source)
        assertEquals(1, record.studios?.size)
        assertEquals(11L, record.studios?.first()?.id)
        assertEquals("A-1 Pictures", record.studios?.first()?.name)
        assertTrue(record.studios?.first()?.isAnimationStudio == true)
        assertTrue(record.studios?.first()?.isFavourite == true)
        assertEquals("https://anilist.co/studio/11", record.studios?.first()?.siteUrl)
        assertEquals("Full description", record.description)
    }

    @Test
    fun `converts generated Int ids and timestamps to domain Longs`() {
        val record = media(
            id = Int.MAX_VALUE,
            updatedAt = Int.MAX_VALUE,
            mediaListEntry = MediaOverviewData.MediaMediaListEntry(id = Int.MAX_VALUE, status = null),
            tags = listOf(
                MediaOverviewData.MediaTags(
                    category = null,
                    description = null,
                    id = Int.MAX_VALUE,
                    isAdult = false,
                    isGeneralSpoiler = false,
                    name = "Tag",
                    rank = 0,
                ),
            ),
            studios = MediaOverviewData.MediaStudios(
                nodes = listOf(
                    MediaOverviewData.MediaStudiosNodes(
                        id = Int.MAX_VALUE,
                        isAnimationStudio = true,
                        isFavourite = false,
                        name = "Studio",
                        siteUrl = null,
                    ),
                ),
            ),
            nextAiringEpisode = MediaOverviewData.MediaNextAiringEpisode(
                airingAt = Int.MAX_VALUE,
                episode = 1,
                id = Int.MAX_VALUE,
                mediaId = Int.MAX_VALUE,
                timeUntilAiring = Int.MAX_VALUE,
            ),
        ).toMediaOverviewRecord()

        assertEquals(Int.MAX_VALUE.toLong(), record.id)
        assertEquals(Int.MAX_VALUE.toLong(), record.updatedAt)
        assertEquals(Int.MAX_VALUE.toLong(), record.mediaListEntry?.id)
        assertEquals(Int.MAX_VALUE.toLong(), record.tags?.first()?.id)
        assertEquals(Int.MAX_VALUE.toLong(), record.studios?.first()?.id)
        assertEquals(Int.MAX_VALUE.toLong(), record.nextAiringEpisode?.airingAt)
        assertEquals(Int.MAX_VALUE.toLong(), record.nextAiringEpisode?.timeUntilAiring)
    }

    @Test
    fun `maps generated enums to their serialized names`() {
        val record = media(
            id = 2,
            type = MediaType.MANGA,
            format = MediaFormat.MOVIE,
            season = MediaSeason.WINTER,
            status = MediaStatus.RELEASING,
            source = MediaSource.MANGA,
            mediaListEntry = MediaOverviewData.MediaMediaListEntry(id = 8, status = MediaListStatus.PLANNING),
        ).toMediaOverviewRecord()

        assertEquals("MANGA", record.type)
        assertEquals("MOVIE", record.format)
        assertEquals("WINTER", record.season)
        assertEquals("RELEASING", record.status)
        assertEquals("MANGA", record.source)
        assertEquals("PLANNING", record.mediaListEntry?.status)
    }

    @Test
    fun `preserves nullable semantics for optional blocks`() {
        val record = media(
            id = 1,
            title = null,
            bannerImage = null,
            coverImage = null,
            type = null,
            format = null,
            season = null,
            status = null,
            meanScore = null,
            averageScore = null,
            startDate = null,
            endDate = null,
            episodes = null,
            chapters = null,
            volumes = null,
            isAdult = null,
            isFavourite = false,
            nextAiringEpisode = null,
            mediaListEntry = null,
            siteUrl = null,
            updatedAt = null,
            genres = null,
            tags = null,
            trailer = null,
            duration = null,
            hashtag = null,
            source = null,
            studios = null,
            description = null,
        ).toMediaOverviewRecord()

        assertEquals(1L, record.id)
        assertNull(record.titleUserPreferred)
        assertNull(record.titleRomaji)
        assertNull(record.titleEnglish)
        assertNull(record.titleOriginal)
        assertNull(record.bannerImage)
        assertNull(record.coverImage)
        assertNull(record.type)
        assertNull(record.format)
        assertNull(record.season)
        assertNull(record.status)
        assertNull(record.meanScore)
        assertNull(record.averageScore)
        assertNull(record.startDate)
        assertNull(record.endDate)
        assertNull(record.episodes)
        assertNull(record.chapters)
        assertNull(record.volumes)
        assertNull(record.isAdult)
        assertFalse(record.isFavourite)
        assertNull(record.nextAiringEpisode)
        assertNull(record.mediaListEntry)
        assertNull(record.siteUrl)
        assertNull(record.updatedAt)
        assertNull(record.genres)
        assertNull(record.tags)
        assertNull(record.trailer)
        assertNull(record.duration)
        assertNull(record.hashtag)
        assertNull(record.source)
        assertNull(record.studios)
        assertNull(record.description)
    }

    @Test
    fun `drops null tag and studio node list elements`() {
        val record = media(
            id = 1,
            tags = listOf(
                MediaOverviewData.MediaTags(
                    category = null,
                    description = null,
                    id = 1,
                    isAdult = false,
                    isGeneralSpoiler = false,
                    name = "Tag A",
                    rank = 0,
                ),
                null,
                MediaOverviewData.MediaTags(
                    category = null,
                    description = null,
                    id = 2,
                    isAdult = false,
                    isGeneralSpoiler = false,
                    name = "Tag B",
                    rank = 0,
                ),
            ),
            studios = MediaOverviewData.MediaStudios(
                nodes = listOf(
                    MediaOverviewData.MediaStudiosNodes(
                        id = 11,
                        isAnimationStudio = true,
                        isFavourite = false,
                        name = "Studio A",
                        siteUrl = null,
                    ),
                    null,
                ),
            ),
        ).toMediaOverviewRecord()

        assertEquals(2, record.tags?.size)
        assertEquals(listOf("Tag A", "Tag B"), record.tags?.map { it.name })
        assertEquals(1, record.studios?.size)
        assertEquals("Studio A", record.studios?.first()?.name)
    }

    @Test
    fun `carries null entry status when the mini fragment status is absent`() {
        val record = media(
            id = 1,
            mediaListEntry = MediaOverviewData.MediaMediaListEntry(id = 5, status = null),
        ).toMediaOverviewRecord()

        assertEquals(5L, record.mediaListEntry?.id)
        assertNull(record.mediaListEntry?.status)
    }

    @Test
    fun `defaults nullable tag rank and spoiler flags to false and zero`() {
        val record = media(
            id = 1,
            tags = listOf(
                MediaOverviewData.MediaTags(
                    category = null,
                    description = null,
                    id = 3,
                    isAdult = null,
                    isGeneralSpoiler = null,
                    name = "Tag",
                    rank = null,
                ),
            ),
        ).toMediaOverviewRecord()

        val tag = record.tags?.first()
        assertEquals(3L, tag?.id)
        assertEquals("Tag", tag?.name)
        assertNull(tag?.description)
        assertNull(tag?.category)
        assertEquals(0, tag?.rank)
        assertFalse(tag?.isGeneralSpoiler == true)
        assertFalse(tag?.isAdult == true)
    }

    @Test
    fun `falls back to userPreferred title when romaji english or native is null`() {
        val record = media(
            id = 1,
            title = MediaOverviewData.MediaTitle(
                english = null,
                native = null,
                romaji = null,
                userPreferred = "No Game No Life",
            ),
        ).toMediaOverviewRecord()

        assertEquals("No Game No Life", record.titleUserPreferred)
        assertEquals("No Game No Life", record.titleRomaji)
        assertEquals("No Game No Life", record.titleEnglish)
        assertEquals("No Game No Life", record.titleOriginal)
    }

    @Test
    fun `keeps raw title fields when present over the userPreferred fallback`() {
        val record = media(
            id = 1,
            title = MediaOverviewData.MediaTitle(
                english = "Sword Art Online",
                native = "ソードアート・オンライン",
                romaji = "Sword Art Online",
                userPreferred = "Different",
            ),
        ).toMediaOverviewRecord()

        assertEquals("Different", record.titleUserPreferred)
        assertEquals("Sword Art Online", record.titleRomaji)
        assertEquals("Sword Art Online", record.titleEnglish)
        assertEquals("ソードアート・オンライン", record.titleOriginal)
    }

    private fun media(
        id: Int,
        title: MediaOverviewData.MediaTitle? = null,
        bannerImage: String? = null,
        coverImage: MediaOverviewData.MediaCoverImage? = null,
        type: MediaType? = null,
        format: MediaFormat? = null,
        season: MediaSeason? = null,
        status: MediaStatus? = null,
        meanScore: Int? = null,
        averageScore: Int? = null,
        startDate: MediaOverviewData.MediaStartDate? = null,
        endDate: MediaOverviewData.MediaEndDate? = null,
        episodes: Int? = null,
        chapters: Int? = null,
        volumes: Int? = null,
        isAdult: Boolean? = null,
        isFavourite: Boolean = false,
        nextAiringEpisode: MediaOverviewData.MediaNextAiringEpisode? = null,
        mediaListEntry: MediaOverviewData.MediaMediaListEntry? = null,
        siteUrl: String? = null,
        updatedAt: Int? = null,
        genres: List<String?>? = null,
        tags: List<MediaOverviewData.MediaTags?>? = null,
        trailer: MediaOverviewData.MediaTrailer? = null,
        duration: Int? = null,
        hashtag: String? = null,
        source: MediaSource? = null,
        studios: MediaOverviewData.MediaStudios? = null,
        description: String? = null,
    ): MediaOverviewData.Media = MediaOverviewData.Media(
        averageScore = averageScore,
        bannerImage = bannerImage,
        chapters = chapters,
        coverImage = coverImage,
        description = description,
        duration = duration,
        endDate = endDate,
        episodes = episodes,
        format = format,
        genres = genres,
        hashtag = hashtag,
        id = id,
        isAdult = isAdult,
        isFavourite = isFavourite,
        meanScore = meanScore,
        mediaListEntry = mediaListEntry,
        nextAiringEpisode = nextAiringEpisode,
        season = season,
        siteUrl = siteUrl,
        source = source,
        startDate = startDate,
        status = status,
        studios = studios,
        tags = tags,
        title = title,
        trailer = trailer,
        type = type,
        updatedAt = updatedAt,
        volumes = volumes,
    )
}
