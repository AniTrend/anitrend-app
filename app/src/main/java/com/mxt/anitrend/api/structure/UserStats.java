package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by Maxwell on 11/15/2016.
 */

public class UserStats implements Parcelable {

    private StatusDistribution status_distribution;
    @SerializedName("score_distribution")
    private HashMap<String, HashMap<String, Integer>> score_distribution_map;
    @SerializedName("favourite_genres")
    private HashMap<String, Integer> favourite_genres_map;

    protected UserStats(Parcel in) {
        status_distribution = in.readParcelable(StatusDistribution.class.getClassLoader());
        score_distribution_map = (HashMap<String, HashMap<String, Integer>>) in.readSerializable();
        favourite_genres_map = (HashMap<String, Integer>) in.readSerializable();
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

    public StatusDistribution getStatus_distribution() {
        return status_distribution;
    }

    public HashMap<String, HashMap<String, Integer>> getScore_distribution_map() {
        return score_distribution_map;
    }

    public HashMap<String, Integer> getFavourite_genres_map() {
        return favourite_genres_map;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(status_distribution, i);
        parcel.writeSerializable(score_distribution_map);
        parcel.writeSerializable(favourite_genres_map);
    }
}
