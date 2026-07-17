package com.mxt.anitrend.model.entity.base

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.group.RecyclerItem

/**
 * Created by Maxwell on 10/4/2016.
 */
open class StaffBase() :
    RecyclerItem(),
    Parcelable {
    var id: Long = 0
    var name: TitleBase? = null
    var image: ImageBase? = null
    var isFavourite: Boolean = false
    var description: String? = null
    var language: String? = null
    var siteUrl: String? = null

    @Suppress("DEPRECATION")
    protected constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        name = parcel.readParcelable(TitleBase::class.java.classLoader)
        image = parcel.readParcelable(ImageBase::class.java.classLoader)
        isFavourite = parcel.readByte().toInt() != 0
        description = parcel.readString()
        language = parcel.readString()
        siteUrl = parcel.readString()
    }

    fun toggleFavourite() {
        isFavourite = !isFavourite
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        dest.writeLong(id)
        dest.writeParcelable(name, flags)
        dest.writeParcelable(image, flags)
        dest.writeByte((if (isFavourite) 1 else 0).toByte())
        dest.writeString(description)
        dest.writeString(language)
        dest.writeString(siteUrl)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (other is StaffBase) {
            return other.id == id
        }
        return super.equals(other)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StaffBase> =
            object : Parcelable.Creator<StaffBase> {
                override fun createFromParcel(parcel: Parcel): StaffBase = StaffBase(parcel)

                override fun newArray(size: Int): Array<StaffBase?> = arrayOfNulls(size)
            }
    }
}
