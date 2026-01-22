package com.mxt.anitrend.model.entity.base

import android.os.Parcel
import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 * Created by max on 2017/11/01.
 * Authenticated user
 */
@Entity
class AuthBase() : Parcelable {

    @Id
    var id: Long = 0
    var code: String? = null
    var refresh_code: String? = null

    constructor(code: String?, refresh_code: String?) : this() {
        this.code = code
        this.refresh_code = refresh_code
    }

    protected constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        code = parcel.readString()
        refresh_code = parcel.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(code)
        dest.writeString(refresh_code)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AuthBase> = object : Parcelable.Creator<AuthBase> {
            override fun createFromParcel(parcel: Parcel): AuthBase = AuthBase(parcel)

            override fun newArray(size: Int): Array<AuthBase?> = arrayOfNulls(size)
        }
    }
}