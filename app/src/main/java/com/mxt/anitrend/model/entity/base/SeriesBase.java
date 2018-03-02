package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.data.converter.list.CustomListConverter;
import com.mxt.anitrend.data.converter.SeriesAiringConverter;
import com.mxt.anitrend.model.entity.general.Airing;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.util.DateUtil;

import java.util.List;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

/**
 * Created by Maxwell on 10/3/2016.
 */
@Entity
public class SeriesBase extends EntityGroup implements Parcelable {

    @Id(assignable = true)
    private long id;
    private String series_type;
    private String title_romaji;
    private String title_english;
    private String title_japanese;
    private String type;
    private int start_date_fuzzy;
    private int end_date_fuzzy;
    private int season;
    @Convert(converter = CustomListConverter.class, dbType = String.class)
    private List<String> synonyms;
    @Convert(converter = CustomListConverter.class, dbType = String.class)
    private List<String> genres;
    private boolean adult;
    private double average_score;
    private int popularity;
    private String role;
    private String image_url_sml;
    private String image_url_med;
    private String image_url_lge;
    private String image_url_banner;
    private long updated_at;
    @Transient
    private List<StaffBase> actors;
    @Transient
    private List<CharacterBase> characters;
    private int total_episodes;
    private int duration;
    private String airing_status;
    private String youtube_id;
    private String hashtag;
    private String source;
    @Convert(converter = SeriesAiringConverter.class, dbType = String.class)
    private Airing airing;

    private int total_chapters;
    private int total_volumes;
    private String publishing_status;


