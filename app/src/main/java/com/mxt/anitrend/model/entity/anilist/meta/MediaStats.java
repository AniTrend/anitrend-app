package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by max on 2018/03/20.
 * MediaStats
 */

public class MediaStats implements Parcelable {

    private List<MediaTrend> airingProgression;
    private List<ScoreDistribution> scoreDistribution;
    private List<StatusDistribution> statusDistribution;

    protected MediaStats(Parcel in) {
        airingProgression = in.createTypedArrayList(MediaTrend.CREATOR);
        scoreDistribution = in.createTypedArrayList(ScoreDistribution.CREATOR);
        statusDistribution = in.createTypedArrayList(StatusDistribution.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(airingProgression);
        dest.writeTypedList(scoreDistribution);
        dest.writeTypedList(statusDistribution);
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

    public List<MediaTrend> getAiringProgression() {
        return airingProgression;
    }

    public List<ScoreDistribution> getScoreDistribution() {
        return scoreDistribution;
    }

    public List<StatusDistribution> getStatusDistribution() {
        return statusDistribution;
    }
}
