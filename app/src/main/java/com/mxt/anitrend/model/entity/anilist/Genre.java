package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Maxwell on 10/24/2016.
 */
@Entity
public class Genre implements Parcelable {

    @Id(assignable = true)
    private long id;
    private String genre;

    public Genre() {

    }

    public Genre(String genre) {
        this.genre = genre;
    }

    public long getId() {
        return id;
    }

    public String getGenre() {
        return genre;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    protected Genre(Parcel in) {
        id = in.readLong();
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
        parcel.writeLong(id);
        parcel.writeString(genre);
    }

    @Override
    public String toString() {
        return genre;
    }
}
