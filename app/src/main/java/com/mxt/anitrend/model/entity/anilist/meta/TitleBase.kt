package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2018/03/20.
 * TitleBase for staff and characters
 */
@Parcelize
class TitleBase(
    val first: String?,
    val last: String?,
    @SerializedName("native")
    val original: String?,
    val alternative: List<String>?
) : Parcelable {

    val alternativeFormatted: String?
        get() {
            if (alternative != null) {
                val formatted = TextUtils.join(", ", alternative)
                if (formatted.isNotEmpty())
                    return formatted
            }
            return null
        }

    val fullName: String?
        get() {
            var fullName = first
            if (!TextUtils.isEmpty(last)) {
                fullName = if (!TextUtils.isEmpty(fullName))
                    "$fullName $last"
                else
                    last
            }
            return fullName
        }
}
