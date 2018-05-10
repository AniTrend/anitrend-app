package com.mxt.anitrend.model.entity.anilist;

import java.util.List;
import android.os.Parcel;
import com.mxt.anitrend.model.entity.base.MediaListCollectionBase;

public class MediaListCollection extends MediaListCollectionBase {

    private List<MediaList> entries;

    protected MediaListCollection(Parcel in) {
        super(in);
        entries = in.createTypedArrayList(MediaList.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(entries);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaListCollection> CREATOR = new Creator<MediaListCollection>() {
        @Override
        public MediaListCollection createFromParcel(Parcel in) {
            return new MediaListCollection(in);
        }

        @Override
        public MediaListCollection[] newArray(int size) {
            return new MediaListCollection[size];
        }
    };

    public List<MediaList> getEntries() {
        return entries;
    }
}
