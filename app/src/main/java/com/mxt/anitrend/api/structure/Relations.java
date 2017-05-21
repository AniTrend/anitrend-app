package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by Maxwell on 10/4/2016.
 */
public class Relations implements Parcelable {

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
    private String image_url_sml;
    private String image_url_med;
    private String image_url_lge;
    private String image_url_banner;
    private long updated_at;
    private int total_chapters;
    private int total_episodes;
    private int total_volumes;
    private String airing_status;
    private String publishing_status;
    private String relation_type;
    private int link_id;

    protected Relations(Parcel in) {
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
        image_url_banner = in.readString();
        updated_at = in.readLong();
        total_chapters = in.readInt();
        total_episodes = in.readInt();
        total_volumes = in.readInt();
        airing_status = in.readString();
        publishing_status = in.readString();
        relation_type = in.readString();
        link_id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(series_type);
        dest.writeString(title_romaji);
        dest.writeString(title_english);
        dest.writeString(title_japanese);
        dest.writeString(type);
        dest.writeInt(start_date_fuzzy);
        dest.writeInt(end_date_fuzzy);
        dest.writeStringArray(synonyms);
        dest.writeStringArray(genres);
        dest.writeByte((byte) (adult ? 1 : 0));
        dest.writeDouble(average_score);
        dest.writeInt(popularity);
        dest.writeString(image_url_sml);
        dest.writeString(image_url_med);
        dest.writeString(image_url_lge);
        dest.writeString(image_url_banner);
        dest.writeLong(updated_at);
        dest.writeInt(total_chapters);
        dest.writeInt(total_episodes);
        dest.writeInt(total_volumes);
        dest.writeString(airing_status);
        dest.writeString(publishing_status);
        dest.writeString(relation_type);
        dest.writeInt(link_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Relations> CREATOR = new Creator<Relations>() {
        @Override
        public Relations createFromParcel(Parcel in) {
            return new Relations(in);
        }

        @Override
        public Relations[] newArray(int size) {
            return new Relations[size];
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

    public long getUpdated_at() {
        return updated_at;
    }

    public int getTotal_episodes() {
        return total_episodes;
    }

    public String getAiring_status() {
        return airing_status;
    }

    public String getRelation_type() {
        return relation_type.substring(0, 1).toUpperCase() + relation_type.substring(1);
    }

    public int getTotal_chapters() {
        return total_chapters;
    }

    public int getTotal_volumes() {
        return total_volumes;
    }

    public String getPublishing_status() {
        return publishing_status;
    }

    public int getLink_id() {
        return link_id;
    }

}
