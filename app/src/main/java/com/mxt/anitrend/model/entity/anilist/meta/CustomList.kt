package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class CustomList(
    val name: String?,
    @SerializedName("enabled")
    val isEnabled: Boolean,
) : Parcelable
