package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by max on 11/15/2016.
 */

public class UserStats implements Parcelable {

    private int watchedTime;
    private int chaptersRead;
    private HashMap<String, Integer> animeStatusDistribution;
    private HashMap<String, Integer> mangaStatusDistribution;
    private HashMap<String, Integer> favouredGenresOverview;

    protected UserStats(Parcel in) {
        watchedTime = in.readInt();
        chaptersRead = in.readInt();
        animeStatusDistribution = in.readHashMap(HashMap.class.getClassLoader());
        mangaStatusDistribution = in.readHashMap(HashMap.class.getClassLoader());
        favouredGenresOverview = in.readHashMap(HashMap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(watchedTime);
        dest.writeInt(chaptersRead);
        dest.writeMap(animeStatusDistribution);
        dest.writeMap(mangaStatusDistribution);
        dest.writeMap(favouredGenresOverview);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserStats> CREATOR = new Creator<UserStats>() {
        @Override
        public UserStats createFromParcel(Parcel in) {
            return new UserStats(in);
        }

        @Override
        public UserStats[] newArray(int size) {
            return new UserStats[size];
        }
    };

    public int getWatchedTime() {
        return watchedTime;
    }

    public int getChaptersRead() {
        return chaptersRead;
    }

    public HashMap<String, Integer> getAnimeStatusDistribution() {
        return animeStatusDistribution;
    }

    public HashMap<String, Integer> getMangaStatusDistribution() {
        return mangaStatusDistribution;
    }

    public HashMap<String, Integer> getFavouredGenresOverview() {
        return favouredGenresOverview;
    }
}
