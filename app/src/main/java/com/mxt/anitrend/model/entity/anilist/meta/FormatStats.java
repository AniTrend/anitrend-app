package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

@Deprecated
public class FormatStats implements Parcelable {

    private String format;
    private int amount;

    protected FormatStats(Parcel in) {
        format = in.readString();
        amount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(format);
        dest.writeInt(amount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FormatStats> CREATOR = new Creator<FormatStats>() {
        @Override
        public FormatStats createFromParcel(Parcel in) {
            return new FormatStats(in);
        }

        @Override
        public FormatStats[] newArray(int size) {
            return new FormatStats[size];
        }
    };

    public String getFormat() {
        return format;
    }

    public int getAmount() {
        return amount;
    }
}
