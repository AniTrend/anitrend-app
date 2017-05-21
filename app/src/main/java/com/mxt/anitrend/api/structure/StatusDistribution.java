package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Maxwell on 11/15/2016.
 */

public class StatusDistribution implements Parcelable {

    private UserAnimeStats anime;
    private UserMangaStats manga;

    protected StatusDistribution(Parcel in) {
        anime = in.readParcelable(UserAnimeStats.class.getClassLoader());
        manga = in.readParcelable(UserMangaStats.class.getClassLoader());
    }

    public static final Creator<StatusDistribution> CREATOR = new Creator<StatusDistribution>() {
        @Override
        public StatusDistribution createFromParcel(Parcel in) {
            return new StatusDistribution(in);
        }

        @Override
        public StatusDistribution[] newArray(int size) {
            return new StatusDistribution[size];
        }
    };

    public UserAnimeStats getAnime() {
        return anime;
    }

    public UserMangaStats getManga() {
        return manga;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(anime, i);
        parcel.writeParcelable(manga, i);
    }
}
