package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;

import androidx.annotation.Nullable;

import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.NotificationBase;
import com.mxt.anitrend.model.entity.base.ThreadBase;
import com.mxt.anitrend.model.entity.base.UserBase;

import java.util.List;

/**
 * Created by max on 2018/03/25.
 * Notification
 */

public class Notification extends NotificationBase {

    // activity id
    private long activityId;
    private long commentId;

    // following or activity notification
    private UserBase user;

    // airing notification
    private int episode;
    private List<String> contexts;
    private MediaBase media;
    private ThreadBase thread;

    // Media data updates notifications
    private String reason;
    private String deletedMediaTitle;
    private List<String> deletedMediaTitles;

    protected Notification(Parcel in) {
        super(in);
        activityId = in.readLong();
        commentId = in.readLong();
        user = in.readParcelable(UserBase.class.getClassLoader());
        episode = in.readInt();
        contexts = in.createStringArrayList();
        media = in.readParcelable(MediaBase.class.getClassLoader());
        thread = in.readParcelable(ThreadBase.class.getClassLoader());
        reason = in.readString();
        deletedMediaTitle = in.readString();
        in.readList(deletedMediaTitles, String.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(activityId);
        dest.writeLong(commentId);
        dest.writeParcelable(user, flags);
        dest.writeInt(episode);
        dest.writeStringList(contexts);
        dest.writeParcelable(media, flags);
        dest.writeParcelable(thread, flags);
        dest.writeString(reason);
        dest.writeString(deletedMediaTitle);
        dest.writeStringList(deletedMediaTitles);
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }


    public UserBase getUser() {
        return user;
    }

    public int getEpisode() {
        return episode;
    }

    public List<String> getContexts() {
        return contexts;
    }

    @Nullable
    public MediaBase getMedia() {
        return media;
    }

    public long getActivityId() {
        return activityId;
    }

    public ThreadBase getThread() {
        return thread;
    }

    public long getCommentId() {
        return commentId;
    }

    public String getReason() {
        return reason;
    }

    public String getDeletedMediaTitle() {
        return deletedMediaTitle;
    }

    public List<String> getDeletedMediaTitles() {
        return deletedMediaTitles;
    }
}
