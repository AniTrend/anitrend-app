package com.mxt.anitrend.model.entity.base

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by Maxwell on 10/3/2016.
 * Media base entity
 */
open class MediaBase() :
    RecyclerItem(),
    Parcelable {
    var id: Long = 0
    var idMal: Long = 0
    var title: MediaTitle? = null
    var coverImage: ImageBase? = null
    var bannerImage: String? = null

    @KeyUtil.MediaType var type: String? = null

    @KeyUtil.MediaFormat var format: String? = null

    @KeyUtil.MediaSeason var season: String? = null

    @KeyUtil.MediaStatus var status: String? = null
    var siteUrl: String? = null
    var meanScore: Int = 0
    var averageScore: Int = 0
    var startDate: FuzzyDate? = null
    var endDate: FuzzyDate? = null
    var episodes: Int = 0
    var duration: Int = 0
    var chapters: Int = 0
    var volumes: Int = 0
    var isAdult: Boolean = false
    var isFavourite: Boolean = false
    var nextAiringEpisode: AiringSchedule? = null
    var mediaListEntry: MediaList? = null

    @Suppress("DEPRECATION")
    protected constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        idMal = parcel.readLong()
        title = parcel.readParcelable(MediaTitle::class.java.classLoader)
        coverImage = parcel.readParcelable(ImageBase::class.java.classLoader)
        bannerImage = parcel.readString()
        type = parcel.readString()
        season = parcel.readString()
        format = parcel.readString()
        status = parcel.readString()
        siteUrl = parcel.readString()
        meanScore = parcel.readInt()
        averageScore = parcel.readInt()
        startDate = parcel.readParcelable(FuzzyDate::class.java.classLoader)
        endDate = parcel.readParcelable(FuzzyDate::class.java.classLoader)
        episodes = parcel.readInt()
        duration = parcel.readInt()
        chapters = parcel.readInt()
        volumes = parcel.readInt()
        isAdult = parcel.readByte().toInt() != 0
        isFavourite = parcel.readByte().toInt() != 0
        nextAiringEpisode = parcel.readParcelable(AiringSchedule::class.java.classLoader)
        mediaListEntry = parcel.readParcelable(MediaList::class.java.classLoader)
    }

    fun toggleFavourite() {
        isFavourite = !isFavourite
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        dest.writeLong(id)
        dest.writeLong(idMal)
        dest.writeParcelable(title, flags)
        dest.writeParcelable(coverImage, flags)
        dest.writeString(bannerImage)
        dest.writeString(type)
        dest.writeString(season)
        dest.writeString(format)
        dest.writeString(status)
        dest.writeString(siteUrl)
        dest.writeInt(meanScore)
        dest.writeInt(averageScore)
        dest.writeParcelable(startDate, flags)
        dest.writeParcelable(endDate, flags)
        dest.writeInt(episodes)
        dest.writeInt(duration)
        dest.writeInt(chapters)
        dest.writeInt(volumes)
        dest.writeByte((if (isAdult) 1 else 0).toByte())
        dest.writeByte((if (isFavourite) 1 else 0).toByte())
        dest.writeParcelable(nextAiringEpisode, flags)
        dest.writeParcelable(mediaListEntry, flags)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (other is MediaBase) {
            return other.id == id
        }
        return super.equals(other)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MediaBase> =
            object : Parcelable.Creator<MediaBase> {
                override fun createFromParcel(parcel: Parcel): MediaBase = MediaBase(parcel)

                override fun newArray(size: Int): Array<MediaBase?> = arrayOfNulls(size)
            }
    }
}
