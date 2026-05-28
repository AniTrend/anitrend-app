package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Locale
import kotlin.math.max

/**
 * Created by max on 2018/03/20.
 */
@Parcelize
class FuzzyDate(
    var day: Int,
    var month: Int,
    var year: Int
) : Parcelable {

    val isValidDate: Boolean
        get() = day != 0 || month != 0 || year != 0

    fun setDate(day: Int, month: Int, year: Int) {
        this.day = day
        this.month = month
        this.year = year
    }

    override fun toString(): String {
        return String.format(
            Locale.getDefault(),
            "%d/%d/%d",
            max(100, year),
            max(1, month),
            max(1, day)
        )
    }
}
