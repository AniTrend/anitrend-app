package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by Maxwell on 10/3/2016.
 */
public class SeriesSmall implements Parcelable {

    private int id;
    private String series_type;
    private String title_romaji;
    private String title_english;
    private String title_japanese;
    private String type;
    private int start_date_fuzzy;
    private int end_date_fuzzy;
    private String[] synonyms;
    private String[] genres;
    private boolean adult;
    private double average_score;
    private int popularity;
    private String image_url_sml;
    private String image_url_med;
    private String image_url_lge;
    private int updated_at;

    protected SeriesSmall(Parcel in) {
        id = in.readInt();
        series_type = in.readString();
        title_romaji = in.readString();
        title_english = in.readString();
        title_japanese = in.readString();
        type = in.readString();
        start_date_fuzzy = in.readInt();
        end_date_fuzzy = in.readInt();
        synonyms = in.createStringArray();
        genres = in.createStringArray();
        adult = in.readByte() != 0;
        average_score = in.readDouble();
        popularity = in.readInt();
        image_url_sml = in.readString();
        image_url_med = in.readString();
        image_url_lge = in.readString();
        updated_at = in.readInt();
    }

    public static final Creator<SeriesSmall> CREATOR = new Creator<SeriesSmall>() {
        @Override
        public SeriesSmall createFromParcel(Parcel in) {
            return new SeriesSmall(in);
        }

        @Override
        public SeriesSmall[] newArray(int size) {
            return new SeriesSmall[size];
        }
    };

    /**
     * Id of series
     */
    public int getId() {
        return id;
    }

    /**
     * anime or manga
     */
    public String getSeries_type() {
        return series_type;
    }

    /**
     * E.g. Kangoku Gakuen     *
     */
    public String getTitle_romaji() {
        return title_romaji;
    }

    /**
     * E.g. Prison School
     * When no English title is available, the romaji title will fill this value.
     */
    public String getTitle_english() {
        return title_english;
    }

    /**
     * Japanese Title
     * When no English title is available, the romaji title will fill this value.
     */
    public String getTitle_japanese() {
        return title_japanese;
    }

    /**
     * TV, OVA, MOVIE e.t.c
     */
    public String getType() {
        return type;
    }

    /**
     * UNIX time of start date
     */
    public int getStart_date_fuzzy() {
        return start_date_fuzzy;
    }

    /**
     * UNIX time of start date
     */
    public int getEnd_date_fuzzy() {
        return end_date_fuzzy;
    }

    /**
     * Alternative seasons_titles.
     * [“The Prison School”]
     */
    public String[] getSynonyms() {
        return synonyms;
    }

    /**
     * ["Horror", "Action"]
     */
    public String[] getGenres() {
        return genres;
    }

    /**
     * True for adult series (Hentai). This does not include ecchi.
     */
    public boolean isAdult() {
        return adult;
    }

    /**
     * E.g. 67.8
     * Score 0-100
     */
    public double getAverage_score() {
        return average_score;
    }

    /**
     * E.g. 15340
     * Number of users with series on their list.
     */
    public int getPopularity() {
        return popularity;
    }


    /**
     * Image url. 24x39* (Not available for manga)
     * Image size may vary.
     */
    public String getImage_url_sml() {
        return image_url_sml;
    }

    /**
     * Image url. 93x133*
     * Image size may vary.
     */
    public String getImage_url_med() {
        return image_url_med;
    }

    /**
     * Image url. 225x323*
     * Image size may vary.
     */
    public String getImage_url_lge() {
        return image_url_lge;
    }

    /**
     * Unix timestamp. Last time the series data was modified.
     */
    public int getUpdated_at() {
        return updated_at;
    }


    @Override
    public String toString() {
        return "SeriesSmall{" +
                "id=" + id +
                ", series_type='" + series_type + "\n" +
                ", title_romaji='" + title_romaji + "\n" +
                ", title_english='" + title_english + "\n" +
                ", title_japanese='" + title_japanese + "\n" +
                ", type='" + type + "\n" +
                ", start_date_fuzzy=" + start_date_fuzzy +
                ", end_date_fuzzy=" + end_date_fuzzy +
                ", synonyms=" + Arrays.toString(synonyms) +
                ", genres=" + Arrays.toString(genres) +
                ", adult=" + adult +
                ", average_score=" + average_score +
                ", popularity=" + popularity +
                ", image_url_sml='" + image_url_sml + "\n" +
                ", image_url_med='" + image_url_med + "\n" +
                ", image_url_lge='" + image_url_lge + "\n" +
                ", updated_at=" + updated_at +
                '}';
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
        parcel.writeStringArray(synonyms);
        parcel.writeStringArray(genres);
        parcel.writeByte((byte) (adult ? 1 : 0));
        parcel.writeDouble(average_score);
        parcel.writeInt(popularity);
        parcel.writeString(image_url_sml);
        parcel.writeString(image_url_med);
        parcel.writeString(image_url_lge);
        parcel.writeInt(updated_at);
    }
}
