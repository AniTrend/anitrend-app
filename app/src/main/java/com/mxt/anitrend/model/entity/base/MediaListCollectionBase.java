package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.util.KeyUtil;

public class MediaListCollectionBase implements Parcelable {

    private String name;
    private boolean isCustomList;
    private boolean isSplitCompletedList;
    private @KeyUtil.MediaListStatus String status;

    protected MediaListCollectionBase(Parcel in) {
        name = in.readString();
        isCustomList = in.readByte() != 0;
        isSplitCompletedList = in.readByte() != 0;
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeByte((byte) (isCustomList ? 1 : 0));
        dest.writeByte((byte) (isSplitCompletedList ? 1 : 0));
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaListCollectionBase> CREATOR = new Creator<MediaListCollectionBase>() {
        @Override
        public MediaListCollectionBase createFromParcel(Parcel in) {
            return new MediaListCollectionBase(in);
        }

        @Override
        public MediaListCollectionBase[] newArray(int size) {
            return new MediaListCollectionBase[size];
        }
    };

    public String getName() {
        return name;
    }

    public boolean isCustomList() {
        return isCustomList;
    }

    public boolean isSplitCompletedList() {
        return isSplitCompletedList;
    }

    public @KeyUtil.MediaListStatus String getStatus() {
        return status;
    }
}
