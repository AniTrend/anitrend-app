package com.mxt.anitrend.domain.user.model

/**
 * Immutable canonical representation of a user's statistics aggregate.
 *
 * Mirrors the legacy `UserStatisticTypes`/`UserStatistics` DTO shape consumed by
 * the profile stats widget and the favourite genre/tag/year/format helpers, and
 * the generated `UserStatsData` transport nesting (`UserStatisticsAnime` and
 * `UserStatisticsManga`). Pure Kotlin value types with immutable lists and stable
 * nullable values; the transport and legacy mappers live in
 * `com.mxt.anitrend.data.mapper.UserStatisticsRecordMapper`.
 *
 * [Anime] and [Manga] share the identical field set of the legacy `UserStatistics`
 * and the generated per-media statistics types. A missing per-media block on the
 * transport side is normalised by the mapper to a zeroed record so consumers can
 * keep dereferencing `.anime`/`.manga` directly.
 */
data class UserStatisticsRecord(
    val anime: Anime,
    val manga: Manga,
) {
    data class Anime(
        val chaptersRead: Int = 0,
        val count: Int = 0,
        val countries: List<UserCountryStatisticRecord>? = null,
        val episodesWatched: Int = 0,
        val formats: List<UserFormatStatisticRecord>? = null,
        val genres: List<UserGenreStatisticRecord>? = null,
        val lengths: List<UserLengthStatisticRecord>? = null,
        val meanScore: Float = 0f,
        val minutesWatched: Int = 0,
        val releaseYears: List<UserReleaseYearStatisticRecord>? = null,
        val scores: List<UserScoreStatisticRecord>? = null,
        val staff: List<UserStaffStatisticRecord>? = null,
        val standardDeviation: Float = 0f,
        val startYears: List<UserStartYearStatisticRecord>? = null,
        val statuses: List<UserStatusStatisticRecord>? = null,
        val studios: List<UserStudioStatisticRecord>? = null,
        val tags: List<UserTagStatisticRecord>? = null,
        val voiceActors: List<UserVoiceActorStatisticRecord>? = null,
        val volumesRead: Int = 0,
    )

    data class Manga(
        val chaptersRead: Int = 0,
        val count: Int = 0,
        val countries: List<UserCountryStatisticRecord>? = null,
        val episodesWatched: Int = 0,
        val formats: List<UserFormatStatisticRecord>? = null,
        val genres: List<UserGenreStatisticRecord>? = null,
        val lengths: List<UserLengthStatisticRecord>? = null,
        val meanScore: Float = 0f,
        val minutesWatched: Int = 0,
        val releaseYears: List<UserReleaseYearStatisticRecord>? = null,
        val scores: List<UserScoreStatisticRecord>? = null,
        val staff: List<UserStaffStatisticRecord>? = null,
        val standardDeviation: Float = 0f,
        val startYears: List<UserStartYearStatisticRecord>? = null,
        val statuses: List<UserStatusStatisticRecord>? = null,
        val studios: List<UserStudioStatisticRecord>? = null,
        val tags: List<UserTagStatisticRecord>? = null,
        val voiceActors: List<UserVoiceActorStatisticRecord>? = null,
        val volumesRead: Int = 0,
    )
}
