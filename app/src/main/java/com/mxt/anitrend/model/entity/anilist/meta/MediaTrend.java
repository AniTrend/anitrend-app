package com.mxt.anitrend.model.entity.anilist.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.base.MediaBase;

/**
 * Created by max on 2018/03/20.
 */

public class MediaTrend implements Parcelable {

    private long mediaId;
    private long date;
    private int trending;
    private int averageScore;
    private int popularity;
    private boolean releasing;
    private float episode;
    private MediaBase media;

    protected MediaTrend(Parcel in) {
        mediaId = in.readLong();
        date = in.readLong();
        trending = in.readInt();
        averageScore = in.readInt();
        popularity = in.readInt();
        releasing = in.readByte() != 0;
        episode = in.readFloat();
        media = in.readParcelable(MediaBase.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mediaId);
        dest.writeLong(date);
        dest.writeInt(trending);
        dest.writeInt(averageScore);
        dest.writeInt(popularity);
        dest.writeByte((byte) (releasing ? 1 : 0));
        dest.writeFloat(episode);
        dest.writeParcelable(media, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaTrend> CREATOR = new Creator<MediaTrend>() {
        @Override
        public MediaTrend createFromParcel(Parcel in) {
            return new MediaTrend(in);
        }

        @Override
        public MediaTrend[] newArray(int size) {
            return new MediaTrend[size];
        }
    };

    public long getMediaId() {
        return mediaId;
    }

    public long getDate() {
        return date;
    }

    public int getTrending() {
        return trending;
    }

    public int getAverageScore() {
        return averageScore;
    }

    public int getPopularity() {
        return popularity;
    }

    public boolean isReleasing() {
        return releasing;
    }

    public float getEpisode() {
        return episode;
    }

    public MediaBase getMedia() {
        return media;
    }
}
