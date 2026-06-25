package com.mxt.anitrend.model.entity.base

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2018/02/24.
 * Notification base meta data class
 */
open class NotificationBase() :
    RecyclerItem(),
    Parcelable {
    var id: Long = 0

    @KeyUtil.NotificationType
    var type: String? = null

    var createdAt: Long = 0

    var context: String = ""
        get() = field.trim()

    protected constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        type = parcel.readString()
        createdAt = parcel.readLong()
        context = parcel.readString().orEmpty()
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        dest.writeLong(id)
        dest.writeString(type)
        dest.writeLong(createdAt)
        dest.writeString(context)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (other is NotificationBase) {
            return other.id == id
        }
        return super.equals(other)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<NotificationBase> =
            object : Parcelable.Creator<NotificationBase> {
                override fun createFromParcel(parcel: Parcel): NotificationBase = NotificationBase(parcel)

                override fun newArray(size: Int): Array<NotificationBase?> = arrayOfNulls(size)
            }
    }
}
