package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.api.model.StaffSmall;
import com.mxt.anitrend.util.DateTimeConverter;

import java.util.List;

/**
 * Created by Maxwell on 10/2/2016.
 *
 */
public class Anime implements Parcelable {

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

    private int total_episodes;
    private Integer duration;
    private String airing_status;
    private String youtube_id;
    private String hashtag;
    private String source;
    private List<StaffSmall> actors;
    private Airing airing;

    protected Anime(Parcel in) {
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
        total_episodes = in.readInt();
        duration = in.readInt();
        airing_status = in.readString();
        youtube_id = in.readString();
        hashtag = in.readString();
        source = in.readString();
        actors = in.createTypedArrayList(StaffSmall.CREATOR);
        airing = in.readParcelable(Airing.class.getClassLoader());
    }

    public static final Creator<Anime> CREATOR = new Creator<Anime>() {
        @Override
        public Anime createFromParcel(Parcel in) {
            return new Anime(in);
        }

        @Override
        public Anime[] newArray(int size) {
            return new Anime[size];
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
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
        parcel.writeInt(total_episodes);
        parcel.writeInt(duration==null?0:duration );
        parcel.writeString(airing_status);
        parcel.writeString(youtube_id);
        parcel.writeString(hashtag);
        parcel.writeString(source);
        parcel.writeTypedList(actors);
        parcel.writeParcelable(airing, flags);
    }

}
