package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.StaffRecord
import com.mxt.anitrend.domain.model.StudioRecord
import com.mxt.anitrend.domain.user.model.MediaTagRecord
import com.mxt.anitrend.domain.user.model.UserCountryStatisticRecord
import com.mxt.anitrend.domain.user.model.UserFormatStatisticRecord
import com.mxt.anitrend.domain.user.model.UserGenreStatisticRecord
import com.mxt.anitrend.domain.user.model.UserLengthStatisticRecord
import com.mxt.anitrend.domain.user.model.UserReleaseYearStatisticRecord
import com.mxt.anitrend.domain.user.model.UserScoreStatisticRecord
import com.mxt.anitrend.domain.user.model.UserStaffStatisticRecord
import com.mxt.anitrend.domain.user.model.UserStartYearStatisticRecord
import com.mxt.anitrend.domain.user.model.UserStatisticsRecord
import com.mxt.anitrend.domain.user.model.UserStatusStatisticRecord
import com.mxt.anitrend.domain.user.model.UserStudioStatisticRecord
import com.mxt.anitrend.domain.user.model.UserTagStatisticRecord
import com.mxt.anitrend.domain.user.model.UserVoiceActorStatisticRecord
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

/**
 * Maps the generated `UserStatsData` transport types and the legacy cached
 * `UserStatisticTypes` DTOs to the immutable [UserStatisticsRecord] aggregate.
 *
 * The generated lane normalises nullable per-media blocks and nullable list
 * elements, converts the generated `Double` scores to the legacy `Float` shape,
 * converts generated `Int` ids to domain `Long`s, and projects the generated
 * enum fields (`MediaFormat`, `MediaListStatus`, `StaffLanguage`) to the legacy
 * string representations (enum constant names, e.g. "TV", "COMPLETED").
 */
fun UserStatsData.toUserStatisticsRecord(): UserStatisticsRecord {
    val statistics = user?.statistics
    return UserStatisticsRecord(
        anime = statistics?.anime?.toAnimeStatistics() ?: UserStatisticsRecord.Anime(),
        manga = statistics?.manga?.toMangaStatistics() ?: UserStatisticsRecord.Manga(),
    )
}

fun UserStatsData.UserStatisticsAnime.toAnimeStatistics(): UserStatisticsRecord.Anime = UserStatisticsRecord.Anime(
    chaptersRead = chaptersRead,
    count = count,
    countries = countries.mapStatItems { it.toUserCountryStatisticRecord() },
    episodesWatched = episodesWatched,
    formats = formats.mapStatItems { it.toUserFormatStatisticRecord() },
    genres = genres.mapStatItems { it.toUserGenreStatisticRecord() },
    lengths = lengths.mapStatItems { it.toUserLengthStatisticRecord() },
    meanScore = meanScore.toFloat(),
    minutesWatched = minutesWatched,
    releaseYears = releaseYears.mapStatItems { it.toUserReleaseYearStatisticRecord() },
    scores = scores.mapStatItems { it.toUserScoreStatisticRecord() },
    staff = staff.mapStatItems { it.toUserStaffStatisticRecord() },
    standardDeviation = standardDeviation.toFloat(),
    startYears = startYears.mapStatItems { it.toUserStartYearStatisticRecord() },
    statuses = statuses.mapStatItems { it.toUserStatusStatisticRecord() },
    studios = studios.mapStatItems { it.toUserStudioStatisticRecord() },
    tags = tags.mapStatItems { it.toUserTagStatisticRecord() },
    voiceActors = voiceActors.mapStatItems { it.toUserVoiceActorStatisticRecord() },
    volumesRead = volumesRead,
)

