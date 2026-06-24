package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import com.mxt.anitrend.util.KeyUtil
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2018/03/24.
 * StatusDistribution for media and userStats
 * @see com.mxt.anitrend.model.entity.anilist.UserStats
 */
@Deprecated("Deprecated in Java")
@Parcelize
class StatusDistribution(
    @param:KeyUtil.MediaListStatus var status: String? = null,
    var amount: Int = 0,
) : Parcelable
