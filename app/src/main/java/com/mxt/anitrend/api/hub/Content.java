package com.mxt.anitrend.api.hub;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.utils.HubUtil;

/**
 * Created by max on 2017/05/14.
 */
public class Content implements Parcelable {

    private String url;
    private int height;
    private int width;
    private String type;
    private String medium;

    protected Content(Parcel in) {
        url = in.readString();
        height = in.readInt();
        width = in.readInt();
        type = in.readString();
        medium = in.readString();
    }

    public static final Creator<Content> CREATOR = new Creator<Content>() {
        @Override
        public Content createFromParcel(Parcel in) {
            return new Content(in);
        }

        @Override
        public Content[] newArray(int size) {
            return new Content[size];
        }
    };

    public String getUrl() {
        return HubUtil.getEpisodeLink(url);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getType() {
        return type;
    }

    public String getMedium() {
        return medium;
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
        dest.writeString(url);
        dest.writeInt(height);
        dest.writeInt(width);
        dest.writeString(type);
        dest.writeString(medium);
    }
}