fun UserStatsData.UserStatisticsManga.toMangaStatistics(): UserStatisticsRecord.Manga = UserStatisticsRecord.Manga(
    chaptersRead = chaptersRead,
    count = count,
    countries = countries.mapStatItems { it.toUserCountryStatisticRecord() },
    episodesWatched = episodesWatched,
    formats = formats.mapStatItems { it.toUserFormatStatisticRecord() },
    genres = genres.mapStatItems { it.toUserGenreStatisticRecord() },
    lengths = lengths.mapStatItems { it.toUserLengthStatisticRecord() },
    meanScore = meanScore.toFloat(),
    minutesWatched = minutesWatched,
    releaseYears = releaseYears.mapStatItems { it.toUserReleaseYearStatisticRecord() },
    scores = scores.mapStatItems { it.toUserScoreStatisticRecord() },
    staff = staff.mapStatItems { it.toUserStaffStatisticRecord() },
    standardDeviation = standardDeviation.toFloat(),
    startYears = startYears.mapStatItems { it.toUserStartYearStatisticRecord() },
    statuses = statuses.mapStatItems { it.toUserStatusStatisticRecord() },
    studios = studios.mapStatItems { it.toUserStudioStatisticRecord() },
    tags = tags.mapStatItems { it.toUserTagStatisticRecord() },
    voiceActors = voiceActors.mapStatItems { it.toUserVoiceActorStatisticRecord() },
    volumesRead = volumesRead,
)

// ── Generated item types: anime ──────────────────────────────────────────────

