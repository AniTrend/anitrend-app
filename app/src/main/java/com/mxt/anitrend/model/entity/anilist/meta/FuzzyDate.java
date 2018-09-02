package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

import javax.annotation.Nullable;

/**
 * Created by max on 2018/03/20.
 */

public class FuzzyDate implements Parcelable {

    private @Nullable Integer day;
    private @Nullable Integer month;
    private @Nullable Integer year;

    public FuzzyDate(@Nullable Integer day,@Nullable Integer month,@Nullable Integer year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    protected FuzzyDate(Parcel in) {
        day = in.readInt();
        month = in.readInt();
        year = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(day);
        dest.writeInt(month);
        dest.writeInt(year);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FuzzyDate> CREATOR = new Creator<FuzzyDate>() {
        @Override
        public FuzzyDate createFromParcel(Parcel in) {
            return new FuzzyDate(in);
        }

        @Override
        public FuzzyDate[] newArray(int size) {
            return new FuzzyDate[size];
        }
    };

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public void setDate(@Nullable Integer day, @Nullable Integer month, @Nullable Integer year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    @SuppressWarnings("NullPointerException")
    public boolean isValidDate() {
        return day != 0 || month != 0 || year != 0;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),"%d/%d/%d", year, month, day);
    }
}
