package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

import com.mxt.anitrend.api.structure.Airing;
import com.mxt.anitrend.api.structure.ExternalLink;
import com.mxt.anitrend.api.structure.Relations;
import com.mxt.anitrend.api.structure.SeriesListStats;
import com.mxt.anitrend.api.structure.Tag;
import com.mxt.anitrend.util.MarkDown;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Maxwell on 10/2/2016.
 * This include Anime or Manga
 */
public class Series implements Parcelable {

    private int id;
    private String series_type;
    private String title_romaji;
    private String title_english;
    private String title_japanese;
    private String type;
    @Deprecated
    private String start_date;
    @Deprecated
    private String end_date;
    private int start_date_fuzzy;
    private int end_date_fuzzy;
    private Integer season;
    private String description;
    private String[] synonyms;
    private String[] genres;
    private boolean adult;
    private double average_score;
    private int popularity;
    private boolean favourite;
    private String image_url_sml;
    private String image_url_med;
    private String image_url_lge;
    private String image_url_banner;
    private long updated_at;
    private SeriesListStats list_stats;
    //private ScoreDistribution score_distribution; giving issues

    /*Manga Mini Model*/
    private int total_chapters;
    private int total_volumes;
    private String publishing_status;

    /* Anime Mini Model */
    private int total_episodes;
    private Integer duration;
    private String airing_status;
    private String youtube_id;
    private String hashtag;
    private String source;

    private List<CharacterSmall> characters;
    private List<StaffSmall> staff;
    private List<Rank> rankings;
    private List<StudioSmall> studio;
    private List<ExternalLink> external_links;
    private List<Relations> relations;
    private List<Relations> relations_manga;
    private List<Relations> relations_anime;
    private List<Tag> tags;
    private List<Review> reviews;
    private Airing airing;

    //private Object[] airing_stats; TODO: AniList has not provided model data on what should be placed in this object yet
    private String classification; //TODO: Not yet announced, periodically check the api for type of data that'll go into this data type

    private HashMap<Integer, Integer> score_distribution;


    protected Series(Parcel in) {
        id = in.readInt();
        series_type = in.readString();
        title_romaji = in.readString();
        title_english = in.readString();
        title_japanese = in.readString();
        type = in.readString();
        start_date = in.readString();
        end_date = in.readString();
        start_date_fuzzy = in.readInt();
        end_date_fuzzy = in.readInt();
        season = in.readInt();
        description = in.readString();
        synonyms = in.createStringArray();
        genres = in.createStringArray();
        adult = in.readByte() != 0;
        average_score = in.readDouble();
        popularity = in.readInt();
        favourite = in.readByte() != 0;
        image_url_sml = in.readString();
        image_url_med = in.readString();
        image_url_lge = in.readString();
        image_url_banner = in.readString();
        updated_at = in.readLong();
        list_stats = in.readParcelable(SeriesListStats.class.getClassLoader());
        total_chapters = in.readInt();
        total_volumes = in.readInt();
        publishing_status = in.readString();
        total_episodes = in.readInt();
        duration = in.readInt();
        airing_status = in.readString();
        youtube_id = in.readString();
        hashtag = in.readString();
        source = in.readString();
        characters = in.createTypedArrayList(CharacterSmall.CREATOR);
        staff = in.createTypedArrayList(StaffSmall.CREATOR);
        rankings = in.createTypedArrayList(Rank.CREATOR);
        studio = in.createTypedArrayList(StudioSmall.CREATOR);
        external_links = in.createTypedArrayList(ExternalLink.CREATOR);
        relations = in.createTypedArrayList(Relations.CREATOR);
        relations_manga = in.createTypedArrayList(Relations.CREATOR);
        relations_anime = in.createTypedArrayList(Relations.CREATOR);
        tags = in.createTypedArrayList(Tag.CREATOR);
        reviews = in.createTypedArrayList(Review.CREATOR);
        airing = in.readParcelable(Airing.class.getClassLoader());
        classification = in.readString();
        score_distribution = (HashMap<Integer, Integer>) in.readSerializable();
    }

    public static final Creator<Series> CREATOR = new Creator<Series>() {
        @Override
        public Series createFromParcel(Parcel in) {
            return new Series(in);
        }

        @Override
        public Series[] newArray(int size) {
            return new Series[size];
        }
    };


    public List<Relations> getRelations_anime() {
        return relations_anime;
    }

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

    /**Use fuzzy date*/
    @Deprecated
    public String getStart_date() {
        return start_date;
    }

