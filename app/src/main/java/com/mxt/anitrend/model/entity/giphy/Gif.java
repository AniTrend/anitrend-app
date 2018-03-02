package com.mxt.anitrend.model.entity.giphy;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by max on 2017/12/09.
 * gif image attributes
 */

public class Gif implements Parcelable {

    private String url;
    private String width;
    private String height;
    private long size;

    protected Gif(Parcel in) {
        url = in.readString();
        width = in.readString();
        height = in.readString();
        size = in.readLong();
    }

    public static final Creator<Gif> CREATOR = new Creator<Gif>() {
        @Override
        public Gif createFromParcel(Parcel in) {
            return new Gif(in);
        }

        @Override
        public Gif[] newArray(int size) {
            return new Gif[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    /**
     * @return size of image in bytes
     */
    public long getSize() {
        return size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(width);
        parcel.writeString(height);
        parcel.writeLong(size);
    }
}
