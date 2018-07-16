package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2018/03/22.
 * https://anilist.github.io/ApiV2-GraphQL-Docs/medialistoptions.doc.html
 */

public class MediaListOptions implements Parcelable {

    private @KeyUtil.ScoreFormat String scoreFormat;
    private String rowOrder;
    private boolean useLegacyLists;
    private MediaListTypeOptions animeList;
    private MediaListTypeOptions mangaList;


    protected MediaListOptions(Parcel in) {
        scoreFormat = in.readString();
        rowOrder = in.readString();
        useLegacyLists = in.readByte() != 0;
        animeList = in.readParcelable(MediaListTypeOptions.class.getClassLoader());
        mangaList = in.readParcelable(MediaListTypeOptions.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(scoreFormat);
        dest.writeString(rowOrder);
        dest.writeByte((byte) (useLegacyLists ? 1 : 0));
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

    public @KeyUtil.ScoreFormat String getScoreFormat() {
        return scoreFormat;
    }

    public String getRowOrder() {
        return rowOrder;
    }

    public boolean isUseLegacyLists() {
        return useLegacyLists;
    }

    public MediaListTypeOptions getAnimeList() {
        return animeList;
    }

    public MediaListTypeOptions getMangaList() {
        return mangaList;
    }
}
