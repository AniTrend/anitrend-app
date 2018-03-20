package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.data.converter.list.CharacterSmallListConverter;
import com.mxt.anitrend.data.converter.list.SeriesListConverter;
import com.mxt.anitrend.data.converter.list.StaffSmallListConverter;
import com.mxt.anitrend.data.converter.list.StudioSmallListConverter;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.base.StudioBase;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Maxwell on 11/12/2016.
 */
@Entity
public class Favourite implements Parcelable {

    @Id(assignable = true)
    private long id;
    @Convert(converter = SeriesListConverter.class, dbType = String.class)
    private List<Media> anime;
    @Convert(converter = SeriesListConverter.class, dbType = String.class)
    private List<Media> manga;
    @Convert(converter = CharacterSmallListConverter.class, dbType = String.class)
    private List<CharacterBase> character;
    @Convert(converter = StaffSmallListConverter.class, dbType = String.class)
    private List<StaffBase> staff;
    @Convert(converter = StudioSmallListConverter.class, dbType = String.class)
    private List<StudioBase> studio;

    public Favourite() {

    }

    public void initCollections() {
        createAnimeCollection();
        createCharacterCollection();
        createMangaCollection();
        createStaffCollection();
        createStudioCollection();
    }

    protected Favourite(Parcel in) {
        id = in.readLong();
        anime = in.createTypedArrayList(Media.CREATOR);
        manga = in.createTypedArrayList(Media.CREATOR);
        character = in.createTypedArrayList(CharacterBase.CREATOR);
        staff = in.createTypedArrayList(StaffBase.CREATOR);
        studio = in.createTypedArrayList(StudioBase.CREATOR);
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAnime(List<Media> anime) {
        this.anime = anime;
    }

    public void setManga(List<Media> manga) {
        this.manga = manga;
    }

    public void setCharacter(List<CharacterBase> character) {
        this.character = character;
    }

    public void setStaff(List<StaffBase> staff) {
        this.staff = staff;
    }

    public void setStudio(List<StudioBase> studio) {
        this.studio = studio;
    }

    public List<Media> getAnime() {
        return anime;
    }

    public List<Media> getManga() {
        return manga;
    }

    public List<CharacterBase> getCharacter() {
        return character;
    }

    public List<StaffBase> getStaff() {
        return staff;
    }

    public List<StudioBase> getStudio() {
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
        parcel.writeLong(id);
        parcel.writeTypedList(anime);
        parcel.writeTypedList(manga);
        parcel.writeTypedList(character);
        parcel.writeTypedList(staff);
        parcel.writeTypedList(studio);
    }

    public void createAnimeCollection() {
        anime = new ArrayList<>();
    }

    public void createMangaCollection() {
        manga = new ArrayList<>();
    }

    public void createCharacterCollection() {
        character = new ArrayList<>();
    }

    public void createStaffCollection() {
        staff = new ArrayList<>();
    }

    public void createStudioCollection() {
        studio = new ArrayList<>();
    }
}
