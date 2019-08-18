package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

@Deprecated
public class YearStats implements Parcelable {

    private int year;
    private int amount;
    private int meanScore;

    protected YearStats(Parcel in) {
        year = in.readInt();
        amount = in.readInt();
        meanScore = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(year);
        dest.writeInt(amount);
        dest.writeInt(meanScore);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<YearStats> CREATOR = new Creator<YearStats>() {
        @Override
        public YearStats createFromParcel(Parcel in) {
            return new YearStats(in);
        }

        @Override
        public YearStats[] newArray(int size) {
            return new YearStats[size];
        }
    };

    public int getYear() {
        return year;
    }

    public int getAmount() {
        return amount;
    }

    public int getMeanScore() {
        return meanScore;
    }
}
