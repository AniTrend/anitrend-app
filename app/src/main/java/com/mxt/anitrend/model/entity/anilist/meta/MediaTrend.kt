package com.mxt.anitrend.model.entity.anilist.meta

import android.os.Parcelable
import com.mxt.anitrend.model.entity.base.MediaBase
import kotlinx.parcelize.Parcelize
import kotlin.jvm.JvmName

/**
 * Created by max on 2018/03/20.
 */
@Parcelize
class MediaTrend(
    var mediaId: Long = 0,
    var date: Long = 0,
    var trending: Int = 0,
    var averageScore: Int = 0,
    var popularity: Int = 0,
    @get:JvmName("isReleasing") var releasing: Boolean = false,
    var episode: Float = 0f,
    var media: MediaBase? = null,
) : Parcelable
