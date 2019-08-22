package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;

import com.annimon.stream.Stream;
import com.mxt.anitrend.model.entity.anilist.meta.MediaStats;
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrailer;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.util.KeyUtil;

import java.util.List;

/**
 * Created by Maxwell on 10/2/2016.
 * Media extension
 */

public class Media extends MediaBase {

    private String description;
    private List<String> synonyms;
    private List<String> genres;
    private List<MediaTag> tags;
    private MediaTrailer trailer;
    private String hashtag;
    private @KeyUtil.MediaSource String source;
    private List<ExternalLink> externalLinks;
    private ConnectionContainer<List<StudioBase>> studios;
    private MediaStats stats;
    private List<MediaRank> rankings;

    protected Media(Parcel in) {
        super(in);
        description = in.readString();
        synonyms = in.createStringArrayList();
        genres = in.createStringArrayList();
        tags = in.createTypedArrayList(MediaTag.CREATOR);
        trailer = in.readParcelable(MediaTrailer.class.getClassLoader());
        hashtag = in.readString();
        source = in.readString();
        externalLinks = in.createTypedArrayList(ExternalLink.CREATOR);
        stats = in.readParcelable(MediaStats.class.getClassLoader());
        rankings = in.createTypedArrayList(MediaRank.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(description);
        dest.writeStringList(synonyms);
        dest.writeStringList(genres);
        dest.writeTypedList(tags);
        dest.writeParcelable(trailer, flags);
        dest.writeString(hashtag);
        dest.writeString(source);
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

    public String getDescription() {
        return description;
    }

    public List<String> getGenres() {
        return genres;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public List<MediaTag> getTags() {
        return tags;
    }

    public List<MediaTag> getTagsNoSpoilers() {
        return Stream.of(tags)
                .filterNot(MediaTag::isMediaSpoiler)
                .toList();
    }

    public MediaTrailer getTrailer() {
        return trailer;
    }

    public String getHashTag() {
        return hashtag;
    }

    public @KeyUtil.MediaSource String getSource() {
        return source;
    }

    public List<ExternalLink> getExternalLinks() {
        return externalLinks;
    }

    public ConnectionContainer<List<StudioBase>> getStudios() {
        return studios;
    }

    public MediaStats getStats() {
        return stats;
    }

    public List<MediaRank> getRankings() {
        return rankings;
    }
}

