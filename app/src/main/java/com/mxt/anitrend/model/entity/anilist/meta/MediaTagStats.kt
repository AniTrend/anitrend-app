package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import com.mxt.anitrend.model.entity.anilist.MediaTag
import kotlinx.parcelize.Parcelize

@Deprecated("Deprecated in Java")
@Parcelize
class MediaTagStats(
    var tag: MediaTag? = null,
    var amount: Int = 0,
    var meanScore: Int = 0,
    var timeWatched: Int = 0
) : Parcelable
