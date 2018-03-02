package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.base.StaffBase;

import java.util.List;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class Staff extends StaffBase {

    private long dob;
    private String info;
    private String website;
    private boolean favourite;
    private String name_first_japanese;
    private String name_last_japanese;
    private List<SeriesBase> anime_staff;
    private List<SeriesBase> manga_staff;
    private List<SeriesBase> anime;

    protected Staff(Parcel in) {
        super(in);
        dob = in.readLong();
        info = in.readString();
        website = in.readString();
        favourite = in.readByte() != 0;
        name_first_japanese = in.readString();
        name_last_japanese = in.readString();
        anime_staff = in.createTypedArrayList(SeriesBase.CREATOR);
        manga_staff = in.createTypedArrayList(SeriesBase.CREATOR);
        anime = in.createTypedArrayList(SeriesBase.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(dob);
        dest.writeString(info);
        dest.writeString(website);
        dest.writeByte((byte) (favourite ? 1 : 0));
        dest.writeString(name_first_japanese);
        dest.writeString(name_last_japanese);
        dest.writeTypedList(anime_staff);
        dest.writeTypedList(manga_staff);
        dest.writeTypedList(anime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Staff> CREATOR = new Creator<Staff>() {
        @Override
        public Staff createFromParcel(Parcel in) {
            return new Staff(in);
        }

        @Override
        public Staff[] newArray(int size) {
            return new Staff[size];
        }
    };

    public long getDob() {
        return dob;
    }

    public String getInfo() {
        return info;
    }

    public String getWebsite() {
        return website;
    }

    public String getName_first_japanese() {
        return name_first_japanese;
    }

    public String getName_last_japanese() {
        return name_last_japanese;
    }

    public String getFullJapaneseName() {
        if(TextUtils.isEmpty(name_last_japanese))
            return name_first_japanese;
        return String.format("%s %s", name_first_japanese, name_last_japanese);
    }

    public List<SeriesBase> getAnime_staff() {
        return anime_staff;
    }

    public List<SeriesBase> getManga_staff() {
        return manga_staff;
    }

    /**
     * Mostly visible to actors
     */
    public List<SeriesBase> getAnime() {
        return anime;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
