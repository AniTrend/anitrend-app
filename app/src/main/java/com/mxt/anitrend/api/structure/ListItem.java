package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.utils.DateTimeConverter;

import java.util.List;

/**
 * Created by Maxwell on 1/12/2017.
 */

public class ListItem implements Parcelable {

    private int record_id;
    private int series_id;
    private String list_status;
    private String score;
    private int score_raw;
    private int episodes_watched;
    private int chapters_read;
    private int volumes_read;
    private int rewatched;
    private int reread;
    private int priority;
    @SerializedName("private")
    private int Private;
    private int hidden_default;
    private String notes;
    /**
     * TODO: Find out what these other types are
     * private [] advanced_rating_scores
     */
    private List<String> custom_lists;
    private String started_on;
    private String finished_on;
    private String added_time;
    private String updated_time;
    private Anime anime;
    private Manga manga;

    protected ListItem(Parcel in) {
        record_id = in.readInt();
        series_id = in.readInt();
        list_status = in.readString();
        score = in.readString();
        score_raw = in.readInt();
        episodes_watched = in.readInt();
        chapters_read = in.readInt();
        volumes_read = in.readInt();
        rewatched = in.readInt();
        reread = in.readInt();
        priority = in.readInt();
        Private = in.readInt();
        hidden_default = in.readInt();
        notes = in.readString();
        custom_lists = in.createStringArrayList();
        started_on = in.readString();
        finished_on = in.readString();
        added_time = in.readString();
        updated_time = in.readString();
        anime = in.readParcelable(Anime.class.getClassLoader());
        manga = in.readParcelable(Manga.class.getClassLoader());
    }

    public static final Creator<ListItem> CREATOR = new Creator<ListItem>() {
        @Override
        public ListItem createFromParcel(Parcel in) {
            return new ListItem(in);
        }

        @Override
        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };

    public int getRecord_id() {
        return record_id;
    }

    public int getSeries_id() {
        return series_id;
    }

    public String getList_status() {
        return list_status;
    }

    public String getScore() {
        return score;
    }

    public int getScore_raw() {
        return score_raw;
    }

    public int getEpisodes_watched() {
        return episodes_watched;
    }

    public int getChapters_read() {
        return chapters_read;
    }

    public int getVolumes_read() {
        return volumes_read;
    }

    public int getRewatched() {
        return rewatched;
    }

    public int getReread() {
        return reread;
    }

    public int getPriority() {
        return priority;
    }

    public Boolean isPrivate() {
        return Private == 1;
    }

    public int getHidden_default() {
        return hidden_default;
    }

    public String getNotes() {
        if(notes == null)
            return "";
        return notes;
    }

    public List<String> getCustom_lists() {
        return custom_lists;
    }

    public String getStarted_on() {
        return started_on == null? "TBA": DateTimeConverter.convertDateString(started_on);
    }

    public String getFinished_on() {
        return finished_on == null? "TBA": DateTimeConverter.convertDateString(finished_on);
    }

    public String getAdded_time() {
        return added_time == null? "N/A": DateTimeConverter.convertDateString(added_time);
    }

    public String getUpdated_time() {
        return updated_time == null? "N/A": DateTimeConverter.convertDateString(updated_time);
    }

    public Anime getAnime() {
        return anime;
    }

    public Manga getManga() {
        return manga;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(record_id);
        parcel.writeInt(series_id);
        parcel.writeString(list_status);
        parcel.writeString(score);
        parcel.writeInt(score_raw);
        parcel.writeInt(episodes_watched);
        parcel.writeInt(chapters_read);
        parcel.writeInt(volumes_read);
        parcel.writeInt(rewatched);
        parcel.writeInt(reread);
        parcel.writeInt(priority);
        parcel.writeInt(Private);
        parcel.writeInt(hidden_default);
        parcel.writeString(notes);
        parcel.writeStringList(custom_lists);
        parcel.writeString(started_on);
        parcel.writeString(finished_on);
        parcel.writeString(added_time);
        parcel.writeString(updated_time);
        parcel.writeParcelable(anime, i);
        parcel.writeParcelable(manga, i);
    }
}
