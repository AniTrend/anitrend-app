package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class Studio implements Parcelable {

    private int id;
    private String studio_name;
    private String studio_wiki;
    private boolean favourite;
    private int main_studio;
    private List<Series> anime;

    protected Studio(Parcel in) {
        id = in.readInt();
        studio_name = in.readString();
        studio_wiki = in.readString();
        favourite = in.readByte() != 0;
        main_studio = in.readInt();
        anime = in.createTypedArrayList(Series.CREATOR);
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

    public String getStudio_name() {
        return studio_name;
    }

    public String getStudio_wiki() {
        return studio_wiki;
    }

    public int getId() {
        return id;
    }

    public int getMain_studio() {
        return main_studio;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public List<Series> getAnime() { return anime; }

    @Override
    public String toString() {
        return "Studio{" +
                "id=" + id +
                ", studio_name='" + studio_name + "\n" +
                ", studio_wiki='" + studio_wiki + "\n" +
                ", favourite=" + favourite +
                ", main_studio='" + main_studio + "\n" +
                ", anime=" + anime +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(studio_name);
        parcel.writeString(studio_wiki);
        parcel.writeByte((byte) (favourite ? 1 : 0));
        parcel.writeInt(main_studio);
        parcel.writeTypedList(anime);
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public void setAnime(List<Series> anime) {
        this.anime = anime;
    }
}
