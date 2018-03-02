package com.mxt.anitrend.model.entity.giphy;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;

/**
 * Created by max on 2017/12/09.
 * giphy image properties
 */

public class Giphy implements Parcelable {

    private String id;
    private String url;
    private String title;
    private HashMap<String, Gif> images;

    protected Giphy(Parcel in) {
        id = in.readString();
        url = in.readString();
        title = in.readString();
        images = in.readHashMap(HashMap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(title);
        dest.writeMap(images);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Giphy> CREATOR = new Creator<Giphy>() {
        @Override
        public Giphy createFromParcel(Parcel in) {
            return new Giphy(in);
        }

        @Override
        public Giphy[] newArray(int size) {
            return new Giphy[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public HashMap<String, Gif> getImages() {
        return images;
    }
}
