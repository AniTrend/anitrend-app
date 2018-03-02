package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.general.UserActivityReply;
import com.mxt.anitrend.util.DateUtil;

import java.util.List;

/**
 * Created by Maxwell on 11/12/2016.
 */
public class UserActivity implements Parcelable {

    private int id;
    private long user_id;
    private int reply_count;
    private String activity_type;
    private String status;
    private String value;
    private String created_at;
    private List<UserBase> users;
    private SeriesBase series;
    private UserBase messenger;
    private List<UserBase> likes;

    protected UserActivity(Parcel in) {
        id = in.readInt();
        user_id = in.readLong();
        reply_count = in.readInt();
        activity_type = in.readString();
        status = in.readString();
        value = in.readString();
        created_at = in.readString();
        users = in.createTypedArrayList(UserBase.CREATOR);
        series = in.readParcelable(SeriesBase.class.getClassLoader());
        messenger = in.readParcelable(UserBase.class.getClassLoader());
        likes = in.createTypedArrayList(UserBase.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeLong(user_id);
        dest.writeInt(reply_count);
        dest.writeString(activity_type);
        dest.writeString(status);
        dest.writeString(value);
        dest.writeString(created_at);
        dest.writeTypedList(users);
        dest.writeParcelable(series, flags);
        dest.writeParcelable(messenger, flags);
        dest.writeTypedList(likes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserActivity> CREATOR = new Creator<UserActivity>() {
        @Override
        public UserActivity createFromParcel(Parcel in) {
            return new UserActivity(in);
        }

        @Override
        public UserActivity[] newArray(int size) {
            return new UserActivity[size];
        }
    };

    public int getId() {
        return id;
    }

    public long getUser_id() {
        return user_id;
    }

    public int getReply_count() {
        return reply_count;
    }

    public String getActivity_type() {
        return activity_type;
    }

    public String getStatus() {
        return status;
    }

    public String getValue() {
        return value;
    }

    public String getCreated_at() {
        return DateUtil.getPrettyDateCustom(created_at);
    }

    public List<UserBase> getUsers() {
        return users;
    }

    public List<UserBase> getLikes() {
        return likes;
    }

    public SeriesBase getSeries() {
        return series;
    }

    public UserBase getMessenger() {
        return messenger;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public void setReply_count(int reply_count) {
        this.reply_count = reply_count;
    }

    public void setActivity_type(String activity_type) {
        this.activity_type = activity_type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setUsers(List<UserBase> users) {
        this.users = users;
    }

    public void setSeries(SeriesBase series) {
        this.series = series;
    }

    public void setMessenger(UserBase messenger) {
        this.messenger = messenger;
    }

    public void setLikes(List<UserBase> likes) {
        this.likes = likes;
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
