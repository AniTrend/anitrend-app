package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Maxwell on 10/16/2016.
 */

public class Airing implements Parcelable {

    private String time;
    private long countdown;
    private int next_episode;

    protected Airing(Parcel in) {
        time = in.readString();
        countdown = in.readLong();
        next_episode = in.readInt();
    }

    public static final Creator<Airing> CREATOR = new Creator<Airing>() {
        @Override
        public Airing createFromParcel(Parcel in) {
            return new Airing(in);
        }

        @Override
        public Airing[] newArray(int size) {
            return new Airing[size];
        }
    };

    public String getTime() {
        return time;
    }

    public long getCountdown() {
        return countdown;
    }

    public int getNext_episode() {
        return next_episode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(time);
        parcel.writeLong(countdown);
        parcel.writeInt(next_episode);
    }
}
