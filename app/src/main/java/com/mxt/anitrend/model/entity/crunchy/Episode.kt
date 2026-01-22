package com.mxt.anitrend.model.entity.crunchy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * Created by max on 2/9/2017.
 */
@Root(name = "item", strict = false)
@Parcelize
class Episode(
    @field:Element(name = "title")
    var title: String = "",
    @field:Element(name = "link")
    var link: String? = null,
    @field:Element(name = "description", required = false)
    @get:JvmName("getRawDescription")
    @set:JvmName("setRawDescription")
    var description: String = "",
    @field:Element(name = "publisher", required = false)
    var publisher: String? = null,
    @field:Element(name = "content", required = false)
    var content: MediaContent? = null,
    @field:ElementList(name = "thumbnail", inline = true, required = false)
    var thumbnail: List<Thumbnail> = mutableListOf()
) : Parcelable {

    fun getDescription(): String {
        val safeDescription = description
            .replace("(<img[^>]*>)".toRegex(), "")
            .replaceFirst("(<br[^>]*>)".toRegex(), "")
        return if (safeDescription.isEmpty()) {
            "$title has no summary information at the moment."
        } else {
            safeDescription
        }
    }
}
