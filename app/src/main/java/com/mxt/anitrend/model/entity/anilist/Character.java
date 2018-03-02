package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.SeriesBase;

import java.util.List;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class Character extends CharacterBase {

    private String info;
    private String name_alt;
    private String name_japanese;
    private boolean favourite;
    private List<SeriesBase> anime;
    private List<SeriesBase> manga;

    protected Character(Parcel in) {
        super(in);
        info = in.readString();
        name_alt = in.readString();
        name_japanese = in.readString();
        favourite = in.readByte() != 0;
        anime = in.createTypedArrayList(SeriesBase.CREATOR);
        manga = in.createTypedArrayList(SeriesBase.CREATOR);
    }

    public static final Creator<Character> CREATOR = new Creator<Character>() {
        @Override
        public Character createFromParcel(Parcel in) {
            return new Character(in);
        }

        @Override
        public Character[] newArray(int size) {
            return new Character[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(info);
        parcel.writeString(name_alt);
        parcel.writeString(name_japanese);
        parcel.writeByte((byte) (favourite ? 1 : 0));
        parcel.writeTypedList(anime);
        parcel.writeTypedList(manga);
    }

    public String getInfo() {
        return info;
    }

    public String getName_alt() {
        return name_alt;
    }

    public String getName_japanese() {
        return name_japanese;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public List<SeriesBase> getAnime() {
        return anime;
    }

    public List<SeriesBase> getManga() {
        return manga;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }
}
