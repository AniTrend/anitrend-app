package com.mxt.anitrend.model.entity.anilist

import android.os.Parcel
import android.os.Parcelable
import com.mxt.anitrend.model.entity.base.MediaListCollectionBase

class MediaListCollection : MediaListCollectionBase {
    var entries: List<MediaList>? = null
        private set

    protected constructor(parcel: Parcel) : super(parcel) {
        entries = parcel.createTypedArrayList(MediaList.CREATOR)
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        super.writeToParcel(dest, flags)
        dest.writeTypedList(entries)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MediaListCollection> {
        override fun createFromParcel(parcel: Parcel): MediaListCollection = MediaListCollection(parcel)

        override fun newArray(size: Int): Array<MediaListCollection?> = arrayOfNulls(size)
    }
}
