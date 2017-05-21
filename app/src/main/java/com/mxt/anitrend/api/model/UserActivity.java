package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

import com.mxt.anitrend.api.structure.UserActivityReply;
import com.mxt.anitrend.utils.DateTimeConverter;
import com.mxt.anitrend.utils.MarkDown;

import java.util.List;

/**
 * Created by Maxwell on 11/12/2016.
 */
public class UserActivity implements Parcelable {

    private int id;
    private int user_id;
    private int reply_count;
    private String activity_type;
    private String status;
    private String value;
    private String created_at;
    private List<UserSmall> users;
    private Series series;
    private UserSmall messenger;
    private List<UserActivityReply> replies;
    private List<UserSmall> likes;


    protected UserActivity(Parcel in) {
        id = in.readInt();
        user_id = in.readInt();
        reply_count = in.readInt();
        activity_type = in.readString();
        status = in.readString();
        value = in.readString();
        created_at = in.readString();
        users = in.createTypedArrayList(UserSmall.CREATOR);
        series = in.readParcelable(Series.class.getClassLoader());
        messenger = in.readParcelable(UserSmall.class.getClassLoader());
        replies = in.createTypedArrayList(UserActivityReply.CREATOR);
        likes = in.createTypedArrayList(UserSmall.CREATOR);
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

    public int getUser_id() {
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

    public void setValue(String value) {
        this.value = value;
    }

    public Spanned getSpannedValue() {
        return MarkDown.convert(value);
    }

    public String getCreated_at() {
        if(created_at != null)
            return DateTimeConverter.getPrettyDateCustom(created_at);
        return "N/A";
    }

    public List<UserSmall> getUsers() {
        return users;
    }

    public List<UserSmall> getLikes() {
        return likes;
    }

    public Series getSeries() {
        return series;
    }

    public UserSmall getMessenger() {
        return messenger;
    }

    public List<UserActivityReply> getReplies() {
        return replies;
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
        dest.writeInt(user_id);
        dest.writeInt(reply_count);
        dest.writeString(activity_type);
        dest.writeString(status);
        dest.writeString(value);
        dest.writeString(created_at);
        dest.writeTypedList(users);
        dest.writeParcelable(series, flags);
        dest.writeParcelable(messenger, flags);
        dest.writeTypedList(replies);
        dest.writeTypedList(likes);
    }
}
