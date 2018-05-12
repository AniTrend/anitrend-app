package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by max on 2018/03/20.
 */

public class MediaTitle implements Parcelable {

    private String romaji;
    private String english;
    @SerializedName("native")
    private String original;
    private String userPreferred;

    protected MediaTitle(Parcel in) {
        romaji = in.readString();
        english = in.readString();
        original = in.readString();
        userPreferred = in.readString();
    }

    public static final Creator<MediaTitle> CREATOR = new Creator<MediaTitle>() {
        @Override
        public MediaTitle createFromParcel(Parcel in) {
            return new MediaTitle(in);
        }

        @Override
        public MediaTitle[] newArray(int size) {
            return new MediaTitle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(romaji);
        dest.writeString(english);
        dest.writeString(original);
        dest.writeString(userPreferred);
    }

    public String getRomaji() {
        return romaji != null ? romaji : userPreferred;
    }

    public String getEnglish() {
        return english != null ? english : userPreferred;
    }

    public String getOriginal() {
        return original != null ? original : userPreferred;
    }

    public String getUserPreferred() {
        return userPreferred;
    }
}
