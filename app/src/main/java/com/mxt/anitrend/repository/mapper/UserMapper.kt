package com.mxt.anitrend.repository.mapper

import com.mxt.anitrend.graphql.generated.AnimeFavouritesData
import com.mxt.anitrend.graphql.generated.CharacterFavouritesData
import com.mxt.anitrend.graphql.generated.CurrentUserData
import com.mxt.anitrend.graphql.generated.MangaFavouritesData
import com.mxt.anitrend.graphql.generated.StaffFavouritesData
import com.mxt.anitrend.graphql.generated.StudioFavouritesData
import com.mxt.anitrend.graphql.generated.ToggleFollowData
import com.mxt.anitrend.graphql.generated.ToggleLikeData
import com.mxt.anitrend.graphql.generated.UserBaseData
import com.mxt.anitrend.graphql.generated.UserFavouriteCountData
import com.mxt.anitrend.graphql.generated.UserFollowersData
import com.mxt.anitrend.graphql.generated.UserFollowingData
import com.mxt.anitrend.graphql.generated.UserOverviewData
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.UserStats
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FormatStats
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.GenreStats
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions
import com.mxt.anitrend.model.entity.anilist.meta.MediaListTypeOptions
import com.mxt.anitrend.model.entity.anilist.meta.MediaTagStats
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.anilist.meta.StatusDistribution
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.anilist.meta.UserOptions
import com.mxt.anitrend.model.entity.anilist.meta.YearStats
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
import com.mxt.anitrend.model.entity.base.CharacterBase as CharacterEntity
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity
import com.mxt.anitrend.model.entity.base.StaffBase as StaffEntity
import com.mxt.anitrend.model.entity.base.StudioBase as StudioEntity
import com.mxt.anitrend.model.entity.base.UserBase as UserEntity
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.KeyUtil

/**
 * Maps generated user transport payloads to the legacy mutable user entities at
 * the repository boundary. Projections follow the legacy Gson DTO shapes, so
 * downstream consumers observe the same entity fields as before.
 */

fun CurrentUserData.Viewer.toUserEntity(): User = User().also { entity ->
    entity.id = id.toLong()
    entity.name = name
    entity.avatar = avatar?.let { avatar -> toImageBase(extraLarge = null, large = avatar.large, medium = avatar.medium) }
    entity.bannerImage = bannerImage
    entity.isFollowing = isFollowing ?: false
    entity.about = about
    entity.options = options?.toUserOptions()
    entity.mediaListOptions = mediaListOptions?.toMediaListOptions() ?: MediaListOptions()
    entity.stats = stats?.toUserStats()
    entity.unreadNotificationCount = unreadNotificationCount ?: 0
}

fun UserOverviewData.User.toUserEntity(): User = User().also { entity ->
    entity.id = id.toLong()
    entity.name = name
    entity.avatar = avatar?.let { avatar -> toImageBase(extraLarge = null, large = avatar.large, medium = avatar.medium) }
    entity.bannerImage = bannerImage
    entity.isFollowing = isFollowing ?: false
    entity.about = about
    entity.statistics = statistics?.toUserStatisticTypes()
    entity.stats = stats?.toUserStats()
    entity.unreadNotificationCount = unreadNotificationCount ?: 0
}

fun UserBaseData.User.toUserBaseEntity(): UserEntity = toUserBaseEntity(
    id = id,
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing,
    avatarLarge = avatar?.large,
    avatarMedium = avatar?.medium,
)

fun ToggleFollowData.ToggleFollow.toUserBaseEntity(): UserEntity = toUserBaseEntity(
    id = id,
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing,
    avatarLarge = avatar?.large,
    avatarMedium = avatar?.medium,
)

fun ToggleLikeData.ToggleLike.toUserBaseEntity(): UserEntity = toUserBaseEntity(
    id = id,
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing,
    avatarLarge = avatar?.large,
    avatarMedium = avatar?.medium,
)

fun List<ToggleLikeData.ToggleLike?>.toUserBaseEntities(): List<UserEntity> = mapNotNull { it?.toUserBaseEntity() }

fun UserFollowersData.Page.toUserPage(): PageContainer<UserEntity> = PageContainer<UserEntity>().also { page ->
    page.pageData = followers.orEmpty().mapNotNull { user -> user?.toUserBaseEntity() }
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = info.perPage, currentPage = info.currentPage, hasNextPage = info.hasNextPage) }
}

