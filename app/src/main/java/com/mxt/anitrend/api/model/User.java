package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

import com.mxt.anitrend.api.structure.UserLists;
import com.mxt.anitrend.api.structure.UserStats;
import com.mxt.anitrend.util.MarkDown;
import com.mxt.anitrend.util.PatternMatcher;

import java.util.List;
import java.util.Locale;

/**
 * Created by Maxwell on 11/12/2016.
 */

public class User implements Parcelable {

    private int id;
    private String display_name;
    private int anime_time;
    private int manga_chap;
    private String about;
    private int list_order;
    private boolean adult_content;
    private boolean following;
    private String image_url_lge;
    private String image_url_med;
    private String image_url_banner;
    private String title_language;
    private int score_type;
    private List<String> custom_list_anime;
    private List<String> custom_list_manga;
    private UserStats stats;
    private boolean advanced_rating;
    private List<String> advanced_rating_names;
    private int notifications;
    private UserLists lists;

    protected User(Parcel in) {
        id = in.readInt();
        display_name = in.readString();
        anime_time = in.readInt();
        manga_chap = in.readInt();
        about = in.readString();
        list_order = in.readInt();
        adult_content = in.readByte() != 0;
        following = in.readByte() != 0;
        image_url_lge = in.readString();
        image_url_med = in.readString();
        image_url_banner = in.readString();
        title_language = in.readString();
        score_type = in.readInt();
        custom_list_anime = in.createStringArrayList();
        custom_list_manga = in.createStringArrayList();
        stats = in.readParcelable(UserStats.class.getClassLoader());
        advanced_rating = in.readByte() != 0;
        advanced_rating_names = in.createStringArrayList();
        notifications = in.readInt();
        lists = in.readParcelable(UserLists.class.getClassLoader());
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

    public int getId() {
        return id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getAnime_time() {
        float item_time = anime_time / 60;
        if(item_time > 60) {
            item_time /= 24;
            if(item_time > 365)
                return String.format(Locale.getDefault(), "%.1f Years", item_time/365);
            return String.format(Locale.getDefault(), "%.1f Days", item_time);
        }
        return String.format(Locale.getDefault(), "%.1f Hours", item_time);
    }

    public String getManga_chap() {
        if(manga_chap > 1000)
            return String.format(Locale.getDefault(), "%.1f K", (float)manga_chap/1000);
        return String.format(Locale.getDefault(), "%d", manga_chap);
    }

    public Spanned getAbout() {
        return MarkDown.convert(PatternMatcher.removeTags(about));
    }

    public String getAboutRaw() {
        if(about != null)
            return about;
        return "User Has No Description Yet!";
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

    public String getImage_url_lge() {
        return image_url_lge;
    }

    public String getImage_url_med() {
        return image_url_med;
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

    public UserLists getLists() {
        return lists;
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
        dest.writeInt(id);
        dest.writeString(display_name);
        dest.writeInt(anime_time);
        dest.writeInt(manga_chap);
        dest.writeString(about);
        dest.writeInt(list_order);
        dest.writeByte((byte) (adult_content ? 1 : 0));
        dest.writeByte((byte) (following ? 1 : 0));
        dest.writeString(image_url_lge);
        dest.writeString(image_url_med);
        dest.writeString(image_url_banner);
        dest.writeString(title_language);
        dest.writeInt(score_type);
        dest.writeStringList(custom_list_anime);
        dest.writeStringList(custom_list_manga);
        dest.writeParcelable(stats, flags);
        dest.writeByte((byte) (advanced_rating ? 1 : 0));
        dest.writeStringList(advanced_rating_names);
        dest.writeInt(notifications);
        dest.writeParcelable(lists, flags);
    }
}
