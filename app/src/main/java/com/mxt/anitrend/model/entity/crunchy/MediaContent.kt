package com.mxt.anitrend.model.entity.crunchy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Root

/**
 * Created by max on 2/9/2017.
 * <media:content url="https://www.crunchyroll.com/syndication/video?type=media&amp;id=727595" type="video/mp4" medium="video" duration="1421"/>
 */
@Root(name = "content", strict = false)
@Parcelize
class MediaContent(
    @field:Attribute(name = "url", required = false)
    var url: String? = null,
    @field:Attribute(name = "type")
    var type: String? = null,
    @field:Attribute(name = "medium", required = false)
    var medium: String? = null,
    @field:Attribute(name = "duration", required = false)
    var duration: String? = null,
) : Parcelable
