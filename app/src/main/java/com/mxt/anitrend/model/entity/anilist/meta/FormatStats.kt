package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Deprecated("Deprecated in Java")
@Parcelize
class FormatStats(
    val format: String?,
    val amount: Int
) : Parcelable
