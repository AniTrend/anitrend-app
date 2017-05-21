package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Maxwell on 10/24/2016.
 */

public class Genre implements Parcelable {

    private int id;
    private String genre;

    public int getId() {
        return id;
    }

    public String getGenre() {
        return genre;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Genre(int id, String genre) {
        this.id = id;
        this.genre = genre;
    }

    protected Genre(Parcel in) {
        id = in.readInt();
        genre = in.readString();
    }

    public static final Creator<Genre> CREATOR = new Creator<Genre>() {
        @Override
        public Genre createFromParcel(Parcel in) {
            return new Genre(in);
        }

        @Override
        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(genre);
    }

    @Override
    public String toString() {
        return genre;
    }
}
