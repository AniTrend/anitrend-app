package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;

import com.mxt.anitrend.model.entity.anilist.meta.MediaStats;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StudioBase;

import java.util.List;

/**
 * Created by Maxwell on 10/2/2016.
 * This include Anime or Manga
 */
public class Media extends MediaBase {

    private String description;
    private List<String> genres;
    private List<MediaTag> tags;
    private String trailer;
    private String hashtag;
    private String source;
    private int meanScore;
    private List<ExternalLink> externalLinks;
    private NodeContainer<StudioBase> studios;
    private MediaStats stats;
    private List<MediaRank> rankings;

    protected Media(Parcel in) {
        super(in);
        description = in.readString();
        genres = in.createStringArrayList();
        tags = in.createTypedArrayList(MediaTag.CREATOR);
        trailer = in.readString();
        hashtag = in.readString();
        source = in.readString();
        meanScore = in.readInt();
        externalLinks = in.createTypedArrayList(ExternalLink.CREATOR);
        stats = in.readParcelable(MediaStats.class.getClassLoader());
        rankings = in.createTypedArrayList(MediaRank.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(description);
        dest.writeStringList(genres);
        dest.writeTypedList(tags);
        dest.writeString(trailer);
        dest.writeString(hashtag);
        dest.writeString(source);
        dest.writeInt(meanScore);
        dest.writeTypedList(externalLinks);
        dest.writeParcelable(stats, flags);
        dest.writeTypedList(rankings);
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };
}

