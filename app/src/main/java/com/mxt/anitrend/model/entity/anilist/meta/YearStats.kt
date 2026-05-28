package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Deprecated("Deprecated in Java")
@Parcelize
class YearStats(
    var year: Int = 0,
    var amount: Int = 0,
    var meanScore: Int = 0
) : Parcelable
