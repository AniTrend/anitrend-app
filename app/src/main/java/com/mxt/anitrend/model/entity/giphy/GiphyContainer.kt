package com.mxt.anitrend.model.entity.giphy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2017/12/09.
 * container for giphy api responses
 */
@Parcelize
class GiphyContainer(
    val data: List<Giphy> = emptyList(),
    val pagination: Pagination? = null,
    val meta: Meta? = null
) : Parcelable