fun UserFollowersData.PageFollowers.toUserBaseEntity(): UserEntity = toUserBaseEntity(
    id = id,
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing,
    avatarLarge = avatar?.large,
    avatarMedium = avatar?.medium,
)

fun UserFollowingData.Page.toUserPage(): PageContainer<UserEntity> = PageContainer<UserEntity>().also { page ->
    page.pageData = following.orEmpty().mapNotNull { user -> user?.toUserBaseEntity() }
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = info.perPage, currentPage = info.currentPage, hasNextPage = info.hasNextPage) }
}

fun UserFollowingData.PageFollowing.toUserBaseEntity(): UserEntity = toUserBaseEntity(
    id = id,
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing,
    avatarLarge = avatar?.large,
    avatarMedium = avatar?.medium,
)

// ── Favourites ───────────────────────────────────────────────────────────────

fun UserFavouriteCountData.toFavouriteConnection(): ConnectionContainer<Favourite> {
    val favourites = user?.favourites ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<Favourite>().also { connection ->
        connection.connection = Favourite().also { favourite ->
            favourite.anime = favourites.anime?.toMediaCountPage()
            favourite.manga = favourites.manga?.toMangaCountPage()
            favourite.characters = favourites.characters?.toCharacterCountPage()
            favourite.staff = favourites.staff?.toStaffCountPage()
            favourite.studios = favourites.studios?.toStudioCountPage()
        }
    }
}

fun AnimeFavouritesData.toFavouriteConnection(): ConnectionContainer<Favourite> {
    val favourites = user?.favourites ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<Favourite>().also { connection ->
        connection.connection = Favourite().also { favourite ->
            favourite.anime = favourites.anime?.toMediaFavouritesPage()
        }
    }
}

fun MangaFavouritesData.toFavouriteConnection(): ConnectionContainer<Favourite> {
    val favourites = user?.favourites ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<Favourite>().also { connection ->
        connection.connection = Favourite().also { favourite ->
            favourite.manga = favourites.manga?.toMangaFavouritesPage()
        }
    }
}

fun CharacterFavouritesData.toFavouriteConnection(): ConnectionContainer<Favourite> {
    val favourites = user?.favourites ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<Favourite>().also { connection ->
        connection.connection = Favourite().also { favourite ->
            favourite.characters = favourites.characters?.toCharacterFavouritesPage()
        }
    }
}

fun StaffFavouritesData.toFavouriteConnection(): ConnectionContainer<Favourite> {
    val favourites = user?.favourites ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<Favourite>().also { connection ->
        connection.connection = Favourite().also { favourite ->
            favourite.staff = favourites.staff?.toStaffFavouritesPage()
        }
    }
}

fun StudioFavouritesData.toFavouriteConnection(): ConnectionContainer<Favourite> {
    val favourites = user?.favourites ?: throw IllegalStateException("Empty response body")
    return ConnectionContainer<Favourite>().also { connection ->
        connection.connection = Favourite().also { favourite ->
            favourite.studios = favourites.studios?.toStudioFavouritesPage()
        }
    }
}

// ── Current user stats blocks ────────────────────────────────────────────────

private fun CurrentUserData.ViewerOptions.toUserOptions(): UserOptions = UserOptions(
    titleLanguage = titleLanguage?.name,
    isDisplayAdultContent = displayAdultContent ?: false,
    isAiringNotifications = airingNotifications ?: false,
    profileColor = profileColor,
)

private fun CurrentUserData.ViewerMediaListOptions.toMediaListOptions(): MediaListOptions = MediaListOptions(
    scoreFormat = scoreFormat?.name ?: KeyUtil.POINT_10_DECIMAL,
    rowOrder = rowOrder,
    useLegacyLists = useLegacyLists ?: false,
    animeList = animeList?.toMediaListTypeOptions(),
    mangaList = mangaList?.toMediaListTypeOptions(),
)

private fun CurrentUserData.ViewerMediaListOptionsAnimeList.toMediaListTypeOptions(): MediaListTypeOptions = MediaListTypeOptions(
    sectionOrder = sectionOrder?.mapNotNull { it },
    isSplitCompletedSectionByFormat = splitCompletedSectionByFormat ?: false,
    customLists = customLists?.mapNotNull { it },
    advancedScoring = advancedScoring?.mapNotNull { it },
    isAdvancedScoringEnabled = advancedScoringEnabled ?: false,
)