    /**Use fuzzy date*/
    @Deprecated
    public String getEnd_date() {
        return end_date;
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
     * E.g. 164
     * First 2 numbers are the year (16 is 2016). Last number is the season starting at 1 (3 is Summer).     *
     */
    public Integer getSeason() {
        return season;
    }

    /**
     * Description of series.
     */
    public Spanned getDescription() {
        return MarkDown.convert(description);
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
     * If the current authenticated user has favorited the series. False if not authenticated.
     */
    public boolean isFavourite() {
        return favourite;
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
     * Image url. 1720x390*
     * Image size may vary.
     */
    public String getImage_url_banner() {
        return image_url_banner;
    }

    /**
     * Unix timestamp. Last time the series data was modified.
     */
    public long getUpdated_at() {
        return updated_at;
    }

    /**Number of episodes in series season. ex: (0 if unknown)	In Small Model: Yes*/
    public int getTotal_episodes() {
        return total_episodes;
    }

    /**duration	int|null	ex: 24	Minuets in the average anime episode.*/
    public int getDuration() {
        return duration == null? 0:duration;
    }

    /**airing_status	string|null	(See anime status types below)	Current airing status of the anime.	In Small Model: Yes*/
    public String getAiring_status() {
        return airing_status;
    }

    /**youtube_id	string|null	JIKFtTMvNSg	Youtube video id*/
    public String getYoutube_id() {
        return youtube_id;
    }

    public String getHashtag() {
        return hashtag;
    }

    /**source	string|nulll	(See anime source types below)	The source adaption media type*/
    public String getSource() {
        if(source != null)
            return source;
        return "N/A";
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

    /**ration between 10 - 100 and it's rating values*/
    public HashMap<Integer, Integer> getScore_distribution() {
        return score_distribution;
    }

    public SeriesListStats getList_stats() {
        return list_stats;
    }

    public List<CharacterSmall> getCharacters() {
        return characters;
    }

    public List<StaffSmall> getStaff() {
        return staff;
    }

    public List<Rank> getRankings() {
        return rankings;
    }

    public List<StudioSmall> getStudio() {
        return studio;
    }

    public List<ExternalLink> getExternal_links() {
        return external_links;
    }

    public List<Relations> getRelations() {
        return relations;
    }

    public List<Relations> getRelations_manga() {
        return relations_manga;
    }

    public List<Tag> getTags() {
        return tags;
    }

    /**
     * Airing data about the anime, only available on anime still airing
     */
    public Airing getNextAiring() {
        return airing;
    }

    /**
     * Returns something similar to R - 17+ (violence & profanity)
     *
     * @return String of classification data
     */
    public String getClassification() {
        return classification;
    }

    public List<Review> getReviews() {
        return reviews;
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
        parcel.writeString(start_date);
        parcel.writeString(end_date);
        parcel.writeInt(start_date_fuzzy);
        parcel.writeInt(end_date_fuzzy);
        parcel.writeInt(season==null?0:season);
        parcel.writeString(description);
        parcel.writeStringArray(synonyms);
        parcel.writeStringArray(genres);
        parcel.writeByte((byte) (adult ? 1 : 0));
        parcel.writeDouble(average_score);
        parcel.writeInt(popularity);
        parcel.writeByte((byte) (favourite ? 1 : 0));
        parcel.writeString(image_url_sml);
        parcel.writeString(image_url_med);
        parcel.writeString(image_url_lge);
        parcel.writeString(image_url_banner);
        parcel.writeLong(updated_at);
        parcel.writeParcelable(list_stats, i);
        parcel.writeInt(total_chapters);
        parcel.writeInt(total_volumes);
        parcel.writeString(publishing_status);
        parcel.writeInt(total_episodes);
        parcel.writeInt(duration==null?0:duration);
        parcel.writeString(airing_status);
        parcel.writeString(youtube_id);
        parcel.writeString(hashtag);
        parcel.writeString(source);
        parcel.writeTypedList(characters);
        parcel.writeTypedList(staff);
        parcel.writeTypedList(rankings);
        parcel.writeTypedList(studio);
        parcel.writeTypedList(external_links);
        parcel.writeTypedList(relations);
        parcel.writeTypedList(relations_manga);
        parcel.writeTypedList(relations_anime);
        parcel.writeTypedList(tags);
        parcel.writeTypedList(reviews);
        parcel.writeParcelable(airing, i);
        parcel.writeString(classification);
        parcel.writeSerializable(score_distribution);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Series) {
            Series series = (Series) obj;
            return series.getId() == this.id;
        }
        return super.equals(obj);
    }
}

