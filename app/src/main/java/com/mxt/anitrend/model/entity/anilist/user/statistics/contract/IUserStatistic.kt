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

package com.mxt.anitrend.model.entity.anilist.user.statistics.contract

/**
 * Contract for user statistics, applied on various items such as countries, formats, genres, years, staff e.t.c
 *
 * @property chaptersRead Chapters read for the statistic
 * @property count Count for the statistic
 * @property meanScore Mean score for the the statistic
 * @property mediaIds List of media ids for the statistic
 * @property minutesWatched Minutes for watched for teh statistic
 */
interface IUserStatistic {
    val chaptersRead: Int
    val count: Int
    val meanScore: Float
    val mediaIds: List<Int>
    val minutesWatched: Int
}