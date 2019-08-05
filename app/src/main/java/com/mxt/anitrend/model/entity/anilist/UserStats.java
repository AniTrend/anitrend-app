package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.anilist.meta.FormatStats;
import com.mxt.anitrend.model.entity.anilist.meta.GenreStats;
import com.mxt.anitrend.model.entity.anilist.meta.MediaTagStats;
import com.mxt.anitrend.model.entity.anilist.meta.StatusDistribution;
import com.mxt.anitrend.model.entity.anilist.meta.YearStats;

import java.util.List;

/**
 * Created by max on 11/15/2016.
 * UserStats for user
 * @see User
 */
@Deprecated
public class UserStats implements Parcelable {

    private int watchedTime;
    private int chaptersRead;
    private List<StatusDistribution> animeStatusDistribution;
    private List<StatusDistribution> mangaStatusDistribution;
    private List<GenreStats> favouredGenres;
    private List<MediaTagStats> favouredTags;
    private List<YearStats> favouredYears;
    private List<FormatStats> favouredFormats;

    protected UserStats(Parcel in) {
        watchedTime = in.readInt();
        chaptersRead = in.readInt();
        animeStatusDistribution = in.createTypedArrayList(StatusDistribution.CREATOR);
        mangaStatusDistribution = in.createTypedArrayList(StatusDistribution.CREATOR);
        favouredGenres = in.createTypedArrayList(GenreStats.CREATOR);
        favouredTags = in.createTypedArrayList(MediaTagStats.CREATOR);
        favouredYears = in.createTypedArrayList(YearStats.CREATOR);
        favouredFormats = in.createTypedArrayList(FormatStats.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(watchedTime);
        dest.writeInt(chaptersRead);
        dest.writeTypedList(animeStatusDistribution);
        dest.writeTypedList(mangaStatusDistribution);
        dest.writeTypedList(favouredGenres);
        dest.writeTypedList(favouredTags);
        dest.writeTypedList(favouredYears);
        dest.writeTypedList(favouredFormats);
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

    public List<GenreStats> getFavouredGenres() {
        return favouredGenres;
    }

    public List<MediaTagStats> getFavouredTags() {
        return favouredTags;
    }

    public List<YearStats> getFavouredYears() {
        return favouredYears;
    }

    public List<FormatStats> getFavouredFormats() {
        return favouredFormats;
    }
}
