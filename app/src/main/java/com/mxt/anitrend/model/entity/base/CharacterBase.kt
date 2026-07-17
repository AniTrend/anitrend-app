package com.mxt.anitrend.model.entity.base

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.group.RecyclerItem

/**
 * Created by Maxwell on 10/4/2016.
 */
open class CharacterBase() :
    RecyclerItem(),
    Parcelable {
    var id: Long = 0
    var name: TitleBase? = null
    var image: ImageBase? = null
    var isFavourite: Boolean = false
    var siteUrl: String? = null

    @Suppress("DEPRECATION")
    protected constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        name = parcel.readParcelable(TitleBase::class.java.classLoader)
        image = parcel.readParcelable(ImageBase::class.java.classLoader)
        isFavourite = parcel.readByte().toInt() != 0
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
        dest.writeString(siteUrl)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (other is CharacterBase) {
            return other.id == id
        }
        return super.equals(other)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CharacterBase> =
            object : Parcelable.Creator<CharacterBase> {
                override fun createFromParcel(parcel: Parcel): CharacterBase = CharacterBase(parcel)

                override fun newArray(size: Int): Array<CharacterBase?> = arrayOfNulls(size)
            }
    }
}
