package com.mxt.anitrend.model.entity.container.attribute

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class PageInfo(
    var total: Int = 0,
    var perPage: Int = 0,
    var currentPage: Int = 0,
    @SerializedName("hasNextPage")
    private var hasNextPageValue: Boolean = false
) : Parcelable {

    fun hasNextPage(): Boolean = hasNextPageValue

    fun setHasNextPage(value: Boolean) {
        hasNextPageValue = value
    }
}
