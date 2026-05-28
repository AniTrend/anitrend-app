package com.mxt.anitrend.model.entity.base

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.KeyUtil

open class ThreadBase() : RecyclerItem(), Parcelable {

    var id: Long = 0

    @KeyUtil.NotificationType
    var type: String? = null

    protected constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        type = parcel.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(type)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (other is ThreadBase) {
            return other.id == id
        }
        return super.equals(other)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ThreadBase> = object : Parcelable.Creator<ThreadBase> {
            override fun createFromParcel(parcel: Parcel): ThreadBase = ThreadBase(parcel)

            override fun newArray(size: Int): Array<ThreadBase?> = arrayOfNulls(size)
        }
    }
}