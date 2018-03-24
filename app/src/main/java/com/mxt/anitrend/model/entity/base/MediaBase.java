package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule;
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate;
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase;
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by Maxwell on 10/3/2016.
 * Media base entity
 */

public class MediaBase extends EntityGroup implements Parcelable {

    private long id;
    private MediaTitle title;
    private ImageBase coverImage;
    private String bannerImage;
    private @KeyUtils.MediaType String type;
    private @KeyUtils.MediaFormat String format;
    private @KeyUtils.MediaSeason String season;
    private @KeyUtils.MediaStatus String status;
    private String siteUrl;
    private int meanScore;
    private int averageScore;
    private FuzzyDate startDate;
    private FuzzyDate endDate;
    private int episodes;
    private int duration;
    private int chapters;
    private int volumes;
    private boolean isAdult;
    private boolean isFavourite;
    private AiringSchedule nextAiringEpisode;


    protected MediaBase(Parcel in) {
        id = in.readLong();
        title = in.readParcelable(MediaTitle.class.getClassLoader());
        coverImage = in.readParcelable(ImageBase.class.getClassLoader());
        bannerImage = in.readString();
        type = in.readString();
        season = in.readString();
        format = in.readString();
        status = in.readString();
        siteUrl = in.readString();
        meanScore = in.readInt();
        averageScore = in.readInt();
        startDate = in.readParcelable(FuzzyDate.class.getClassLoader());
        endDate = in.readParcelable(FuzzyDate.class.getClassLoader());
        episodes = in.readInt();
        duration = in.readInt();
        chapters = in.readInt();
        volumes = in.readInt();
        isAdult = in.readByte() != 0;
        isFavourite = in.readByte() != 0;
        nextAiringEpisode = in.readParcelable(AiringSchedule.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeParcelable(title, flags);
        dest.writeParcelable(coverImage, flags);
        dest.writeString(bannerImage);
        dest.writeString(type);
        dest.writeString(season);
        dest.writeString(format);
        dest.writeString(status);
        dest.writeString(siteUrl);
        dest.writeInt(meanScore);
        dest.writeInt(averageScore);
        dest.writeParcelable(startDate, flags);
        dest.writeParcelable(endDate, flags);
        dest.writeInt(episodes);
        dest.writeInt(duration);
        dest.writeInt(chapters);
        dest.writeInt(volumes);
        dest.writeByte((byte) (isAdult ? 1 : 0));
        dest.writeByte((byte) (isFavourite ? 1 : 0));
        dest.writeParcelable(nextAiringEpisode, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaBase> CREATOR = new Creator<MediaBase>() {
        @Override
        public MediaBase createFromParcel(Parcel in) {
            return new MediaBase(in);
        }

        @Override
        public MediaBase[] newArray(int size) {
            return new MediaBase[size];
        }
    };

    public long getId() {
        return id;
    }

    public MediaTitle getTitle() {
        return title;
    }

    public ImageBase getCoverImage() {
        return coverImage;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public @KeyUtils.MediaType String getType() {
        return type;
    }

    public @KeyUtils.MediaSeason String getSeason() {
        return season;
    }

    public @KeyUtils.MediaFormat String getFormat() {
        return format;
    }

    public @KeyUtils.MediaStatus String getStatus() {
        return status;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public FuzzyDate getStartDate() {
        return startDate;
    }

    public FuzzyDate getEndDate() {
        return endDate;
    }

    public int getEpisodes() {
        return episodes;
    }

    public int getDuration() {
        return duration;
    }

    public int getChapters() {
        return chapters;
    }

    public int getVolumes() {
        return volumes;
    }

    public boolean isAdult() {
        return isAdult;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public AiringSchedule getNextAiringEpisode() {
        return nextAiringEpisode;
    }

    public int getMeanScore() {
        return meanScore;
    }

    public int getAverageScore() {
        return averageScore;
    }

    public void toggleFavourite() {
        isFavourite = !isFavourite;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MediaBase)
            return ((MediaBase)obj).getId() == getId();
        return super.equals(obj);
    }
}
