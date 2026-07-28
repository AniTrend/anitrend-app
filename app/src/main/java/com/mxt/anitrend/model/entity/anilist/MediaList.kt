package com.mxt.anitrend.model.entity.anilist

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mxt.anitrend.model.entity.anilist.meta.CustomList
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by Maxwell on 1/12/2017.
 */
class MediaList() :
    RecyclerItem(),
    Parcelable,
    Cloneable {
    var id: Long = 0
    var mediaId: Long = 0

    @KeyUtil.MediaListStatus var status: String? = null
    var score: Float = 0f
    var scoreRaw: Int? = null
    var progress: Int = 0
    var progressVolumes: Int = 0
    var repeat: Int = 0
    var priority: Int = 0
    var notes: String? = null

    @SerializedName("private")
    var isHidden: Boolean = false
    var isHiddenFromStatusLists: Boolean = false
    var advancedScores: Map<String, Float>? = null
    var customLists: List<CustomList>? = null
    var startedAt: FuzzyDate? = null
    var completedAt: FuzzyDate? = null
    var updatedAt: Long = 0
    var createdAt: Long = 0
    var media: MediaBase = MediaBase()

    @Suppress("DEPRECATION")
    protected constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        mediaId = parcel.readLong()
        status = parcel.readString()
        score = parcel.readFloat()
        scoreRaw = parcel.readValue(Int::class.java.classLoader) as? Int
        progress = parcel.readInt()
        progressVolumes = parcel.readInt()
        repeat = parcel.readInt()
        priority = parcel.readInt()
        notes = parcel.readString()
        isHidden = parcel.readByte().toInt() != 0
        isHiddenFromStatusLists = parcel.readByte().toInt() != 0
        @Suppress("UNCHECKED_CAST")
        val readCustomLists = parcel.readArrayList(CustomList::class.java.classLoader) as? ArrayList<CustomList>
        customLists = readCustomLists
        startedAt = parcel.readParcelable(FuzzyDate::class.java.classLoader)
        completedAt = parcel.readParcelable(FuzzyDate::class.java.classLoader)
        updatedAt = parcel.readLong()
        createdAt = parcel.readLong()
        media = parcel.readParcelable(MediaBase::class.java.classLoader) ?: MediaBase()
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        dest.writeLong(id)
        dest.writeLong(mediaId)
        dest.writeString(status)
        dest.writeFloat(score)
        dest.writeValue(scoreRaw)
        dest.writeInt(progress)
        dest.writeInt(progressVolumes)
        dest.writeInt(repeat)
        dest.writeInt(priority)
        dest.writeString(notes)
        dest.writeByte((if (isHidden) 1 else 0).toByte())
        dest.writeByte((if (isHiddenFromStatusLists) 1 else 0).toByte())
        dest.writeTypedList(customLists)
        dest.writeParcelable(startedAt, flags)
        dest.writeParcelable(completedAt, flags)
        dest.writeLong(updatedAt)
        dest.writeLong(createdAt)
        dest.writeParcelable(media, flags)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean = when (other) {
        is MediaList -> other.id == id && other.mediaId == mediaId
        is MediaBase -> other.id == mediaId
        else -> super.equals(other)
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): MediaList {
        super.clone()
        return this
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MediaList> =
            object : Parcelable.Creator<MediaList> {
                override fun createFromParcel(parcel: Parcel): MediaList = MediaList(parcel)

                override fun newArray(size: Int): Array<MediaList?> = arrayOfNulls(size)
            }
    }
}
