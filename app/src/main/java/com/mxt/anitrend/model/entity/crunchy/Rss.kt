package com.mxt.anitrend.model.entity.crunchy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

/**
 * Created by max on 2017/02/07.
 */
@Root(name = "rss", strict = false)
@Parcelize
class Rss(
    @field:Element(name = "channel")
    var channel: Channel? = null
) : Parcelable