private fun CurrentUserData.ViewerMediaListOptionsMangaList.toMediaListTypeOptions(): MediaListTypeOptions = MediaListTypeOptions(
    sectionOrder = sectionOrder?.mapNotNull { it },
    isSplitCompletedSectionByFormat = splitCompletedSectionByFormat ?: false,
    customLists = customLists?.mapNotNull { it },
    advancedScoring = advancedScoring?.mapNotNull { it },
    isAdvancedScoringEnabled = advancedScoringEnabled ?: false,
)

private fun CurrentUserData.ViewerStats.toUserStats(): UserStats = UserStats(
    watchedTime = watchedTime ?: 0,
    chaptersRead = chaptersRead ?: 0,
    animeStatusDistribution = animeStatusDistribution?.mapNotNull { it?.toStatusDistribution() },
    mangaStatusDistribution = mangaStatusDistribution?.mapNotNull { it?.toStatusDistribution() },
    favouredGenres = favouredGenres?.mapNotNull { it?.toGenreStats() },
    favouredTags = favouredTags?.mapNotNull { it?.toMediaTagStats() },
    favouredYears = favouredYears?.mapNotNull { it?.toYearStats() },
    favouredFormats = favouredFormats?.mapNotNull { it?.toFormatStats() },
)

private fun UserOverviewData.UserStats.toUserStats(): UserStats = UserStats(
    favouredGenres = favouredGenres?.mapNotNull { it?.toGenreStats() },
)

private fun CurrentUserData.ViewerStatsAnimeStatusDistribution.toStatusDistribution(): StatusDistribution = StatusDistribution(
    status = status?.name,
    amount = amount ?: 0,
)

private fun CurrentUserData.ViewerStatsMangaStatusDistribution.toStatusDistribution(): StatusDistribution = StatusDistribution(
    status = status?.name,
    amount = amount ?: 0,
)

private fun CurrentUserData.ViewerStatsFavouredGenres.toGenreStats(): GenreStats = GenreStats(
    genre = genre,
    amount = amount ?: 0,
    meanScore = meanScore ?: 0,
    timeWatched = timeWatched ?: 0,
)

private fun UserOverviewData.UserStatsFavouredGenres.toGenreStats(): GenreStats = GenreStats(
    genre = genre,
    amount = amount ?: 0,
    meanScore = meanScore ?: 0,
    timeWatched = timeWatched ?: 0,
)

private fun CurrentUserData.ViewerStatsFavouredTags.toMediaTagStats(): MediaTagStats = MediaTagStats(
    tag = tag?.toMediaTagEntity(),
    amount = amount ?: 0,
    meanScore = meanScore ?: 0,
    timeWatched = timeWatched ?: 0,
)

private fun CurrentUserData.ViewerStatsFavouredYears.toYearStats(): YearStats = YearStats(
    year = year ?: 0,
    amount = amount ?: 0,
    meanScore = meanScore ?: 0,
)

private fun CurrentUserData.ViewerStatsFavouredFormats.toFormatStats(): FormatStats = FormatStats(
    format = format?.name,
    amount = amount ?: 0,
)

private fun CurrentUserData.ViewerStatsFavouredTagsTag.toMediaTagEntity(): MediaTag = MediaTag(
    name = name,
    description = description,
    category = category,
    rank = rank ?: 0,
    isGeneralSpoiler = isGeneralSpoiler ?: false,
    isMediaSpoiler = isMediaSpoiler ?: false,
    isAdult = isAdult ?: false,
).also { mediaTag -> mediaTag.id = id.toLong() }

// ── Overview statistics blocks ───────────────────────────────────────────────

private fun UserOverviewData.UserStatistics.toUserStatisticTypes(): UserStatisticTypes = UserStatisticTypes(
    anime = anime?.toAnimeStatistics() ?: emptyUserStatistics(),
    manga = manga?.toMangaStatistics() ?: emptyUserStatistics(),
)

