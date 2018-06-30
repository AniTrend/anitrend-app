package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.anilist.MediaTag;

public class MediaTagStats implements Parcelable {

    private MediaTag tag;
    private int amount;
    private int meanScore;
    private int timeWatched;

    protected MediaTagStats(Parcel in) {
        tag = in.readParcelable(MediaTag.class.getClassLoader());
        amount = in.readInt();
        meanScore = in.readInt();
        timeWatched = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(tag, flags);
        dest.writeInt(amount);
        dest.writeInt(meanScore);
        dest.writeInt(timeWatched);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaTagStats> CREATOR = new Creator<MediaTagStats>() {
        @Override
        public MediaTagStats createFromParcel(Parcel in) {
            return new MediaTagStats(in);
        }

        @Override
        public MediaTagStats[] newArray(int size) {
            return new MediaTagStats[size];
        }
    };

    public MediaTag getTag() {
        return tag;
    }

    public int getAmount() {
        return amount;
    }

    public int getMeanScore() {
        return meanScore;
    }

    public int getTimeWatched() {
        return timeWatched;
    }
}
