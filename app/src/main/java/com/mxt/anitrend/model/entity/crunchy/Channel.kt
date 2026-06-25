package com.mxt.anitrend.model.entity.crunchy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Path
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

/**
 * Created by max on 2/9/2017.
 */
@Root(name = "channel", strict = false)
@Parcelize
class Channel(
    @field:Element(name = "title", required = false)
    var title: String? = null,
    @field:Text(required = false)
    @field:Path("link")
    var link: String? = null,
    @field:Element(name = "description", required = false)
    var description: String? = null,
    @field:Element(name = "copyright", required = false)
    var copyright: String? = null,
    @field:ElementList(name = "episode", inline = true, required = false)
    var episode: List<Episode> = mutableListOf(),
) : Parcelable
