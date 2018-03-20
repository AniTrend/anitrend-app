package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;

import com.mxt.anitrend.data.converter.UserStatsConverter;
import com.mxt.anitrend.data.converter.list.CustomListConverter;
import com.mxt.anitrend.data.converter.list.SeriesListTrackingConverter;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.general.MediaList;
import com.mxt.anitrend.model.entity.general.UserStats;

import java.util.List;
import java.util.Map;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;

/**
 * Created by Maxwell on 11/12/2016.
 */
@Entity
public class User extends UserBase {

    private int anime_time;
    private int manga_chap;
    private String about;
    private int list_order;
    private boolean adult_content;
    private boolean following;
    private String image_url_banner;
    private String title_language;
    private int score_type;
    @Convert(converter = CustomListConverter.class, dbType = String.class)
    private List<String> custom_list_anime;
    @Convert(converter = CustomListConverter.class, dbType = String.class)
    private List<String> custom_list_manga;
    @Convert(converter = UserStatsConverter.class, dbType = String.class)
    private UserStats stats;
    private boolean advanced_rating;
    @Convert(converter = CustomListConverter.class, dbType = String.class)
    private List<String> advanced_rating_names;
    private int notifications;
    @Convert(converter = SeriesListTrackingConverter.class, dbType = String.class)
    private Map<String, List<MediaList>> lists;

    public User() {

    }

    protected User(Parcel in) {
        super(in);
        anime_time = in.readInt();
        manga_chap = in.readInt();
        about = in.readString();
        list_order = in.readInt();
        adult_content = in.readByte() != 0;
        following = in.readByte() != 0;
        image_url_banner = in.readString();
        title_language = in.readString();
        score_type = in.readInt();
        custom_list_anime = in.createStringArrayList();
        custom_list_manga = in.createStringArrayList();
        stats = in.readParcelable(UserStats.class.getClassLoader());
        advanced_rating = in.readByte() != 0;
        advanced_rating_names = in.createStringArrayList();
        notifications = in.readInt();
        in.readMap(lists, Map.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public int getAnime_time() {
        return anime_time;
    }

    public long getManga_chap() {
        return manga_chap;
    }

    public String getAbout() {
        return about;
    }

    public int getList_order() {
        return list_order;
    }

    public Boolean getAdult_content() {
        return adult_content;
    }

    public Boolean getFollowing() {
        return following;
    }

    public void setFollowing() {
        this.following = !following;
    }

    public String getImage_url_banner() {
        return image_url_banner;
    }

    public String getTitle_language() {
        return title_language;
    }

    public int getScore_type() {
        return score_type;
    }

    public List<String> getCustom_list_anime() {
        return custom_list_anime;
    }

    public List<String> getCustom_list_manga() {
        return custom_list_manga;
    }

    public UserStats getStats() {
        return stats;
    }

    public Boolean getAdvanced_rating() {
        return advanced_rating;
    }

    public List<String> getAdvanced_rating_names() {
        return advanced_rating_names;
    }

    public int getNotifications() {
        return notifications;
    }

    public void setNotifications(int count) {
        notifications = count;
    }

    public Map<String, List<MediaList>> getLists() {
        return lists;
    }

    public void setAnime_time(int anime_time) {
        this.anime_time = anime_time;
    }

    public void setManga_chap(int manga_chap) {
        this.manga_chap = manga_chap;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setList_order(int list_order) {
        this.list_order = list_order;
    }

    public void setAdult_content(boolean adult_content) {
        this.adult_content = adult_content;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public void setImage_url_banner(String image_url_banner) {
        this.image_url_banner = image_url_banner;
    }

    public void setTitle_language(String title_language) {
        this.title_language = title_language;
    }

    public void setScore_type(int score_type) {
        this.score_type = score_type;
    }

    public void setCustom_list_anime(List<String> custom_list_anime) {
        this.custom_list_anime = custom_list_anime;
    }

    public void setCustom_list_manga(List<String> custom_list_manga) {
        this.custom_list_manga = custom_list_manga;
    }

    public void setStats(UserStats stats) {
        this.stats = stats;
    }

    public void setAdvanced_rating(boolean advanced_rating) {
        this.advanced_rating = advanced_rating;
    }

    public void setAdvanced_rating_names(List<String> advanced_rating_names) {
        this.advanced_rating_names = advanced_rating_names;
    }

    public void setLists(Map<String, List<MediaList>> lists) {
        this.lists = lists;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     * @see #CONTENTS_FILE_DESCRIPTOR
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(anime_time);
        dest.writeInt(manga_chap);
        dest.writeString(about);
        dest.writeInt(list_order);
        dest.writeByte((byte) (adult_content ? 1 : 0));
        dest.writeByte((byte) (following ? 1 : 0));
        dest.writeString(image_url_banner);
        dest.writeString(title_language);
        dest.writeInt(score_type);
        dest.writeStringList(custom_list_anime);
        dest.writeStringList(custom_list_manga);
        dest.writeParcelable(stats, flags);
        dest.writeByte((byte) (advanced_rating ? 1 : 0));
        dest.writeStringList(advanced_rating_names);
        dest.writeInt(notifications);
        dest.writeMap(lists);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
