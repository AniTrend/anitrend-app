package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;

import com.mxt.anitrend.data.converter.MediaListOptionsConverter;
import com.mxt.anitrend.data.converter.UserOptionsConverter;
import com.mxt.anitrend.data.converter.UserStatsConverter;
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions;
import com.mxt.anitrend.model.entity.anilist.meta.UserOptions;
import com.mxt.anitrend.model.entity.base.UserBase;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;

/**
 * Created by Maxwell on 11/12/2016.
 */
@Entity
public class User extends UserBase {

    private String about;
    @Convert(converter = UserOptionsConverter.class, dbType = String.class)
    private UserOptions options;
    @Convert(converter = MediaListOptionsConverter.class, dbType = String.class)
    private MediaListOptions mediaListOptions;
    @Convert(converter = UserStatsConverter.class, dbType = String.class)
    private UserStats stats;
    private int unreadNotificationCount;

    public User() {

    }

    protected User(Parcel in) {
        super(in);
        about = in.readString();
        options = in.readParcelable(UserOptions.class.getClassLoader());
        mediaListOptions = in.readParcelable(MediaListOptions.class.getClassLoader());
        stats = in.readParcelable(UserStats.class.getClassLoader());
        unreadNotificationCount = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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
        return super.describeContents();
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
        super.writeToParcel(dest, flags);
        dest.writeString(about);
        dest.writeParcelable(options, flags);
        dest.writeParcelable(mediaListOptions, flags);
        dest.writeParcelable(stats, flags);
        dest.writeInt(unreadNotificationCount);
    }

    public String getAbout() {
        return about;
    }

    public UserStats getStats() {
        return stats;
    }

    public int getUnreadNotificationCount() {
        return unreadNotificationCount;
    }

    public void setUnreadNotificationCount(int count) {
        unreadNotificationCount = count;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setStats(UserStats stats) {
        this.stats = stats;
    }

    public UserOptions getOptions() {
        return options;
    }
}
