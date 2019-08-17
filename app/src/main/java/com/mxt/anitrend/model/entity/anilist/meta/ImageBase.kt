package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by max on 2018/03/20.
 */
@Parcelize
class ImageBase(
        val extraLarge: String?,
        val large: String?,
        val medium: String?
) : Parcelable
