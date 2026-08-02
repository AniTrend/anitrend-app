package com.mxt.anitrend.domain.user.model

import com.mxt.anitrend.domain.model.StaffRecord
import com.mxt.anitrend.domain.model.StudioRecord

/**
 * Immutable canonical representations of the twelve per-item user statistics
 * entries, mirroring the legacy `UserStatisticTypes`/`UserStatistics` item DTOs
 * (`UserCountryStatistic`, `UserFormatStatistic`, ..., `UserVoiceActorStatistic`).
 *
 * Each record preserves the legacy discriminator field (country/format/genre/
 * length/releaseYear/score/staff/startYear/status/studio/tag/voiceActor) plus the
 * common `IUserStatistic` fields (`chaptersRead`, `count`, `meanScore`,
 * `mediaIds`, `minutesWatched`). Nested staff/studio/tag references project to
 * the existing pure-Kotlin domain records where available. Lists are immutable
 * and nullable values stay nullable.
 */
data class UserCountryStatisticRecord(
    val country: String?,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)

data class UserFormatStatisticRecord(
    val format: String?,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)

data class UserGenreStatisticRecord(
    val genre: String?,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)

data class UserLengthStatisticRecord(
    val length: String?,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)

data class UserReleaseYearStatisticRecord(
    val releaseYear: Int?,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)

data class UserScoreStatisticRecord(
    val score: Int?,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)

data class UserStaffStatisticRecord(
    val staff: StaffRecord?,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)

data class UserStartYearStatisticRecord(
    val startYear: Int?,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)

data class UserStatusStatisticRecord(
    val status: String,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)

data class UserStudioStatisticRecord(
    val studio: StudioRecord?,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)

data class UserTagStatisticRecord(
    val tag: MediaTagRecord?,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)

data class UserVoiceActorStatisticRecord(
    val voiceActor: StaffRecord?,
    val chaptersRead: Int,
    val count: Int,
    val meanScore: Float,
    val mediaIds: List<Int>,
    val minutesWatched: Int,
)
