package com.mxt.anitrend.model.entity.giphy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by max on 2017/12/09.
 * giphy image properties
 */
@Parcelize
class Giphy(
    val id: String? = null,
    val url: String? = null,
    val title: String? = null,
    val images: HashMap<String, Gif> = hashMapOf(),
) : Parcelable
