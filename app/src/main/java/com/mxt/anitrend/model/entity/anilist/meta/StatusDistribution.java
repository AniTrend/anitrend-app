package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.anilist.UserStats;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2018/03/24.
 * StatusDistribution for media and userStats
 * @see UserStats
 */

public class StatusDistribution implements Parcelable {

    private @KeyUtil.MediaListStatus String status;
    private int amount;

    protected StatusDistribution(Parcel in) {
        status = in.readString();
        amount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status);
        dest.writeInt(amount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StatusDistribution> CREATOR = new Creator<StatusDistribution>() {
        @Override
        public StatusDistribution createFromParcel(Parcel in) {
            return new StatusDistribution(in);
        }

        @Override
        public StatusDistribution[] newArray(int size) {
            return new StatusDistribution[size];
        }
    };

    public String getStatus() {
        return status;
    }

    public int getAmount() {
        return amount;
    }
}
