package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by max on 2018/03/20.
 */

public class ImageBase implements Parcelable {

    private String large;
    private String medium;

    protected ImageBase(Parcel in) {
        large = in.readString();
        medium = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(large);
        dest.writeString(medium);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImageBase> CREATOR = new Creator<ImageBase>() {
        @Override
        public ImageBase createFromParcel(Parcel in) {
            return new ImageBase(in);
        }

        @Override
        public ImageBase[] newArray(int size) {
            return new ImageBase[size];
        }
    };

    public String getLarge() {
        return large;
    }

    public String getMedium() {
        return medium;
    }
}
