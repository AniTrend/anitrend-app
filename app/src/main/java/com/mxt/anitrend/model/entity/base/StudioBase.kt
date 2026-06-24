package com.mxt.anitrend.model.entity.base

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by max on 11/12/2016.
 */
open class StudioBase() : Parcelable {
    var id: Long = 0
    var name: String? = null
    var siteUrl: String? = null
    var isFavourite: Boolean = false

    protected constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        name = parcel.readString()
        siteUrl = parcel.readString()
        isFavourite = parcel.readByte().toInt() != 0
    }

    fun toggleFavourite() {
        isFavourite = !isFavourite
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        dest.writeLong(id)
        dest.writeString(name)
        dest.writeString(siteUrl)
        dest.writeByte((if (isFavourite) 1 else 0).toByte())
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (other is StudioBase) {
            return other.id == id
        }
        return super.equals(other)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StudioBase> =
            object : Parcelable.Creator<StudioBase> {
                override fun createFromParcel(parcel: Parcel): StudioBase = StudioBase(parcel)

                override fun newArray(size: Int): Array<StudioBase?> = arrayOfNulls(size)
            }
    }
}
