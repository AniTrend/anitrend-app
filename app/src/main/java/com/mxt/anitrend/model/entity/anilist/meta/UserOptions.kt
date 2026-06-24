package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mxt.anitrend.util.KeyUtil
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2018/03/20.
 */
@Parcelize
class UserOptions(
    @param:KeyUtil.UserLanguageTitle
    val titleLanguage: String?,
    @SerializedName("displayAdultContent")
    val isDisplayAdultContent: Boolean,
    @SerializedName("airingNotifications")
    val isAiringNotifications: Boolean,
    @param:KeyUtil.ProfileColor
    val profileColor: String?,
) : Parcelable
