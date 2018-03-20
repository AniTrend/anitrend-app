package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by max on 2018/03/20.
 */

public class MediaStats implements Parcelable {

    private List<MediaAiringProgression> airingProgression;
    private HashMap<String, Map<Integer, Integer>> scoreDistribution;
    private HashMap<String, Map<String, Integer>> statusDistribution;

    protected MediaStats(Parcel in) {
        airingProgression = in.createTypedArrayList(MediaAiringProgression.CREATOR);
        scoreDistribution = in.readHashMap(HashMap.class.getClassLoader());
        statusDistribution = in.readHashMap(HashMap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(airingProgression);
        dest.writeMap(scoreDistribution);
        dest.writeMap(statusDistribution);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaStats> CREATOR = new Creator<MediaStats>() {
        @Override
        public MediaStats createFromParcel(Parcel in) {
            return new MediaStats(in);
        }

        @Override
        public MediaStats[] newArray(int size) {
            return new MediaStats[size];
        }
    };

    public List<MediaAiringProgression> getAiringProgression() {
        return airingProgression;
    }

    public HashMap<String, Map<Integer, Integer>> getScoreDistribution() {
        return scoreDistribution;
    }

    public HashMap<String, Map<String, Integer>> getStatusDistribution() {
        return statusDistribution;
    }
}
