package com.mxt.anitrend.model.entity.anilist

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by Max on 10/4/2016.
 */
class Review() : Parcelable {
    var id: Long = 0
    var summary: String? = null

    @KeyUtil.MediaType var mediaType: String? = null
    var body: String? = null
    var rating: Int = 0
    var ratingAmount: Int = 0

    @KeyUtil.ReviewRating var userRating: String? = null
    var score: Int = 0

    @SerializedName("private")
    var isPrivate: Boolean = false
    var createdAt: Long = 0
    var user: UserBase = UserBase()
    var media: MediaBase = MediaBase()

    @Suppress("DEPRECATION")
    protected constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        summary = parcel.readString()
        mediaType = parcel.readString()
        body = parcel.readString()
        rating = parcel.readInt()
        ratingAmount = parcel.readInt()
        userRating = parcel.readString()
        score = parcel.readInt()
        isPrivate = parcel.readByte().toInt() != 0
        createdAt = parcel.readLong()
        user = parcel.readParcelable(UserBase::class.java.classLoader) ?: UserBase()
        media = parcel.readParcelable(MediaBase::class.java.classLoader) ?: MediaBase()
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        dest.writeLong(id)
        dest.writeString(summary)
        dest.writeString(mediaType)
        dest.writeString(body)
        dest.writeInt(rating)
        dest.writeInt(ratingAmount)
        dest.writeString(userRating)
        dest.writeInt(score)
        dest.writeByte((if (isPrivate) 1 else 0).toByte())
        dest.writeLong(createdAt)
        dest.writeParcelable(user, flags)
        dest.writeParcelable(media, flags)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Review> =
            object : Parcelable.Creator<Review> {
                override fun createFromParcel(parcel: Parcel): Review = Review(parcel)

                override fun newArray(size: Int): Array<Review?> = arrayOfNulls(size)
            }
    }
}
