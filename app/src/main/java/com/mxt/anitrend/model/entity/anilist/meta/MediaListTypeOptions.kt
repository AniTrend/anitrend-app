package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2018/03/22.
 * MediaListTypeOptions for media types
 */
@Parcelize
class MediaListTypeOptions(
    val sectionOrder: List<String>?,
    @SerializedName("splitCompletedSectionByFormat")
    val isSplitCompletedSectionByFormat: Boolean,
    val customLists: List<String>?,
    val advancedScoring: List<String>?,
    @SerializedName("advancedScoringEnabled")
    val isAdvancedScoringEnabled: Boolean
) : Parcelable
