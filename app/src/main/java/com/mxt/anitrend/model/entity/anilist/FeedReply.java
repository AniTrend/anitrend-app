package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.util.DateUtil;

import java.util.List;

/**
 * Created by max on 2017/03/13.
 */

public class FeedReply implements Parcelable {

    private long id;
    private String text;
    private long createdAt;
    private UserBase user;
    private List<UserBase> likes;


    protected FeedReply(Parcel in) {
        id = in.readLong();
        text = in.readString();
        createdAt = in.readLong();
        user = in.readParcelable(UserBase.class.getClassLoader());
        likes = in.createTypedArrayList(UserBase.CREATOR);
    }

    public static final Creator<FeedReply> CREATOR = new Creator<FeedReply>() {
        @Override
        public FeedReply createFromParcel(Parcel in) {
            return new FeedReply(in);
        }

        @Override
        public FeedReply[] newArray(int size) {
            return new FeedReply[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getReply() {
        return text;
    }

    public long getCreatedAt() {
        return createdAt;
    }


    public UserBase getUser() {
        return user;
    }

    public List<UserBase> getLikes() {
        return likes;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUser(UserBase user) {
        this.user = user;
    }

    public void setLikes(List<UserBase> likes) {
        this.likes = likes;
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
        dest.writeLong(id);
        dest.writeString(text);
        dest.writeLong(createdAt);
        dest.writeParcelable(user, flags);
        dest.writeTypedList(likes);
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
