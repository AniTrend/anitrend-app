package com.mxt.anitrend.model.entity.general;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by max on 11/15/2016.
 */

public class UserStats implements Parcelable {

    private HashMap<String, HashMap<String, Integer>> status_distribution;
    private HashMap<String, HashMap<String, Integer>> score_distribution;
    private HashMap<String, Integer> favourite_genres;

    protected UserStats(Parcel in) {
        status_distribution = in.readHashMap(HashMap.class.getClassLoader());
        score_distribution = in.readHashMap(HashMap.class.getClassLoader());
        favourite_genres = in.readHashMap(HashMap.class.getClassLoader());
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

    public HashMap<String, HashMap<String, Integer>> getStatus_distribution() {
        return status_distribution;
    }

    public HashMap<String, HashMap<String, Integer>> getScore_distribution() {
        return score_distribution;
    }

    public HashMap<String, Integer> getFavourite_genres() {
        return favourite_genres;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeMap(status_distribution);
        parcel.writeMap(score_distribution);
        parcel.writeMap(favourite_genres);
    }
}
