package com.mxt.anitrend.model.entity.anilist

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.data.converter.MediaListOptionsConverter
import com.mxt.anitrend.data.converter.UserOptionsConverter
import com.mxt.anitrend.data.converter.UserStatisticTypesConverter
import com.mxt.anitrend.data.converter.UserStatsConverter
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions
import com.mxt.anitrend.model.entity.anilist.meta.UserOptions
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import com.mxt.anitrend.model.entity.base.UserBase
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity

/**
 * Created by Maxwell on 11/12/2016.
 * User
 */
@Entity
class User() : UserBase(), Parcelable {

    var about: String? = null

    @field:Convert(converter = UserOptionsConverter::class, dbType = String::class)
    var options: UserOptions? = null

    @field:Convert(converter = MediaListOptionsConverter::class, dbType = String::class)
    var mediaListOptions: MediaListOptions = MediaListOptions()

    @Deprecated("Deprecated in Java")
    @field:Convert(converter = UserStatsConverter::class, dbType = String::class)
    var stats: UserStats? = null

    @field:Convert(converter = UserStatisticTypesConverter::class, dbType = String::class)
    var statistics: UserStatisticTypes? = null

    var unreadNotificationCount: Int = 0

    protected constructor(parcel: Parcel) : this() {
        about = parcel.readString()
        options = parcel.readParcelable(UserOptions::class.java.classLoader)
        mediaListOptions = parcel.readParcelable(MediaListOptions::class.java.classLoader) ?: MediaListOptions()
        stats = parcel.readParcelable(UserStats::class.java.classLoader)
        unreadNotificationCount = parcel.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeString(about)
        dest.writeParcelable(options, flags)
        dest.writeParcelable(mediaListOptions, flags)
        dest.writeParcelable(stats, flags)
        dest.writeInt(unreadNotificationCount)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(parcel: Parcel): User = User(parcel)

            override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
        }
    }
}