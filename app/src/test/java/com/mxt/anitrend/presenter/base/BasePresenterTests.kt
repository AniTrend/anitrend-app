package com.mxt.anitrend.presenter.base

import android.content.Context
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import com.mxt.anitrend.model.entity.anilist.user.UserStatistics
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserFormatStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserGenreStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserReleaseYearStatistic
import com.mxt.anitrend.model.entity.anilist.user.statistics.UserTagStatistic
import com.mxt.anitrend.util.Settings
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class BasePresenterTests {
    @Test
    fun getTopFavouriteYears_returnsCachedYears() {
        val presenter = BasePresenter(
            context = mock(Context::class.java),
            boxQuery = mock(BoxQuery::class.java),
            settings = mock(Settings::class.java),
        )
        val years = listOf("2024", "2023")
        val tags = listOf("Action", "Drama")

        presenter.setPrivateField("favouriteYears", years)
        presenter.setPrivateField("favouriteTags", tags)

        assertEquals(years, presenter.getTopFavouriteYears(limit = 2))
    }

    @Test
    fun getTopFavouriteGenres_projectsCachedStatisticsThroughRecordMapper() {
        val user = User().apply {
            statistics = UserStatisticTypes(
                anime = UserStatistics(
                    chaptersRead = 0,
                    count = 0,
                    countries = null,
                    episodesWatched = 0,
                    formats = null,
                    genres = listOf(
                        UserGenreStatistic(
                            genre = "Action",
                            chaptersRead = 0,
                            count = 5,
                            meanScore = 80f,
                            mediaIds = listOf(1),
                            minutesWatched = 100,
                        ),
                        UserGenreStatistic(
                            genre = "Drama",
                            chaptersRead = 0,
                            count = 3,
                            meanScore = 75f,
                            mediaIds = listOf(2),
                            minutesWatched = 90,
                        ),
                    ),
                    lengths = null,
                    meanScore = 0f,
                    minutesWatched = 0,
                    releaseYears = null,
                    scores = null,
                    staff = null,
                    standardDeviation = 0f,
                    startYears = null,
                    statuses = null,
                    studios = null,
                    tags = null,
                    voiceActors = null,
                    volumesRead = 0,
                ),
                manga = UserStatistics(
                    chaptersRead = 0,
                    count = 0,
                    countries = null,
                    episodesWatched = 0,
                    formats = null,
                    genres = null,
                    lengths = null,
                    meanScore = 0f,
                    minutesWatched = 0,
                    releaseYears = null,
                    scores = null,
                    staff = null,
                    standardDeviation = 0f,
                    startYears = null,
                    statuses = null,
                    studios = null,
                    tags = null,
                    voiceActors = null,
                    volumesRead = 0,
                ),
            )
        }
        val boxQuery = mock(BoxQuery::class.java)
        `when`(boxQuery.currentUser).thenReturn(user)
        val presenter = BasePresenter(
            context = mock(Context::class.java),
            boxQuery = boxQuery,
            settings = mock(Settings::class.java),
        )

        assertEquals(listOf("Action", "Drama"), presenter.getTopFavouriteGenres(limit = 2))
    }

    @Test
    fun getTopFavouriteTags_projectsTagNamesInCountOrder() {
        val user = User().apply {
            statistics = UserStatisticTypes(
                anime = UserStatistics(
                    chaptersRead = 0,
                    count = 0,
                    countries = null,
                    episodesWatched = 0,
                    formats = null,
                    genres = null,
                    lengths = null,
                    meanScore = 0f,
                    minutesWatched = 0,
                    releaseYears = null,
                    scores = null,
                    staff = null,
                    standardDeviation = 0f,
                    startYears = null,
                    statuses = null,
                    studios = null,
                    tags = listOf(
                        UserTagStatistic(
                            tag = com.mxt.anitrend.model.entity.anilist.MediaTag().apply { name = "Comedy" },
                            chaptersRead = 0,
                            count = 4,
                            meanScore = 70f,
                            mediaIds = listOf(1),
                            minutesWatched = 80,
                        ),
                        UserTagStatistic(
                            tag = null,
                            chaptersRead = 0,
                            count = 2,
                            meanScore = 71f,
                            mediaIds = listOf(2),
                            minutesWatched = 70,
                        ),
                    ),
                    voiceActors = null,
                    volumesRead = 0,
                ),
                manga = emptyStatistics(),
            )
        }
        val boxQuery = mock(BoxQuery::class.java)
        `when`(boxQuery.currentUser).thenReturn(user)
        val presenter = BasePresenter(
            context = mock(Context::class.java),
            boxQuery = boxQuery,
            settings = mock(Settings::class.java),
        )

        assertEquals(listOf("Comedy"), presenter.getTopFavouriteTags(limit = 5))
    }

    @Test
    fun getTopFavouriteYears_projectsYearsInCountOrder() {
        val user = User().apply {
            statistics = UserStatisticTypes(
                anime = UserStatistics(
                    chaptersRead = 0,
                    count = 0,
                    countries = null,
                    episodesWatched = 0,
                    formats = null,
                    genres = null,
                    lengths = null,
                    meanScore = 0f,
                    minutesWatched = 0,
                    releaseYears = listOf(
                        UserReleaseYearStatistic(
                            releaseYear = 2022,
                            chaptersRead = 0,
                            count = 6,
                            meanScore = 72f,
                            mediaIds = listOf(1),
                            minutesWatched = 60,
                        ),
                        UserReleaseYearStatistic(
                            releaseYear = 2021,
                            chaptersRead = 0,
                            count = 1,
                            meanScore = 73f,
                            mediaIds = listOf(2),
                            minutesWatched = 50,
                        ),
                    ),
                    scores = null,
                    staff = null,
                    standardDeviation = 0f,
                    startYears = null,
                    statuses = null,
                    studios = null,
                    tags = null,
                    voiceActors = null,
                    volumesRead = 0,
                ),
                manga = emptyStatistics(),
            )
        }
        val boxQuery = mock(BoxQuery::class.java)
        `when`(boxQuery.currentUser).thenReturn(user)
        val presenter = BasePresenter(
            context = mock(Context::class.java),
            boxQuery = boxQuery,
            settings = mock(Settings::class.java),
        )

        assertEquals(listOf("2022", "2021"), presenter.getTopFavouriteYears(limit = 2))
    }

    @Test
    fun getTopFormats_projectsFormatStringsInCountOrder() {
        val user = User().apply {
            statistics = UserStatisticTypes(
                anime = UserStatistics(
                    chaptersRead = 0,
                    count = 0,
                    countries = null,
                    episodesWatched = 0,
                    formats = listOf(
                        UserFormatStatistic(
                            format = "TV",
                            chaptersRead = 0,
                            count = 8,
                            meanScore = 74f,
                            mediaIds = listOf(1),
                            minutesWatched = 400,
                        ),
                        UserFormatStatistic(
                            format = "MOVIE",
                            chaptersRead = 0,
                            count = 2,
                            meanScore = 75f,
                            mediaIds = listOf(2),
                            minutesWatched = 120,
                        ),
                    ),
                    genres = null,
                    lengths = null,
                    meanScore = 0f,
                    minutesWatched = 0,
                    releaseYears = null,
                    scores = null,
                    staff = null,
                    standardDeviation = 0f,
                    startYears = null,
                    statuses = null,
                    studios = null,
                    tags = null,
                    voiceActors = null,
                    volumesRead = 0,
                ),
                manga = emptyStatistics(),
            )
        }
        val boxQuery = mock(BoxQuery::class.java)
        `when`(boxQuery.currentUser).thenReturn(user)
        val presenter = BasePresenter(
            context = mock(Context::class.java),
            boxQuery = boxQuery,
            settings = mock(Settings::class.java),
        )

        assertEquals(listOf("TV", "MOVIE"), presenter.getTopFormats(limit = 2))
    }

    private fun emptyStatistics(): UserStatistics = UserStatistics(
        chaptersRead = 0,
        count = 0,
        countries = null,
        episodesWatched = 0,
        formats = null,
        genres = null,
        lengths = null,
        meanScore = 0f,
        minutesWatched = 0,
        releaseYears = null,
        scores = null,
        staff = null,
        standardDeviation = 0f,
        startYears = null,
        statuses = null,
        studios = null,
        tags = null,
        voiceActors = null,
        volumesRead = 0,
    )

    private fun BasePresenter.setPrivateField(
        name: String,
        value: Any?,
    ) {
        val field = BasePresenter::class.java.getDeclaredField(name)
        field.isAccessible = true
        field.set(this, value)
    }
}
