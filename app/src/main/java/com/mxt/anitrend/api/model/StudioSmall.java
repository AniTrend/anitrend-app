package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Maxwell on 11/12/2016.
 */

public class StudioSmall implements Parcelable{

    private int id;
    private String studio_name;
    private String studio_wiki;
    private int main_studio;
    private boolean favourite;


    protected StudioSmall(Parcel in) {
        id = in.readInt();
        studio_name = in.readString();
        studio_wiki = in.readString();
        main_studio = in.readInt();
        favourite = in.readByte() != 0;
    }

    public static final Creator<StudioSmall> CREATOR = new Creator<StudioSmall>() {
        @Override
        public StudioSmall createFromParcel(Parcel in) {
            return new StudioSmall(in);
        }

        @Override
        public StudioSmall[] newArray(int size) {
            return new StudioSmall[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getStudio_name() {
        return studio_name;
    }

    public String getStudio_wiki() {
        return studio_wiki;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public int getMain_studio() {
        return main_studio;
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
        dest.writeInt(id);
        dest.writeString(studio_name);
        dest.writeString(studio_wiki);
        dest.writeInt(main_studio);
        dest.writeByte((byte) (favourite ? 1 : 0));
    }
}
