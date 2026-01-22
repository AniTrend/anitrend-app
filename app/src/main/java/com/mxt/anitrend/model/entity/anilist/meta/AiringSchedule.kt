package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Maxwell on 10/16/2016.
 */
@Parcelize
class AiringSchedule(
    var airingAt: Long = 0,
    var timeUntilAiring: Long = 0,
    var episode: Int = 0
) : Parcelable
