package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2018/03/24.
 */
@Parcelize
class ScoreDistribution(
    val score: Int,
    val amount: Int
) : Parcelable
