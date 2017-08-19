package com.mxt.anitrend.api.hub;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

import com.mxt.anitrend.util.HubUtil;
import com.mxt.anitrend.util.MarkDown;
import com.mxt.anitrend.util.PatternMatcher;

import java.util.List;

/**
 * Created by max on 2017/05/14.
 */
public class Video implements Parcelable {

    private String fileName;
    private long size;
    private String audioLang;
    private double duration;
    private int totalUpvotes;
    private String sectionDate;
    private String uploadStatus;
    private String addedDate;
    private String addedBy;
    private String _id;
    private String thumbnail;
    private List<Subtitle> subtitles;
    private List<Content> content;
    private String title;
    private String description;
    private String category;
    private int quality;

    protected Video(Parcel in) {
        fileName = in.readString();
        size = in.readLong();
        audioLang = in.readString();
        duration = in.readDouble();
        totalUpvotes = in.readInt();
        sectionDate = in.readString();
        uploadStatus = in.readString();
        addedDate = in.readString();
        addedBy = in.readString();
        _id = in.readString();
        thumbnail = in.readString();
        subtitles = in.createTypedArrayList(Subtitle.CREATOR);
        content = in.createTypedArrayList(Content.CREATOR);
        title = in.readString();
        description = in.readString();
        category = in.readString();
        quality = in.readInt();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public String getFileName() {
        return PatternMatcher.stripBloat(fileName);
    }

    public long getSize() {
        return size;
    }

    public String getAudioLang() {
        return audioLang;
    }

    public double getDuration() {
        return duration;
    }

    public int getTotalUpvotes() {
        return totalUpvotes;
    }

    public String getSectionDate() {
        return sectionDate;
    }

    public String getUploadStatus() {
        return uploadStatus;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public String get_id() {
        return _id;
    }

    public String getThumbnail() {
        return HubUtil.getThubnailLink(thumbnail);
    }

    public List<Subtitle> getSubtitles() {
        return subtitles;
    }

    public List<Content> getContent() {
        return content;
    }

    public String getTitle() {
        if(title != null)
            return title;
        return getFileName();
    }

    public Spanned getDescription() {
        return MarkDown.convert(description);
    }

    public String getCategory() {
        return category;
    }

    public int getQuality() {
        return quality;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     * @see #CONTENTS_FILE_DESCRIPTOR
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeLong(size);
        dest.writeString(audioLang);
        dest.writeDouble(duration);
        dest.writeInt(totalUpvotes);
        dest.writeString(sectionDate);
        dest.writeString(uploadStatus);
        dest.writeString(addedDate);
        dest.writeString(addedBy);
        dest.writeString(_id);
        dest.writeString(thumbnail);
        dest.writeTypedList(subtitles);
        dest.writeTypedList(content);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(category);
        dest.writeInt(quality);
    }
}
