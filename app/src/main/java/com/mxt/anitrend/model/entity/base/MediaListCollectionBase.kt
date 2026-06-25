package com.mxt.anitrend.model.entity.base

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.util.KeyUtil

open class MediaListCollectionBase() : Parcelable {
    var name: String? = null
    var isCustomList: Boolean = false
    var isSplitCompletedList: Boolean = false

    @KeyUtil.MediaListStatus
    var status: String? = null

    protected constructor(parcel: Parcel) : this() {
        name = parcel.readString()
        isCustomList = parcel.readByte().toInt() != 0
        isSplitCompletedList = parcel.readByte().toInt() != 0
        status = parcel.readString()
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        dest.writeString(name)
        dest.writeByte((if (isCustomList) 1 else 0).toByte())
        dest.writeByte((if (isSplitCompletedList) 1 else 0).toByte())
        dest.writeString(status)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MediaListCollectionBase> =
            object : Parcelable.Creator<MediaListCollectionBase> {
                override fun createFromParcel(parcel: Parcel): MediaListCollectionBase = MediaListCollectionBase(parcel)

                override fun newArray(size: Int): Array<MediaListCollectionBase?> = arrayOfNulls(size)
            }
    }
}
