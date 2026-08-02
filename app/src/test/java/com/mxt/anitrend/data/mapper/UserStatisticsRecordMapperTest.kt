package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.UserStatsData
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import com.mxt.anitrend.model.entity.anilist.user.UserStatistics
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserCountryStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserFormatStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserGenreStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserLengthStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserReleaseYearStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserScoreStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserStaffStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserStartYearStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserStatusStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserStudioStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserTagStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserVoiceActorStatistic
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserStatisticsRecordMapperTest {

    // ── Generated transport lane ──────────────────────────────────────────────

    @Test
    fun `maps generated stats preserving nested values and numeric conversions`() {
        val data = userStatsData(
            anime = anime(
                chaptersRead = 10,
                count = 4,
                countries = listOf(
                    UserStatsData.UserStatisticsAnimeCountries(
                        chaptersRead = 10,
                        count = 4,
                        country = "JP",
                        meanScore = 77.5,
                        mediaIds = listOf(1, 2),
                        minutesWatched = 1_200,
                    ),
                ),
                episodesWatched = 24,
                formats = listOf(
                    UserStatsData.UserStatisticsAnimeFormats(
                        chaptersRead = 0,
                        count = 3,
                        format = MediaFormat.TV,
                        meanScore = 80.0,
                        mediaIds = listOf(3),
                        minutesWatched = 600,
                    ),
                ),
                genres = listOf(
                    UserStatsData.UserStatisticsAnimeGenres(
                        chaptersRead = 0,
                        count = 2,
                        genre = "Action",
                        meanScore = 75.5,
                        mediaIds = listOf(1, 3),
                        minutesWatched = 500,
                    ),
                ),
                lengths = listOf(
                    UserStatsData.UserStatisticsAnimeLengths(
                        chaptersRead = 0,
                        count = 1,
                        length = "2 hours",
                        meanScore = 70.0,
                        mediaIds = listOf(4),
                        minutesWatched = 300,
                    ),
                ),
                meanScore = 78.4,
                minutesWatched = 2_400,
                releaseYears = listOf(
                    UserStatsData.UserStatisticsAnimeReleaseYears(
                        chaptersRead = 0,
                        count = 2,
                        meanScore = 74.0,
                        mediaIds = listOf(1, 2),
                        minutesWatched = 400,
                        releaseYear = 2024,
                    ),
                ),
                scores = listOf(
                    UserStatsData.UserStatisticsAnimeScores(
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 90.0,
                        mediaIds = listOf(5),
                        minutesWatched = 200,
                        score = 90,
                    ),
                ),
                staff = listOf(
                    UserStatsData.UserStatisticsAnimeStaff(
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 71.0,
                        mediaIds = listOf(6),
                        minutesWatched = 150,
                    ),
                ),
                standardDeviation = 12.3,
                startYears = listOf(
                    UserStatsData.UserStatisticsAnimeStartYears(
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 72.0,
                        mediaIds = listOf(7),
                        minutesWatched = 100,
                        startYear = 2023,
                    ),
                ),
                statuses = listOf(
                    UserStatsData.UserStatisticsAnimeStatuses(
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 73.0,
                        mediaIds = listOf(8),
                        minutesWatched = 90,
                        status = MediaListStatus.COMPLETED,
                    ),
                ),
                studios = listOf(
                    UserStatsData.UserStatisticsAnimeStudios(
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 74.0,
                        mediaIds = listOf(9),
                        minutesWatched = 80,
                        studio = UserStatsData.UserStatisticsAnimeStudiosStudio(
                            id = 21,
                            isAnimationStudio = true,
                            isFavourite = true,
                            name = "Madhouse",
                            siteUrl = "https://anilist.co/studio/21",
                        ),
                    ),
                ),
                tags = listOf(
                    UserStatsData.UserStatisticsAnimeTags(
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 75.0,
                        mediaIds = listOf(10),
                        minutesWatched = 70,
                        tag = UserStatsData.UserStatisticsAnimeTagsTag(
                            category = "Themes",
                            description = "A tag",
                            id = 321,
                            isAdult = false,
                            isGeneralSpoiler = true,
                            name = "Martial Arts",
                            rank = 5,
                        ),
                    ),
                ),
                voiceActors = listOf(
                    UserStatsData.UserStatisticsAnimeVoiceActors(
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 76.0,
                        mediaIds = listOf(11),
                        minutesWatched = 60,
                        voiceActor = UserStatsData.UserStatisticsAnimeVoiceActorsVoiceActor(
                            id = 987,
                            image = null,
                            isFavourite = true,
                            language = com.mxt.anitrend.graphql.generated.StaffLanguage.JAPANESE,
                            name = UserStatsData.UserStatisticsAnimeVoiceActorsVoiceActorName(
                                alternative = null,
                                first = "Rie",
                                full = "Rie Takahashi",
                                last = "Takahashi",
                                native = "高橋李依",
                            ),
                            siteUrl = "https://anilist.co/staff/987",
                        ),
                    ),
                ),
                volumesRead = 0,
            ),
            manga = manga(count = 5),
        )

        val record = data.toUserStatisticsRecord()
        val anime = record.anime
        assertEquals(10, anime.chaptersRead)
        assertEquals(4, anime.count)
        assertEquals(24, anime.episodesWatched)
        assertEquals(78.4f, anime.meanScore, 0f)
        assertEquals(2_400, anime.minutesWatched)
        assertEquals(12.3f, anime.standardDeviation, 0f)
        assertEquals(0, anime.volumesRead)

        assertEquals("JP", anime.countries?.single()?.country)
        assertEquals(77.5f, anime.countries?.single()?.meanScore ?: -1f, 0f)
        assertEquals(listOf(1, 2), anime.countries?.single()?.mediaIds)

        assertEquals("TV", anime.formats?.single()?.format)
        assertEquals("Action", anime.genres?.single()?.genre)
        assertEquals("2 hours", anime.lengths?.single()?.length)
        assertEquals(2024, anime.releaseYears?.single()?.releaseYear)
        assertEquals(90, anime.scores?.single()?.score)
        assertNull(anime.staff?.single()?.staff)
        assertEquals(2023, anime.startYears?.single()?.startYear)
        assertEquals("COMPLETED", anime.statuses?.single()?.status)

        val studio = anime.studios?.single()?.studio
        assertEquals(21L, studio?.id)
        assertEquals("Madhouse", studio?.name)
        assertEquals("https://anilist.co/studio/21", studio?.siteUrl)
        assertTrue(studio?.isFavourite == true)

        val tag = anime.tags?.single()?.tag
        assertEquals(321L, tag?.id)
        assertEquals("Martial Arts", tag?.name)
        assertEquals("Themes", tag?.category)
        assertEquals(5, tag?.rank)
        assertTrue(tag?.isGeneralSpoiler == true)
        assertFalse(tag?.isAdult == true)

        val voiceActor = anime.voiceActors?.single()?.voiceActor
        assertEquals(987L, voiceActor?.id)
        assertEquals("Rie Takahashi", voiceActor?.name)
        assertEquals("https://anilist.co/staff/987", voiceActor?.siteUrl)
        assertTrue(voiceActor?.isFavourite == true)

        assertEquals(5, record.manga.count)
    }

    @Test
    fun `maps generated enum fields to legacy string representations`() {
        val data = userStatsData(
            anime = anime(
                formats = listOf(
                    UserStatsData.UserStatisticsAnimeFormats(
                        chaptersRead = 0,
                        count = 1,
                        format = MediaFormat.MOVIE,
                        meanScore = 81.0,
                        mediaIds = listOf(1),
                        minutesWatched = 120,
                    ),
                    UserStatsData.UserStatisticsAnimeFormats(
                        chaptersRead = 0,
                        count = 1,
                        format = null,
                        meanScore = 82.0,
                        mediaIds = listOf(2),
                        minutesWatched = 130,
                    ),
                ),
                statuses = listOf(
                    UserStatsData.UserStatisticsAnimeStatuses(
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 83.0,
                        mediaIds = listOf(3),
                        minutesWatched = 140,
                        status = null,
                    ),
                ),
            ),
            manga = manga(
                formats = listOf(
                    UserStatsData.UserStatisticsMangaFormats(
                        chaptersRead = 5,
                        count = 1,
                        format = MediaFormat.MANGA,
                        meanScore = 84.0,
                        mediaIds = listOf(4),
                        minutesWatched = 0,
                    ),
                ),
            ),
        )

        val record = data.toUserStatisticsRecord()

        assertEquals("MOVIE", record.anime.formats?.first()?.format)
        assertNull(record.anime.formats?.last()?.format)
        assertEquals("", record.anime.statuses?.single()?.status)
        assertEquals("MANGA", record.manga.formats?.single()?.format)
    }

    @Test
    fun `filters null media ids and drops null list entries`() {
        val data = userStatsData(
            anime = anime(
                genres = listOf(
                    null,
                    UserStatsData.UserStatisticsAnimeGenres(
                        chaptersRead = 0,
                        count = 1,
                        genre = "Drama",
                        meanScore = 74.0,
                        mediaIds = listOf(1, null, 3, null),
                        minutesWatched = 200,
                    ),
                ),
            ),
        )

        val record = data.toUserStatisticsRecord()

        assertEquals(1, record.anime.genres?.size)
        assertEquals("Drama", record.anime.genres?.single()?.genre)
        assertEquals(listOf(1, 3), record.anime.genres?.single()?.mediaIds)
    }

    @Test
    fun `returns default record when user or statistics blocks are absent`() {
        assertEquals(0, UserStatsData(user = null).toUserStatisticsRecord().anime.count)
        assertEquals(0, UserStatsData(user = null).toUserStatisticsRecord().manga.count)

        val noStatistics = userStatsData(anime = null, manga = null)
        val record = noStatistics.toUserStatisticsRecord()
        assertEquals(0, record.anime.count)
        assertEquals(0, record.manga.count)

        val animeOnly = userStatsData(anime = anime(count = 7), manga = null)
        assertEquals(7, animeOnly.toUserStatisticsRecord().anime.count)
        assertEquals(0, animeOnly.toUserStatisticsRecord().manga.count)
    }

    // ── Legacy cached DTO lane ───────────────────────────────────────────────

    @Test
    fun `maps legacy cached statistics preserving all fields`() {
        val stats = UserStatisticTypes(
            anime = legacyStatistics(
                count = 4,
                genres = listOf(
                    UserGenreStatistic(
                        genre = "Action",
                        chaptersRead = 0,
                        count = 2,
                        meanScore = 75.5f,
                        mediaIds = listOf(1, 2),
                        minutesWatched = 500,
                    ),
                ),
                formats = listOf(
                    UserFormatStatistic(
                        format = "TV",
                        chaptersRead = 0,
                        count = 3,
                        meanScore = 80f,
                        mediaIds = listOf(3),
                        minutesWatched = 600,
                    ),
                ),
            ),
            manga = legacyStatistics(count = 9),
        )

        val record = stats.toUserStatisticsRecord()

        assertEquals(4, record.anime.count)
        assertEquals("Action", record.anime.genres?.single()?.genre)
        assertEquals(75.5f, record.anime.genres?.single()?.meanScore ?: -1f, 0f)
        assertEquals(listOf(1, 2), record.anime.genres?.single()?.mediaIds)
        assertEquals("TV", record.anime.formats?.single()?.format)
        assertEquals(9, record.manga.count)
    }

    @Test
    fun `maps legacy staff studio and tag references into domain records`() {
        val stats = UserStatisticTypes(
            anime = legacyStatistics(
                staff = listOf(
                    UserStaffStatistic(
                        staff = StaffBase().apply {
                            id = 42L
                            name = TitleBase(first = "Satoru", last = "Gojo", original = null, alternative = null)
                            siteUrl = "https://anilist.co/staff/42"
                            isFavourite = true
                        },
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 70f,
                        mediaIds = listOf(1),
                        minutesWatched = 100,
                    ),
                ),
                studios = listOf(
                    UserStudioStatistic(
                        studio = StudioBase().apply {
                            id = 7L
                            name = "Bones"
                            siteUrl = "https://anilist.co/studio/7"
                            isFavourite = false
                        },
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 71f,
                        mediaIds = listOf(2),
                        minutesWatched = 110,
                    ),
                ),
                tags = listOf(
                    UserTagStatistic(
                        tag = MediaTag().apply {
                            id = 5L
                            name = "Comedy"
                            description = "Humor"
                            category = "Themes"
                            rank = 3
                            isGeneralSpoiler = true
                        },
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 72f,
                        mediaIds = listOf(3),
                        minutesWatched = 120,
                    ),
                ),
                voiceActors = listOf(
                    UserVoiceActorStatistic(
                        voiceActor = StaffBase().apply {
                            id = 99L
                            name = TitleBase(first = "Yui", last = "Ishikawa", original = null, alternative = null)
                            siteUrl = "https://anilist.co/staff/99"
                            isFavourite = false
                        },
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 73f,
                        mediaIds = listOf(4),
                        minutesWatched = 130,
                    ),
                ),
            ),
            manga = legacyStatistics(),
        )

        val record = stats.toUserStatisticsRecord()
        val anime = record.anime

        assertEquals(42L, anime.staff?.single()?.staff?.id)
        assertEquals("Satoru Gojo", anime.staff?.single()?.staff?.name)
        assertTrue(anime.staff?.single()?.staff?.isFavourite == true)

        assertEquals(7L, anime.studios?.single()?.studio?.id)
        assertEquals("Bones", anime.studios?.single()?.studio?.name)

        val tag = anime.tags?.single()?.tag
        assertEquals(5L, tag?.id)
        assertEquals("Comedy", tag?.name)
        assertEquals("Humor", tag?.description)
        assertEquals("Themes", tag?.category)
        assertEquals(3, tag?.rank)
        assertTrue(tag?.isGeneralSpoiler == true)

        assertEquals(99L, anime.voiceActors?.single()?.voiceActor?.id)
        assertEquals("Yui Ishikawa", anime.voiceActors?.single()?.voiceActor?.name)
    }

    @Test
    fun `legacy release year score and status fields survive projection`() {
        val stats = UserStatisticTypes(
            anime = legacyStatistics(
                releaseYears = listOf(
                    UserReleaseYearStatistic(
                        releaseYear = 2021,
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 60f,
                        mediaIds = listOf(1),
                        minutesWatched = 60,
                    ),
                ),
                scores = listOf(
                    UserScoreStatistic(
                        score = 88,
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 88f,
                        mediaIds = listOf(2),
                        minutesWatched = 70,
                    ),
                ),
                statuses = listOf(
                    UserStatusStatistic(
                        status = "COMPLETED",
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 61f,
                        mediaIds = listOf(3),
                        minutesWatched = 80,
                    ),
                ),
                startYears = listOf(
                    UserStartYearStatistic(
                        startYear = 2020,
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 62f,
                        mediaIds = listOf(4),
                        minutesWatched = 90,
                    ),
                ),
                lengths = listOf(
                    UserLengthStatistic(
                        length = "Short",
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 63f,
                        mediaIds = listOf(5),
                        minutesWatched = 100,
                    ),
                ),
                countries = listOf(
                    UserCountryStatistic(
                        country = "KR",
                        chaptersRead = 0,
                        count = 1,
                        meanScore = 64f,
                        mediaIds = listOf(6),
                        minutesWatched = 110,
                    ),
                ),
            ),
            manga = legacyStatistics(),
        )

        val record = stats.toUserStatisticsRecord()
        val anime = record.anime

        assertEquals(2021, anime.releaseYears?.single()?.releaseYear)
        assertEquals(88, anime.scores?.single()?.score)
        assertEquals("COMPLETED", anime.statuses?.single()?.status)
        assertEquals(2020, anime.startYears?.single()?.startYear)
        assertEquals("Short", anime.lengths?.single()?.length)
        assertEquals("KR", anime.countries?.single()?.country)
    }

    // ── Fixtures ─────────────────────────────────────────────────────────────

    private fun userStatsData(
        anime: UserStatsData.UserStatisticsAnime? = null,
        manga: UserStatsData.UserStatisticsManga? = null,
    ): UserStatsData = UserStatsData(
        user = UserStatsData.User(
            statistics = UserStatsData.UserStatistics(anime = anime, manga = manga),
        ),
    )

    private fun anime(
        chaptersRead: Int = 0,
        count: Int = 0,
        countries: List<UserStatsData.UserStatisticsAnimeCountries?>? = null,
        episodesWatched: Int = 0,
        formats: List<UserStatsData.UserStatisticsAnimeFormats?>? = null,
        genres: List<UserStatsData.UserStatisticsAnimeGenres?>? = null,
        lengths: List<UserStatsData.UserStatisticsAnimeLengths?>? = null,
        meanScore: Double = 0.0,
        minutesWatched: Int = 0,
        releaseYears: List<UserStatsData.UserStatisticsAnimeReleaseYears?>? = null,
        scores: List<UserStatsData.UserStatisticsAnimeScores?>? = null,
        staff: List<UserStatsData.UserStatisticsAnimeStaff?>? = null,
        standardDeviation: Double = 0.0,
        startYears: List<UserStatsData.UserStatisticsAnimeStartYears?>? = null,
        statuses: List<UserStatsData.UserStatisticsAnimeStatuses?>? = null,
        studios: List<UserStatsData.UserStatisticsAnimeStudios?>? = null,
        tags: List<UserStatsData.UserStatisticsAnimeTags?>? = null,
        voiceActors: List<UserStatsData.UserStatisticsAnimeVoiceActors?>? = null,
        volumesRead: Int = 0,
    ): UserStatsData.UserStatisticsAnime = UserStatsData.UserStatisticsAnime(
        chaptersRead = chaptersRead,
        count = count,
        countries = countries,
        episodesWatched = episodesWatched,
        formats = formats,
        genres = genres,
        lengths = lengths,
        meanScore = meanScore,
        minutesWatched = minutesWatched,
        releaseYears = releaseYears,
        scores = scores,
        staff = staff,
        standardDeviation = standardDeviation,
        startYears = startYears,
        statuses = statuses,
        studios = studios,
        tags = tags,
        voiceActors = voiceActors,
        volumesRead = volumesRead,
    )

    private fun manga(
        chaptersRead: Int = 0,
        count: Int = 0,
        countries: List<UserStatsData.UserStatisticsMangaCountries?>? = null,
        episodesWatched: Int = 0,
        formats: List<UserStatsData.UserStatisticsMangaFormats?>? = null,
        genres: List<UserStatsData.UserStatisticsMangaGenres?>? = null,
        lengths: List<UserStatsData.UserStatisticsMangaLengths?>? = null,
        meanScore: Double = 0.0,
        minutesWatched: Int = 0,
        releaseYears: List<UserStatsData.UserStatisticsMangaReleaseYears?>? = null,
        scores: List<UserStatsData.UserStatisticsMangaScores?>? = null,
        staff: List<UserStatsData.UserStatisticsMangaStaff?>? = null,
        standardDeviation: Double = 0.0,
        startYears: List<UserStatsData.UserStatisticsMangaStartYears?>? = null,
        statuses: List<UserStatsData.UserStatisticsMangaStatuses?>? = null,
        studios: List<UserStatsData.UserStatisticsMangaStudios?>? = null,
        tags: List<UserStatsData.UserStatisticsMangaTags?>? = null,
        voiceActors: List<UserStatsData.UserStatisticsMangaVoiceActors?>? = null,
        volumesRead: Int = 0,
    ): UserStatsData.UserStatisticsManga = UserStatsData.UserStatisticsManga(
        chaptersRead = chaptersRead,
        count = count,
        countries = countries,
        episodesWatched = episodesWatched,
        formats = formats,
        genres = genres,
        lengths = lengths,
        meanScore = meanScore,
        minutesWatched = minutesWatched,
        releaseYears = releaseYears,
        scores = scores,
        staff = staff,
        standardDeviation = standardDeviation,
        startYears = startYears,
        statuses = statuses,
        studios = studios,
        tags = tags,
        voiceActors = voiceActors,
        volumesRead = volumesRead,
    )

    private fun legacyStatistics(
        chaptersRead: Int = 0,
        count: Int = 0,
        countries: List<UserCountryStatistic>? = null,
        episodesWatched: Int = 0,
        formats: List<UserFormatStatistic>? = null,
        genres: List<UserGenreStatistic>? = null,
        lengths: List<UserLengthStatistic>? = null,
        meanScore: Float = 0f,
        minutesWatched: Int = 0,
        releaseYears: List<UserReleaseYearStatistic>? = null,
        scores: List<UserScoreStatistic>? = null,
        staff: List<UserStaffStatistic>? = null,
        standardDeviation: Float = 0f,
        startYears: List<UserStartYearStatistic>? = null,
        statuses: List<UserStatusStatistic>? = null,
        studios: List<UserStudioStatistic>? = null,
        tags: List<UserTagStatistic>? = null,
        voiceActors: List<UserVoiceActorStatistic>? = null,
        volumesRead: Int = 0,
    ): UserStatistics = UserStatistics(
        chaptersRead = chaptersRead,
        count = count,
        countries = countries,
        episodesWatched = episodesWatched,
        formats = formats,
        genres = genres,
        lengths = lengths,
        meanScore = meanScore,
        minutesWatched = minutesWatched,
        releaseYears = releaseYears,
        scores = scores,
        staff = staff,
        standardDeviation = standardDeviation,
        startYears = startYears,
        statuses = statuses,
        studios = studios,
        tags = tags,
        voiceActors = voiceActors,
        volumesRead = volumesRead,
    )
}
