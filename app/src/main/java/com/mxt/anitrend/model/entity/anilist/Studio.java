package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;

import com.mxt.anitrend.model.entity.base.StudioBase;

import java.util.List;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class Studio extends StudioBase {

    private List<Media> anime;

    protected Studio(Parcel in) {
        super(in);
        anime = in.createTypedArrayList(Media.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeTypedList(anime);
    }

    public static final Creator<Studio> CREATOR = new Creator<Studio>() {
        @Override
        public Studio createFromParcel(Parcel in) {
            return new Studio(in);
        }

        @Override
        public Studio[] newArray(int size) {
            return new Studio[size];
        }
    };

    public List<Media> getAnime() { return anime; }

    public void setAnime(List<Media> anime) {
        this.anime = anime;
    }
}
