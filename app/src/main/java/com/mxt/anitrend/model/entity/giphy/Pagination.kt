package com.mxt.anitrend.model.entity.giphy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2017/12/09.
 * giphy pagination data
 */
@Parcelize
class Pagination(
    val total_count: Int = 0,
    val count: Int = 0,
    val offset: Int = 0
) : Parcelable
