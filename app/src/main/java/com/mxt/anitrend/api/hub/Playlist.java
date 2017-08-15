package com.mxt.anitrend.api.hub;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by max on 2017/05/14.
 */
public class Playlist implements Parcelable {

    private String title;
    private List<String> videos;
    private String addedDate;
    private String addedBy;
    private String _id;
    private String thumbnail;

    protected Playlist(Parcel in) {
        title = in.readString();
        videos = in.createStringArrayList();
        addedDate = in.readString();
        addedBy = in.readString();
        _id = in.readString();
        thumbnail = in.readString();
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public List<String> getVideos() {
        return videos;
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
        return thumbnail;
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
        dest.writeString(title);
        dest.writeStringList(videos);
        dest.writeString(addedDate);
        dest.writeString(addedBy);
        dest.writeString(_id);
        dest.writeString(thumbnail);
    }
}
