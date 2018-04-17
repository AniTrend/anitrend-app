package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by max on 2018/03/20.
 */

public class MediaTrailer implements Parcelable {

    private String id;
    private String site;

    protected MediaTrailer(Parcel in) {
        id = in.readString();
        site = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(site);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaTrailer> CREATOR = new Creator<MediaTrailer>() {
        @Override
        public MediaTrailer createFromParcel(Parcel in) {
            return new MediaTrailer(in);
        }

        @Override
        public MediaTrailer[] newArray(int size) {
            return new MediaTrailer[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getSite() {
        return site;
    }
}
