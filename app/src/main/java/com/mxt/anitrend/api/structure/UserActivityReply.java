package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.util.DateTimeConverter;
import com.mxt.anitrend.util.MarkDown;
import com.mxt.anitrend.util.PatternMatcher;

import java.util.List;

/**
 * Created by max on 2017/03/13.
 */

public class UserActivityReply implements Parcelable {

    private int id;
    private String reply_value;
    private String created_at;
    private String updated_at;
    private UserSmall user;
    private List<UserSmall> likes;


    protected UserActivityReply(Parcel in) {
        id = in.readInt();
        reply_value = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
        user = in.readParcelable(UserSmall.class.getClassLoader());
        likes = in.createTypedArrayList(UserSmall.CREATOR);
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

    public Spanned getReply_value() {
        return MarkDown.convert(PatternMatcher.removeTags(reply_value));
    }

    public String getReply() {
        return reply_value;
    }

    public String getCreated_at() {
        return DateTimeConverter.getPrettyDateCustom(created_at);
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public UserSmall getUser() {
        return user;
    }

    public List<UserSmall> getLikes() {
        return likes;
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
}
