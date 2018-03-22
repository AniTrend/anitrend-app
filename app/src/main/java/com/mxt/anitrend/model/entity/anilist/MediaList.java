package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.data.converter.FuzzyDateConverter;
import com.mxt.anitrend.data.converter.SeriesBaseConverter;
import com.mxt.anitrend.data.converter.StringListConverter;
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.util.KeyUtils;

import java.util.List;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Transient;

/**
 * Created by Maxwell on 1/12/2017.
 */

@Entity
public class MediaList extends EntityGroup implements Parcelable {

    @Id(assignable = true)
    private long id;
    @Index
    private long mediaId;
    private @KeyUtils.MediaListStatus String status;
    private float score;
    private int progress;
    private int progressVolumes;
    private int repeat;
    private int priority;
    private String notes;
    @SerializedName("private")
    private boolean hidden;
    private boolean hiddenFromStatusLists;
    private List<Float> advancedScores;
    @Convert(converter = StringListConverter.class, dbType = String.class)
    private List<String> customLists;
    @Convert(converter = FuzzyDateConverter.class, dbType = String.class)
    private FuzzyDate startedAt;
    @Convert(converter = FuzzyDateConverter.class, dbType = String.class)
    private FuzzyDate completedAt;
    private long updatedAt;
    private long createdAt;
    @Convert(converter = SeriesBaseConverter.class, dbType = String.class)
    private MediaBase media;
    @Transient
    private UserBase user;

    public MediaList() {

    }

    protected MediaList(Parcel in) {
        id = in.readLong();
        mediaId = in.readLong();
        status = in.readString();
        score = in.readFloat();
        progress = in.readInt();
        progressVolumes = in.readInt();
        repeat = in.readInt();
        priority = in.readInt();
        notes = in.readString();
        hidden = in.readByte() != 0;
        hiddenFromStatusLists = in.readByte() != 0;
        customLists = in.createStringArrayList();
        startedAt = in.readParcelable(FuzzyDate.class.getClassLoader());
        completedAt = in.readParcelable(FuzzyDate.class.getClassLoader());
        updatedAt = in.readLong();
        createdAt = in.readLong();
        media = in.readParcelable(MediaBase.class.getClassLoader());
        user = in.readParcelable(UserBase.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(mediaId);
        dest.writeString(status);
        dest.writeFloat(score);
        dest.writeInt(progress);
        dest.writeInt(progressVolumes);
        dest.writeInt(repeat);
        dest.writeInt(priority);
        dest.writeString(notes);
        dest.writeByte((byte) (hidden ? 1 : 0));
        dest.writeByte((byte) (hiddenFromStatusLists ? 1 : 0));
        dest.writeStringList(customLists);
        dest.writeParcelable(startedAt, flags);
        dest.writeParcelable(completedAt, flags);
        dest.writeLong(updatedAt);
        dest.writeLong(createdAt);
        dest.writeParcelable(media, flags);
        dest.writeParcelable(user, flags);
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

    public long getId() {
        return id;
    }

    public long getMediaId() {
        return mediaId;
    }

    public @KeyUtils.MediaListStatus String getStatus() {
        return status;
    }

    public float getScore() {
        return score;
    }

    public int getProgress() {
        return progress;
    }

    public int getProgressVolumes() {
        return progressVolumes;
    }

    public int getRepeat() {
        return repeat;
    }

    public int getPriority() {
        return priority;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isHiddenFromStatusLists() {
        return hiddenFromStatusLists;
    }

    public List<String> getCustomLists() {
        return customLists;
    }

    public List<Float> getAdvancedScores() {
        return advancedScores;
    }

    public FuzzyDate getStartedAt() {
        return startedAt;
    }

    public FuzzyDate getCompletedAt() {
        return completedAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public MediaBase getMedia() {
        return media;
    }

    public UserBase getUser() {
        return user;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setMediaId(long mediaId) {
        this.mediaId = mediaId;
    }

    public void setStatus(@KeyUtils.MediaListStatus String status) {
        this.status = status;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setProgressVolumes(int progressVolumes) {
        this.progressVolumes = progressVolumes;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setHiddenFromStatusLists(boolean hiddenFromStatusLists) {
        this.hiddenFromStatusLists = hiddenFromStatusLists;
    }

    public void setAdvancedScores(List<Float> advancedScores) {
        this.advancedScores = advancedScores;
    }

    public void setCustomLists(List<String> customLists) {
        this.customLists = customLists;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MediaList)
            return ((MediaList)obj).id == id && ((MediaList)obj).mediaId == mediaId;
        else if (obj instanceof Media)
            return ((Media)obj).getId() == mediaId;
        else if (obj instanceof MediaBase)
            return ((MediaBase)obj).getId() == mediaId;
        return super.equals(obj);
    }
}
