package com.mxt.anitrend.model.entity.general;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.data.converter.SeriesBaseConverter;
import com.mxt.anitrend.data.converter.list.CustomListConverter;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.util.DateUtil;

import java.util.List;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

/**
 * Created by Maxwell on 1/12/2017.
 */

@Entity
public class MediaList extends EntityGroup implements Parcelable {

    @Id(assignable = true)
    private long record_id;
    @Index
    private long series_id;
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
    @Convert(converter = CustomListConverter.class, dbType = String.class)
    private List<String> custom_lists;
    private String started_on;
    private String finished_on;
    private String added_time;
    private String updated_time;

    @Convert(converter = SeriesBaseConverter.class, dbType = String.class)
    private MediaBase anime;
    @Convert(converter = SeriesBaseConverter.class, dbType = String.class)
    private MediaBase manga;

    public MediaList() {

    }

    protected MediaList(Parcel in) {
        record_id = in.readLong();
        series_id = in.readLong();
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
        anime = in.readParcelable(MediaBase.class.getClassLoader());
        manga = in.readParcelable(MediaBase.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(record_id);
        dest.writeLong(series_id);
        dest.writeString(list_status);
        dest.writeString(score);
        dest.writeInt(score_raw);
        dest.writeInt(episodes_watched);
        dest.writeInt(chapters_read);
        dest.writeInt(volumes_read);
        dest.writeInt(rewatched);
        dest.writeInt(reread);
        dest.writeInt(priority);
        dest.writeInt(Private);
        dest.writeInt(hidden_default);
        dest.writeString(notes);
        dest.writeStringList(custom_lists);
        dest.writeString(started_on);
        dest.writeString(finished_on);
        dest.writeString(added_time);
        dest.writeString(updated_time);
        dest.writeParcelable(anime, flags);
        dest.writeParcelable(manga, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaList> CREATOR = new Creator<MediaList>() {
        @Override
        public MediaList createFromParcel(Parcel in) {
            return new MediaList(in);
        }

        @Override
        public MediaList[] newArray(int size) {
            return new MediaList[size];
        }
    };

    public long getRecord_id() {
        return record_id;
    }

    public long getSeries_id() {
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

    public boolean isPrivate() {
        return Private == 1;
    }

    public int getPrivate() {
        return Private;
    }

    public int getHidden_default() {
        return hidden_default;
    }

    public String getNotes() {
        return notes;
    }

    public List<String> getCustom_lists() {
        return custom_lists;
    }

    public String getStarted_on_pretty() {
        return DateUtil.convertDateString(started_on);
    }

    public String getFinished_on_pretty() {
        return DateUtil.convertDateString(finished_on);
    }

    public String getAdded_time_pretty() {
        return DateUtil.convertDateString(added_time);
    }

    public String getUpdated_time_pretty() {
        return DateUtil.convertDateString(updated_time);
    }

    public String getStarted_on() {
        return started_on;
    }

    public String getFinished_on() {
        return finished_on;
    }

    public String getAdded_time() {
        return added_time;
    }

    public String getUpdated_time() {
        return updated_time;
    }

    public MediaBase getAnime() {
        return anime;
    }

    public MediaBase getManga() {
        return manga;
    }

    public void setRecord_id(long record_id) {
        this.record_id = record_id;
    }

    public void setSeries_id(long series_id) {
        this.series_id = series_id;
    }

    public void setList_status(String list_status) {
        this.list_status = list_status;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public void setScore_raw(int score_raw) {
        this.score_raw = score_raw;
    }

    public void setEpisodes_watched(int episodes_watched) {
        this.episodes_watched = episodes_watched;
    }

    public void setChapters_read(int chapters_read) {
        this.chapters_read = chapters_read;
    }

    public void setVolumes_read(int volumes_read) {
        this.volumes_read = volumes_read;
    }

    public void setRewatched(int rewatched) {
        this.rewatched = rewatched;
    }

    public void setReread(int reread) {
        this.reread = reread;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setPrivate(int aPrivate) {
        Private = aPrivate;
    }

    public void setHidden_default(int hidden_default) {
        this.hidden_default = hidden_default;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCustom_lists(List<String> custom_lists) {
        this.custom_lists = custom_lists;
    }

    public void setStarted_on(String started_on) {
        this.started_on = started_on;
    }

    public void setFinished_on(String finished_on) {
        this.finished_on = finished_on;
    }

    public void setAdded_time(String added_time) {
        this.added_time = added_time;
    }

    public void setUpdated_time(String updated_time) {
        this.updated_time = updated_time;
    }

    public void setAnime(MediaBase anime) {
        this.anime = anime;
    }

    public void setManga(MediaBase manga) {
        this.manga = manga;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MediaList)
            return ((MediaList)obj).record_id == record_id && ((MediaList)obj).series_id == series_id;
        else if (obj instanceof Media)
            return ((Media)obj).getId() == series_id;
        else if (obj instanceof MediaBase)
            return ((MediaBase)obj).getId() == series_id;
        return super.equals(obj);
    }
}
