package com.mxt.anitrend.model.entity.anilist

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.NotificationBase
import com.mxt.anitrend.model.entity.base.ThreadBase
import com.mxt.anitrend.model.entity.base.UserBase

/**
 * Created by max on 2018/03/25.
 * Notification
 */
class Notification() : NotificationBase(), Parcelable {

    var activityId: Long = 0
    var commentId: Long = 0
    var user: UserBase = UserBase()
    var episode: Int = 0
    var contexts: List<String> = emptyList()
    var media: MediaBase? = null
    var thread: ThreadBase = ThreadBase()
    var reason: String? = null
    var deletedMediaTitle: String? = null
    var deletedMediaTitles: List<String> = emptyList()

    protected constructor(parcel: Parcel) : this() {
        activityId = parcel.readLong()
        commentId = parcel.readLong()
        user = parcel.readParcelable(UserBase::class.java.classLoader) ?: UserBase()
        episode = parcel.readInt()
        contexts = parcel.createStringArrayList().orEmpty()
        media = parcel.readParcelable(MediaBase::class.java.classLoader)
        thread = parcel.readParcelable(ThreadBase::class.java.classLoader) ?: ThreadBase()
        reason = parcel.readString()
        deletedMediaTitle = parcel.readString()
        deletedMediaTitles = parcel.createStringArrayList().orEmpty()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeLong(activityId)
        dest.writeLong(commentId)
        dest.writeParcelable(user, flags)
        dest.writeInt(episode)
        dest.writeStringList(contexts)
        dest.writeParcelable(media, flags)
        dest.writeParcelable(thread, flags)
        dest.writeString(reason)
        dest.writeString(deletedMediaTitle)
        dest.writeStringList(deletedMediaTitles)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Notification> = object : Parcelable.Creator<Notification> {
            override fun createFromParcel(parcel: Parcel): Notification = Notification(parcel)

            override fun newArray(size: Int): Array<Notification?> = arrayOfNulls(size)
        }
    }
}