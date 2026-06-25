package com.mxt.anitrend.model.entity.anilist

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.model.entity.base.ThreadBase

class Thread : ThreadBase {
    constructor() : super()

    private constructor(parcel: Parcel) : super(parcel)

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Thread> =
            object : Parcelable.Creator<Thread> {
                override fun createFromParcel(parcel: Parcel): Thread = Thread(parcel)

                override fun newArray(size: Int): Array<Thread?> = arrayOfNulls(size)
            }
    }
}
