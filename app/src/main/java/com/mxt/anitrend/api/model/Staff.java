package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

import com.mxt.anitrend.util.MarkDown;

import java.util.List;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class Staff implements Parcelable {

    private long dob;
    private String website;
    private String info;
    private boolean favourite;
    private int id;
    private String name_first;
    private String name_last;
    private String name_first_japanese;
    private String name_last_japanese;
    private String image_url_lge;
    private String image_url_med;
    private String language;
    private String role;
    private List<SeriesSmall> anime_staff;
    private List<SeriesSmall> manga_staff;
    private List<SeriesSmall> anime;

    protected Staff(Parcel in) {
        dob = in.readLong();
        website = in.readString();
        info = in.readString();
        favourite = in.readByte() != 0;
        id = in.readInt();
        name_first = in.readString();
        name_last = in.readString();
        name_first_japanese = in.readString();
        name_last_japanese = in.readString();
        image_url_lge = in.readString();
        image_url_med = in.readString();
        language = in.readString();
        role = in.readString();
        anime_staff = in.createTypedArrayList(SeriesSmall.CREATOR);
        manga_staff = in.createTypedArrayList(SeriesSmall.CREATOR);
        anime = in.createTypedArrayList(SeriesSmall.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(dob);
        dest.writeString(website);
        dest.writeString(info);
        dest.writeByte((byte) (favourite ? 1 : 0));
        dest.writeInt(id);
        dest.writeString(name_first);
        dest.writeString(name_last);
        dest.writeString(name_first_japanese);
        dest.writeString(name_last_japanese);
        dest.writeString(image_url_lge);
        dest.writeString(image_url_med);
        dest.writeString(language);
        dest.writeString(role);
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

    public String getWebsite() {
        return website != null? website:"No website available";
    }

    public Spanned getInfo() {
        return MarkDown.convert(info);
    }

    public int getId() {
        return id;
    }

    public String getName_first() {
        return name_first;
    }

    public String getName_last() {
        return name_last;
    }

    public String getName_first_japanese() {
        return name_first_japanese;
    }

    public String getName_last_japanese() {
        return name_last_japanese;
    }

    public String getFullJapaneseName() {
        if(name_first_japanese == null && getName_last_japanese() == null)
            return "N/A";
        return name_first_japanese + " " + name_last_japanese;
    }

    public String getImage_url_lge() {
        return image_url_lge;
    }

    public String getImage_url_med() {
        return image_url_med;
    }

    public String getLanguage() {
        return language;
    }

    public String getRole() {
        return role;
    }

    public List<SeriesSmall> getAnime_staff() {
        return anime_staff;
    }

    public List<SeriesSmall> getManga_staff() {
        return manga_staff;
    }

    /**
     * Mostly visible to actors
     */
    public List<SeriesSmall> getAnime() {
        return anime;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }
}
