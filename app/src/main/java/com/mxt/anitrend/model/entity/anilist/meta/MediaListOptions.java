package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2018/03/22.
 */

public class MediaListOptions implements Parcelable {

    private @KeyUtils.ScoreFormat String scoreFormat;
    private String rowOrder;
    private MediaListTypeOptions animeList;
    private MediaListTypeOptions mangaList;

    protected MediaListOptions(Parcel in) {
        scoreFormat = in.readString();
        rowOrder = in.readString();
        animeList = in.readParcelable(MediaListTypeOptions.class.getClassLoader());
        mangaList = in.readParcelable(MediaListTypeOptions.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(scoreFormat);
        dest.writeString(rowOrder);
        dest.writeParcelable(animeList, flags);
        dest.writeParcelable(mangaList, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaListOptions> CREATOR = new Creator<MediaListOptions>() {
        @Override
        public MediaListOptions createFromParcel(Parcel in) {
            return new MediaListOptions(in);
        }

        @Override
        public MediaListOptions[] newArray(int size) {
            return new MediaListOptions[size];
        }
    };

    public @KeyUtils.ScoreFormat String getScoreFormat() {
        return scoreFormat;
    }

    public String getRowOrder() {
        return rowOrder;
    }

    public MediaListTypeOptions getAnimeList() {
        return animeList;
    }

    public MediaListTypeOptions getMangaList() {
        return mangaList;
    }
}
