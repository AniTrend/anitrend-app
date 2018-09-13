package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.model.entity.anilist.meta.CustomList;
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.group.RecyclerItem;
import com.mxt.anitrend.util.KeyUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by Maxwell on 1/12/2017.
 */
public class MediaList extends RecyclerItem implements Parcelable, Cloneable {

    private long id;
    private long mediaId;
    private @KeyUtil.MediaListStatus String status;
    private float score;
    private int progress;
    private int progressVolumes;
    private int repeat;
    private int priority;
    private String notes;
    @SerializedName("private")
    private boolean hidden;
    private boolean hiddenFromStatusLists;
    private Map<String, Float> advancedScores;
    private List<CustomList> customLists;
    private FuzzyDate startedAt;
    private FuzzyDate completedAt;
    private long updatedAt;
    private long createdAt;
    private MediaBase media;

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
        customLists = in.createTypedArrayList(CustomList.CREATOR);
        startedAt = in.readParcelable(FuzzyDate.class.getClassLoader());
        completedAt = in.readParcelable(FuzzyDate.class.getClassLoader());
        updatedAt = in.readLong();
        createdAt = in.readLong();
        media = in.readParcelable(MediaBase.class.getClassLoader());
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
        dest.writeTypedList(customLists);
        dest.writeParcelable(startedAt, flags);
        dest.writeParcelable(completedAt, flags);
        dest.writeLong(updatedAt);
        dest.writeLong(createdAt);
        dest.writeParcelable(media, flags);
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

    public @KeyUtil.MediaListStatus String getStatus() {
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

    public Map<String, Float> getAdvancedScores() {
        return advancedScores;
    }

    public List<CustomList> getCustomLists() {
        return customLists;
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

    public void setId(long id) {
        this.id = id;
    }

    public void setMediaId(long mediaId) {
        this.mediaId = mediaId;
    }

    public void setMedia(MediaBase media) {
        this.media = media;
    }

    public void setStatus(@KeyUtil.MediaListStatus String status) {
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

    public void setAdvancedScores(Map<String, Float> advancedScores) {
        this.advancedScores = advancedScores;
    }

    public void setCustomLists(List<CustomList> customLists) {
        this.customLists = customLists;
    }

    public void setStartedAt(FuzzyDate startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(FuzzyDate completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MediaList)
            return ((MediaList)obj).id == id && ((MediaList)obj).mediaId == mediaId;
        else if (obj instanceof MediaBase)
            return ((MediaBase)obj).getId() == mediaId;
        return super.equals(obj);
    }

    @Override
    public MediaList clone() throws CloneNotSupportedException {
        super.clone();
        return this;
    }
}
