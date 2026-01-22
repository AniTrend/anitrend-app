package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import com.mxt.anitrend.model.entity.anilist.UserStats
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2018/03/25.
 * GenreStats for userStats
 * @see UserStats
 */
@Deprecated("Deprecated in Java")
@Parcelize
class GenreStats(
    val genre: String?,
    val amount: Int,
    val meanScore: Int,
    /**
     * The amount of time in minutes the genre has been watched by the user
     */
    val timeWatched: Int
) : Parcelable
