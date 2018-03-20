package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule;
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate;
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase;
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle;
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase;
import com.mxt.anitrend.model.entity.group.EntityGroup;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Maxwell on 10/3/2016.
 */
@Entity
public class MediaBase extends EntityGroup implements Parcelable {

    @Id(assignable = true)
    private long id;
    private MediaTitle title;
    private ImageBase coverImage;
    private String bannerImage;
    private String type;
    private String season;
    private String status;
    private FuzzyDate startDate;
    private FuzzyDate endDate;
    private int episodes;
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
        status = in.readString();
        startDate = in.readParcelable(FuzzyDate.class.getClassLoader());
        endDate = in.readParcelable(FuzzyDate.class.getClassLoader());
        episodes = in.readInt();
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
        dest.writeString(status);
        dest.writeParcelable(startDate, flags);
        dest.writeParcelable(endDate, flags);
        dest.writeInt(episodes);
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

    public String getType() {
        return type;
    }

    public String getSeason() {
        return season;
    }

    public String getStatus() {
        return status;
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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MediaBase)
            return ((MediaBase)obj).getId() == getId();
        return super.equals(obj);
    }
}
