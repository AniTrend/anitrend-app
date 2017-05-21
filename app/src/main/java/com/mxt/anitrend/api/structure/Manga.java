package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.api.model.StaffSmall;

import java.util.List;

/**
 * Created by Maxwell on 10/2/2016.
 */
public class Manga implements Parcelable {

    private int id;
    private String series_type;
    private String title_romaji;
    private String title_english;
    private String title_japanese;
    private String type;
    private int start_date_fuzzy;
    private int end_date_fuzzy;
    private Integer season;
    private String[] synonyms;
    private String[] genres;
    private boolean adult;
    private double average_score;
    private int popularity;
    private String role;
    private String image_url_sml;
    private String image_url_med;
    private String image_url_lge;
    private String image_url_banner;
    private int updated_at;

    private int total_chapters;
    private int total_volumes;
    private String publishing_status;
    private List<StaffSmall> actros;

    protected Manga(Parcel in) {
        id = in.readInt();
        series_type = in.readString();
        title_romaji = in.readString();
        title_english = in.readString();
        title_japanese = in.readString();
        type = in.readString();
        start_date_fuzzy = in.readInt();
        end_date_fuzzy = in.readInt();
        season = in.readInt();
        synonyms = in.createStringArray();
        genres = in.createStringArray();
        adult = in.readByte() != 0;
        average_score = in.readDouble();
        role = in.readString();
        popularity = in.readInt();
        image_url_sml = in.readString();
        image_url_med = in.readString();
        image_url_lge = in.readString();
        image_url_banner = in.readString();
        updated_at = in.readInt();
        total_chapters = in.readInt();
        total_volumes = in.readInt();
        publishing_status = in.readString();
        actros = in.createTypedArrayList(StaffSmall.CREATOR);
    }

    public static final Creator<Manga> CREATOR = new Creator<Manga>() {
        @Override
        public Manga createFromParcel(Parcel in) {
            return new Manga(in);
        }

        @Override
        public Manga[] newArray(int size) {
            return new Manga[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getSeries_type() {
        return series_type;
    }

    public String getTitle_romaji() {
        return title_romaji;
    }

    public String getTitle_english() {
        return title_english;
    }

    public String getTitle_japanese() {
        return title_japanese;
    }

    public String getType() {
        return type;
    }

    public int getStart_date_fuzzy() {
        return start_date_fuzzy;
    }

    public int getEnd_date_fuzzy() {
        return end_date_fuzzy;
    }

    public Integer getSeason() {
        return season;
    }

    public String[] getSynonyms() {
        return synonyms;
    }

    public String[] getGenres() {
        return genres;
    }

    public boolean isAdult() {
        return adult;
    }

    public double getAverage_score() {
        return average_score;
    }

    public int getPopularity() {
        return popularity;
    }

    public String getRole() {
        return role;
    }

    public String getImage_url_sml() {
        return image_url_sml;
    }

    public String getImage_url_med() {
        return image_url_med;
    }

    public String getImage_url_lge() {
        return image_url_lge;
    }

    public String getImage_url_banner() {
        return image_url_banner;
    }

    public int getUpdated_at() {
        return updated_at;
    }

    /**total_chapters	int	24	Number of total chapters in the manga. (0 if unknown). Yes*/
    public int getTotal_chapters() {
        return total_chapters;
    }

    /**total_volumes	int	2	Number of total volumes in the manga. (0 if unknown)*/
    public int getTotal_volumes() {
        return total_volumes;
    }

    /**publishing_status	string|null	(See manga status types below)	Current publishing status of the manga.	Yes*/
    public String getPublishing_status() {
        return publishing_status;
    }

    public List<StaffSmall> getActros() {
        return actros;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(series_type);
        parcel.writeString(title_romaji);
        parcel.writeString(title_english);
        parcel.writeString(title_japanese);
        parcel.writeString(type);
        parcel.writeInt(start_date_fuzzy);
        parcel.writeInt(end_date_fuzzy);
        parcel.writeInt(season==null?0:season);
        parcel.writeStringArray(synonyms);
        parcel.writeStringArray(genres);
        parcel.writeByte((byte) (adult ? 1 : 0));
        parcel.writeDouble(average_score);
        parcel.writeInt(popularity);
        parcel.writeString(role);
        parcel.writeString(image_url_sml);
        parcel.writeString(image_url_med);
        parcel.writeString(image_url_lge);
        parcel.writeString(image_url_banner);
        parcel.writeInt(updated_at);
        parcel.writeInt(total_chapters);
        parcel.writeInt(total_volumes);
        parcel.writeString(publishing_status);
        parcel.writeTypedList(actros);
    }
}
