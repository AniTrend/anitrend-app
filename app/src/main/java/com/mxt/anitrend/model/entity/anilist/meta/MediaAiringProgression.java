package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by max on 2018/03/20.
 */

public class MediaAiringProgression implements Parcelable {

    private float episode;
    private float score;
    private int watching;

    protected MediaAiringProgression(Parcel in) {
        episode = in.readFloat();
        score = in.readFloat();
        watching = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(episode);
        dest.writeFloat(score);
        dest.writeInt(watching);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaAiringProgression> CREATOR = new Creator<MediaAiringProgression>() {
        @Override
        public MediaAiringProgression createFromParcel(Parcel in) {
            return new MediaAiringProgression(in);
        }

        @Override
        public MediaAiringProgression[] newArray(int size) {
            return new MediaAiringProgression[size];
        }
    };

    public float getEpisode() {
        return episode;
    }

    public float getScore() {
        return score;
    }

    public int getWatching() {
        return watching;
    }
}
