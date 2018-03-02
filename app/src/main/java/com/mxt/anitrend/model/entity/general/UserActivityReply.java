package com.mxt.anitrend.model.entity.general;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.anilist.UserActivity;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.util.DateUtil;

import java.util.List;

/**
 * Created by max on 2017/03/13.
 */

public class UserActivityReply implements Parcelable {

    private int id;
    private String reply_value;
    private String created_at;
    private String updated_at;
    private UserBase user;
    private List<UserBase> likes;


    protected UserActivityReply(Parcel in) {
        id = in.readInt();
        reply_value = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
        user = in.readParcelable(UserBase.class.getClassLoader());
        likes = in.createTypedArrayList(UserBase.CREATOR);
    }

    public static final Creator<UserActivityReply> CREATOR = new Creator<UserActivityReply>() {
        @Override
        public UserActivityReply createFromParcel(Parcel in) {
            return new UserActivityReply(in);
        }

        @Override
        public UserActivityReply[] newArray(int size) {
            return new UserActivityReply[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getReply() {
        return reply_value;
    }

    public String getCreated_at() {
        return DateUtil.getPrettyDateCustom(created_at);
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public UserBase getUser() {
        return user;
    }

    public List<UserBase> getLikes() {
        return likes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setReply_value(String reply_value) {
        this.reply_value = reply_value;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
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
        dest.writeInt(id);
        dest.writeString(reply_value);
        dest.writeString(created_at);
        dest.writeString(updated_at);
        dest.writeParcelable(user, flags);
        dest.writeTypedList(likes);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UserActivityReply)
            return ((UserActivityReply)obj).getId() == getId();
        if(obj instanceof UserActivity)
            return ((UserActivity)obj).getId() == getId();
        return super.equals(obj);
    }
}
