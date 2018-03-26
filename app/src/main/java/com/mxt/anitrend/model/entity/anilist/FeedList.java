package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.KeyUtils;

import java.util.List;

/**
 * Created by Maxwell on 11/12/2016.
 */
public class FeedList implements Parcelable {

    private long id;
    private int replyCount;
    private @KeyUtils.FeedType String type;
    private String status;
    @SerializedName(value = "progress", alternate = {"message"})
    private String value;
    private long createdAt;
    private UserBase user;
    private MediaBase media;
    private UserBase messenger;
    private UserBase recipient;
    private List<UserBase> likes;
    private List<FeedReply> replies;


    protected FeedList(Parcel in) {
        id = in.readLong();
        replyCount = in.readInt();
        type = in.readString();
        status = in.readString();
        value = in.readString();
        createdAt = in.readLong();
        user = in.readParcelable(UserBase.class.getClassLoader());
        media = in.readParcelable(MediaBase.class.getClassLoader());
        messenger = in.readParcelable(UserBase.class.getClassLoader());
        recipient = in.readParcelable(UserBase.class.getClassLoader());
        likes = in.createTypedArrayList(UserBase.CREATOR);
        replies = in.createTypedArrayList(FeedReply.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(replyCount);
        dest.writeString(type);
        dest.writeString(status);
        dest.writeString(value);
        dest.writeLong(createdAt);
        dest.writeParcelable(user, flags);
        dest.writeParcelable(media, flags);
        dest.writeParcelable(messenger, flags);
        dest.writeParcelable(recipient, flags);
        dest.writeTypedList(likes);
        dest.writeTypedList(replies);
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

    public @KeyUtils.FeedType String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getValue() {
        return value;
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

    public void setValue(String value) {
        this.value = value;
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
