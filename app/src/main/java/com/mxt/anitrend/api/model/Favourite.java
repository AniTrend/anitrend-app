package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Maxwell on 11/12/2016.
 */

public class Favourite implements Parcelable {

    private List<Series> anime;
    private List<Series> manga;
    private List<CharacterSmall> character;
    private List<StaffSmall> staff;
    private List<StudioSmall> studio;

    protected Favourite(Parcel in) {
        anime = in.createTypedArrayList(Series.CREATOR);
        manga = in.createTypedArrayList(Series.CREATOR);
        character = in.createTypedArrayList(CharacterSmall.CREATOR);
        staff = in.createTypedArrayList(StaffSmall.CREATOR);
        studio = in.createTypedArrayList(StudioSmall.CREATOR);
    }

    public static final Creator<Favourite> CREATOR = new Creator<Favourite>() {
        @Override
        public Favourite createFromParcel(Parcel in) {
            return new Favourite(in);
        }

        @Override
        public Favourite[] newArray(int size) {
            return new Favourite[size];
        }
    };

    public List<Series> getAnime() {
        return anime;
    }

    public List<Series> getManga() {
        return manga;
    }

    public List<CharacterSmall> getCharacter() {
        return character;
    }

    public List<StaffSmall> getStaff() {
        return staff;
    }

    public List<StudioSmall> getStudio() {
        return studio;
    }

    public int getFavouritesCount() {
        return (anime != null? anime.size():0) +
                (manga != null? manga.size():0) +
                (character != null? character.size():0) +
                (staff != null? staff.size():0) +
                (studio != null? studio.size():0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(anime);
        parcel.writeTypedList(manga);
        parcel.writeTypedList(character);
        parcel.writeTypedList(staff);
        parcel.writeTypedList(studio);
    }
}
