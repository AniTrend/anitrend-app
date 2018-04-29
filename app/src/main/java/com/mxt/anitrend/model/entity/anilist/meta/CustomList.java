package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomList implements Parcelable {

    private String name;
    private boolean enabled;

    protected CustomList(Parcel in) {
        name = in.readString();
        enabled = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeByte((byte) (enabled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CustomList> CREATOR = new Creator<CustomList>() {
        @Override
        public CustomList createFromParcel(Parcel in) {
            return new CustomList(in);
        }

        @Override
        public CustomList[] newArray(int size) {
            return new CustomList[size];
        }
    };

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
