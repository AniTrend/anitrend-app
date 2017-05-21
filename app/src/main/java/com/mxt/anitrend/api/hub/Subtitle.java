package com.mxt.anitrend.api.hub;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.utils.HubUtil;

/**
 * Created by max on 2017/05/14.
 */
public class Subtitle implements Parcelable {

    private String fileId;
    private String lang;
    private String type;

    protected Subtitle(Parcel in) {
        fileId = in.readString();
        lang = in.readString();
        type = in.readString();
    }

    public static final Creator<Subtitle> CREATOR = new Creator<Subtitle>() {
        @Override
        public Subtitle createFromParcel(Parcel in) {
            return new Subtitle(in);
        }

        @Override
        public Subtitle[] newArray(int size) {
            return new Subtitle[size];
        }
    };

    public String getFileId() {
        return HubUtil.getSubtitleLink(fileId);
    }

    public String getLang() {
        return lang;
    }

    public String getType() {
        return type;
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
        dest.writeString(fileId);
        dest.writeString(lang);
        dest.writeString(type);
    }
}
