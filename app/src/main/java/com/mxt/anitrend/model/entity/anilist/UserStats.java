package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.anilist.meta.GenreStats;
import com.mxt.anitrend.model.entity.anilist.meta.StatusDistribution;

import java.util.List;

/**
 * Created by max on 11/15/2016.
 * UserStats for user
 * @see User
 */

public class UserStats implements Parcelable {

    private int watchedTime;
    private int chaptersRead;
    private List<StatusDistribution> animeStatusDistribution;
    private List<StatusDistribution> mangaStatusDistribution;
    private List<GenreStats> favouredGenresOverview;

    protected UserStats(Parcel in) {
        watchedTime = in.readInt();
        chaptersRead = in.readInt();
        animeStatusDistribution = in.createTypedArrayList(StatusDistribution.CREATOR);
        mangaStatusDistribution = in.createTypedArrayList(StatusDistribution.CREATOR);
        favouredGenresOverview = in.createTypedArrayList(GenreStats.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(watchedTime);
        dest.writeInt(chaptersRead);
        dest.writeTypedList(animeStatusDistribution);
        dest.writeTypedList(mangaStatusDistribution);
        dest.writeTypedList(favouredGenresOverview);
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

    public List<StatusDistribution> getAnimeStatusDistribution() {
        return animeStatusDistribution;
    }

    public List<StatusDistribution> getMangaStatusDistribution() {
        return mangaStatusDistribution;
    }

    public List<GenreStats> getFavouredGenresOverview() {
        return favouredGenresOverview;
    }
}
