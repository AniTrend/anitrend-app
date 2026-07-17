package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2018/03/20.
 * MediaStats
 */
@Suppress("DEPRECATION")
@Parcelize
class MediaStats(
    var airingProgression: List<MediaTrend>? = null,
    var scoreDistribution: List<ScoreDistribution>? = null,
    var statusDistribution: List<StatusDistribution>? = null,
) : Parcelable
