package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.graphql.generated.UpdateUserData
import com.mxt.anitrend.graphql.generated.UserTitleLanguage
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions
import com.mxt.anitrend.model.entity.anilist.meta.MediaListTypeOptions
import com.mxt.anitrend.model.entity.anilist.meta.UserOptions
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import com.mxt.anitrend.model.entity.anilist.user.UserStatistics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Focused tests for the safe cache merge of the `UpdateUser` mutation
 * response ([applyUserSettingsTo]).
 */
class UserSettingsMergeMapperTest {

    // ── happy path ──

    @Test
    fun `merge applies only the fields returned by the mutation`() {
        val cached = createCachedUser()
        val response = createResponse(
            about = "New bio",
            profileColor = "purple",
            titleLanguage = UserTitleLanguage.NATIVE,
            displayAdultContent = true,
            airingNotifications = false,
            scoreFormat = ScoreFormat.POINT_5,
            rowOrder = "CUSTOM",
        )

        response.applyUserSettingsTo(cached)

        assertEquals("updated-name", cached.name)
        assertEquals("https://avatar-large", cached.avatar?.large)
        assertEquals("https://avatar-medium", cached.avatar?.medium)
        assertNull(cached.avatar?.extraLarge)
        assertEquals("https://banner-new", cached.bannerImage)
        assertEquals("New bio", cached.about)
        assertEquals("NATIVE", cached.options?.titleLanguage)
        assertEquals(true, cached.options?.isDisplayAdultContent)
        assertEquals(false, cached.options?.isAiringNotifications)
        assertEquals("purple", cached.options?.profileColor)
        assertEquals("POINT_5", cached.mediaListOptions.scoreFormat)
        assertEquals("CUSTOM", cached.mediaListOptions.rowOrder)
    }

    @Test
    fun `merge preserves stats statistics unread count and identity fields`() {
        val cached = createCachedUser(
            isFollowing = true,
            statsWatchedTime = 42,
            unreadNotificationCount = 5,
        )

        createResponse(about = "New bio").applyUserSettingsTo(cached)

        // Not returned by the mutation: must survive untouched.
        assertEquals(7L, cached.id)
        assertTrue(cached.isFollowing)
        assertEquals(42, cached.stats?.watchedTime)
        assertEquals(5, cached.unreadNotificationCount)
        assertTrue(cached.statistics?.anime?.count == 11)
        assertTrue(cached.statistics?.manga?.count == 22)
    }

    @Test
    fun `merge preserves media list options not returned by the mutation`() {
        val cached = createCachedUser()
        cached.mediaListOptions.useLegacyLists = true
        val animeList = MediaListTypeOptions(sectionOrder = null, isSplitCompletedSectionByFormat = false, customLists = null, advancedScoring = null, isAdvancedScoringEnabled = false)
        val mangaList = MediaListTypeOptions(sectionOrder = listOf("CUSTOM"), isSplitCompletedSectionByFormat = false, customLists = null, advancedScoring = null, isAdvancedScoringEnabled = false)
        cached.mediaListOptions.animeList = animeList
        cached.mediaListOptions.mangaList = mangaList

        createResponse(scoreFormat = ScoreFormat.POINT_10, rowOrder = "CUSTOM").applyUserSettingsTo(cached)

        assertEquals("POINT_10", cached.mediaListOptions.scoreFormat)
        assertEquals("CUSTOM", cached.mediaListOptions.rowOrder)
        assertTrue(cached.mediaListOptions.useLegacyLists)
        assertSame(animeList, cached.mediaListOptions.animeList)
        assertSame(mangaList, cached.mediaListOptions.mangaList)
    }

    // ── null / absent semantics ──

    @Test
    fun `merge applies null scalars verbatim when the server returns them`() {
        val cached = createCachedUser()

        createResponse(
            about = null,
            bannerImage = null,
            avatar = null,
            options = null,
            mediaListOptions = null,
        ).applyUserSettingsTo(cached)

        assertNull(cached.about)
        assertNull(cached.bannerImage)
        assertNull(cached.avatar)
        // Name is returned by the mutation and applied; option blocks not returned are preserved.
        assertEquals("updated-name", cached.name)
        assertEquals("blue", cached.options?.profileColor)
        assertEquals("POINT_10", cached.mediaListOptions.scoreFormat)
    }

