package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.api.structure.Airing;
import com.mxt.anitrend.util.DateTimeConverter;

import java.util.Arrays;
import java.util.List;

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
    private List<StaffSmall> actors;
    private List<CharacterSmall> characters;

    private int total_episodes;
    private Integer duration;
    private String airing_status;
    private String youtube_id;
    private String hashtag;
    private String source;
    private Airing airing;

    private int total_chapters;
    private int total_volumes;
    private String publishing_status;

    protected SeriesSmall(Parcel in) {
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
        popularity = in.readInt();
        role = in.readString();
        image_url_sml = in.readString();
        image_url_med = in.readString();
        image_url_lge = in.readString();
        image_url_banner = in.readString();
        updated_at = in.readInt();
        actors = in.createTypedArrayList(StaffSmall.CREATOR);
        characters = in.createTypedArrayList(CharacterSmall.CREATOR);
        total_episodes = in.readInt();
        duration = in.readInt();
        airing_status = in.readString();
        youtube_id = in.readString();
        hashtag = in.readString();
        source = in.readString();
        airing = in.readParcelable(Airing.class.getClassLoader());
        total_chapters = in.readInt();
        total_volumes = in.readInt();
        publishing_status = in.readString();
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
        dest.writeInt(season==null?0:season);
        dest.writeStringArray(synonyms);
        dest.writeStringArray(genres);
        dest.writeByte((byte) (adult ? 1 : 0));
        dest.writeDouble(average_score);
        dest.writeInt(popularity);
        dest.writeString(role);
        dest.writeString(image_url_sml);
        dest.writeString(image_url_med);
        dest.writeString(image_url_lge);
        dest.writeString(image_url_banner);
        dest.writeInt(updated_at);
        dest.writeTypedList(actors);
        dest.writeTypedList(characters);
        dest.writeInt(total_episodes);
        dest.writeInt(duration==null?0:duration );
        dest.writeString(airing_status);
        dest.writeString(youtube_id);
        dest.writeString(hashtag);
        dest.writeString(source);
        dest.writeParcelable(airing, flags);
        dest.writeInt(total_chapters);
        dest.writeInt(total_volumes);
        dest.writeString(publishing_status);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getStart_date_fuzzy() {
        if(start_date_fuzzy != 0)
            return DateTimeConverter.convertDate(start_date_fuzzy);
        return "TBA";
    }

    public String getEnd_date_fuzzy() {
        if(end_date_fuzzy != 0)
            return DateTimeConverter.convertDate(end_date_fuzzy);
        return "TBA";
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

    /**Number of episodes in series season. ex: (0 if unknown)	In Small Model: Yes*/
    public int getTotal_episodes() {
        return total_episodes;
    }

    /**duration	int|null	ex: 24	Minuets in the average anime episode.*/
    public Integer getDuration() {
        return duration;
    }

    /**airing_status	string|null	(See anime status types below)	Current airing status of the anime.	In Small Model: Yes*/
    public String getAiring_status() {
        return airing_status;
    }

    /**youtube_id	string|null	JIKFtTMvNSg	Youtube video id*/
    public String getYoutube_id() {
        return youtube_id;
    }

    /**youtube_id	string|null	JIKFtTMvNSg	Youtube video id*/
    public String getHashtag() {
        return hashtag;
    }

    /**source	string|nulll	(See anime source types below)	The source adaption media type*/
    public String getSource() {
        return source;
    }

    public List<StaffSmall> getActors() {
        return actors;
    }

    public Airing getAiring() {
        return airing;
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

    public List<CharacterSmall> getCharacters() {
        return characters;
    }
}
