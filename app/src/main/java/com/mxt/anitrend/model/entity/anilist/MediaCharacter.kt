package com.mxt.anitrend.model.entity.anilist

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.model.entity.base.CharacterBase

/**
 * Created by Maxwell on 10/4/2016.
 */
class MediaCharacter : CharacterBase {
    var description: String? = null
        private set

    protected constructor(parcel: Parcel) : super(parcel) {
        description = parcel.readString()
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        super.writeToParcel(dest, flags)
        dest.writeString(description)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MediaCharacter> {
        override fun createFromParcel(parcel: Parcel): MediaCharacter = MediaCharacter(parcel)

        override fun newArray(size: Int): Array<MediaCharacter?> = arrayOfNulls(size)
    }
}
