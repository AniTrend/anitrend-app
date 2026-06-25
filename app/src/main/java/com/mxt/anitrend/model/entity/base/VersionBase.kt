package com.mxt.anitrend.model.entity.base

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.BuildConfig
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 * Created by max on 2017/10/22.
 * Version model from github
 */
@Entity
class VersionBase() : Parcelable {
    @Id(assignable = true)
    var code: Long = 0
    var lastChecked: Long = 0
    var migration: Boolean = false
    var releaseNotes: String? = null
    var version: String? = null
    var appId: String? = null

    constructor(code: Int, version: String?) : this() {
        this.code = code.toLong()
        this.version = version
    }

    protected constructor(parcel: Parcel) : this() {
        code = parcel.readLong()
        lastChecked = parcel.readLong()
        migration = parcel.readByte().toInt() != 0
        releaseNotes = parcel.readString()
        version = parcel.readString()
        appId = parcel.readString()
    }

    fun isNewerVersion(): Boolean = code > BuildConfig.versionCode

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        dest.writeLong(code)
        dest.writeLong(lastChecked)
        dest.writeByte((if (migration) 1 else 0).toByte())
        dest.writeString(releaseNotes)
        dest.writeString(version)
        dest.writeString(appId)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VersionBase> =
            object : Parcelable.Creator<VersionBase> {
                override fun createFromParcel(parcel: Parcel): VersionBase = VersionBase(parcel)

                override fun newArray(size: Int): Array<VersionBase?> = arrayOfNulls(size)
            }
    }
}
