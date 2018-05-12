package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.util.KeyUtil;

import java.util.List;

/**
 * Created by Maxwell on 11/12/2016.
 */
public class FeedList implements Parcelable {

    private long id;
    private int replyCount;
    private @KeyUtil.FeedType String type;
    private String status;
    @SerializedName(value = "text", alternate = {"message", "progress"})
    private String text;
    private long createdAt;
    private UserBase user;
    private MediaBase media;
    private UserBase messenger;
    private UserBase recipient;
    private List<UserBase> likes;
    private List<FeedReply> replies;
    private String siteUrl;


    protected FeedList(Parcel in) {
        id = in.readLong();
        replyCount = in.readInt();
        type = in.readString();
        status = in.readString();
        text = in.readString();
        createdAt = in.readLong();
        user = in.readParcelable(UserBase.class.getClassLoader());
        media = in.readParcelable(MediaBase.class.getClassLoader());
        messenger = in.readParcelable(UserBase.class.getClassLoader());
        recipient = in.readParcelable(UserBase.class.getClassLoader());
        likes = in.createTypedArrayList(UserBase.CREATOR);
        siteUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(replyCount);
        dest.writeString(type);
        dest.writeString(status);
        dest.writeString(text);
        dest.writeLong(createdAt);
        dest.writeParcelable(user, flags);
        dest.writeParcelable(media, flags);
        dest.writeParcelable(messenger, flags);
        dest.writeParcelable(recipient, flags);
        dest.writeTypedList(likes);
        dest.writeString(siteUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FeedList> CREATOR = new Creator<FeedList>() {
        @Override
        public FeedList createFromParcel(Parcel in) {
            return new FeedList(in);
        }

        @Override
        public FeedList[] newArray(int size) {
            return new FeedList[size];
        }
    };

    public long getId() {
        return id;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public @KeyUtil.FeedType String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getText() {
        return text;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public UserBase getUser() {
        return user;
    }

    public MediaBase getMedia() {
        return media;
    }

    public UserBase getMessenger() {
        return messenger;
    }

    public UserBase getRecipient() {
        return recipient;
    }

    public List<UserBase> getLikes() {
        return likes;
    }

    public List<FeedReply> getReplies() {
        return replies;
    }

    public void setText(String value) {
        this.text = value;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FeedReply)
            return ((FeedReply)obj).getId() == getId();
        if(obj instanceof FeedList)
            return ((FeedList)obj).getId() == getId();
        return super.equals(obj);
    }
}
