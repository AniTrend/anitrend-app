/*
 * Copyright (C) 2019  AniTrend
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mxt.anitrend.model.entity.anilist.user

import com.mxt.anitrend.model.entity.anilist.user.statistics.*

/** [UserStatistics](https://anilist.github.io/ApiV2-GraphQL-Docs/UserStatistics.doc.html)
 *
 * @param chaptersRead TBA
 * @param count TBA
 * @param countries TBA
 * @param episodesWatched TBA
 * @param formats TBA
 * @param genres TBA
 * @param lengths TBA
 * @param meanScore TBA
 * @param minutesWatched TBA
 * @param releaseYears TBA
 * @param scores TBA
 * @param staff TBA
 * @param standardDeviation TBA
 * @param startYears TBA
 * @param statuses TBA
 * @param studios TBA
 * @param tags TBA
 * @param voiceActors TBA
 * @param volumesRead TBA
 */
data class UserStatistics(
        val chaptersRead: Int,
        val count: Int,
        val countries: List<UserCountryStatistic>?,
        val episodesWatched: Int,
        val formats: List<UserFormatStatistic>?,
        val genres: List<UserGenreStatistic>?,
        val lengths: List<UserLengthStatistic>?,
        val meanScore: Float,
        val minutesWatched: Int,
        val releaseYears: List<UserReleaseYearStatistic>?,
        val scores: List<UserScoreStatistic>?,
        val staff: List<UserStaffStatistic>?,
        val standardDeviation: Float,
        val startYears: List<UserStartYearStatistic>?,
        val statuses: List<UserStatusStatistic>?,
        val studios: List<UserStudioStatistic>?,
        val tags: List<UserTagStatistic>?,
        val voiceActors: List<UserVoiceActorStatistic>?,
        val volumesRead: Int
)