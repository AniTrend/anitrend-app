package com.mxt.anitrend.model.entity.crunchy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Root

/**
 * Created by max on 2/9/2017.
 */
@Root(name = "thumbnail", strict = false)
@Parcelize
class Thumbnail(
    @field:Attribute(name = "url")
    var url: String? = null,
    @field:Attribute(name = "width")
    var width: Int = 0,
    @field:Attribute(name = "height")
    var height: Int = 0
) : Parcelable