fun UserStatsData.UserStatisticsAnimeCountries.toUserCountryStatisticRecord(): UserCountryStatisticRecord = UserCountryStatisticRecord(
    country = country,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsAnimeFormats.toUserFormatStatisticRecord(): UserFormatStatisticRecord = UserFormatStatisticRecord(
    format = format?.name,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsAnimeGenres.toUserGenreStatisticRecord(): UserGenreStatisticRecord = UserGenreStatisticRecord(
    genre = genre,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsAnimeLengths.toUserLengthStatisticRecord(): UserLengthStatisticRecord = UserLengthStatisticRecord(
    length = length,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsAnimeReleaseYears.toUserReleaseYearStatisticRecord(): UserReleaseYearStatisticRecord = UserReleaseYearStatisticRecord(
    releaseYear = releaseYear,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsAnimeScores.toUserScoreStatisticRecord(): UserScoreStatisticRecord = UserScoreStatisticRecord(
    score = score,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsAnimeStaff.toUserStaffStatisticRecord(): UserStaffStatisticRecord = UserStaffStatisticRecord(
    staff = null,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsAnimeStartYears.toUserStartYearStatisticRecord(): UserStartYearStatisticRecord = UserStartYearStatisticRecord(
    startYear = startYear,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsAnimeStatuses.toUserStatusStatisticRecord(): UserStatusStatisticRecord = UserStatusStatisticRecord(
    status = status?.name.orEmpty(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsAnimeStudios.toUserStudioStatisticRecord(): UserStudioStatisticRecord = UserStudioStatisticRecord(
    studio = studio?.toStudioRecord(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsAnimeTags.toUserTagStatisticRecord(): UserTagStatisticRecord = UserTagStatisticRecord(
    tag = tag?.toMediaTagRecord(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsAnimeVoiceActors.toUserVoiceActorStatisticRecord(): UserVoiceActorStatisticRecord = UserVoiceActorStatisticRecord(
    voiceActor = voiceActor?.toStaffRecord(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

// ── Generated item types: manga ──────────────────────────────────────────────

fun UserStatsData.UserStatisticsMangaCountries.toUserCountryStatisticRecord(): UserCountryStatisticRecord = UserCountryStatisticRecord(
    country = country,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsMangaFormats.toUserFormatStatisticRecord(): UserFormatStatisticRecord = UserFormatStatisticRecord(
    format = format?.name,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsMangaGenres.toUserGenreStatisticRecord(): UserGenreStatisticRecord = UserGenreStatisticRecord(
    genre = genre,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsMangaLengths.toUserLengthStatisticRecord(): UserLengthStatisticRecord = UserLengthStatisticRecord(
    length = length,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsMangaReleaseYears.toUserReleaseYearStatisticRecord(): UserReleaseYearStatisticRecord = UserReleaseYearStatisticRecord(
    releaseYear = releaseYear,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsMangaScores.toUserScoreStatisticRecord(): UserScoreStatisticRecord = UserScoreStatisticRecord(
    score = score,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsMangaStaff.toUserStaffStatisticRecord(): UserStaffStatisticRecord = UserStaffStatisticRecord(
    staff = null,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsMangaStartYears.toUserStartYearStatisticRecord(): UserStartYearStatisticRecord = UserStartYearStatisticRecord(
    startYear = startYear,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsMangaStatuses.toUserStatusStatisticRecord(): UserStatusStatisticRecord = UserStatusStatisticRecord(
    status = status?.name.orEmpty(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsMangaStudios.toUserStudioStatisticRecord(): UserStudioStatisticRecord = UserStudioStatisticRecord(
    studio = studio?.toStudioRecord(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsMangaTags.toUserTagStatisticRecord(): UserTagStatisticRecord = UserTagStatisticRecord(
    tag = tag?.toMediaTagRecord(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

fun UserStatsData.UserStatisticsMangaVoiceActors.toUserVoiceActorStatisticRecord(): UserVoiceActorStatisticRecord = UserVoiceActorStatisticRecord(
    voiceActor = voiceActor?.toStaffRecord(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore.toFloat(),
    mediaIds = mediaIds.mapNotNull { it },
    minutesWatched = minutesWatched,
)

// ── Generated nested references ──────────────────────────────────────────────

fun UserStatsData.UserStatisticsAnimeStudiosStudio.toStudioRecord(): StudioRecord = StudioRecord(
    id = id.toLong(),
    name = name,
    siteUrl = siteUrl,
    isFavourite = isFavourite,
)

fun UserStatsData.UserStatisticsMangaStudiosStudio.toStudioRecord(): StudioRecord = StudioRecord(
    id = id.toLong(),
    name = name,
    siteUrl = siteUrl,
    isFavourite = isFavourite,
)

fun UserStatsData.UserStatisticsAnimeTagsTag.toMediaTagRecord(): MediaTagRecord = MediaTagRecord(
    id = id.toLong(),
    name = name,
    description = description,
    category = category,
    rank = rank ?: 0,
    isGeneralSpoiler = isGeneralSpoiler ?: false,
    isMediaSpoiler = false,
    isAdult = isAdult ?: false,
    isSelected = false,
)

fun UserStatsData.UserStatisticsMangaTagsTag.toMediaTagRecord(): MediaTagRecord = MediaTagRecord(
    id = id.toLong(),
    name = name,
    description = description,
    category = category,
    rank = rank ?: 0,
    isGeneralSpoiler = isGeneralSpoiler ?: false,
    isMediaSpoiler = false,
    isAdult = isAdult ?: false,
    isSelected = false,
)

fun UserStatsData.UserStatisticsAnimeVoiceActorsVoiceActor.toStaffRecord(): StaffRecord = StaffRecord(
    id = id.toLong(),
    name = name?.toStaffName(),
    siteUrl = siteUrl,
    isFavourite = isFavourite,
)

fun UserStatsData.UserStatisticsMangaVoiceActorsVoiceActor.toStaffRecord(): StaffRecord = StaffRecord(
    id = id.toLong(),
    name = name?.toStaffName(),
    siteUrl = siteUrl,
    isFavourite = isFavourite,
)

/**
 * Mirrors the legacy `TitleBase.fullName` semantics: first and last joined with a
 * single space when both are present, falling back to whichever of first/last is
 * present, and null when both are missing.
 */
private fun UserStatsData.UserStatisticsAnimeVoiceActorsVoiceActorName.toStaffName(): String? {
    var fullName = first
    if (!last.isNullOrEmpty()) {
        fullName = if (!fullName.isNullOrEmpty()) "$fullName $last" else last
    }
    return fullName
}

private fun UserStatsData.UserStatisticsMangaVoiceActorsVoiceActorName.toStaffName(): String? {
    var fullName = first
    if (!last.isNullOrEmpty()) {
        fullName = if (!fullName.isNullOrEmpty()) "$fullName $last" else last
    }
    return fullName
}

private inline fun <T, R> List<T?>?.mapStatItems(transform: (T) -> R): List<R>? =
    this?.mapNotNull { it?.let(transform) }

// ── Legacy cached DTO lane ───────────────────────────────────────────────────

fun UserStatisticTypes.toUserStatisticsRecord(): UserStatisticsRecord = UserStatisticsRecord(
    anime = anime.toAnimeStatistics(),
    manga = manga.toMangaStatistics(),
)

fun UserStatistics.toAnimeStatistics(): UserStatisticsRecord.Anime = UserStatisticsRecord.Anime(
    chaptersRead = chaptersRead,
    count = count,
    countries = countries.toUserCountryStatisticRecords(),
    episodesWatched = episodesWatched,
    formats = formats.toUserFormatStatisticRecords(),
    genres = genres.toUserGenreStatisticRecords(),
    lengths = lengths.toUserLengthStatisticRecords(),
    meanScore = meanScore,
    minutesWatched = minutesWatched,
    releaseYears = releaseYears.toUserReleaseYearStatisticRecords(),
    scores = scores.toUserScoreStatisticRecords(),
    staff = staff.toUserStaffStatisticRecords(),
    standardDeviation = standardDeviation,
    startYears = startYears.toUserStartYearStatisticRecords(),
    statuses = statuses.toUserStatusStatisticRecords(),
    studios = studios.toUserStudioStatisticRecords(),
    tags = tags.toUserTagStatisticRecords(),
    voiceActors = voiceActors.toUserVoiceActorStatisticRecords(),
    volumesRead = volumesRead,
)

fun UserStatistics.toMangaStatistics(): UserStatisticsRecord.Manga = UserStatisticsRecord.Manga(
    chaptersRead = chaptersRead,
    count = count,
    countries = countries.toUserCountryStatisticRecords(),
    episodesWatched = episodesWatched,
    formats = formats.toUserFormatStatisticRecords(),
    genres = genres.toUserGenreStatisticRecords(),
    lengths = lengths.toUserLengthStatisticRecords(),
    meanScore = meanScore,
    minutesWatched = minutesWatched,
    releaseYears = releaseYears.toUserReleaseYearStatisticRecords(),
    scores = scores.toUserScoreStatisticRecords(),
    staff = staff.toUserStaffStatisticRecords(),
    standardDeviation = standardDeviation,
    startYears = startYears.toUserStartYearStatisticRecords(),
    statuses = statuses.toUserStatusStatisticRecords(),
    studios = studios.toUserStudioStatisticRecords(),
    tags = tags.toUserTagStatisticRecords(),
    voiceActors = voiceActors.toUserVoiceActorStatisticRecords(),
    volumesRead = volumesRead,
)

private fun List<UserCountryStatistic>?.toUserCountryStatisticRecords(): List<UserCountryStatisticRecord>? = this?.map { it.toUserCountryStatisticRecord() }

private fun List<UserFormatStatistic>?.toUserFormatStatisticRecords(): List<UserFormatStatisticRecord>? = this?.map { it.toUserFormatStatisticRecord() }

private fun List<UserGenreStatistic>?.toUserGenreStatisticRecords(): List<UserGenreStatisticRecord>? = this?.map { it.toUserGenreStatisticRecord() }

private fun List<UserLengthStatistic>?.toUserLengthStatisticRecords(): List<UserLengthStatisticRecord>? = this?.map { it.toUserLengthStatisticRecord() }

private fun List<UserReleaseYearStatistic>?.toUserReleaseYearStatisticRecords(): List<UserReleaseYearStatisticRecord>? = this?.map { it.toUserReleaseYearStatisticRecord() }

private fun List<UserScoreStatistic>?.toUserScoreStatisticRecords(): List<UserScoreStatisticRecord>? = this?.map { it.toUserScoreStatisticRecord() }

private fun List<UserStaffStatistic>?.toUserStaffStatisticRecords(): List<UserStaffStatisticRecord>? = this?.map { it.toUserStaffStatisticRecord() }

private fun List<UserStartYearStatistic>?.toUserStartYearStatisticRecords(): List<UserStartYearStatisticRecord>? = this?.map { it.toUserStartYearStatisticRecord() }

private fun List<UserStatusStatistic>?.toUserStatusStatisticRecords(): List<UserStatusStatisticRecord>? = this?.map { it.toUserStatusStatisticRecord() }

private fun List<UserStudioStatistic>?.toUserStudioStatisticRecords(): List<UserStudioStatisticRecord>? = this?.map { it.toUserStudioStatisticRecord() }

private fun List<UserTagStatistic>?.toUserTagStatisticRecords(): List<UserTagStatisticRecord>? = this?.map { it.toUserTagStatisticRecord() }

private fun List<UserVoiceActorStatistic>?.toUserVoiceActorStatisticRecords(): List<UserVoiceActorStatisticRecord>? = this?.map { it.toUserVoiceActorStatisticRecord() }

fun UserCountryStatistic.toUserCountryStatisticRecord(): UserCountryStatisticRecord = UserCountryStatisticRecord(
    country = country,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

fun UserFormatStatistic.toUserFormatStatisticRecord(): UserFormatStatisticRecord = UserFormatStatisticRecord(
    format = format,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

fun UserGenreStatistic.toUserGenreStatisticRecord(): UserGenreStatisticRecord = UserGenreStatisticRecord(
    genre = genre,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

fun UserLengthStatistic.toUserLengthStatisticRecord(): UserLengthStatisticRecord = UserLengthStatisticRecord(
    length = length,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

fun UserReleaseYearStatistic.toUserReleaseYearStatisticRecord(): UserReleaseYearStatisticRecord = UserReleaseYearStatisticRecord(
    releaseYear = releaseYear,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

fun UserScoreStatistic.toUserScoreStatisticRecord(): UserScoreStatisticRecord = UserScoreStatisticRecord(
    score = score,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

fun UserStaffStatistic.toUserStaffStatisticRecord(): UserStaffStatisticRecord = UserStaffStatisticRecord(
    staff = staff?.toStaffRecord(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

fun UserStartYearStatistic.toUserStartYearStatisticRecord(): UserStartYearStatisticRecord = UserStartYearStatisticRecord(
    startYear = startYear,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

fun UserStatusStatistic.toUserStatusStatisticRecord(): UserStatusStatisticRecord = UserStatusStatisticRecord(
    status = status,
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

fun UserStudioStatistic.toUserStudioStatisticRecord(): UserStudioStatisticRecord = UserStudioStatisticRecord(
    studio = studio?.toStudioRecord(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

fun UserTagStatistic.toUserTagStatisticRecord(): UserTagStatisticRecord = UserTagStatisticRecord(
    tag = tag?.toMediaTagRecord(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

fun UserVoiceActorStatistic.toUserVoiceActorStatisticRecord(): UserVoiceActorStatisticRecord = UserVoiceActorStatisticRecord(
    voiceActor = voiceActor?.toStaffRecord(),
    chaptersRead = chaptersRead,
    count = count,
    meanScore = meanScore,
    mediaIds = mediaIds,
    minutesWatched = minutesWatched,
)

private fun StaffBase.toStaffRecord(): StaffRecord = StaffRecord(
    id = id,
    name = name?.fullName,
    siteUrl = siteUrl,
    isFavourite = isFavourite,
)

private fun StudioBase.toStudioRecord(): StudioRecord = StudioRecord(
    id = id,
    name = name.orEmpty(),
    siteUrl = siteUrl,
    isFavourite = isFavourite,
)

private fun MediaTag.toMediaTagRecord(): MediaTagRecord = MediaTagRecord(
    id = id,
    name = name,
    description = description,
    category = category,
    rank = rank,
    isGeneralSpoiler = isGeneralSpoiler,
    isMediaSpoiler = isMediaSpoiler,
    isAdult = isAdult,
    isSelected = isSelected,
)
