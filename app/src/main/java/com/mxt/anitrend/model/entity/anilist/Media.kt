package com.mxt.anitrend.model.entity.anilist

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.model.entity.anilist.meta.MediaStats
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrailer
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by Maxwell on 10/2/2016.
 * Media extension
 */
class Media() :
    MediaBase(),
    Parcelable {
    var description: String? = null
    var synonyms: List<String>? = null
    var genres: List<String>? = null
    var tags: List<MediaTag>? = null
    var trailer: MediaTrailer? = null
    var hashTag: String? = null

    @KeyUtil.MediaSource var source: String? = null
    var externalLinks: List<ExternalLink>? = null
    var studios: ConnectionContainer<List<StudioBase>>? = null
    var stats: MediaStats? = null
    var rankings: List<MediaRank>? = null

    @Suppress("DEPRECATION")
    protected constructor(parcel: Parcel) : this() {
        description = parcel.readString()
        synonyms = parcel.createStringArrayList()
        genres = parcel.createStringArrayList()
        @Suppress("UNCHECKED_CAST")
        val tagsList = parcel.readArrayList(MediaTag::class.java.classLoader) as? ArrayList<MediaTag>
        tags = tagsList
        trailer = parcel.readParcelable(MediaTrailer::class.java.classLoader)
        hashTag = parcel.readString()
        source = parcel.readString()
        @Suppress("UNCHECKED_CAST")
        val externalLinksList = parcel.readArrayList(ExternalLink::class.java.classLoader) as? ArrayList<ExternalLink>
        externalLinks = externalLinksList
        stats = parcel.readParcelable(MediaStats::class.java.classLoader)
        @Suppress("UNCHECKED_CAST")
        val rankingsList = parcel.readArrayList(MediaRank::class.java.classLoader) as? ArrayList<MediaRank>
        rankings = rankingsList
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        super.writeToParcel(dest, flags)
        dest.writeString(description)
        dest.writeStringList(synonyms)
        dest.writeStringList(genres)
        dest.writeTypedList(tags)
        dest.writeParcelable(trailer, flags)
        dest.writeString(hashTag)
        dest.writeString(source)
        dest.writeTypedList(externalLinks)
        dest.writeParcelable(stats, flags)
        dest.writeTypedList(rankings)
    }

    override fun describeContents(): Int = 0

    val tagsNoSpoilers: List<MediaTag>
        get() = tags.orEmpty().filterNot { it.isMediaSpoiler }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Media> =
            object : Parcelable.Creator<Media> {
                override fun createFromParcel(parcel: Parcel): Media = Media(parcel)

                override fun newArray(size: Int): Array<Media?> = arrayOfNulls(size)
            }
    }
}
