package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by max on 2018/03/24.
 */

public class ScoreDistribution implements Parcelable {

    private int score;
    private int amount;

    protected ScoreDistribution(Parcel in) {
        score = in.readInt();
        amount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(score);
        dest.writeInt(amount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ScoreDistribution> CREATOR = new Creator<ScoreDistribution>() {
        @Override
        public ScoreDistribution createFromParcel(Parcel in) {
            return new ScoreDistribution(in);
        }

        @Override
        public ScoreDistribution[] newArray(int size) {
            return new ScoreDistribution[size];
        }
    };

    public int getScore() {
        return score;
    }

    public int getAmount() {
        return amount;
    }
}
