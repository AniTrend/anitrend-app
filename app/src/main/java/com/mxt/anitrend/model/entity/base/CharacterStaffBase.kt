package com.mxt.anitrend.model.entity.base

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.model.entity.group.RecyclerItem

/**
 * Created by LuK1337 on 2021/05/05.
 */
open class CharacterStaffBase() :
    RecyclerItem(),
    Parcelable {
    lateinit var character: CharacterBase
    lateinit var media: MediaBase

    constructor(character: CharacterBase, media: MediaBase) : this() {
        this.character = character
        this.media = media
    }

    protected constructor(parcel: Parcel) : this() {
        character = parcel.readParcelable(CharacterBase::class.java.classLoader) ?: CharacterBase()
        media = parcel.readParcelable(MediaBase::class.java.classLoader) ?: MediaBase()
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        dest.writeParcelable(character, flags)
        dest.writeParcelable(media, flags)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (other is CharacterStaffBase) {
            return other.character.id == character.id
        }
        return super.equals(other)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CharacterStaffBase> =
            object : Parcelable.Creator<CharacterStaffBase> {
                override fun createFromParcel(parcel: Parcel): CharacterStaffBase = CharacterStaffBase(parcel)

                override fun newArray(size: Int): Array<CharacterStaffBase?> = arrayOfNulls(size)
            }
    }
}
