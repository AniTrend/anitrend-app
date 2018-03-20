package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by max on 11/12/2016.
 */

public class StudioBase implements Parcelable {

    private long id;
    private String name;
    private String siteUrl;
    private boolean isFavourite;

    protected StudioBase(Parcel in) {
        id = in.readLong();
        name = in.readString();
        siteUrl = in.readString();
        isFavourite = in.readByte() != 0;
    }

    public static final Creator<StudioBase> CREATOR = new Creator<StudioBase>() {
        @Override
        public StudioBase createFromParcel(Parcel in) {
            return new StudioBase(in);
        }

        @Override
        public StudioBase[] newArray(int size) {
            return new StudioBase[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        this.isFavourite = favourite;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof StudioBase)
            return ((StudioBase) obj).getId() == id;
        return super.equals(obj);
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     * @see #CONTENTS_FILE_DESCRIPTOR
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
        dest.writeString(name);
        dest.writeString(siteUrl);
        dest.writeByte((byte) (isFavourite ? 1 : 0));
    }
}
