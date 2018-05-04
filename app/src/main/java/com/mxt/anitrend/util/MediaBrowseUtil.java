package com.mxt.anitrend.util;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.view.activity.detail.MediaBrowseActivity;

/**
 * Helper configuration class for global configurable browsing activity
 * @see MediaBrowseActivity
 */
public class MediaBrowseUtil implements Parcelable {

    private boolean compactType;
    private boolean filterDisabled;
    private boolean basicFilter;

    public MediaBrowseUtil() {

    }

    protected MediaBrowseUtil(Parcel in) {
        compactType = in.readByte() != 0;
        filterDisabled = in.readByte() != 0;
        basicFilter = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (compactType ? 1 : 0));
        dest.writeByte((byte) (filterDisabled ? 1 : 0));
        dest.writeByte((byte) (basicFilter ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaBrowseUtil> CREATOR = new Creator<MediaBrowseUtil>() {
        @Override
        public MediaBrowseUtil createFromParcel(Parcel in) {
            return new MediaBrowseUtil(in);
        }

        @Override
        public MediaBrowseUtil[] newArray(int size) {
            return new MediaBrowseUtil[size];
        }
    };

    public MediaBrowseUtil setCompactType(boolean compactType) {
        this.compactType = compactType;
        return this;
    }

    public MediaBrowseUtil setFilterDisabled(boolean filterDisabled) {
        this.filterDisabled = filterDisabled;
        return this;
    }

    public MediaBrowseUtil setBasicFilter(boolean basicFiltering) {
        this.basicFilter = basicFiltering;
        return this;
    }

    public boolean isCompactType() {
        return compactType;
    }

    public boolean isFilterDisabled() {
        return filterDisabled;
    }

    public boolean isBasicFilter() {
        return basicFilter;
    }
}
