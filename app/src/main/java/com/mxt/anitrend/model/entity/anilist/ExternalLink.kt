package com.mxt.anitrend.model.entity.anilist

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Created by Maxwell on 10/4/2016.
 */
@Parcelize
class ExternalLink @JvmOverloads constructor(
    var url: String? = null,
    var site: String? = null
) : Parcelable {

    @IgnoredOnParcel
    var id: Int = 0
}
