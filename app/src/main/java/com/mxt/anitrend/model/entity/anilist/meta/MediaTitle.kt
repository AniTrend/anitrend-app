package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2018/03/20.
 */
@Parcelize
class MediaTitle(
    @SerializedName("romaji")
    private val romajiRaw: String?,
    @SerializedName("english")
    private val englishRaw: String?,
    @SerializedName("native")
    private val originalRaw: String?,
    @SerializedName("userPreferred")
    private val userPreferredRaw: String?
) : Parcelable {

    val romaji: String?
        get() = romajiRaw ?: userPreferredRaw

    val english: String?
        get() = englishRaw ?: userPreferredRaw

    val original: String?
        get() = originalRaw ?: userPreferredRaw

    val userPreferred: String?
        get() = userPreferredRaw
}
