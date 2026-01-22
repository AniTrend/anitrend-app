package com.mxt.anitrend.model.entity.giphy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2017/12/09.
 * gif image attributes
 */
@Parcelize
class Gif(
    val url: String? = null,
    val width: String? = null,
    val height: String? = null,
    val size: Long = 0L
) : Parcelable