    protected SeriesBase(Parcel in) {
        id = in.readLong();
        series_type = in.readString();
        title_romaji = in.readString();
        title_english = in.readString();
        title_japanese = in.readString();
        type = in.readString();
        start_date_fuzzy = in.readInt();
        end_date_fuzzy = in.readInt();
        season = in.readInt();
        synonyms = in.createStringArrayList();
        genres = in.createStringArrayList();
        adult = in.readByte() != 0;
        average_score = in.readDouble();
        popularity = in.readInt();
        role = in.readString();
        image_url_sml = in.readString();
        image_url_med = in.readString();
        image_url_lge = in.readString();
        image_url_banner = in.readString();
        updated_at = in.readLong();
        actors = in.createTypedArrayList(StaffBase.CREATOR);
        characters = in.createTypedArrayList(CharacterBase.CREATOR);
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
        dest.writeLong(id);
        dest.writeString(series_type);
        dest.writeString(title_romaji);
        dest.writeString(title_english);
        dest.writeString(title_japanese);
        dest.writeString(type);
        dest.writeInt(start_date_fuzzy);
        dest.writeInt(end_date_fuzzy);
        dest.writeInt(season);
        dest.writeStringList(synonyms);
        dest.writeStringList(genres);
        dest.writeByte((byte) (adult ? 1 : 0));
        dest.writeDouble(average_score);
        dest.writeInt(popularity);
        dest.writeString(role);
        dest.writeString(image_url_sml);
        dest.writeString(image_url_med);
        dest.writeString(image_url_lge);
        dest.writeString(image_url_banner);
        dest.writeLong(updated_at);
        dest.writeTypedList(actors);
        dest.writeTypedList(characters);
        dest.writeInt(total_episodes);
        dest.writeInt(duration);
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

    public static final Creator<SeriesBase> CREATOR = new Creator<SeriesBase>() {
        @Override
        public SeriesBase createFromParcel(Parcel in) {
            return new SeriesBase(in);
        }

        @Override
        public SeriesBase[] newArray(int size) {
            return new SeriesBase[size];
        }
    };

    public long getId() {
        return id;
    }

    /**
     * anime or manga
     */
    public String getSeries_type() {
        return series_type;
    }

    /**
     * E.g. Kangoku Gakuen
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
     * KeyUtils -> AnimeMediaTypes or MangaMediaTypes
     */
    public String getType() {
        return type;
    }

    public String getStart_date_fuzzy_formatted() {
        return DateUtil.convertDate(start_date_fuzzy);
    }

    public String getEnd_date_fuzzy_formatted() {
        return DateUtil.convertDate(end_date_fuzzy);
    }

    /**
     * UNIX time of start date
     */
    public int getStart_date_fuzzy() {
        return start_date_fuzzy;
    }

    /**
     * UNIX time of end date
     */
    public int getEnd_date_fuzzy() {
        return end_date_fuzzy;
    }

    /**
     * E.g. 164
     * First 2 numbers are the year (16 is 2016). Last number is the season starting at 1 (3 is Summer).
     */
    public int getSeason() {
        return season;
    }

    /**
     * Alternative seasons_titles.
     * [“The Prison School”]
     */
    public List<String> getSynonyms() {
        return synonyms;
    }

    /**
     * ["Horror", "Action"]
     */
    public List<String> getGenres() {
        return genres;
    }

    /**
     * True for adult series (Hentai). This does not include Ecchi.
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

    /**
     * Duration of episodes e.g. 24 Minuets in the average anime episode.
     * */
    public int getDuration() {
        return duration;
    }

    /**
     * Current airing status of the series
     * */
    public String getAiring_status() {
        return airing_status;
    }

    /**
     * e.g JIKFtTMvNSg Youtube video id
     * */
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

    public List<StaffBase> getActors() {
        return actors;
    }

    public String getFirstActorFullName() {
        if(actors != null && !actors.isEmpty())
            return actors.get(0).getFullName();
        return null;
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

    /**
     * Current publishing status of the series
     * */
    public String getPublishing_status() {
        return publishing_status;
    }

    public List<CharacterBase> getCharacters() {
        return characters;
    }

    public CharacterBase getFirstCharacter() {
        if(characters != null && !characters.isEmpty())
            return characters.get(0);
        return null;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSeries_type(String series_type) {
        this.series_type = series_type;
    }

    public void setTitle_romaji(String title_romaji) {
        this.title_romaji = title_romaji;
    }

    public void setTitle_english(String title_english) {
        this.title_english = title_english;
    }

    public void setTitle_japanese(String title_japanese) {
        this.title_japanese = title_japanese;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStart_date_fuzzy(int start_date_fuzzy) {
        this.start_date_fuzzy = start_date_fuzzy;
    }

    public void setEnd_date_fuzzy(int end_date_fuzzy) {
        this.end_date_fuzzy = end_date_fuzzy;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public void setAverage_score(double average_score) {
        this.average_score = average_score;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setImage_url_sml(String image_url_sml) {
        this.image_url_sml = image_url_sml;
    }

    public void setImage_url_med(String image_url_med) {
        this.image_url_med = image_url_med;
    }

    public void setImage_url_lge(String image_url_lge) {
        this.image_url_lge = image_url_lge;
    }

    public void setImage_url_banner(String image_url_banner) {
        this.image_url_banner = image_url_banner;
    }

    public void setUpdated_at(int updated_at) {
        this.updated_at = updated_at;
    }

    public void setActors(List<StaffBase> actors) {
        this.actors = actors;
    }

    public void setCharacters(List<CharacterBase> characters) {
        this.characters = characters;
    }

    public void setTotal_episodes(int total_episodes) {
        this.total_episodes = total_episodes;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setAiring_status(String airing_status) {
        this.airing_status = airing_status;
    }

    public void setYoutube_id(String youtube_id) {
        this.youtube_id = youtube_id;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setAiring(Airing airing) {
        this.airing = airing;
    }

    public void setTotal_chapters(int total_chapters) {
        this.total_chapters = total_chapters;
    }

    public void setTotal_volumes(int total_volumes) {
        this.total_volumes = total_volumes;
    }

    public void setPublishing_status(String publishing_status) {
        this.publishing_status = publishing_status;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SeriesBase)
            return ((SeriesBase)obj).getId() == getId();
        return super.equals(obj);
    }
}
