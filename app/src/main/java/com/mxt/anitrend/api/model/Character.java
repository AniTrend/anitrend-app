package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

import com.mxt.anitrend.api.structure.Anime;
import com.mxt.anitrend.api.structure.Manga;
import com.mxt.anitrend.util.MarkDown;

import java.util.List;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class Character implements Parcelable {

    private int id;
    private String name_alt;
    private String info;
    private String name_first;
    private String name_last;
    private String name_japanese;
    private String image_url_lge;
    private String image_url_med;
    private boolean favourite;
    private String role;
    private List<Anime> anime;
    private List<Manga> manga;

    protected Character(Parcel in) {
        id = in.readInt();
        name_alt = in.readString();
        info = in.readString();
        name_first = in.readString();
        name_last = in.readString();
        name_japanese = in.readString();
        image_url_lge = in.readString();
        image_url_med = in.readString();
        favourite = in.readByte() != 0;
        role = in.readString();
        anime = in.createTypedArrayList(Anime.CREATOR);
        manga = in.createTypedArrayList(Manga.CREATOR);
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

    public String getName_alt() {
        return name_alt == null? "No Alternative Name!":name_alt;
    }

    public Spanned getInfo() {
        return MarkDown.convert(info);
    }

    public int getId() {
        return id;
    }

    public String getName_first() {
        return name_first == null? "N/A":name_first;
    }

    public String getName_last() {
        return name_last == null? "N/A":name_last;
    }

    public String getName_japanese() {
        return name_japanese;
    }

    public String getImage_url_lge() {
        return image_url_lge;
    }

    public String getImage_url_med() {
        return image_url_med;
    }

    public String getRole() {
        if(role != null)
            return role;
        return "N/A";
    }

    public boolean isFavourite() {
        return favourite;
    }

    public List<Anime> getAnime() {
        return anime;
    }

    public List<Manga> getManga() {
        return manga;
    }

    @Override
    public String toString() {
        return "Character{" +
                "name_alt='" + name_alt + "\n" +
                ", info='" + info + "\n" +
                ", id=" + id +
                ", name_first='" + name_first + "\n" +
                ", name_last='" + name_last + "\n" +
                ", name_japanese='" + name_japanese + "\n" +
                ", image_url_lge='" + image_url_lge + "\n" +
                ", image_url_med='" + image_url_med + "\n" +
                ", role='" + role + "\n" +
                ", anime=" + anime +
                ", manga=" + manga +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name_alt);
        parcel.writeString(info);
        parcel.writeString(name_first);
        parcel.writeString(name_last);
        parcel.writeString(name_japanese);
        parcel.writeString(image_url_lge);
        parcel.writeString(image_url_med);
        parcel.writeByte((byte) (favourite ? 1 : 0));
        parcel.writeString(role);
        parcel.writeTypedList(anime);
        parcel.writeTypedList(manga);
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }
}