private fun emptyUserStatistics(): UserStatistics = UserStatistics(
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

private fun UserOverviewData.UserStatisticsAnime.toAnimeStatistics(): UserStatistics = UserStatistics(
    chaptersRead = chaptersRead,
    count = count,
    countries = countries.mapStatItems { it.toUserCountryStatistic() },
    episodesWatched = episodesWatched,
    formats = formats.mapStatItems { it.toUserFormatStatistic() },
    genres = genres.mapStatItems { it.toUserGenreStatistic() },
    lengths = lengths.mapStatItems { it.toUserLengthStatistic() },
    meanScore = meanScore.toFloat(),
    minutesWatched = minutesWatched,
    releaseYears = releaseYears.mapStatItems { it.toUserReleaseYearStatistic() },
    scores = scores.mapStatItems { it.toUserScoreStatistic() },
    staff = staff.mapStatItems { it.toUserStaffStatistic() },
    standardDeviation = standardDeviation.toFloat(),
    startYears = startYears.mapStatItems { it.toUserStartYearStatistic() },
    statuses = statuses.mapStatItems { it.toUserStatusStatistic() },
    studios = studios.mapStatItems { it.toUserStudioStatistic() },
    tags = tags.mapStatItems { it.toUserTagStatistic() },
    voiceActors = voiceActors.mapStatItems { it.toUserVoiceActorStatistic() },
    volumesRead = volumesRead,
)

private fun UserOverviewData.UserStatisticsManga.toMangaStatistics(): UserStatistics = UserStatistics(
    chaptersRead = chaptersRead,
    count = count,
    countries = countries.mapStatItems { it.toUserCountryStatistic() },
    episodesWatched = episodesWatched,
    formats = formats.mapStatItems { it.toUserFormatStatistic() },
    genres = genres.mapStatItems { it.toUserGenreStatistic() },
    lengths = lengths.mapStatItems { it.toUserLengthStatistic() },
    meanScore = meanScore.toFloat(),
    minutesWatched = minutesWatched,
    releaseYears = releaseYears.mapStatItems { it.toUserReleaseYearStatistic() },
    scores = scores.mapStatItems { it.toUserScoreStatistic() },
    staff = staff.mapStatItems { it.toUserStaffStatistic() },
    standardDeviation = standardDeviation.toFloat(),
    startYears = startYears.mapStatItems { it.toUserStartYearStatistic() },
    statuses = statuses.mapStatItems { it.toUserStatusStatistic() },
    studios = studios.mapStatItems { it.toUserStudioStatistic() },
    tags = tags.mapStatItems { it.toUserTagStatistic() },
    voiceActors = voiceActors.mapStatItems { it.toUserVoiceActorStatistic() },
    volumesRead = volumesRead,
)

private fun UserOverviewData.UserStatisticsAnimeCountries.toUserCountryStatistic(): UserCountryStatistic = UserCountryStatistic(
    country = country,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeFormats.toUserFormatStatistic(): UserFormatStatistic = UserFormatStatistic(
    format = format?.name,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeGenres.toUserGenreStatistic(): UserGenreStatistic = UserGenreStatistic(
    genre = genre,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeLengths.toUserLengthStatistic(): UserLengthStatistic = UserLengthStatistic(
    length = length,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeReleaseYears.toUserReleaseYearStatistic(): UserReleaseYearStatistic = UserReleaseYearStatistic(
    releaseYear = releaseYear,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeScores.toUserScoreStatistic(): UserScoreStatistic = UserScoreStatistic(
    score = score,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeStaff.toUserStaffStatistic(): UserStaffStatistic = UserStaffStatistic(
    staff = null,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeStartYears.toUserStartYearStatistic(): UserStartYearStatistic = UserStartYearStatistic(
    startYear = startYear,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeStatuses.toUserStatusStatistic(): UserStatusStatistic = UserStatusStatistic(
    status = status?.name.orEmpty(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeStudios.toUserStudioStatistic(): UserStudioStatistic = UserStudioStatistic(
    studio = studio?.toStudioBaseEntity(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeTags.toUserTagStatistic(): UserTagStatistic = UserTagStatistic(
    tag = tag?.toMediaTagEntity(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeVoiceActors.toUserVoiceActorStatistic(): UserVoiceActorStatistic = UserVoiceActorStatistic(
    voiceActor = voiceActor?.toStaffBaseEntity(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaCountries.toUserCountryStatistic(): UserCountryStatistic = UserCountryStatistic(
    country = country,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaFormats.toUserFormatStatistic(): UserFormatStatistic = UserFormatStatistic(
    format = format?.name,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaGenres.toUserGenreStatistic(): UserGenreStatistic = UserGenreStatistic(
    genre = genre,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaLengths.toUserLengthStatistic(): UserLengthStatistic = UserLengthStatistic(
    length = length,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaReleaseYears.toUserReleaseYearStatistic(): UserReleaseYearStatistic = UserReleaseYearStatistic(
    releaseYear = releaseYear,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaScores.toUserScoreStatistic(): UserScoreStatistic = UserScoreStatistic(
    score = score,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaStaff.toUserStaffStatistic(): UserStaffStatistic = UserStaffStatistic(
    staff = null,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaStartYears.toUserStartYearStatistic(): UserStartYearStatistic = UserStartYearStatistic(
    startYear = startYear,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaStatuses.toUserStatusStatistic(): UserStatusStatistic = UserStatusStatistic(
    status = status?.name.orEmpty(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaStudios.toUserStudioStatistic(): UserStudioStatistic = UserStudioStatistic(
    studio = studio?.toStudioBaseEntity(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaTags.toUserTagStatistic(): UserTagStatistic = UserTagStatistic(
    tag = tag?.toMediaTagEntity(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsMangaVoiceActors.toUserVoiceActorStatistic(): UserVoiceActorStatistic = UserVoiceActorStatistic(
    voiceActor = voiceActor?.toStaffBaseEntity(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

private fun UserOverviewData.UserStatisticsAnimeStudiosStudio.toStudioBaseEntity(): StudioEntity = StudioEntity().also { entity ->
    entity.id = id.toLong()
    entity.name = name
    entity.siteUrl = siteUrl
    entity.isFavourite = isFavourite
}

private fun UserOverviewData.UserStatisticsMangaStudiosStudio.toStudioBaseEntity(): StudioEntity = StudioEntity().also { entity ->
    entity.id = id.toLong()
    entity.name = name
    entity.siteUrl = siteUrl
    entity.isFavourite = isFavourite
}

private fun UserOverviewData.UserStatisticsAnimeTagsTag.toMediaTagEntity(): MediaTag = MediaTag(
    name = name,
    description = description,
    category = category,
    rank = rank ?: 0,
    isGeneralSpoiler = isGeneralSpoiler ?: false,
    isAdult = isAdult ?: false,
).also { mediaTag -> mediaTag.id = id.toLong() }

private fun UserOverviewData.UserStatisticsMangaTagsTag.toMediaTagEntity(): MediaTag = MediaTag(
    name = name,
    description = description,
    category = category,
    rank = rank ?: 0,
    isGeneralSpoiler = isGeneralSpoiler ?: false,
    isAdult = isAdult ?: false,
).also { mediaTag -> mediaTag.id = id.toLong() }

private fun UserOverviewData.UserStatisticsAnimeVoiceActorsVoiceActor.toStaffBaseEntity(): StaffEntity = StaffEntity().also { entity ->
    entity.id = id.toLong()
    entity.name = name?.toTitleBase()
    entity.image = image?.let { image -> toImageBase(extraLarge = null, large = image.large, medium = image.medium) }
    entity.language = language?.name
    entity.isFavourite = isFavourite
    entity.siteUrl = siteUrl
}

private fun UserOverviewData.UserStatisticsMangaVoiceActorsVoiceActor.toStaffBaseEntity(): StaffEntity = StaffEntity().also { entity ->
    entity.id = id.toLong()
    entity.name = name?.toTitleBase()
    entity.image = image?.let { image -> toImageBase(extraLarge = null, large = image.large, medium = image.medium) }
    entity.language = language?.name
    entity.isFavourite = isFavourite
    entity.siteUrl = siteUrl
}

private fun UserOverviewData.UserStatisticsAnimeVoiceActorsVoiceActorName.toTitleBase(): TitleBase = TitleBase(
    first = first,
    last = last,
    original = native,
    alternative = alternative?.mapNotNull { it },
)

private fun UserOverviewData.UserStatisticsMangaVoiceActorsVoiceActorName.toTitleBase(): TitleBase = TitleBase(
    first = first,
    last = last,
    original = native,
    alternative = alternative?.mapNotNull { it },
)

// ── Favourites page projections ──────────────────────────────────────────────

private fun UserFavouriteCountData.UserFavouritesAnime.toMediaCountPage(): PageContainer<MediaEntity> = PageContainer<MediaEntity>().also { page ->
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = null, currentPage = null, hasNextPage = null) }
}

private fun UserFavouriteCountData.UserFavouritesManga.toMangaCountPage(): PageContainer<MediaEntity> = PageContainer<MediaEntity>().also { page ->
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = null, currentPage = null, hasNextPage = null) }
}

private fun UserFavouriteCountData.UserFavouritesCharacters.toCharacterCountPage(): PageContainer<CharacterEntity> = PageContainer<CharacterEntity>().also { page ->
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = null, currentPage = null, hasNextPage = null) }
}

private fun UserFavouriteCountData.UserFavouritesStaff.toStaffCountPage(): PageContainer<StaffEntity> = PageContainer<StaffEntity>().also { page ->
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = null, currentPage = null, hasNextPage = null) }
}

private fun UserFavouriteCountData.UserFavouritesStudios.toStudioCountPage(): PageContainer<StudioEntity> = PageContainer<StudioEntity>().also { page ->
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = null, currentPage = null, hasNextPage = null) }
}

private fun AnimeFavouritesData.UserFavouritesAnime.toMediaFavouritesPage(): PageContainer<MediaEntity> = PageContainer<MediaEntity>().also { page ->
    page.pageData = nodes.orEmpty().mapNotNull { node -> node?.toMediaEntity() }
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = info.perPage, currentPage = info.currentPage, hasNextPage = info.hasNextPage) }
}

private fun MangaFavouritesData.UserFavouritesManga.toMangaFavouritesPage(): PageContainer<MediaEntity> = PageContainer<MediaEntity>().also { page ->
    page.pageData = nodes.orEmpty().mapNotNull { node -> node?.toMediaEntity() }
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = info.perPage, currentPage = info.currentPage, hasNextPage = info.hasNextPage) }
}

private fun CharacterFavouritesData.UserFavouritesCharacters.toCharacterFavouritesPage(): PageContainer<CharacterEntity> = PageContainer<CharacterEntity>().also { page ->
    page.pageData = nodes.orEmpty().mapNotNull { node -> node?.toCharacterBaseEntity() }
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = info.perPage, currentPage = info.currentPage, hasNextPage = info.hasNextPage) }
}

private fun StaffFavouritesData.UserFavouritesStaff.toStaffFavouritesPage(): PageContainer<StaffEntity> = PageContainer<StaffEntity>().also { page ->
    page.pageData = nodes.orEmpty().mapNotNull { node -> node?.toStaffBaseEntity() }
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = info.perPage, currentPage = info.currentPage, hasNextPage = info.hasNextPage) }
}

private fun StudioFavouritesData.UserFavouritesStudios.toStudioFavouritesPage(): PageContainer<StudioEntity> = PageContainer<StudioEntity>().also { page ->
    page.pageData = nodes.orEmpty().mapNotNull { node -> node?.toStudioBaseEntity() }
    pageInfo?.let { info -> page.pageInfo = toPageInfo(total = info.total, perPage = info.perPage, currentPage = info.currentPage, hasNextPage = info.hasNextPage) }
}

private fun AnimeFavouritesData.UserFavouritesAnimeNodes.toMediaEntity(): MediaEntity = MediaEntity().also { entity ->
    entity.id = id.toLong()
    entity.title = title?.toMediaTitle()
    entity.bannerImage = bannerImage
    entity.coverImage = coverImage?.let { image -> toImageBase(extraLarge = image.extraLarge, large = image.large, medium = image.medium) }
    entity.type = type?.name
    entity.format = format?.name
    entity.season = season?.name
    entity.status = status?.name
    entity.siteUrl = siteUrl
    entity.meanScore = meanScore ?: 0
    entity.averageScore = averageScore ?: 0
    entity.startDate = startDate?.toFuzzyDate()
    entity.endDate = endDate?.toFuzzyDate()
    entity.episodes = episodes ?: 0
    entity.chapters = chapters ?: 0
    entity.volumes = volumes ?: 0
    entity.isAdult = isAdult ?: false
    entity.isFavourite = isFavourite
    entity.nextAiringEpisode = nextAiringEpisode?.toAiringSchedule()
    entity.mediaListEntry = mediaListEntry?.toMediaList()
}

private fun MangaFavouritesData.UserFavouritesMangaNodes.toMediaEntity(): MediaEntity = MediaEntity().also { entity ->
    entity.id = id.toLong()
    entity.title = title?.toMediaTitle()
    entity.bannerImage = bannerImage
    entity.coverImage = coverImage?.let { image -> toImageBase(extraLarge = image.extraLarge, large = image.large, medium = image.medium) }
    entity.type = type?.name
    entity.format = format?.name
    entity.season = season?.name
    entity.status = status?.name
    entity.siteUrl = siteUrl
    entity.meanScore = meanScore ?: 0
    entity.averageScore = averageScore ?: 0
    entity.startDate = startDate?.toFuzzyDate()
    entity.endDate = endDate?.toFuzzyDate()
    entity.episodes = episodes ?: 0
    entity.chapters = chapters ?: 0
    entity.volumes = volumes ?: 0
    entity.isAdult = isAdult ?: false
    entity.isFavourite = isFavourite
    entity.nextAiringEpisode = nextAiringEpisode?.toAiringSchedule()
    entity.mediaListEntry = mediaListEntry?.toMediaList()
}

private fun AnimeFavouritesData.UserFavouritesAnimeNodesTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun MangaFavouritesData.UserFavouritesMangaNodesTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun AnimeFavouritesData.UserFavouritesAnimeNodesStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun AnimeFavouritesData.UserFavouritesAnimeNodesEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MangaFavouritesData.UserFavouritesMangaNodesStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun MangaFavouritesData.UserFavouritesMangaNodesEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun AnimeFavouritesData.UserFavouritesAnimeNodesNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun MangaFavouritesData.UserFavouritesMangaNodesNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun AnimeFavouritesData.UserFavouritesAnimeNodesMediaListEntry.toMediaList(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun MangaFavouritesData.UserFavouritesMangaNodesMediaListEntry.toMediaList(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun CharacterFavouritesData.UserFavouritesCharactersNodes.toCharacterBaseEntity(): CharacterEntity = CharacterEntity().also { entity ->
    entity.id = id.toLong()
    entity.name = name?.toTitleBase()
    entity.image = image?.let { image -> toImageBase(extraLarge = null, large = image.large, medium = image.medium) }
    entity.isFavourite = isFavourite
    entity.siteUrl = siteUrl
}

private fun CharacterFavouritesData.UserFavouritesCharactersNodesName.toTitleBase(): TitleBase = TitleBase(
    first = first,
    last = last,
    original = native,
    alternative = alternative?.mapNotNull { it },
)

private fun StaffFavouritesData.UserFavouritesStaffNodes.toStaffBaseEntity(): StaffEntity = StaffEntity().also { entity ->
    entity.id = id.toLong()
    entity.name = name?.toTitleBase()
    entity.image = image?.let { image -> toImageBase(extraLarge = null, large = image.large, medium = image.medium) }
    entity.language = language?.name
    entity.isFavourite = isFavourite
    entity.siteUrl = siteUrl
}

private fun StaffFavouritesData.UserFavouritesStaffNodesName.toTitleBase(): TitleBase = TitleBase(
    first = first,
    last = last,
    original = native,
    alternative = alternative?.mapNotNull { it },
)

private fun StudioFavouritesData.UserFavouritesStudiosNodes.toStudioBaseEntity(): StudioEntity = StudioEntity().also { entity ->
    entity.id = id.toLong()
    entity.name = name
    entity.siteUrl = siteUrl
    entity.isFavourite = isFavourite
}

// ── Shared projections ────────────────────────────────────────────────────────

private fun toUserBaseEntity(
    id: Int,
    name: String,
    bannerImage: String?,
    isFollowing: Boolean?,
    avatarLarge: String?,
    avatarMedium: String?,
): UserEntity = UserEntity(
    name = name,
    bannerImage = bannerImage,
    isFollowing = isFollowing ?: false,
).also { entity ->
    entity.id = id.toLong()
    entity.avatar = toImageBase(extraLarge = null, large = avatarLarge, medium = avatarMedium)
}

private fun toImageBase(
    extraLarge: String?,
    large: String?,
    medium: String?,
): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun toPageInfo(
    total: Int?,
    perPage: Int?,
    currentPage: Int?,
    hasNextPage: Boolean?,
): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo -> pageInfo.setHasNextPage(hasNextPage ?: false) }

private inline fun <T, R> List<T?>?.mapStatItems(transform: (T) -> R): List<R>? = this?.mapNotNull { it?.let(transform) }
