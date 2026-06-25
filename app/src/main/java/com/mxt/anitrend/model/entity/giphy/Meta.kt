package com.mxt.anitrend.model.entity.giphy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2017/12/09.
 * giphy request meta data
 */
@Parcelize
class Meta(
    val status: Int = 0,
    val msg: String? = null,
    val response_id: String? = null,
) : Parcelable
