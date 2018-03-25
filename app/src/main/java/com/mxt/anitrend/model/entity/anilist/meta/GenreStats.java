package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.anilist.UserStats;

/**
 * Created by max on 2018/03/25.
 * GenreStats for userStats
 * @see UserStats
 */

public class GenreStats implements Parcelable {

    private String genre;
    private int amount;
    private int meanScore;
    private int timeWatched;


    protected GenreStats(Parcel in) {
        genre = in.readString();
        amount = in.readInt();
        meanScore = in.readInt();
        timeWatched = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(genre);
        dest.writeInt(amount);
        dest.writeInt(meanScore);
        dest.writeInt(timeWatched);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GenreStats> CREATOR = new Creator<GenreStats>() {
        @Override
        public GenreStats createFromParcel(Parcel in) {
            return new GenreStats(in);
        }

        @Override
        public GenreStats[] newArray(int size) {
            return new GenreStats[size];
        }
    };

    public String getGenre() {
        return genre;
    }

    public int getAmount() {
        return amount;
    }

    public int getMeanScore() {
        return meanScore;
    }

    /**
     *  The amount of time in minutes the genre has been watched by the user
     */
    public int getTimeWatched() {
        return timeWatched;
    }
}