    @Test
    fun `merge leaves cached option blocks untouched when the response omits them`() {
        val cached = createCachedUser()

        createResponse(
            options = null,
            mediaListOptions = null,
            about = "kept bio",
        ).applyUserSettingsTo(cached)

        assertEquals("kept bio", cached.about)
        assertEquals("blue", cached.options?.profileColor)
        assertEquals(true, cached.options?.isAiringNotifications)
        assertEquals("POINT_10", cached.mediaListOptions.scoreFormat)
        assertNull(cached.mediaListOptions.rowOrder)
    }

    @Test
    fun `merge preserves cached media list values when the returned subfields are null`() {
        val cached = createCachedUser()

        createResponse(
            scoreFormat = null,
            rowOrder = null,
            mediaListOptions = UpdateUserData.UpdateUserMediaListOptions(rowOrder = null, scoreFormat = null),
        ).applyUserSettingsTo(cached)

        assertEquals("POINT_10", cached.mediaListOptions.scoreFormat)
        assertNull(cached.mediaListOptions.rowOrder)
    }

    @Test
    fun `merge falls back to cached options for absent booleans inside a present block`() {
        val cached = createCachedUser()

        UpdateUserData.UpdateUser(
            about = "New bio",
            avatar = null,
            bannerImage = null,
            id = 7,
            isFollowing = null,
            mediaListOptions = null,
            name = "mxt",
            options = UpdateUserData.UpdateUserOptions(
                airingNotifications = null,
                displayAdultContent = null,
                profileColor = null,
                titleLanguage = null,
            ),
            updatedAt = null,
        ).applyUserSettingsTo(cached)

        assertEquals("New bio", cached.about)
        // Absent booleans fall back to the cached options; nullable values stay null.
        assertEquals(true, cached.options?.isAiringNotifications)
        assertEquals(false, cached.options?.isDisplayAdultContent)
        assertNull(cached.options?.titleLanguage)
        assertNull(cached.options?.profileColor)
    }

    // ── fixtures ──

    private fun createCachedUser(
        isFollowing: Boolean = false,
        statsWatchedTime: Int = 0,
        unreadNotificationCount: Int = 0,
    ): User = User().also {
        it.id = 7L
        it.name = "mxt"
        it.bannerImage = "https://banner-old"
        it.avatar = ImageBase(extraLarge = "https://extra", large = "https://large", medium = "https://medium")
        it.about = "Old bio"
        it.options = UserOptions(
            titleLanguage = "ROMAJI",
            isDisplayAdultContent = false,
            isAiringNotifications = true,
            profileColor = "blue",
        )
        it.mediaListOptions = MediaListOptions(scoreFormat = "POINT_10", rowOrder = null)
        it.isFollowing = isFollowing
        it.stats = com.mxt.anitrend.model.entity.anilist.UserStats(watchedTime = statsWatchedTime)
        it.statistics = createStatistics(animeCount = 11, mangaCount = 22)
        it.unreadNotificationCount = unreadNotificationCount
    }

    private fun createStatistics(
        animeCount: Int,
        mangaCount: Int,
    ): UserStatisticTypes = UserStatisticTypes(
        anime = statistics(animeCount),
        manga = statistics(mangaCount),
    )

    private fun statistics(count: Int): UserStatistics = UserStatistics(
        chaptersRead = 0,
        count = count,
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

    private fun createResponse(
        about: String? = "New bio",
        profileColor: String? = "purple",
        titleLanguage: UserTitleLanguage? = UserTitleLanguage.NATIVE,
        displayAdultContent: Boolean = true,
        airingNotifications: Boolean = false,
        scoreFormat: ScoreFormat? = ScoreFormat.POINT_5,
        rowOrder: String? = "CUSTOM",
        options: UpdateUserData.UpdateUserOptions? = UpdateUserData.UpdateUserOptions(
            airingNotifications = airingNotifications,
            displayAdultContent = displayAdultContent,
            profileColor = profileColor,
            titleLanguage = titleLanguage,
        ),
        mediaListOptions: UpdateUserData.UpdateUserMediaListOptions? =
            UpdateUserData.UpdateUserMediaListOptions(rowOrder = rowOrder, scoreFormat = scoreFormat),
        avatar: UpdateUserData.UpdateUserAvatar? = UpdateUserData.UpdateUserAvatar(
            large = "https://avatar-large",
            medium = "https://avatar-medium",
        ),
        bannerImage: String? = "https://banner-new",
        name: String = "updated-name",
    ): UpdateUserData.UpdateUser = UpdateUserData.UpdateUser(
        about = about,
        avatar = avatar,
        bannerImage = bannerImage,
        id = 7,
        isFollowing = null,
        mediaListOptions = mediaListOptions,
        name = name,
        options = options,
        updatedAt = null,
    )
}
