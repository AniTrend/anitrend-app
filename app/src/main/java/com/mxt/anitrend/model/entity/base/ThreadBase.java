package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.group.RecyclerItem;
import com.mxt.anitrend.util.KeyUtil;

public class ThreadBase extends RecyclerItem implements Parcelable {

    private long id;
    private @KeyUtil.NotificationType String type;

    protected ThreadBase(Parcel in) {
        id = in.readLong();
        type = in.readString();
    }

    public static final Creator<ThreadBase> CREATOR = new Creator<ThreadBase>() {
        @Override
        public ThreadBase createFromParcel(Parcel in) {
            return new ThreadBase(in);
        }

        @Override
        public ThreadBase[] newArray(int size) {
            return new ThreadBase[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(type);
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ThreadBase)
            return ((ThreadBase) obj).id == id;
        return super.equals(obj);
    }
}
