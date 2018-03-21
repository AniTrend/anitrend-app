package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.model.entity.anilist.meta.ImageBase;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

/**
 * Created by max on 10/4/2016.
 */
@Entity
public class UserBase implements Parcelable {

    @Id(assignable = true)
    private long id;
    @Index
    private String name;
    private ImageBase avatar;
    private String bannerImage;
    private boolean isFollowing;

    public UserBase() {

    }

    protected UserBase(Parcel in) {
        id = in.readLong();
        name = in.readString();
        bannerImage = in.readString();
        avatar = in.readParcelable(ImageBase.class.getClassLoader());
        isFollowing = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(bannerImage);
        dest.writeParcelable(avatar, flags);
        dest.writeByte((byte) (isFollowing ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserBase> CREATOR = new Creator<UserBase>() {
        @Override
        public UserBase createFromParcel(Parcel in) {
            return new UserBase(in);
        }

        @Override
        public UserBase[] newArray(int size) {
            return new UserBase[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ImageBase getAvatar() {
        return avatar;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserBase)
            return ((UserBase) obj).id == id;
        return super.equals(obj);
    }
}
