package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Maxwell on 10/16/2016.
 */

public class AiringSchedule implements Parcelable {

    private long airingAt;
    private long timeUntilAiring;
    private int episode;


    protected AiringSchedule(Parcel in) {
        airingAt = in.readLong();
        timeUntilAiring = in.readLong();
        episode = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(airingAt);
        dest.writeLong(timeUntilAiring);
        dest.writeInt(episode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AiringSchedule> CREATOR = new Creator<AiringSchedule>() {
        @Override
        public AiringSchedule createFromParcel(Parcel in) {
            return new AiringSchedule(in);
        }

        @Override
        public AiringSchedule[] newArray(int size) {
            return new AiringSchedule[size];
        }
    };

    public long getAiringAt() {
        return airingAt;
    }

    public long getTimeUntilAiring() {
        return timeUntilAiring;
    }

    public int getEpisode() {
        return episode;
    }
}
